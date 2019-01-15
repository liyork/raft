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
    private TimeoutElectionProcessor timeoutElectionProcessor;

    //todo 总是担心由于是两个线程，spirng启动Initializer的同时tomcat有线程进来，所以这里暂时是每个方法执行下init..
    @Autowired
    private Initializer initializer;

    //接收投票/心跳，比对term，响应
    //todo 可能后期分开，因为两者返回数据不一样
    @RequestMapping(path = "/vote", method = {RequestMethod.POST})
    public String vote(@RequestBody Node remoteNode) {

        initializer.init();

        Node cloneLocalNode = clusterManger.cloneLocalNode();
        Node localNode = clusterManger.getLocalNode();

        int remoteNodeTerm = remoteNode.getTerm();

        //localNode的term会同leader一样，所以直接比对
        if (remoteNodeTerm > cloneLocalNode.getTerm()) {

            logger.info("receive vote,local ipPort:{},term:{},remote ipPort:{},term:{}," +
                            " remote term is bigger,vote it，and restart wait.",
                    cloneLocalNode.getIpPort(), cloneLocalNode.getTerm(), remoteNode.getIpPort(), remoteNodeTerm);

            cloneLocalNode.setTerm(remoteNodeTerm);
            cloneLocalNode.setVoteFor(remoteNode);
            //不论什么状态，接收到高投票则同意并降级(非follower)
            cloneLocalNode.setState(State.FOLLOW);
            //若收到投票请求后，与此同时，自己超时选举那边已经获得多数投票变成leader，那么以最快的为准
            if(!clusterManger.cas(localNode,cloneLocalNode)){
                return JSON.toJSONString(localNode);
            }
            //唤醒，让自己重新计数并等待
            //这个类似于Heartbeat，但是稍有不同，因为若是不唤醒，可能会延长整体服务器没有leader的时间！
            timeoutElectionProcessor.resetElectionTime(true);
        } else {
            logger.info("receive vote,local ipPort:{},term:{},remote ipPort:{},term:{}," +
                            " remote term is smaller,not vote it.",
                    cloneLocalNode.getIpPort(), cloneLocalNode.getTerm(), remoteNode.getIpPort(), remoteNodeTerm);
        }

        return JSON.toJSONString(cloneLocalNode);
    }

    @RequestMapping(path = "/heartbeat", method = {RequestMethod.POST})
    public void heartbeat(@RequestBody Node remoteNode) {

        initializer.init();

        int remoteNodeTerm = remoteNode.getTerm();

        Node cloneLocalNode = clusterManger.cloneLocalNode();
        Node localNode = clusterManger.getLocalNode();

        //localNode的term会同leader一样，所以直接比对，大于等于则同意
        int localNodeTerm = cloneLocalNode.getTerm();
        //todo 是否有状态前后不一致的问题？

        if (remoteNodeTerm >= localNodeTerm) {

            logger.info("receive heartbeat,local ipPort:{},term:{},remote ipPort:{},term:{}," +
                            " remote term is bigger follower it ，and restart wait.",
                    cloneLocalNode.getIpPort(), localNodeTerm, remoteNode.getIpPort(), remoteNodeTerm);

            cloneLocalNode.setTerm(remoteNodeTerm);
            cloneLocalNode.setVoteFor(remoteNode);
            //不论什么状态，接收到高投票则同意并降级(非follower)
            cloneLocalNode.setState(State.FOLLOW);
            if(!clusterManger.cas(localNode,cloneLocalNode)){
                return;
            }
            //唤醒，让自己重新计数并等待//todo 可能优化，直接重置但是不唤醒
            //这个问题同vote方法，为了整体服务器能最快选举出leader，这个先保留
            timeoutElectionProcessor.resetElectionTime(true);
        } else {
            logger.info("receive heartbeat,local ipPort:{},term:{},remote ipPort:{},term:{}," +
                            " remote term is smaller,not vote it.",
                    cloneLocalNode.getIpPort(), localNodeTerm, remoteNode.getIpPort(), remoteNodeTerm);
        }
    }
}
