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

    @Autowired
    private TimeElection timeElection;

    private static final Object heartbeatLock = new Object();

    private static AtomicBoolean initial = new AtomicBoolean();

    public void init() {

        if (!initial.compareAndSet(false, true)) {
            logger.info("heartbeat has already initial!");
            return;
        }

        logger.info("heartbeat initial!");

        new Thread(() -> {
            //心跳时间一旦初始化就不用再换了，一会发快一会发慢，也没啥太大冲突和影响
            long heartbeatInterval = TimeHelper.genHeartbeatInterval();

            long sleepMill = TimeUnit.MILLISECONDS.convert(heartbeatInterval, TimeUnit.NANOSECONDS);

            for (; ; ) {

                Node localNode = clusterManger.cloneLocalNode();
                if (localNode.getState() == State.LEADER) {

                    logger.info("i am leader,set isNeedContinueWait flag,send heartbeat to follower,interval:{}", sleepMill);

                    //唤醒，让自己重新计数并等待//todo 可能优化，直接重置但是不唤醒，每次都重置好么？
                    //当上leader，马上通知重置选举超时(以后定期)，需要么？
                    //当选leader，若是马上通知并唤醒，那么醒了再次进行“"整"”时间等待
                    //当选leader，若是不重新计算选举超时并唤醒，那TimeElection到时就会触发选举。
                    //当选leader，若是设置重新计算选举超时并不唤醒，那么可能心跳线程出现问题，那么可能"下次选举时间"会边长,
                    // 因为是本次剩余时间+超时时间，无非就是影响下次再发起投票的时间，可能被别人投票了，优化一下，暂时不唤醒，
                    // 关键感觉每次heartbeat都会触发TimeElection唤醒再睡眠，似乎有cpu上下文切换线程问题
                    timeElection.resetElectionTime(false);

                    String body = JSON.toJSONString(localNode);

                    for (String otherNode : clusterManger.getOtherNodes()) {

                        ExecutorManager.execute(() -> {
                            //todo 待优化
                            String uri = "http://" + otherNode + "/heartbeat";

                            try {
                                HttpClientUtil.post(uri, body);
                            } catch (Exception e) {
                                //投票/心跳，遇到网络问题则不管，等待下次被投票或者再发起
                                logger.error("send heartbeat error,uri:" + uri + ",body:" + body, e);
                            }
                        });
                    }

                    synchronized (heartbeatLock) {
                        try {
                            heartbeatLock.wait(sleepMill);
                        } catch (InterruptedException e) {
                            logger.error("heartbeat wait sleepMill:" + sleepMill + "was interrupted", e);
                        }
                    }

                } else {
                    logger.info("since i am not leader,waiting until become leader...");

                    synchronized (heartbeatLock) {
                        try {
                            heartbeatLock.wait();//暂时常眠
                        } catch (InterruptedException e) {
                            logger.error("heartbeat wait was interrupted", e);
                        }
                    }
                }
            }
        },"raft-heartbeat").start();

    }

    public void startHeartbeat() {

        synchronized (heartbeatLock) {
            heartbeatLock.notify();
        }
    }

    public void turnFollower() {

        Node localNode = clusterManger.cloneLocalNode();
        localNode.setState(State.FOLLOW);

        synchronized (heartbeatLock) {
            heartbeatLock.notify();
        }
    }
}
