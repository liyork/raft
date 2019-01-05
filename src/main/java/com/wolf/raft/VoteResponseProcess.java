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
    int voteCount = 1;

    //处理结果，若是同意则+1，否则不管
    public void process(String response) {

        Node remoteNode = JSON.parseObject(response, Node.class);
        Node voteFor = remoteNode.getVoteFor();

        ClusterManger clusterManger = Container.getBean("clusterManger", ClusterManger.class);
        Node localNode = clusterManger.getLocalNode();

        String localNodeUrl = localNode.getUrl();
        int localNodeTerm = localNode.getTerm();
        String voteForUrl = voteFor.getUrl();
        int voteForTerm = voteFor.getTerm();

        Heartbeat heartbeat = Container.getBean("heartbeat", Heartbeat.class);

        if (localNodeUrl.equals(voteForUrl) &&
                localNodeTerm == voteForTerm) {
            logger.info("receive vote for me,ip:" + localNodeUrl + ",term:" + localNodeTerm +
                    ",voteCount:" + voteCount);

            voteCount++;

            if (voteCount > (clusterManger.size() / 2 + 1)) {

                logger.info("receive major vote,i am leader:" + localNodeUrl + ",term:" + localNodeTerm);

                localNode.setState(State.LEADER);
                heartbeat.init();
            }
        } else {
            logger.info("not vote for me,localIp:" + localNodeUrl + ",voteForUrl:" + voteForUrl +
                    ",localTerm:" + localNodeTerm + ",voteForTerm:" + voteForTerm);
        }
    }

    //担心两次请求互相影响，外面重新构造对象，而不是重用了
//    public void reset() {
//
//        voteCount = 1;
//    }
}
