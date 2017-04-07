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
import org.springframework.stereotype.Component;

import java.io.Closeable;

/**
 * 领导选举切面类，用于参与选举，并根据自己的领导权决定是否激活当前应用的任务
 */
@Aspect
@Component
public class LeaderAspect extends LeaderSelectorListenerAdapter implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(LeaderAspect.class);
    // 领导任期，这里为了测试设为2秒，真实环境下可以设为Long.MAX_VALUE
    private static final long TENURE_MS = 2000L;
    // 执行选举算法的根路径
    private static final String ELECTION_ROOT = "/election";

    private volatile boolean isLeader = false;
    private final LeaderSelector selector;

    @Autowired
    public LeaderAspect(CuratorFramework client) {
        selector = new LeaderSelector(client, ELECTION_ROOT, this);
        selector.autoRequeue();
        selector.start();
    }

    /**
     * 执行该方法，为获得领导权，退出该方法，为撤销领导权
     */
    public void takeLeadership(CuratorFramework cf) {
        // 获得领导权
        isLeader = true;
        log.info("Leadership granted.");

        // 沉睡，保持住领导权
        try {
            Thread.sleep(TENURE_MS);
        } catch (InterruptedException ex) {
            // nothing to do
        }

        // 执行到这一步，将撤销领导权
        revokeLeadership();
    }

    private void revokeLeadership() {
        isLeader = false;
        log.info("Leadership revoked.");
    }

    @Override
    public void close() {
        revokeLeadership();
    }

    @Around("@annotation(com.lwmuk.spring.boot.leader.annotation.LeaderOnly)")
    public void onlyExecuteForLeader(ProceedingJoinPoint joinPoint) {
        if (!isLeader) {
            log.debug("I'm not leader, skip leader-only tasks.");
            return;
        }

        try {
            log.debug("I'm leader, execute leader-only tasks.");
            joinPoint.proceed();
        } catch (Throwable ex) {
            log.error(ex.getMessage());
        }
    }
}