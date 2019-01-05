package com.wolf.raft;

import com.alibaba.fastjson.JSON;

/**
 * Description:处理每一次的发起投票后的响应
 * <br/> Created on 1/3/2019
 *
 * @author 李超
 * @since 1.0.0
 */
public class ResponseProcess {

    //从1开始，自己本身就是一票
    int voteCount = 1;

    //处理结果，若是同意则+1，否则不管
    public void process(String response) {

        Node remoteNode = JSON.parseObject(response, Node.class);
        Node voteFor = remoteNode.getVoteFor();

        Node localNode = Container.getClusterManger().getLocalNode();

        String localNodeUrl = localNode.getUrl();
        int localNodeTerm = localNode.getTerm();
        String voteForUrl = voteFor.getUrl();
        int voteForTerm = voteFor.getTerm();
        if (localNodeUrl.equals(voteForUrl) &&
                localNodeTerm == voteForTerm) {
            System.out.println("receive vote for me,ip:" + localNodeUrl + ",term:" + localNodeTerm +
                    ",voteCount:" + voteCount);

            voteCount++;

            if (voteCount > (Container.getClusterManger().size() / 2 + 1)) {

                System.out.println("receive major vote,i am leader:" + localNodeUrl + ",term:" + localNodeTerm);

                localNode.setState(State.LEADER);
                Heartbeat.init();
            }
        } else {
            System.out.println("not vote for me,localIp:" + localNodeUrl + ",voteForUrl:" + voteForUrl +
                    ",localTerm:" + localNodeTerm + ",voteForTerm:" + voteForTerm);
        }
    }

    //担心两次请求互相影响，外面重新构造对象，而不是重用了
//    public void reset() {
//
//        voteCount = 1;
//    }
}
