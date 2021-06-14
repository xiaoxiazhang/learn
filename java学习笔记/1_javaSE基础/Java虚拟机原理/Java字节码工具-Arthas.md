#### Arthas使用详解

官方文档：https://alibaba.github.io/arthas/install-detail.html#arthas-boot

##### 快速安装

下载`arthas-boot.jar`，然后用`java -jar`的方式启动：

```shell
wget https://alibaba.github.io/arthas/arthas-boot.jar
java -jar arthas-boot.jar
```

打印帮助信息：

```shell
java -jar arthas-boot.jar -h
```

Docker里的Java进程：

```shell
docker exec -it  ${containerId} /bin/bash -c "wget https://alibaba.github.io/arthas/arthas-boot.jar && java -jar arthas-boot.jar"
```



##### 基础命令

- help——查看命令帮助信息
- cls——清空当前屏幕区域
- session——查看当前会话的信息
- [reset](https://alibaba.github.io/arthas/reset.html)——重置增强类，将被 Arthas 增强过的类全部还原，Arthas 服务端关闭时会重置所有增强过的类
- version——输出当前目标 Java 进程所加载的 Arthas 版本号
- history——打印命令历史
- quit——退出当前 Arthas 客户端，其他 Arthas 客户端不受影响
- shutdown——关闭 Arthas 服务端，所有 Arthas 客户端全部退出



##### thread命令

```shell
#查看具体线程的栈
thread 16 

#查看CPU使用率top n线程的栈
thread -n 3

#查找线程是否有阻塞
thread -b
```



##### trace命令(神器)

```shell
#查询类CouponRecommendBusiness，所有方法(*)也可以指定具体方法响应时间大于10ms的执行所在行
trace com.ggj.platform.promotion.business.coupon.CouponRecommendBusiness * '#cost > 10'
```



##### watch命令

```shell
# 插入接口调用情况
watch org.apache.commons.lang3.StringUtils contains  {params,returnObj} 'params[1]=="Arthas"' -x 2
```





##### JVM相关

* **dashboard：**当前系统的实时数据面板，按 ctrl+c 退出

* **jvm：**查看当前jvm信息
* **sysenv：**查看当前JVM的环境属性
* **sysprop：**查看当前JVM的系统属性



##### class相关

* **sc**：查看JVM已加载的类信息            例如：sc demo.*   sc -d demo.MathGame
* **sm**：查看已加载类的方法信息         例如：sm java.lang.String
* **jad**：反编译指定已加载类的源码     例如：jad java.lang.String













