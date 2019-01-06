package com.wolf.raft;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Description:
 * <br/> Created on 1/5/2019
 *
 * @author 李超
 * @since 1.0.0
 */
public class ExecutorManager {

    private static int count;

    private static ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {

            return new Thread(r, "http send thread-" + (++count));
        }
    });

    public static void execute(Runnable runnable) {

        executorService.execute(runnable);
    }
}
