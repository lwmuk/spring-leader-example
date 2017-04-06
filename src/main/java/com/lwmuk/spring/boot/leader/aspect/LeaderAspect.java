package com.lwmuk.spring.boot.leader.aspect;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Closeable;

/**
 * 领导选举切面类，用于参与选举，并根据自己的领导权决定是否激活当前应用的任务
 */
@Aspect
@Component
public class LeaderAspect extends LeaderSelectorListenerAdapter implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(LeaderAspect.class);

    private volatile boolean isLeader = false;

    @Autowired
    public LeaderAspect(CuratorFramework client, @Value("${election.path}") String path) {
        LeaderSelector selector = new LeaderSelector(client, path, this);
        selector.autoRequeue();
        selector.start();
    }

    /**
     * 执行该方法，为获得领导权，退出该方法，为失去领导权
     */
    public void takeLeadership(CuratorFramework cf) {
        // 获得领导权
        isLeader = true;
        log.info("Leadership granted.");

        // 一直沉睡，保持住领导权
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException ex) {
            // nothing to do
        }

        // 执行到这一步，将失去领导权
        close();
    }

    @Override
    public void close() {
        // 失去领导权
        isLeader = false;
        log.info("Leadership revoked.");
    }

    @Around("@annotation(com.zhyt.service.center.annotation.LeaderOnly)")
    public void onlyExecutionForLeader(ProceedingJoinPoint joinPoint) {
        if (!isLeader) {
            log.debug("I'm not leader, skip leader-only tasks.");
            return;
        }

        log.debug("I'm leader, execute leader-only tasks.");
        try {
            joinPoint.proceed();
        } catch (Throwable ex) {
            log.error(ex.getMessage());
        }
    }
}