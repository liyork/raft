package com.wolf.raft;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

/**
 * Description:
 * <br/> Created on 1/6/2019
 *
 * @author 李超
 * @since 1.0.0
 */
public class InitializerListener implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent  event) {
        System.out.println(Thread.currentThread().getName()+"_#####InitializerListener found!#####");

        Initializer initializer = RaftContainer.getBean("initializer",Initializer.class);
        initializer.init();
    }
}