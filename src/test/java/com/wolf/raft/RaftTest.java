package com.wolf.raft;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:
 * <br/> Created on 1/1/2019
 *
 * @author 李超
 * @since 1.0.0
 */
public class RaftTest {

    private static Logger logger = LoggerFactory.getLogger(RaftTest.class);

    public static void main(String[] args) throws InterruptedException {

//        testBaseInit();
//        testRest();
        //logger.info(TimeUnit.SECONDS.toNanos(1));

//        testFollowerHeartbeatInit();
//        testLeaderHeartbeatInit();
//        testLeaderTurnFollowerHeartbeatInit();

//        testResponseProcessOneVote();
        testResponseProcessTwoVote();
    }

    private static void testBaseInit() throws InterruptedException {
        new TimeoutElectionProcessor().init();
    }

    //测试nextAwakeTime = nextAwakeTime + addElectionTime;
    //这样无线增加也不是个事！
    static int term = 1;

    private static void testRest() throws InterruptedException {

        RequestReceive requestReceive = new RequestReceive();

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Node node = new Node();
                node.setTerm(term++);
                requestReceive.vote(node);
            }
        }).start();

        TimeoutElectionProcessor timeoutElectionProcessor = new TimeoutElectionProcessor();
        timeoutElectionProcessor.init();
    }

    private static void testFollowerHeartbeatInit() throws InterruptedException {

        HeartbeatProcessor heartbeatProcessor = new HeartbeatProcessor();
        heartbeatProcessor.init();
    }

    private static void testLeaderHeartbeatInit() throws InterruptedException {

        ClusterManger clusterManger = getClusterManger();
        clusterManger.init();
        Node localNode = clusterManger.cloneLocalNode();
        localNode.setState(State.LEADER);

        HeartbeatProcessor heartbeatProcessor = new HeartbeatProcessor();
        heartbeatProcessor.init();
    }

    private static void testLeaderTurnFollowerHeartbeatInit() throws InterruptedException {

        ClusterManger clusterManger = getClusterManger();
        clusterManger.init();
        Node localNode = clusterManger.cloneLocalNode();

        HeartbeatProcessor heartbeatProcessor = new HeartbeatProcessor();

        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            logger.info("turn to follower");
            heartbeatProcessor.turnFollower();

        }).start();

        localNode.setState(State.LEADER);
        heartbeatProcessor.init();
    }

    private static void testResponseProcessOneVote() {

        ClusterManger clusterManger = getClusterManger();
        clusterManger.init();

        VoteResponseProcess voteResponseProcess = new VoteResponseProcess();

        Node responseNode = new Node();
        responseNode.setTerm(0);
        responseNode.setVoteFor(new Node("127.0.0.1"));

        voteResponseProcess.process("http://127.0.0.1:8080/vote", JSON.toJSONString(responseNode));
    }

    private static void testResponseProcessTwoVote() {

        ClusterManger clusterManger = getClusterManger();
        clusterManger.init();

        VoteResponseProcess voteResponseProcess = new VoteResponseProcess();

        Node responseNode = new Node();
        responseNode.setTerm(0);
        responseNode.setVoteFor(new Node("127.0.0.1"));

        String uri = "http://127.0.0.1:8080/vote";
        voteResponseProcess.process(uri, JSON.toJSONString(responseNode));
        voteResponseProcess.process(uri, JSON.toJSONString(responseNode));
    }

    private static ClusterManger getClusterManger() {
        return new ClusterManger();
    }


}
