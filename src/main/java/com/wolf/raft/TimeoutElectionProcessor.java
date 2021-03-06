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
public class TimeoutElectionProcessor {

    private Logger logger = LoggerFactory.getLogger(TimeoutElectionProcessor.class);

    @Autowired
    private ClusterManger clusterManger;

    protected static volatile boolean isNeedContinueWait;

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
            Node cloneLocalNode = clusterManger.cloneLocalNode();

            for (; ; ) {

                while (sleepNanos > 0) {

                    int localNodeTerm = cloneLocalNode.getTerm();

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
                    cloneLocalNode = clusterManger.cloneLocalNode();

                    //todo 是否需要状态判断？应该不用，有isNeedContinueWait字段了，若有高term则必然修改此字段，
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

                cloneLocalNode.setState(State.CANDIDATE);
                cloneLocalNode.incrTerm();
                cloneLocalNode.setVoteFor(cloneLocalNode);

                //新建超时时间,准备发起投票
                sleepNanos = TimeHelper.genElectionTime();

                //准备修改前，若是有人(高term的投票或心跳)修改，那么表明这次醒来后到准备投票之间存在其他高term修改
                // 自己状态，自己同意了，所以就等待下轮超时吧。
                if (!clusterManger.cas(cloneLocalNode.getSource(), cloneLocalNode)) {
                    continue;
                }

                VoteResponseProcess voteResponseProcess = new VoteResponseProcess();

                String body = JSON.toJSONString(cloneLocalNode);

                for (String otherNode : clusterManger.getOtherNodes()) {

                    Node finalLocalNode = cloneLocalNode;
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
        }, "raft-timeElection").start();
    }

    protected void resetElectionTime(boolean isImmediately) {

        synchronized (timeElectionLock) {
            isNeedContinueWait = true;
            if (isImmediately) {
                //System.out.println("isImmediately:"+isImmediately);
                timeElectionLock.notify();
            }
        }
    }
}
