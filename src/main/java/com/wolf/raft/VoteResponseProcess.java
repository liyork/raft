package com.wolf.raft;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:处理每一次的发起投票后的响应
 * <br/> Created on 1/3/2019
 *
 * @author 李超
 * @since 1.0.0
 */
public class VoteResponseProcess {

    private Logger logger = LoggerFactory.getLogger(VoteResponseProcess.class);

    //从1开始，自己本身就是一票
    private int voteCount = 1;

    //处理结果，若是同意则+1，否则不管
    public void process(String uri, String response) {

        Node remoteNode = JSON.parseObject(response, Node.class);
        logger.info("request uri:{},vote response:{}", uri, remoteNode.toString());//似乎response转换的json不对。。
        Node voteFor = remoteNode.getVoteFor();

        ClusterManger clusterManger = Container.getBean("clusterManger", ClusterManger.class);
        Node localNode = clusterManger.cloneLocalNode();

        String localNodeUrl = localNode.getIpPort();
        int localNodeTerm = localNode.getTerm();
        String voteForIpPort = voteFor.getIpPort();
        int voteForTerm = voteFor.getTerm();

        if (localNodeUrl.equals(voteForIpPort) && localNodeTerm == voteForTerm) {

            voteCount++;

            logger.info("receive vote for me,local ipPort:{},term:{},get voteCount:{}",
                    localNodeUrl, localNodeTerm, voteCount);

            //半数(包含)以上即可成为leader
            if (voteCount >= clusterManger.getMajority()) {

                logger.info("receive major vote,i am leader:{},term:{}",
                        localNodeUrl, localNodeTerm);

                localNode.setState(State.LEADER);

                clusterManger.setLocalNode(localNode);

                Heartbeat heartbeat = Container.getBean("heartbeat", Heartbeat.class);
                heartbeat.startHeartbeat();
            }
        } else {
            logger.info("not vote for me,local ipPort:{},term:{},voteFor ipPort:{},term{}",
                    localNodeUrl, localNodeTerm, voteForIpPort, voteForTerm);
        }
    }

    //担心两次请求互相影响，外面重新构造对象，而不是重用了
//    public void reset() {
//
//        voteCount = 1;
//    }
}
