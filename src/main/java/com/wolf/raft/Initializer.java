package com.wolf.raft;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    //todo 这个不能作为组件出现，应该在spring启动之后才能初始化！
    //@PostConstruct
    public void init() {

        clusterManger.init();
        timeElection.init();
        Heartbeat.init();
    }
}
