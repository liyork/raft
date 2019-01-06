package com.wolf.raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Description:统一启动
 * <br/> Created on 12/29/2018
 *
 * @author 李超
 * @since 1.0.0
 */
@Component
public class Initializer {

    private Logger logger = LoggerFactory.getLogger(Initializer.class);

    @Autowired
    private ClusterManger clusterManger;

    @Autowired
    private TimeElection timeElection;

    @Autowired
    private Heartbeat Heartbeat;

    private AtomicBoolean initial = new AtomicBoolean();

    //todo 这个不能作为组件出现，应该在spring启动之后才能初始化！
    //@PostConstruct
    public void init() {

        //todo 暂时解法
        if (!initial.compareAndSet(false, true)) {
            return;
        }

        clusterManger.init();
        timeElection.init();
        Heartbeat.init();
    }
}
