package com.wolf.raft;

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

        new Thread(() -> {

            heartbeatInterval = TimeHelper.genHeartbeatInterval();

            long sleepMill = TimeUnit.MILLISECONDS.convert(heartbeatInterval, TimeUnit.NANOSECONDS);

            for (; ; ) {

                synchronized (waitObject) {

                    logger.info("Heartbeat before systemnano:" + System.nanoTime());

                    Node localNode = clusterManger.getLocalNode();
                    if (localNode.getState() == State.LEADER) {

                        //send heatbeat
                        logger.info("send heartbeat leader,wait:" + sleepMill);

                        try {
                            waitObject.wait(sleepMill);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } else {
                        logger.info("send heartbeat follower,wait");
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
