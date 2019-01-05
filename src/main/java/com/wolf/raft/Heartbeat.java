package com.wolf.raft;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Description:
 * <br/> Created on 12/29/2018
 *
 * @author 李超
 * @since 1.0.0
 */
public class Heartbeat {

    private static long heartbeatInterval;

    private static final Object waitObject = new Object();

    private static AtomicBoolean initial = new AtomicBoolean();

    public static void init() {

        if (!initial.compareAndSet(false, true)) {
            System.out.println("heartbeat has already initial!");
            return;
        }

        Container.getClusterManger().init();

        new Thread(() -> {

            heartbeatInterval = TimeHelper.genHeartbeatInterval();

            long sleepMill = TimeUnit.MILLISECONDS.convert(heartbeatInterval, TimeUnit.NANOSECONDS);

            for (; ; ) {

                synchronized (waitObject) {

                    System.out.println("Heartbeat before systemnano:" + System.nanoTime());

                    Node localNode = Container.getClusterManger().getLocalNode();
                    if (localNode.getState() == State.LEADER) {

                        //send heatbeat
                        System.out.println("send heartbeat leader,wait:" + sleepMill);

                        try {
                            waitObject.wait(sleepMill);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } else {
                        System.out.println("send heartbeat follower,wait");
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

    public static void turnFollower() {

        Node localNode = Container.getClusterManger().getLocalNode();
        localNode.setState(State.FOLLOW);

        synchronized (waitObject) {
            waitObject.notify();
        }
    }
}
