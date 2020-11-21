### JMH微基准测试

#### 性能基准测试介绍

性能基准测试存在着许多深坑（pitfall）：JIT代码优化，垃圾回收，CPU缓存，超线程技术等

JMH 是由 Hotspot JVM 团队专家开发的，除了支持完整的基准测试过程，包括预热、运行、统计和报告等，还支持 Java 和其他 JVM 语言。更重要的是，它针对 Hotspot JVM 提供了各种特性，以保证基准测试的正确性，准确性，并且JMH 还提供了用近乎白盒的方式进行 Profiling 等工作的能力。



#### JMH使用方式

第一步：添加maven对应依赖。

```xml
<dependency>
    <groupId>org.openjdk.jmh</groupId>
    <artifactId>jmh-core</artifactId>
    <version>1.22</version>
</dependency>
<dependency>
    <groupId>org.openjdk.jmh</groupId>
    <artifactId>jmh-generator-annprocess</artifactId>
    <version>1.22</version>
    <scope>provided</scope>
</dependency>
```



第二步：编写基准测试对应的方法

```java
@Threads(5)
@Fork(5)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations=10, time=1000, timeUnit= TimeUnit.MILLISECONDS)
@Measurement(iterations=10, time=1000, timeUnit=TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BuilderVSBuffer {
    @Benchmark
    public void builder() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            sb.append("i");
        }
    }

    @Benchmark
    public void buffer() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 100000; i++) {
            sb.append("i");
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(BuilderVSBuffer.class.getSimpleName())
                .build();
        new Runner(options).run();
    }
}
```

注意：运行不支持Debug模式，不然会报错哦。



第三步：分析测试结果

```txt
# JMH version: 1.22
# VM version: JDK 1.8.0_211, Java HotSpot(TM) 64-Bit Server VM, 25.211-b12
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_211.jdk/Contents/Home/jre/bin/java
# VM options: -javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=49398:/Applications/IntelliJ IDEA.app/Contents/bin -Dfile.encoding=UTF-8
# Warmup: 10 iterations, 1000 ms each            【预热迭代，迭代次数和每次迭代时间】
# Measurement: 10 iterations, 1000 ms each       【测试迭代，迭代次数和每次迭代时间】
# Timeout: 10 min per iteration                  【每个迭代超时时间10min】
# Threads: 5 thread, will synchronize iterations 【同时运行测试代码的线程数】
# Benchmark mode: Throughput, ops/time           【操作数/时间单位】
# Benchmark: com.example.java.learn.jmh.BuilderVSBuffer.buffer【测试方法】


# Run progress: 0.00% complete, ETA 00:03:20  【进度和预期时间】
# Fork: 1 of 5  【fork的1号Java 虚拟机】
# Warmup Iteration   1: 9.370 ops/ms   【预热代码期每毫秒操作执行方法调用次数】
# Warmup Iteration   2: 14.544 ops/ms
# Warmup Iteration   3: 15.155 ops/ms
# Warmup Iteration   4: 14.924 ops/ms
# Warmup Iteration   5: 14.535 ops/ms
# Warmup Iteration   6: 13.721 ops/ms
# Warmup Iteration   7: 12.945 ops/ms
# Warmup Iteration   8: 13.258 ops/ms
# Warmup Iteration   9: 13.535 ops/ms
# Warmup Iteration  10: 13.572 ops/ms
Iteration   1: 13.332 ops/ms  【测试代码期每毫秒操作执行方法调用次数】
Iteration   2: 13.388 ops/ms
Iteration   3: 12.912 ops/ms
Iteration   4: 13.230 ops/ms
Iteration   5: 13.063 ops/ms
Iteration   6: 12.718 ops/ms
Iteration   7: 11.288 ops/ms
Iteration   8: 12.481 ops/ms
Iteration   9: 11.863 ops/ms
Iteration  10: 12.314 ops/ms

... 

【buffer方法执行结果说明】
Result "com.example.java.learn.jmh.BuilderVSBuffer.buffer":
  12.297 ±(99.9%) 0.257 ops/ms [Average]
  (min, avg, max) = (10.821, 12.297, 13.388), stdev = 0.519
  CI (99.9%): [12.040, 12.554] (assumes normal distribution)
  
【builder方法执行结果说明】 
Result "com.example.java.learn.jmh.BuilderVSBuffer.builder":
  12.697 ±(99.9%) 0.346 ops/ms [Average]
  (min, avg, max) = (11.221, 12.697, 13.841), stdev = 0.699
  CI (99.9%): [12.351, 13.043] (assumes normal distribution)


# Run complete. Total time: 00:03:29
...

【Benchmark方法结果汇总】
Benchmark                 Mode  Cnt   Score   Error   Units
BuilderVSBuffer.buffer   thrpt   50  12.297 ± 0.257  ops/ms
BuilderVSBuffer.builder  thrpt   50  12.697 ± 0.346  ops/ms
```

