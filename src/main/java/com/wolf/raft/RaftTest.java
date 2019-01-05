package com.wolf.raft;

import com.alibaba.fastjson.JSON;

/**
 * Description:
 * <br/> Created on 1/1/2019
 *
 * @author 李超
 * @since 1.0.0
 */
public class RaftTest {

    public static void main(String[] args) throws InterruptedException {

//        testBaseInit();
//        testRest();
        //System.out.println(TimeUnit.SECONDS.toNanos(1));

//        testFollowerHeartbeatInit();
//        testLeaderHeartbeatInit();
//        testLeaderTurnFollowerHeartbeatInit();

//        testResponseProcessOneVote();
        testResponseProcessTwoVote();
    }

    private static void testBaseInit() throws InterruptedException {
        Vote.init();
    }

    //测试nextAwakeTime = nextAwakeTime + addElectionTime;
    //这样无线增加也不是个事！
    static int term = 1;
    private static void testRest() throws InterruptedException {

        new Thread(()->{
            while (true) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Node node = new Node();
                node.setTerm(term++);
                RequestReceive.receive(node);
            }
        }).start();

        Vote.init();
    }

    private static void testFollowerHeartbeatInit() throws InterruptedException {

        Heartbeat.init();
    }

    private static void testLeaderHeartbeatInit() throws InterruptedException {

        Container.getClusterManger().init();
        Node localNode = Container.getClusterManger().getLocalNode();
        localNode.setState(State.LEADER);
        Heartbeat.init();
    }

    private static void testLeaderTurnFollowerHeartbeatInit() throws InterruptedException {

        Container.getClusterManger().init();
        Node localNode = Container.getClusterManger().getLocalNode();

        new Thread(()-> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("turn to follower");
            Heartbeat.turnFollower();

        }).start();

        localNode.setState(State.LEADER);
        Heartbeat.init();
    }

    private static void testResponseProcessOneVote() {

        Container.getClusterManger().init();

        ResponseProcess responseProcess = new ResponseProcess();

        Node responseNode = new Node();
        responseNode.setTerm(0);
        responseNode.setVoteFor(new Node("127.0.0.1"));

        responseProcess.process(JSON.toJSONString(responseNode));
    }

    private static void testResponseProcessTwoVote() {

        Container.getClusterManger().init();

        ResponseProcess responseProcess = new ResponseProcess();

        Node responseNode = new Node();
        responseNode.setTerm(0);
        responseNode.setVoteFor(new Node("127.0.0.1"));

        responseProcess.process(JSON.toJSONString(responseNode));
        responseProcess.process(JSON.toJSONString(responseNode));
    }

}
