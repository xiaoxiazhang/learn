### Lombok使用讲解

Lombok能以简单的注解形式来简化java代码，提高开发人员的开发效率。例如开发中经常需要写的javabean，都需要花时间去添加相应的getter/setter，也许还要去写构造器、equals等方法，而且需要维护，当属性多时会出现大量的getter/setter方法，这些显得很冗长也没有太多技术含量，一旦修改属性，就容易出现忘记修改对应方法的失误。所以我们统一使用Lombok来实现这些非功能代码，减少代码量是代码更加简洁。

官方地址：https://projectlombok.org

github地址：<https://github.com/rzwitserloot/lombok> 


#### IntelliJ IDEA集成Lombok

第一步：安装插件

1). 定位到 File > Settings > Plugins
2). 点击 Browse repositories…
3). 搜索 Lombok Plugin
4). 点击 Install plugin
5). 重启 IDEA

第二步：激活annotation-processing的插件

![1545881042097](..\..\..\images\lombok_idea_1.png)



第三步：使用添加maven依赖

```xml
 <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.2</version>
</dependency>
```



#### Lombok常用注解说明

**@AllArgsConstructor**：作用于类，生成所有属性的代参构造函数

**@NoArgsConstructor**：作用于类，生成无参构造函数

**@toString**：作用于类，生成toString方法

**@EqualsAndHashCode**：作用于类，生成equals和hashcode方法

**@Setter，@Getter**：作用于类，为所有属性生成set和get方法，作用于属性，为该属性生成set和get方法；

**@Data**：作用于类，可以生成上面除了@AllArgsConstructor以外所有的方法。若想生成所有参数构造，需配合@AllArgsConstructor使用，但是无参构造就会失效，所以需要自定义或搭配@NoArgsConstructor一起使用。

**需要注意：**若自定义了set或get方法，@Setter，@Getter和@Data三个注解使用时是不会覆盖生成set或get方法

 

**@Builder**：用于类，生成一个当前类的builder构建器、builder方法和全参构造函数。构建器中的所有方法为当前类的属性名命名，功能涵盖set方法功能，但同时返回构建器对象，还有一个build方法，最终调用生成当前类对象。Builder方法用于生成构造器对象。

**需要注意：**使用该注解会同时生成全参构造器；如需无参构造器需要自定义或配合@NoArgsConstructor使用



**@Accessors(chain = true)**：一个还在实验阶段或公测阶段的一个功能注解。需要配合@Data或@set、@get使用。可包含三个参数：

* fluent：布尔值(默认false)，true时生成的set和get方法都会去掉set/get前缀。
* chain：布尔值(默认false)，只有fluent为true时默认true。true时所有set方法返回值由void变成当前对象；
* Prefix：前缀属性



**@Slf4j**：用于类，生成Log对象



//不常用注解

**@val @var**：使用Lombok ，java也能够像javascript一样使用弱类型定义变量
**@NonNull** ：在方法或构造函数的参数上使用@NonNull，lombok将生成一个空值检查语句。
**@Cleanup**： 使用该注解能够自动释放io资源
**@RequiredArgsConstruct**：
**@value**： @data的不可变对象 （不可变对象的用处和创建)：
**@SneakyThrows**：把checked异常转化为unchecked异常，好处是不用再往上层方法抛出了，美其名曰暗埋异常
**@Synchronized**：类似于Synchronized 关键字 但是可以隐藏同步锁

```java
//pojo类使用姿势(DO,DTO,VO)
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CoinAccountDO implements Serializable 

//service使用姿势
@Slf4j
@Service
public class CoinAccountDetailService 

//说明:@builder其实可以使用@Accessors(chain = true)类替代，效果基本一样
```





#### Lombok工作原理分析

JDK5引入了注解的同时，也提供了两种解析方式。

**运行时解析**：运行时能够解析的注解，必须将@Retention设置为RUNTIME，这样就可以通过反射拿到该注解。java.lang,reflect反射包中提供了一个接口AnnotatedElement，该接口定义了获取注解信息的几个方法，Class、Constructor、Field、Method、Package等都实现了该接口。

**编译时解析**：编译时解析有两种机制，分别简单描述下：

1. Annotation Processing Tool

   >apt自JDK5产生，JDK7已标记为过期，不推荐使用，JDK8中已彻底删除，自JDK6开始，可以使用Pluggable Annotation Processing API来替换它，apt被替换主要有2点原因：api都在com.sun.mirror非标准包下;没有集成到javac中，需要额外运行

2. Pluggable Annotation Processing API

   [JSR 269](https://jcp.org/en/jsr/detail?id=269)自JDK6加入，作为apt的替代方案，它解决了apt的两个问题，javac在执行的时候会调用实现了该API的程序，这样我们就可以对编译器做一些增强，这时javac执行的过程如下： ![1545879013802](..\..\..\images\lombok_jsr.png)

   Lombok本质上就是一个实现了“[JSR 269 API](https://www.jcp.org/en/jsr/detail?id=269)”的程序。在使用javac的过程中，它产生作用的具体流程如下：

   第一步：javac对源代码进行分析，生成了一棵抽象语法树（AST）

   第二步：运行过程中调用实现了“JSR 269 API”的Lombok程序

   第三步：此时Lombok就对第一步骤得到的AST进行处理，找到@Data注解所在类对应的语法树（AST），然后修改该语法树（AST），增加getter和setter方法定义的相应树节点

   第四步：javac使用修改后的抽象语法树（AST）生成字节码文件，即给class增加新的节点（代码块）
