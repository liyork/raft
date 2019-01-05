package com.wolf.raft;

import com.alibaba.fastjson.JSON;
import com.wolf.utils.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
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

        new Thread(() -> {

            sleepNanos = TimeHelper.genElectionTime();//初始睡眠时间

            for (; ; ) {

                while (sleepNanos > 0) {
                    synchronized (waitObject) {

                        //应该醒来时间,用于再次醒来时重新计算还需睡眠时间
                        logger.info("before systemnano:" + System.nanoTime());

                        long sleepMill = TimeUnit.MILLISECONDS.convert(sleepNanos, TimeUnit.NANOSECONDS);
                        logger.info("wait sleepMill:" + TimeUnit.SECONDS.convert(sleepNanos, TimeUnit.NANOSECONDS));
                        try {
                            waitObject.wait(sleepMill);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        logger.info("after systemnano:" + System.nanoTime());

                        if (isNeedRest) {
                            logger.info("isNeedRest:" + isNeedRest);
                            isNeedRest = false;
                            sleepNanos = TimeHelper.genElectionTime();
                        } else {
                            sleepNanos = 0;
                        }
                    }
                }

                Node localNode = clusterManger.getLocalNode();
                localNode.setState(State.CANDIDATE);
                localNode.incrTerm();
                localNode.setVoteFor(localNode);

                //新建超时时间,准备发起投票
                sleepNanos = TimeHelper.genElectionTime();

                logger.info("vote for me!!");

                VoteResponseProcess voteResponseProcess = new VoteResponseProcess();
                Map<String, String> map = new HashMap<>();
                map.put("voteNode", JSON.toJSONString(localNode));

                for (String otherNodes : clusterManger.getOtherNodes()) {

                    ExecutorManager.execute(() -> {

                        String response = null;
                        try {
                            response = HttpClientUtil.post(otherNodes, map);
                        } catch (Exception e) {
                            e.printStackTrace();
                            //投票/心跳，遇到网络问题则不管，等待下次被投票或者再发起
                        }

                        voteResponseProcess.process(response);
                    });
                }
            }
        }).start();
    }
}
