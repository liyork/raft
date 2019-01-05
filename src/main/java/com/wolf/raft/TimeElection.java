package com.wolf.raft;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

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

    protected static boolean isNeedRest;

    private static long sleepNanos;

    protected static final Object waitObject = new Object();

    public void init() {

        Node localNode = clusterManger.getLocalNode();

        new Thread(() -> {

            sleepNanos = TimeHelper.genElectionTime();//初始睡眠时间

            for (; ; ) {

                while (sleepNanos > 0) {
                    synchronized (waitObject) {

                        int localNodeTerm = localNode.getTerm();

                        long sleepMill = TimeUnit.MILLISECONDS.convert(sleepNanos, TimeUnit.NANOSECONDS);
                        //应该醒来时间,用于再次醒来时重新计算还需睡眠时间
                        logger.info("{} term before wait, systemnano:{},wait sleepSecond:{}",
                                localNodeTerm, System.nanoTime(), TimeUnit.SECONDS.convert(sleepNanos, TimeUnit.NANOSECONDS));

                        try {
                            waitObject.wait(sleepMill);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        logger.info("{} term after wait, systemnano:{}", localNodeTerm, System.nanoTime());

                        if (isNeedRest) {
                            logger.info("receive notify me,isNeedRest:" + isNeedRest);
                            isNeedRest = false;
                            //接收到vote或者心跳后，需要重新睡眠，不需要重新生成睡眠时间
                            //sleepNanos = TimeHelper.genElectionTime();
                        } else {
                            sleepNanos = 0;
                        }
                    }
                }

                localNode.setState(State.CANDIDATE);
                localNode.incrTerm();
                localNode.setVoteFor(localNode);

                //新建超时时间,准备发起投票
                sleepNanos = TimeHelper.genElectionTime();

                VoteResponseProcess voteResponseProcess = new VoteResponseProcess();

                String body = JSON.toJSONString(localNode);

                for (String otherNode : clusterManger.getOtherNodes()) {

                    ExecutorManager.execute(() -> {

                        String uri = "http://" + otherNode + "/vote";

                        try {
                            logger.info("send vote to follower:{}", otherNode);
                            String response = HttpClientUtil.post(uri, body);
                            if (!StringUtils.isEmpty(response)) {
                                voteResponseProcess.process(response);
                            } else {
                                logger.info("vote response is null，uri:{},body:{}",
                                        uri, localNode.toString());
                            }

                        } catch (Exception e) {
                            //投票/心跳，遇到网络问题则不管，等待下次被投票或者再发起
                            logger.error("send vote error,uri:" + uri + ",body:" + localNode.toString(), e);
                        }
                    });
                }
            }
        }).start();
    }
}