说明总的执行时间：(预热次数 * 单次预热时间 + 测试次数 * 单次测试执行时间) * fork数 * 方法数 = (10 + 10) * 5 * 2 = 200s = 3分钟20秒



#### 注解说明

##### @BenchmarkMode

Mode 表示 JMH 进行 Benchmark 时所使用的模式。通常是测量的维度不同，或是测量的方式不同。目前 JMH 共有四种模式：

* Throughput：整体吞吐量，单位是操作数/时间。 例如 ==> 1秒内可以执行多少次调用。
* AverageTime：调用的平均时间，单位是时间/操作数。例如 ==> 每次调用平均耗时xxx毫秒，
* SampleTime：随机取样输出取样结果分布。例如 ==> “99%调用在x毫秒以内，99.99%调用在x毫秒以内”
* SingleShotTime：以上模式都是默认一次 iteration 是 1s， SingleShotTime 只运行一次iteration。同时把 warmup 次数设为0，用于测试冷启动时的性能。
* ALL：代表上面所有。



##### @OutputTimeUnit

基准测试使用的时间类型。



##### @Thread

测试的线程数，可以注解在类上，也可以在方法上.



##### @Fork

JMH Fork出n个新的 Java 虚拟机，来运行性能基准测试。每个Fork包含n个预热迭代和n个测试迭代



##### @WarmUp

@Warmup注解有四个参数，分别为【预热迭代的次数iterations】，【每次迭代持续的时间time】和【timeUnit单位】（前者是数值，后者是单位），以及每次操作包含多少次对测试方法的调用【batchSize】。

说明：预热的目的是让JIT将代码编译成机器码，接近真实性能。



##### @Meansurement

测试迭代配置。它的可配置选项和@Warmup的一致。



##### @Benchmark

方法注解，表示该方法是需要进行 benchmark 的对象。



##### @State

类注解，JMH测试类必须使用 @State 注解，它定义了一个类实例的生命周期。由于 JMH 允许多线程同时执行测试，不同的选项含义如下：
* Scope.Thread：默认的 State，线程私有；
* Scope.Benchmark：所有测试线程共享一个实例，用于测试有状态实例在多线程共享下的性能；
* Scope.Group：每个线程组共享一个实例；

```java
public class MyBenchmark {
  @State(Scope.Benchmark)
  public static class MyBenchmarkState {
    int count;

    @Setup(Level.Invocation)
    public void before() {
      count = 0;
    }

    @TearDown(Level.Invocation)
    public void after() {
      // Run with -ea
      assert count == 1 : "ERROR";
    }
  }

  @Benchmark
  public void testMethod(MyBenchmarkState state) {
    state.count++;
  }
}
```

##### @Setup

方法注解，会在执行 benchmark 之前被执行，主要用于初始化。

##### @TearDown

方法注解，与@Setup相对的，会在所有 benchmark 执行结束以后执行，主要用于资源的回收等。@Setup/@TearDown注解使用Level参数来指定何时调用fixture：

| 名称             | 描述                                 |
| :--------------- | :----------------------------------- |
| Level.Trial      | 默认level。全部benchmark运行前后执行 |
| Level.Iteration  | 一次迭代前后执行                     |
| Level.Invocation | 每个方法调用前后(不推荐使用)         |



##### @Param

成员注解，可以用来指定某项参数的多种情况。特别适合用来测试一个函数在不同的参数输入的情况下的性能。

@Param 注解接收一个String数组，在 @Setup 方法执行前转化为为对应的数据类型。多个 @Param 注解的成员之间是乘积关系，如有两个用 @Param 注解的字段，第一个有5个值，第二个字段有2个值，那么每个测试方法会跑5*2=10次。