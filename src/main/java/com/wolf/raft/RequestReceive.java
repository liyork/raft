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

    @Autowired
    private TimeElection timeElection;

    //接收投票/心跳，比对term，响应
    //todo 可能后期分开，因为两者返回数据不一样
    @RequestMapping(path = "/vote", method = {RequestMethod.POST})
    public String vote(@RequestBody Node remoteNode) {

        Node localNode = clusterManger.cloneLocalNode();

        int remoteNodeTerm = remoteNode.getTerm();

        //localNode的term会同leader一样，所以直接比对
        if (remoteNodeTerm > localNode.getTerm()) {

            logger.info("receive vote,local ipPort:{},term:{},remote ipPort:{},term:{}," +
                            " remote term is bigger,vote it，and restart wait.",
                    localNode.getIpPort(), localNode.getTerm(), remoteNode.getIpPort(), remoteNodeTerm);

            localNode.setTerm(remoteNodeTerm);
            localNode.setVoteFor(remoteNode);
            //不论什么状态，接收到高投票则同意并降级(非follower)
            localNode.setState(State.FOLLOW);
            clusterManger.setLocalNode(localNode);
            //唤醒，让自己重新计数并等待
            //这个类似于Heartbeat，但是稍有不同，因为若是不唤醒，可能会延长整体服务器没有leader的时间！
            timeElection.resetElectionTime(true);
        } else {
            logger.info("receive vote,local ipPort:{},term:{},remote ipPort:{},term:{}," +
                            " remote term is smaller,not vote it.",
                    localNode.getIpPort(), localNode.getTerm(), remoteNode.getIpPort(), remoteNodeTerm);
        }

        return JSON.toJSONString(localNode);
    }

    @RequestMapping(path = "/heartbeat", method = {RequestMethod.POST})
    public void heartbeat(@RequestBody Node remoteNode) {

        int remoteNodeTerm = remoteNode.getTerm();

        Node localNode = clusterManger.cloneLocalNode();
        //localNode的term会同leader一样，所以直接比对，大于等于则同意
        int localNodeTerm = localNode.getTerm();
        //todo 是否有状态前后不一致的问题？

        if (remoteNodeTerm >= localNodeTerm) {

            logger.info("receive heartbeat,local ipPort:{},term:{},remote ipPort:{},term:{}," +
                            " remote term is bigger follower it ，and restart wait.",
                    localNode.getIpPort(), localNodeTerm, remoteNode.getIpPort(), remoteNodeTerm);

            localNode.setTerm(remoteNodeTerm);
            localNode.setVoteFor(remoteNode);
            //不论什么状态，接收到高投票则同意并降级(非follower)
            localNode.setState(State.FOLLOW);
            clusterManger.setLocalNode(localNode);
            //唤醒，让自己重新计数并等待//todo 可能优化，直接重置但是不唤醒
            //这个问题同vote方法，为了整体服务器能最快选举出leader，这个先保留
            timeElection.resetElectionTime(true);
        } else {
            logger.info("receive heartbeat,local ipPort:{},term:{},remote ipPort:{},term:{}," +
                            " remote term is smaller,not vote it.",
                    localNode.getIpPort(), localNodeTerm, remoteNode.getIpPort(), remoteNodeTerm);
        }
    }
}
