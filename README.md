领导选举用于在集群中指定单个进程执行一些全局任务，使单例服务能以集群方式部署，预防单点故障。这个例子是以Spring AOP + Zookeeper来实现领导选举的简单Demo。

安装方法：
```bash
# 克隆项目
git clone git@github.com:lwmuk/spring-leader-example.git
# 切换到项目目录
cd spring-leader-example
# 编译运行
mvn spring-boot:run
# 或
mvn clean package
java -jar target/spring-leader-example*.jar
```
