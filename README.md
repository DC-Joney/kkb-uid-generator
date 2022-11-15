## uid generator

### RingBuffer

单线程阻塞写入

### 配置使用

```java
@SpringBootApplication
@EnableUidGenerator
public class UidApplication {
    public static void main(String[] args) {
        SpringApplication.run(UidApplication.class, args);
    }
}
```

```yaml
server:
  port: 9999
spring:
  application:
    name: uid-generator-app
  profiles:
    active: test
kkb:
  plugins:
    uid-generator:
      dataSource:
        driver: com.mysql.jdbc.Driver
        url: jdbc:mysql://localhost:3306/test
        username: root
        password: 123456
      snowflake:
        useCache: false # 是否使用缓存
        epoch: 2021-11-11 # 初始时间 yyyy-MM-dd
        timeBits: 28 # 28 位 时间戳 - 可用 8.5 年左右
        workerBits: 22 # 22 位 workerId，最大 1 << 22 = 4194304
        seqBits: 13 # 13 位 序列号，最大 1 << 13 = 8192
        boostPower: 3 # 设置缓存大小（大小为 (maxSequence + 1) << boostPower）
        useCycleWorkerId: true # workerId 基于数据库 id，使用 & 运算计算 id；测试环境开启，线上环境关闭
        scheduleInterval: 300 # 填充缓存线程定时调度执行周期，300 秒 一次

---
spring:
  profiles: test

---
spring:
  profiles: prod
kkb:
  plugins:
    uid-generator:
      snowflake:
        useCycleWorkerId: false
```
