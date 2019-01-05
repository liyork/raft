package com.wolf.raft;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 * <br/> Created on 1/3/2019
 *
 * @author 李超
 * @since 1.0.0
 */
@RestController
public class RequestReceive {

    private Logger logger = LoggerFactory.getLogger(RequestReceive.class);

    @Autowired
    private ClusterManger clusterManger;

    //接收投票/心跳，比对term，响应
    //todo 可能后期分开，因为两者返回数据不一样
    @RequestMapping(path = "/vote", method = {RequestMethod.POST})
    public String vote(@RequestBody Node remoteNode) {

        Node localNode = clusterManger.getLocalNode();

        int remoteNodeTerm = remoteNode.getTerm();
        logger.info("receive vote,local ipPort:{},term:{},remote ipPort:{},term:{}",
                localNode.getIpPort(), localNode.getTerm(), remoteNode.getIpPort(), remoteNodeTerm);

        //localNode的term会同leader一样，所以直接比对
        if (remoteNodeTerm > localNode.getTerm()) {

            logger.info("receive vote, remote term is bigger,vote it，and rewait.");

            localNode.setTerm(remoteNodeTerm);
            localNode.setVoteFor(remoteNode);
            //不论什么状态，接收到高投票则同意并降级(非follower)
            localNode.setState(State.FOLLOW);
            //唤醒，让自己重新计数并等待//todo 可能优化，直接重置但是不唤醒
            synchronized (TimeElection.waitObject) {
                TimeElection.isNeedRest = true;
                TimeElection.waitObject.notify();
            }
        } else {
            logger.info("receive vote, remote term is smaller,not vote it.");
        }

        return JSON.toJSONString(localNode);
    }

    @RequestMapping(path = "/heartbeat", method = {RequestMethod.POST})
    public void heartbeat(@RequestBody Node remoteNode) {

        Node localNode = clusterManger.getLocalNode();

        int term = remoteNode.getTerm();
        logger.info("receive heartbeat,remote ipPort:{},term:{},local ipPort:{},term:{}",
                remoteNode.getIpPort(), term, localNode.getIpPort(), localNode.getTerm());

        //localNode的term会同leader一样，所以直接比对，大于等于则同意
        if (term >= localNode.getTerm()) {

            logger.info("heartbeat remote term is bigger follower it ，and rewait.");

            localNode.setTerm(term);
            localNode.setVoteFor(remoteNode);
            //不论什么状态，接收到高投票则同意并降级(非follower)
            localNode.setState(State.FOLLOW);
            //唤醒，让自己重新计数并等待//todo 可能优化，直接重置但是不唤醒
            synchronized (TimeElection.waitObject) {
                TimeElection.isNeedRest = true;
                TimeElection.waitObject.notify();
            }
        } else {
            logger.info("heartbeat remote term is smaller,not vote it.");
        }
    }
}
