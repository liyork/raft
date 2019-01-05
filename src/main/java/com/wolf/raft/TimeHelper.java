package com.wolf.raft;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 * <br/> Created on 12/29/2018
 *
 * @author 李超
 * @since 1.0.0
 */
public class TimeHelper {

    private static Random random = new Random();

    //随机时间10~15
    public static long genElectionTime() {
        int duration = random.nextInt(6);
        duration += 10;
        long result = TimeUnit.SECONDS.toNanos(duration);
        System.out.println("genElectionTime:"+result);
        return result;
    }

    //随机时间2~3
    public static long genHeartbeatInterval() {
        int duration = random.nextInt(2);
        duration += 2;
        long result = TimeUnit.SECONDS.toNanos(duration);
        System.out.println("genHeartbeatInterval:"+result);
        return result;
    }

    public static void main(String[] args) {

        long convert = TimeUnit.SECONDS.toNanos(1);
        System.out.println(convert);
    }
}
