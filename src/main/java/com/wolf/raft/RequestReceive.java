package com.wolf.raft;

/**
 * Description:
 * <br/> Created on 1/3/2019
 *
 * @author 李超
 * @since 1.0.0
 */
//todo controller
public class RequestReceive {

    //接收投票/心跳，比对term，响应
    //todo 可能后期分开，因为两者返回数据不一样
    public static Node receive(Node remoteNode) {

        Node localNode = Container.getClusterManger().getLocalNode();

        int term = remoteNode.getTerm();
        System.out.println("remote term:" + term + ",local term:" + localNode.getTerm());

        //localNode的term会同leader一样，所以直接比对
        if (term > localNode.getTerm()) {
            localNode.setTerm(term);
            localNode.setVoteFor(remoteNode);
            //不论什么状态，接收到高投票则同意并降级(非follower)
            localNode.setState(State.FOLLOW);
            //唤醒，让自己重新计数并等待
            synchronized (Vote.waitObject) {
                Vote.isNeedRest = true;
                Vote.waitObject.notify();
            }
        }

        return localNode;
    }
}
