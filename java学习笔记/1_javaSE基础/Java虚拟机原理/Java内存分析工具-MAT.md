### MAT工具使用

MAT是Memory Analyzer的简称，它是一款功能强大的Java堆内存分析器。可以用于查找内存泄露以及查看内存消耗情况。MAT是基于Eclipse开发的，是一款免费的性能分析工具。

> 下载地址：https://www.eclipse.org/mat/downloads.php



#### 1. 概念说明

**Shallow heap:**  指的是对象自身所占据的内存。

**Retained heap:**  指的是当对象不再被引用时，垃圾回收器所能回收的总内存，包括对象自身所占据的内存，以及仅能够通过该对象引用到的其他对象所占据的内存。

**GC Roots对象**常说的GC(Garbage Collector) Roots：指的是垃圾收集器（Garbage Collector）的对象，GC会收集那些不是GC Roots且没有被GC Roots引用的对象。一个对象可以属于多个root。

GC Roots：通过一系列的称为“GC Roots”的对象作为起始点， 从这些节点开始向下搜索， 搜索所走过的路径称为引用链（ Reference Chain），当一个对象到 GC Roots 没有任何引用链相连（ 用图论的话来说，就是从GC Roots到这个对象不可达）时，则证明此对象是不可用的。

GC Roots有以下几种：

* Class对象：由系统类加载器(system class loader)加载的对象，这些类是不能够被回收的，他们可以以静态字段的方式保存持有其它对象。我们需要注意的一点就是， 通过用户自定义的类加载器加载的类，除非相应的Java.lang.Class实例以其它的某种（或多种）方式成为roots，否则它们并不是roots.
* Thread：活着的线程
* Stack Local：Java方法的local变量或参数
* JNI Local：JNI方法的local变量或参数
* JNI Global：全局JNI引用
* Monitor Used：用于同步的监控对象
* Held by JVM：用于JVM特殊目的由GC保留的对象，但实际上这个与JVM的实现是有关的。





#### 2. MAT使用说明

MAT 获取二进制快照的方式有三种，一是使用 Attach API，二是新建一个 Java 虚拟机来运行 Attach API，三是使用jmap工具。当加载完堆快照之后，MAT 的主界面将展示一张饼状图，其中列举占据的 Retained heap 最多的几个对象。如图所示：overview的饼状图便是基于 Retained heap 的。

![img](..\..\..\images\da2e5894d0be535b6daa5084beb33ebf.png)



**Inspector窗口：**展示该类的 Class 实例的相关信息,还可以查看对象属性值。

**Actions视图选项**：直方图（histogram）和支配树（dominator tree）

* MAT 的直方图和jmap的子命令一样，都能够展示各个类的实例数目以及这些实例的 Shallow heap 总和。但是，MAT 的直方图还能够计算 Retained heap，并支持基于实例数目或 Retained heap 的排序方式（默认为 Shallow heap）。此外，MAT 还可以将直方图中的类按照超类、类加载器或者包名分组。

  ![1556202909288](..\..\..\images\1556202909288.png)



* 支配树的概念源自图论。在一则流图（flow diagram）中，如果从入口节点到 b 节点的所有路径都要经过 a 节点，那么 a 支配（dominate）b。在 a 支配 b，且 a 不同于 b 的情况下（即 a 严格支配 b），如果从 a 节点到 b 节点的所有路径中不存在支配 b 的其他节点，那么 a 直接支配（immediate dominate）b。这里的支配树指的便是由节点的直接支配节点所组成的树状结构。
  我们可以将堆中所有的对象看成一张对象图，每个对象是一个图节点，而 GC Roots 则是对象图的入口，对象之间的引用关系则构成了对象图中的有向边。这样一来，我们便能够构造出该对象图所对应的支配树。MAT 将按照每个对象 Retained heap 的大小排列该支配树。如下图所示：

  ![1556203092358](..\..\..\images\1556203092358.png)



**Reports选项(Leak Suspects)：** 自动匹配内存泄漏中的常见模式，并汇报潜在的内存泄漏问题。

* Description：可以查看线程调用栈信息。

  ![1556204485346](..\..\..\images\1556204485346.png)

* Shortest Paths To the Accumulation Point：GC root到聚集点的最短路径

* Accumulated Objects in Dominator Tree：对象支配树统计信息

* Accumulated Objects by Class in Dominator Tree：支配树统计类对象信息

* All Accumulated Objects by Class:：按类统计对象



#### 3. 排查问题的步骤

**第一步：**通过MAT工具找到使用内存最大的GC-Root，在**对象支配树**中找到具体占用内存空间的对象。

**第二步：**通过线程调用栈信息，定位出具体实现代码。**对象支配树**中有具体入参出参信息。