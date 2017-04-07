package com.lwmuk.spring.boot.leader;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {

    private static final int PORT = 2181;

    @Bean
    public CuratorFramework curatorFramework() {
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:" + PORT, new ExponentialBackoffRetry(1000, 3));
        client.start();
        return client;
    }

    public static void main(String[] args) {
        // 尝试启动一个内嵌的Zookeeper，如果异常可能是已经有别的Zookeeper占住端口，就不用再启动了
        try {
            TestingServer zk = new TestingServer(PORT);
        } catch (Exception ex) {
            // nothing to do
        }

        SpringApplication.run(Application.class, args);
    }
}
