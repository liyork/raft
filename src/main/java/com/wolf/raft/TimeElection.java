package com.wolf.raft;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Description:
 * <br/> Created on 12/29/2018
 *
 * @author 李超
 * @since 1.0.0
 */
@Component
public class TimeElection {

    private Logger logger = LoggerFactory.getLogger(TimeElection.class);

    @Autowired
    private ClusterManger clusterManger;

    protected static boolean isNeedContinueWait;

    private static long sleepNanos;

    private static final Object timeElectionLock = new Object();

    private AtomicBoolean initial = new AtomicBoolean();

    public void init() {

        if (!initial.compareAndSet(false, true)) {
            logger.info("timeElection has already initial!");
            return;
        }

        logger.info("timeElection initial!");

        new Thread(() -> {

            sleepNanos = TimeHelper.genElectionTime();//初始睡眠时间
            //初始化
            Node localNode = clusterManger.cloneLocalNode();

            for (; ; ) {

                while (sleepNanos > 0) {

                    int localNodeTerm = localNode.getTerm();

                    long sleepMill = TimeUnit.MILLISECONDS.convert(sleepNanos, TimeUnit.NANOSECONDS);
                    //应该醒来时间,用于再次醒来时重新计算还需睡眠时间
                    logger.info("{} term start wait, systemnano:{},wait sleepSecond:{}",
                            localNodeTerm, System.nanoTime(), TimeUnit.SECONDS.convert(sleepNanos, TimeUnit.NANOSECONDS));

                    synchronized (timeElectionLock) {
                        try {
                            timeElectionLock.wait(sleepMill);
                        } catch (InterruptedException e) {
                            logger.error("timeElection wait sleepMill:" + sleepMill + "was interrupted", e);
                        }
                    }

                    //每次超时醒来，重新获取，查看当前localnode到哪个term了
                    localNode = clusterManger.cloneLocalNode();
                    //todo 是否需要状态判断？

                    if (isNeedContinueWait) {
                        isNeedContinueWait = false;
                        //接收到vote或者心跳后，需要重新睡眠，不需要重新生成睡眠时间，
                        // 因为就是简单的为了让他继续睡眠，若是没人提醒他，那么下次唤醒后就是发起投票了
                        //sleepNanos = TimeHelper.genElectionTime();
                    } else {
                        sleepNanos = 0;
                    }

                    logger.info("{} term end wait, systemnano:{},sleepNanos:{}",
                            localNodeTerm, System.nanoTime(), sleepNanos);
                }

                localNode.setState(State.CANDIDATE);
                localNode.incrTerm();
                localNode.setVoteFor(localNode);

                clusterManger.setLocalNode(localNode);

                //新建超时时间,准备发起投票
                sleepNanos = TimeHelper.genElectionTime();

                VoteResponseProcess voteResponseProcess = new VoteResponseProcess();

                String body = JSON.toJSONString(localNode);

                for (String otherNode : clusterManger.getOtherNodes()) {

                    Node finalLocalNode = localNode;
                    ExecutorManager.execute(() -> {

                        String uri = "http://" + otherNode + "/vote";

                        try {
                            logger.info("send vote to follower:{}", otherNode);
                            String response = HttpClientUtil.post(uri, body);
                            if (!StringUtils.isEmpty(response)) {
                                voteResponseProcess.process(uri, response);
                            } else {
                                logger.info("vote response is null，uri:{},body:{}",
                                        uri, finalLocalNode.toString());
                            }

                        } catch (Exception e) {
                            //投票/心跳，遇到网络问题则不管，等待下次被投票或者再发起
                            logger.error("send vote error,uri:" + uri + ",body:" + finalLocalNode.toString(), e);
                        }
                    });
                }
            }
        }).start();
    }

    protected void resetElectionTime(boolean isImmediately) {

        synchronized (TimeElection.timeElectionLock) {
            TimeElection.isNeedContinueWait = true;
            if (isImmediately) {
                TimeElection.timeElectionLock.notify();
            }
        }
    }
}
