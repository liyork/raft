package com.wolf.raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Description: 集群管理
 * <br/> Created on 12/29/2018
 *
 * @author 李超
 * @since 1.0.0
 */
@Component
public class ClusterManger {

    private Logger logger = LoggerFactory.getLogger(ClusterManger.class);

    private List<String> otherNodes = new ArrayList<>();

    private volatile AtomicReference<Node> localNode;

    private AtomicBoolean initial = new AtomicBoolean();

    @Value("${localIpPort}")
    private String localIpPort;

    @Value("${othersIpPort}")
    private String othersIpPort;

    //半数
    private int majority;

    public void init() {

        if (!initial.compareAndSet(false, true)) {
            logger.info("cluster manager has already initial!");
            return;
        }

        logger.info("cluster manager initial!");

        //todo 是否考虑动态修改
        localNode = new AtomicReference<Node>(new Node(localIpPort));
        String[] otherIpPortArr = othersIpPort.split(",");
        otherNodes.addAll(Arrays.asList(otherIpPortArr));

        majority = otherNodes.size() / 2 + 1;
    }

    //防止本jvm中所有方法都直接操作相同引用，导致取出后的数据仍有被修改的问题
    public Node cloneLocalNode() {

        Node source = localNode.get();

        Node node = new Node(source.getIpPort());
        node.setVoteFor(source.getVoteFor());
        node.setState(source.getState());
        node.setTerm(source.getTerm());
        node.setSource(source);

        return node;
    }


    //提供查看，但是不能修改！
    public Node getLocalNode() {

        return localNode.get();
    }

    public int getMajority() {
        return majority;
    }

    //===get/set

    public List<String> getOtherNodes() {
        return otherNodes;
    }

    public void setOtherNodes(List<String> otherNodes) {
        this.otherNodes = otherNodes;
    }

    public boolean cas(Node expect, Node update) {

        return this.localNode.compareAndSet(expect, update);
    }

    public void setLocalNode(Node localNode) {
        this.localNode = new AtomicReference<>(localNode);
    }
}
