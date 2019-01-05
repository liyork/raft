package com.wolf.raft;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
public class Heartbeat {

    private Logger logger = LoggerFactory.getLogger(Heartbeat.class);

    @Autowired
    private ClusterManger clusterManger;

    private static long heartbeatInterval;

    private static final Object waitObject = new Object();

    private static AtomicBoolean initial = new AtomicBoolean();

    public void init() {

        if (!initial.compareAndSet(false, true)) {
            logger.info("heartbeat has already initial!");
            return;
        }

        logger.info("heartbeat initial!");

        new Thread(() -> {

            heartbeatInterval = TimeHelper.genHeartbeatInterval();

            long sleepMill = TimeUnit.MILLISECONDS.convert(heartbeatInterval, TimeUnit.NANOSECONDS);

            for (; ; ) {

                synchronized (waitObject) {

                    Node localNode = clusterManger.getLocalNode();
                    if (localNode.getState() == State.LEADER) {

                        logger.info("i am leader,reset election time,send heartbeat to follower,interval:{}", sleepMill);

                        //唤醒，让自己重新计数并等待//todo 可能优化，直接重置但是不唤醒，每次都重置好么？

                        synchronized (TimeElection.waitObject) {
                            TimeElection.isNeedRest = true;
                            TimeElection.waitObject.notify();
                        }

                        String body = JSON.toJSONString(localNode);

                        for (String otherNode : clusterManger.getOtherNodes()) {

                            ExecutorManager.execute(() -> {

                                String uri = "http://" + otherNode + "/heartbeat";

                                try {
                                    HttpClientUtil.post(uri, body);
                                } catch (Exception e) {
                                    //投票/心跳，遇到网络问题则不管，等待下次被投票或者再发起
                                    logger.error("send heartbeat error,uri:" + uri + ",body:" + body, e);
                                }
                            });
                        }

                        try {
                            waitObject.wait(sleepMill);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } else {
                        logger.info("i am not leader,waiting until become leader...");
                        try {
                            waitObject.wait();//暂时常眠，
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();

    }

    public void turnFollower() {

        Node localNode = clusterManger.getLocalNode();
        localNode.setState(State.FOLLOW);

        synchronized (waitObject) {
            waitObject.notify();
        }
    }
}
