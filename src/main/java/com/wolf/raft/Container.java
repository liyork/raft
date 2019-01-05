package com.wolf.raft;

/**
 * Description:
 * <br/> Created on 1/4/2019
 *
 * @author 李超
 * @since 1.0.0
 */
public class Container {

    private static ClusterManger clusterManger = new ClusterManger();

    public static ClusterManger getClusterManger() {

        return clusterManger;
    }
}
