package com.wolf.raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Description:
 * <br/> Created on 12/29/2018
 *
 * @author 李超
 * @since 1.0.0
 */
@Component
public class ClusterManger {

    private Logger logger = LoggerFactory.getLogger(ClusterManger.class);

    private List<String> otherNodes = new ArrayList<>();

    private Node localNode;

    private AtomicBoolean initial = new AtomicBoolean();

    public void init() {

        if (!initial.compareAndSet(false, true)) {
            logger.info("cluster manager has already initial!");
            return;
        }

        //todo 可能从某地获取
        localNode = new Node("127.0.0.1:8080");
        otherNodes.add("127.0.0.1:8081");
        otherNodes.add("127.0.0.1:8082");
    }

    public List<String> getOtherNodes() {
        return otherNodes;
    }

    public void setOtherNodes(List<String> otherNodes) {
        this.otherNodes = otherNodes;
    }

    public Node getLocalNode() {
        return localNode;
    }

    public void setLocalNode(Node localNode) {
        this.localNode = localNode;
    }

    public int size() {
        //todo 待优化
        return otherNodes.size() + 1;
    }
}
