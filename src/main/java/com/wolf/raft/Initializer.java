package com.wolf.raft;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Description:统一启动
 * <br/> Created on 12/29/2018
 *
 * @author 李超
 * @since 1.0.0
 */
@Component
public class Initializer {

    @Autowired
    private ClusterManger clusterManger;

    @Autowired
    private TimeElection timeElection;

    @Autowired
    private Heartbeat Heartbeat;

    @PostConstruct
    public void init() {

        clusterManger.init();
        timeElection.init();
        Heartbeat.init();
    }
}
