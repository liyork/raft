package com.wolf.raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 * <br/> Created on 12/29/2018
 *
 * @author 李超
 * @since 1.0.0
 */
public class TimeHelper {

    private static Logger logger = LoggerFactory.getLogger(TimeHelper.class);

    private static Random random = ThreadLocalRandom.current();

    //随机时间10~15
    public static long genElectionTime() {
        int duration = random.nextInt(6);
        duration += 10;
        long result = TimeUnit.SECONDS.toNanos(duration);
        logger.info("genElectionTime:"+result);
        return result;
    }

    //随机时间2~3
    public static long genHeartbeatInterval() {
        int duration = random.nextInt(2);
        duration += 2;
        long result = TimeUnit.SECONDS.toNanos(duration);
        logger.info("genHeartbeatInterval:"+result);
        return result;
    }

}
