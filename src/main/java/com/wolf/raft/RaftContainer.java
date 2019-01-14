package com.wolf.raft;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Description: 容器，用于获取spring中的bean
 * <br/> Created on 1/5/2019
 *
 * @author 李超
 * @since 1.0.0
 */
@Component
public class RaftContainer implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        RaftContainer.applicationContext = applicationContext;
    }

    public static <T> T getBean(String beanName, Class<T> clazz) {
        return applicationContext.getBean(beanName, clazz);
    }
}
