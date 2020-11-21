### Maven使用详解

#### maven 简介

Maven是一个跨平台的项目管理工具，主要负责Java项目的构建、依赖管理和项目信息管理。

Maven的优势：约定优于配置，项目构建简单，CI集成，插件丰富，测试支持

　

#### Maven安装配置

第一步：下载安装包：http://maven.apache.org/download.html
第二步：安装，解压下载的zip包
第三步：环境变量的设置
M2_HOME:  Maven的安装目录 （D:\maven）
M2:  %M2_HOME%\bin
将M2添加到环境变量path中: path:%M2%

第四步：进行测试`mvn –v`




####  Maven结构

##### 项目结构

```
app
|-- pom.xml
|-- src
    |-- main
    |   |-- java      (项目java类文件位置)
    |   |-- resources (项目配置资源文件位置)
    |   |-- webapp    (web应用打包后的位置) 
    |-- test
        |-- java      (项目单元测试java类文件位置)
        |-- resources (项目单元测试配置资源文件位置)
```



##### pom.xml结构

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" 
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd "> 
    <!-- 声明项目描述符遵循POM模型版本。当Maven引入了新的特性或者其他模型变更时确保稳定性。 --> 
    <modelVersion>4.0.0</modelVersion>
    <!-- 项目产生的构件类型，例如jar、war、ear、pom。--> 
    <packaging>pom</packaging>
    <parent>
        <!-- 父项目的坐标。 --> 
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.0.RELEASE</version>
         <!-- 父项目的pom.xml文件的相对路径。相对路径允许你选择一个不同的路径。默认值				 是../pom.xml。Maven首先在构建当前项目的地方寻找父项目的pom，其次在文件系统的这个位置		（relativePath位置），然后在本地仓库，最后在远程仓库寻找父项目的pom。 --> 
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
   
   <!-- 模块.列出的每个模块元素是指向该模块的目录的相对路径  -->
   <modules>
        <!--子项目相对路径-->
        <module>trade-service-consign-common</module>
        <module>trade-service-consign-api</module>
        <module>trade-service-consign-core</module>
        <module>trade-service-consign-app</module>
        <module>trade-service-consign-generator</module>
    </modules>


    <!-- 项目GAV坐标--> 
    <groupId>com.ggj.trade</groupId> 
    <artifactId>trade-consign-service</artifactId> 
    <version> 1.0.0</version> 
    <!-- 打包方式，父工程使用pom -->
    <packaging>jar</packaging> 
    

    <!-- 项目的名称, Maven产生的文档用 --> 
    <name> ... </name> 
    <!-- 项目主页的URL, Maven产生的文档--> 
    <url> http://maven.apache.org </url> 
    <!-- 项目的详细描述,  --> 
    <description>...</description> 
    
    <!-- 描述了这个项目构建环境中的前提条件。 --> 
    <prerequisites> 
       ...
    </prerequisites> 
    <!-- 项目持续集成信息 --> 
    <ciManagement> 
       ...
    </ciManagement>
    <!-- 项目创建年份，4位数字。当产生版权信息时需要使用这个值。 --> 
    <inceptionYear /> 
    <!-- 项目相关邮件列表信息 --> 
    <mailingLists> 
       ...
    </mailingLists> 
    <!-- 项目开发者列表 --> 
    <developers> 
       ...
    </developers> 
    <!-- 项目的其他贡献者列表 --> 
    <contributors> 
       ...
    </contributors> 
    <!-- 该元素描述了项目所有License列表。 --> 
    <licenses> 
       ...
    </licenses> 
    <!-- 描述项目所属组织的各种属性。--> 
    <organization> 
        ... 
    </organization> 
    
    
    <!-- 配置属性信息,后面的配置项可以通过${name}引用 --> 
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <java.version>1.8</java.version>
    </properties> 
   
     
    <!-- build项目配置信息 --> 
    <build> 
        <!-- 执行build任务时，如果没有指定目标，将使用的默认值。-->
        <defaultGoal>install</defaultGoal>
        <!-- build目标文件的存放目录，默认在${basedir}/target目录 -->
        <directory>${basedir}/target</directory>  
    	<!-- 产生的构件的文件名，默认值是${artifactId}-${version}。 --> 
        <finalName></finalName> 
        <!-- 定义在filter的文件中的name=value键值对，会在build时代替${name}值应用到resources中。maven的默认filter文件夹为${basedir}/src/main/filters-->
        <filters>
            <filter>filters/filter1.properties</filter>
        </filters> 
    
        <!-- 这个元素描述了项目相关的所有资源路径列表。 --> 
        <resources> 
            <!-- 这个元素描述了项目相关或测试相关的所有资源路径 --> 
            <resource> 
                <!--  指定build后的resource存放的文件夹，默认是basedir。--> 
                <targetPath></targetPath> 
                <!-- true/false，表示为这个resource，filter是否激活 --> 
                <filtering></filtering>
                <!-- 定义resource文件所在文件夹，默认为${basedir}/src/main/resources --> 
                <directory></directory>
                <!--  指定哪些文件将被匹配，以*作为通配符.例如**/*.xml. --> 
                <includes>
                    <include></include>
                </includes>
                <!--  指定哪些文件将被忽略，例如**/*.xml -->
                <excludes>
                    <exclude></exclude>
                </excludes>
            </resource> 
        </resources> 

        <!-- 这个元素描述了单元测试相关的所有资源路径，例如和单元测试相关的属性文件。 --> 
        <testResources> 
           ...
        </testResources> 

        <!--  pluginManagement配置和plugins是一样的，只是用于继承，使得可以在子pom中使用 --> 
        <pluginManagement> 
            ...
        </pluginManagement> 

        <!-- 该项目使用的插件列表 --> 
         <plugins> 
             <!-- plugin模板配置 -->
             <plugin>  
                <groupId>org.apache.maven.plugins</groupId>  
                <artifactId>maven-jar-plugin</artifactId>  
                <version>2.0</version>  
                <extensions>false</extensions>  
                <inherited>true</inherited>  
                <configuration>  
                    <classifier>test</classifier>  
                </configuration>  
                <dependencies>...</dependencies>  
                <executions>...</executions>  
            </plugin>         
        </plugins> 
    </build> 

    <!-- 多环境配置(配合build->resources节点，把需要的文件引入对应的环境) --> 
    <profiles>
        <profile>
            <id>dev</id>
            <properties>
                <profileActive>dev</profileActive>
            </properties>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
        </profile>

        <profile>
            <id>test</id>
            <properties>
                <profileActive>test</profileActive>
            </properties>
        </profile>

        <profile>
            <id>pre</id>
            <properties>
                <profileActive>pre</profileActive>
            </properties>
        </profile>

        <profile>
            <id>prod</id>
            <properties>
                <profileActive>prod</profileActive>
            </properties>
        </profile>
    </profiles>  

    <!-- 描述了项目相关的所有依赖 --> 
    <dependencies> 
        <dependency> 
            <groupId>org.apache.maven</groupId> 
            <artifactId>maven-artifact</artifactId> 
            <version>3.8.1</version> 
            <type>jar</type> 
            <classifier></classifier>         
            <scope>test</scope> 
            <!-- 排除依赖 --> 
            <exclusions> 
                <exclusion> 
                    <artifactId> spring-core </artifactId> 
                    <groupId> org.springframework </groupId> 
                </exclusion> 
            </exclusions> 
        </dependency> 
        ...
    </dependencies> 
                                     
     <!-- 继承自该项目的所有子项目的默认依赖信息。用于版本仲裁 --> 
    <dependencyManagement> 
        <dependencies> 
            <!-- 参见dependencies/dependency元素 --> 
            <dependency> 
            </dependency> 
        </dependencies> 
    </dependencyManagement> 

    <!-- 描述项目deploy相关信息 - (mvn deploy) --> 
    <distributionManagement>
        <snapshotRepository>
            <id>nexus-snapshots</id>
            <name>Internal Snapshot</name>
            <url>http://nexus.ops.yangege.cn/repository/maven-snapshots/</url>
        </snapshotRepository>
        <repository>
            <id>nexus-releases</id>
            <name>Internal Releases</name>
            <url>http://nexus.ops.yangege.cn/repository/maven-releases/</url>
        </repository>
    </distributionManagement>
        
        
    <!-- 远程仓库列表。 --> 
    <repositories> 
        ...
    </repositories> 

    <!-- 插件的远程仓库列表 --> 
    <pluginRepositories> 
       ...
    </pluginRepositories> 
               
    <!-- 描述使用报表插件产生报表的规范。--> 
    <reporting> 
       ...
    </reporting> 
</project> 
```



##### 内置隐式变量

Maven提供了三个隐式的变量可以用来访问环境变量env，POM信息，和Maven Settings

* env变量，暴露操作系统或者shell的环境变量。如POM中对\${env.PATH}引用将会被\${PATH}环境变量替换
* project变量：使用点标记(.)的路径来引用POM元素的值。如${project.packaging} 打包类型，缺省为jar
* settings变量：暴露了Maven settings信息。可以使用点标记(.)的路径来引用settings.xml文件中元素的值。例如${settings.offline}会引用~/.m2/settings.xml文件中offline元素的值。

* 其他：${basedir} 项目根目录



####  Maven生命周期

​        Maven拥有三套相互独立的生命周期，它们分别为clean，default和site。 每个生命周期包含一些阶段，这些阶段是有顺序的，并且后面的阶段依赖于前面的阶段，用户和Maven最直接的交互方式就是调用这些生命周期阶段。 以clean生命周期为例，包含的阶段有pre-clean, clean 和 post clean。当用户调用pre-clean的时候，只有pre-clean得以执行，当用户调用clean的时候，pre-clean和clean阶段会得以顺序执行；当用户调用post-clean的时候，pre-clean,clean,post-clean会得以顺序执行。
​		较之于生命周期阶段的前后依赖关系，三套生命周期本身是相互独立的，用户可以仅仅调用clean生命周期的某个阶段，或者仅仅调用default生命周期的某个阶段，而不会对其他生命周期产生任何影响。 

##### clean 生命周期

clean生命周期的目的是清理项目，它包含三个阶段：

* pre-clean 执行一些清理前需要完成的工作。

* clean 清理上一次构建生成的文件。

* post-clean 执行一些清理后需要完成的工作。 

 

##### default 生命周期

default生命周期定义了真正构件时所需要执行的所有步骤，它是生命周期中最核心的部分，它包含的阶段如下：

* validate 验证项目是否正确和所有需要的相关资源是否可用
* initialize 初始化构建

* generate-sources

* process-sources 处理源代码

* generate-resources 

* process-resources 处理项目主资源文件。对src/main/resources目录的内容进行变量替换等工作后，复制到项目输出的主classpath目录中。

* compile 编译项目的主源代码

* process-classes

* generate-test-sources

* process-test-sources 处理项目测试资源文件

* generate-test-resources

* process-test-resources 处理测试的资源文件

* test-compile 编译项目的测试代码

* process-test-classes

* test 使用单元测试框架运行测试，测试代码不会被打包或部署

* prepare-package 做好打包的准备

* package 接受编译好的代码，打包成可发布的格式
* pre-integration-test
* integration-test
* post integration-test
* verify
* install 将包安装到Maven本地仓库，供本地其他Maven项目使用
* deploy 将最终的包复制到远程仓库，供其他Maven项目使用

​       

##### site 生命周期

site生命周期的目的是建立和发布项目站点，Maven能够基于POM所包含的信息，自动生成一个友好的站点，方便团队交流和发布项目信息。该生命周期包含如下阶段：

* pre-site 执行一些在生成项目站点之前需要完成的工作
* site 生成项目站点文档
* post-site 执行一些在生成项目站点之后需要完成的工作
* site-deploy 将生成的项目站点发布到服务器上

**注意：**生命周期各个阶段由不同插件来完成。



####  Maven坐标

在我们开发Maven项目的时候，需要为其定义适当的坐标，这是Maven强制要求的。在这个基础上，其他Maven项目才能应用该项目生成的构件。Maven坐标为各种构件引入了秩序，任何一个构件都必须明确定义自己的坐标，而一组Maven坐标是通过一些元素定义的，它们是groupId,artifactId,version,packaging,class-sifer。下面讲解一下各个坐标元素： 

```xml
<dependency>  
    <groupId>com.company.my-app</groupId>  
    <artifactId>my-app</artifactId>  
    <packaging>jar</packaging>  
    <version>0.0.1-SNAPSHOT</version>
</depandency>
```

**groupId** ：定义当前Maven项目隶属的实际项目。首先，Maven项目和实际项目不一定是一对一的关系。比如SpringFrameWork这一实际项目，其对应的Maven项目会有很多，如spring-core,spring-context等。这是由于Maven中模块的概念，因此，一个实际项目往往会被划分成很多模块。其次，groupId不应该对应项目隶属的组织或公司。原因很简单，一个组织下会有很多实际项目。groupId应该定义到组织实际项目级别，而artifactId只能定义Maven项目（模块）。groupId的表示方式与Java包名的表达方式类似，通常与域名反向一一对应。

**artifactId** : 该元素定义当前实际项目中的一个Maven项目（模块），推荐的做法是使用实际项目名称作为artifactId的前缀。比如上例中的my-app。 

**version** : 该元素定义Maven项目当前的版本 （0.0.1SNAPSHOT）

**packaging** ：定义Maven项目打包的方式，首先，打包方式通常与所生成构件的文件扩展名对应，如上例中的packaging为jar,最终的文件名为xxx.jar。也可以打包成war等。当不定义packaging的时候，Maven 会使用默认值使用的是jar。pom用于聚合项目

**classifier**: 该元素用来帮助定义构建输出的一些附件。附属构件与主构件对应，如上例中的主构件为my-app-0.0.1-SNAPSHOT.jar,该项目可能还会通过一些插件生成如my-app-0.0.1-SNAPSHOT-javadoc.jar,my-app-0.0.1-SNAPSHOT-sources.jar, 这样附属构件也就拥有了自己唯一的坐标



####  Maven依赖

```xml
<dependency>
	<groupId>org.slf4j</groupId>
	<artifactId>slf4j-api</artifactId>
	<version>1.7.6</version>
	<scope>compile</scope>
</dependency>
```

* groupId, artifactId, version: 依赖的基本坐标（GAV）

* type：依赖的类型，对应于项目坐标定义的packaging。默认值为jar。

* scope：依赖范围

  > compile依赖范围：测试和主程序都有效，打包；
  >
  > test依赖范围：测试程序有效，不打包(测试测序只存在于mvn测试生命周期阶段) ；
  >
  > provided依赖范围： --测试和主程序都有效，不打包 如：javax.servlet-api。服务器含有该api)

* optional：标记依赖是否可选

* exclusions：排除传递性依赖

 

##### 依赖范围(scope)

* compile：编译依赖范围，在编译，测试，运行时都需要，依赖范围默认值
* test：测试依赖范围，测试时需要。编译和运行不需要，如junit
* provided：已提供依赖范围，编译和测试时需要。运行时不需要,如servlet-api
* runtime：运行时依赖范围，测试和运行时需要。编译不需要,例如面向接口编程，JDBC驱动实现jar
* system：系统依赖范围。本地依赖，不在maven中央仓库，结合systemPath标签使用



##### 分类器(classifier)

GAV是Maven坐标最基本最重要的组成部分，但GAV不是全部。还有一个元素叫做分类器（classifier），90%的情况你不会用到它，但有些时候，分类器非常不可或缺。举个简单的例子，当我们需要依赖TestNG的时候，简单的声明GAV会出错，因为TestNG强制需要你提供分类器，以区别jdk14和jdk15，我们需要声明对TestNG的依赖：

```xml
<dependency>  
    <groupId>org.testng</groupId>  
    <artifactId>testng</artifactId>  
	<version>5.7</version>  
	<classifier>jdk15</classifier>  
</dependency>
```

你会注意到maven下载了一个名为testng-5.7-jdk15.jar的文件。其命名模式实际上是<artifactId>-<version>-<classifier>.<packaging>。理解了这个模式以后，你就会发现很多文件其实都是默认构件的分类器扩展，如 myapp-1.0-test.jar, myapp-1.0-sources.jar。

分类器还有一个非常有用的用途是：我们可以用它来声明对test构件的依赖，比如，我们在一个核心模块的src/test/java中声明了一些基础类，然后我们发现这些测试基础类对于很多其它模块的测试类都有用。没有分类器，我们是没有办法去依赖src/test/java中的内容的，因为这些内容不会被打包到主构件中，它们单独被打包成一个模式为<artifactId>-<version>-test.jar的文件。我们可以使用分类器来依赖这样的test构件：

```xml
<dependency>  
    <groupId>org.myorg.myapp</groupId>  
    <artifactId>core</artifactId>  
    <version>${project.version}</version>  
    <classifier>test</classifier>  
</dependency>
```

**说明：**理解了分类器，那么可供依赖的资源就变得更加丰富。



##### 依赖归类

如果我们项目中用到很多关于Spring Framework的依赖，它们分别是org.springframework:spring-core:4.0.0, org.springframework:spring-beans:4.0.0。它们都是来自同一项目的不同模块。因此，所有这些依赖的版本都是相同的，如果将来需要升级Spring Framework，这些依赖的版本会一起升级。因此，我们应该在一个唯一的地方定义版本，并且在dependency声明引用这一版本，只需要修改一处即可。

```xml
<properties>  
    <springframework.version>4.0.0</springframework.version>  
</properties>  
  
<dependencies>    
    <dependency>  
        <groupId>org.springframework</groupId>  
        <artifactId>spring-core</artifactId>  
        <version>${springframework.version}</version>  
    </dependency>  
    <dependency>  
        <groupId>org.springframework</groupId>  
        <artifactId>spring-beans</artifactId>  
        <version>${springframework.version}</version>             
    </dependency>  
</dependencies>  
```



##### 依赖传递

项目中依赖第三方包A, A又依赖第三方B,C。那么项目就会间接依赖B,C。比如项目引入spring-core依赖，会把commons-logging也依赖进来。这就是依赖传递。依赖传递时仲裁原则：

* 依赖路径最短原则
* 依赖路径一样长,加载先后原则

使用Maven时，发生诸如"NoSuchMethodError"或者"ClassNotFoundException"之类的问题【jar包冲突】。Maven采用"最近获胜策略（nearest wins strategy）"的方式处理依赖冲突，即如果一个项目最终依赖于相同artifact的多个版本，在依赖树中离项目最近的那个版本将被使用。**如果依赖路径长度相同，先声明dependency的依赖的版本会被使用。**所以，如果项目中某个包的老版本别依赖进项目，就会因为某个新加的类或者类中新加的方法没有导致运行时异常。对于这种有依赖冲突所导致的问题，我们有两种解决方法：

* 显式加入对冲突jar包依赖
* 使用  <exclusion>将比较老的jar包依赖排除掉

```xml
<dependency>    
     <groupId>org.springframework</groupId>  
     <artifactId>spring-core</artifactId>  
     <version>5.1.8</version>  
     <exclusions>  
           <exclusion>      
                <groupId>commons-logging</groupId>          
                <artifactId>commons-logging</artifactId>  
           </exclusion>  
     </exclusions>  
</dependency>  
```



说明：并非所有的依赖都可以被传递。（非compile范围的依赖不能传递）



####   Maven聚合和继承

##### 聚合模块

聚合项目的目的是一条Maven命令可以构建多个模块，packaging为pom。Modules中定义模块，module中为子模块的相对路径。作用是：能根据模块之间的依赖，自动计算出合理的模块构建顺序。

```xml
<!-- 在聚合模块中指定 -->
<packaging>pom</packaging>

<modules>
    <module>trade-reconciliation-api</module>
    <module>trade-reconciliation-core</module>
    <module>trade-reconciliation-service</module>
    <module>trade-reconciliation-generator</module>
</modules>

```



##### 继承模块

继承的目的就是在父工程中定义通用的配置。子模块负责继承，这样可以减少大量的重复配置。

可被继承的POM元素：**groupId**，**version**，description，organization，inceptionYear，developers，contributors，distributionManagement，issueManagement，ciManagement，scm，mailingLists，**properties**，**dependencies**，**dependencyManagement**，**repositories**，**build**，reporting

常见的使用是：父工程使用<dependencyManage>做版本仲裁。子工程继承依赖的版本号。

```xml
<!-- 父工程添加 -->
<parent>
    <artifactId>trade-reconciliation</artifactId>
    <groupId>com.ggj.trade</groupId>
    <version>1.0.0</version>
</parent>
<dependencyManagement>
    <dependencies>
        <!-- 对账系统内部依赖 -->
        <dependency>
            <groupId>com.ggj.trade</groupId>
            <artifactId>trade-reconciliation-api</artifactId>
            <version>${trade.reconciliation.api.version}</version>
        </dependency>
        <dependency>
            <groupId>com.ggj.trade</groupId>
            <artifactId>trade-reconciliation-core</artifactId>
            <version>${trade.reconciliation.core.version}</version>
        </dependency>
        ...
    </dependencies>
</dependencyManagement>
```





##### 聚合和继承的关系

聚合和继承没有关系，但是一般来讲他们都是配合起来使用。聚合模块是把多个项目聚合起来，处理模块之间的依赖关系。继承的作用为了消除重复配置，我们把很多相同的配置提取出来，例如：grouptId，version等。可以存在一个模块是被聚合模块但是不是子模块的情况。

**反应堆（Reactor）**是指所有模块组成的一个构建结构。对于单个模块的项目来说反应堆就是该模块本身，但是对于多模块的项目来说，反应堆就包含了各个模块之间的继承与依赖的关系，最后再根据各个模块在pom中声明的先后顺序，自动计算出合理的模块构建顺序。

 

#### Maven Setting配置解析

**文件存放位置**：

- 全局配置: ${M2_HOME}/conf/settings.xml
- 用户配置: ${user.home}/.m2/settings.xml  ()

优先级说明：用户配置优先于全局配置



##### 声明规范

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
```



##### localRepository

```xml
 <!-- 本地仓库的路径。默认值为${user.home}/.m2/repository。 -->
 <localRepository>usr/local/maven</localRepository>
```



##### interactiveMode

```xml
 <!--Maven是否需要和用户交互以获得输入。默认为true。-->
 <interactiveMode>true</interactiveMode>
```



##### usePluginRegistry

```xml
<!--Maven是否需要使用plugin-registry.xml文件来管理插件版本。如果需要让Maven使用文件${user.home}/.m2/plugin-registry.xml来管理插件版本，则设为true。默认为false。-->
 <usePluginRegistry>false</usePluginRegistry>
```



##### offline

```xml
 <!--表示Maven是否需要在离线模式下运行。如果构建系统需要在离线模式下运行，则为true，默认为false。当由于网络设置原因或者安全因素，构建服务器不能连接远程仓库的时候，该配置就十分有用。 -->
 <offline>false</offline>
```



##### pluginGroups

```xml
<!--当插件的组织Id（groupId）没有显式提供时，供搜寻插件组织Id（groupId）的列表。该元素包含一个pluginGroup元素列表，每个子元素包含了一个组织Id（groupId）。当我们使用某个插件，并且没有在命令行为其提供组织Id（groupId）的时候，Maven就会使用该列表。默认情况下该列表包含了org.apache.maven.plugins和org.codehaus.mojo -->
<pluginGroups>
    <!--plugin的组织Id（groupId） -->
    <pluginGroup>org.codehaus.mojo</pluginGroup>
</pluginGroups>
```



##### proxies

```xml
<!--用来配置不同的代理，多代理profiles 可以应对笔记本或移动设备的工作环境：通过简单的设置profile id就可以很容易的更换整个代理配置。 -->
 <proxies>
  <!--代理元素包含配置代理时需要的信息-->
  <proxy>
   <!--代理的唯一定义符，用来区分不同的代理元素。-->
   <id>myproxy</id>
   <!--该代理是否是激活的那个。true则激活代理。当我们声明了一组代理，而某个时候只需要激活一个代理的时候，该元素就可以派上用处。 -->
   <active>true</active>
   <!--代理的协议。 协议://主机名:端口，分隔成离散的元素以方便配置。-->
   <protocol>http</protocol>
   <!--代理的主机名。协议://主机名:端口，分隔成离散的元素以方便配置。  -->
   <host>proxy.somewhere.com</host>
   <!--代理的端口。协议://主机名:端口，分隔成离散的元素以方便配置。 -->
   <port>8080</port>
   <!--代理的用户名，用户名和密码表示代理服务器认证的登录名和密码。 -->
   <username>proxyuser</username>
   <!--代理的密码，用户名和密码表示代理服务器认证的登录名和密码。 -->
   <password>somepassword</password>
   <!--不该被代理的主机名列表。该列表的分隔符由代理服务器指定；例子中使用了竖线分隔符，使用逗号分隔也很常见。-->
   <nonProxyHosts>*.google.com|ibiblio.org</nonProxyHosts>
  </proxy>
 </proxies>
```



##### servers

```xml
<!-- 定义jar包下载的Maven仓库,定义部署服务器 -->
<servers>
    <server>
        <id>tomcat</id>
        <username>bruce</username>
        <password>password</password>
    </server>
   <!-- 指定私服nexus-releases账号密码 -->
    <server>
        <id>nexus-releases</id>
        <username>admin</username>
        <password>password</password>
    </server>
  </servers>
```



**mirrors**

```xml
<!-- 仓库下载镜像列表-->
<mirrors>
    <mirror>
        <id>alimaven</id>
        <name>aliyun maven</name>
        <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
        <mirrorOf>central</mirrorOf>        
    </mirror>
</mirrors>
```



##### profiles

```xml
<!--根据环境参数来调整构建配置的列表.它包含了id，activation, repositories, pluginRepositories和 properties元素.如果一个settings中的profile被激活，它的值会覆盖任何其它定义在POM中或者profile.xml中的带有相同id的profile。-->
<profiles>
    <profile>       
        <id>jdk-1.8</id>       
        <activation>       
            <activeByDefault>true</activeByDefault>       
            <jdk>1.8</jdk>       
         </activation>       
         <properties>       
             <maven.compiler.source>1.8</maven.compiler.source>       
             <maven.compiler.target>1.8</maven.compiler.target>       
             <maven.compiler.compilerVersion>1.8</maven.compiler.compilerVersion>              </properties>       
    </profile>
    <profile>
			<id>nexus</id>
            <!--远程仓库列表 --> 
			<repositories>
				<repository>
					<id>localRepo</id>
					<name>localRepo</name>
					<url>http://nexus.ops.yangege.cn/repository/maven-public</url>
					<layout>default</layout>
					<releases>
						<enabled>true</enabled>
					</releases>
					<snapshots>
						<enabled>true</enabled>
					</snapshots>
				</repository>
			</repositories>
			
          <!--插件的远程仓库列表-->
			<pluginRepositories>  
            <pluginRepository>  
              <id>localRepo</id>
              <url>http://nexus.ops.yangege.cn/repository/maven-public/</url>
              <snapshots><enabled>true</enabled></snapshots>  
              <releases><enabled>true</enabled></releases>
            </pluginRepository>  
          </pluginRepositories>
		</profile>
</profiles>
```



##### activeProfiles

```xml
<!--手动激活profiles的列表 -->
<activeProfiles>
    <activeProfile>nexus</activeProfile>
	<activeProfile>jdk-1.8</activeProfile>
</activeProfiles> 
```



####    Maven插件

```xml
<!-- 编译插件 -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.8.1</version>
    <configuration>
        <source>1.8</source>
        <target>1.8</target>
        <encoding>UTF-8</encoding>
    </configuration>
</plugin>

<!-- 源码插件 -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-source-plugin</artifactId>
    <version>3.1.0</version>
    <executions>
        <execution>
            <id>attach-sources</id>
            <goals>
                <goal>jar</goal>
            </goals>
        </execution>
    </executions>
</plugin>

<!-- deploy插件 -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-deploy-plugin</artifactId>
    <configuration>
        <skip>true</skip>
    </configuration>
</plugin>

<!-- SpringBoot编译插件 -->
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>repackage</goal>
            </goals>
        </execution>
    </executions>
</plugin>

```

 

 

####  Maven仓库

Maven仓库分为**本地仓库**和**远程仓库**，集中存放项目引用的jar包，无需将jar包放在程序中，结合Maven项目的pom.xml，使得项目管理jar包更容易，有以下几个优点：

* 对于项目来说，无需关心jar包存储问题，只管理好配置即可
* 易于搜索和使用jar包，不需要到网上到处找，仓库提供了几乎所有的jar包资源，并提供专业的搜索引擎，我们很容易就能找到我们所需的jar，并获取它的坐标信息，在Maven项目中增加相应的依赖即可，简单快捷
*  易于管理jar包版本，pom.xml中可以一目了然的看到jar包的版本，且可以通过相关配置来约束项目使用的jar包的版本
*  易于发现并排除jar包的依赖冲突，在传统方式管理jar包的情况下，如果出现多个jar包的依赖出现冲突的时候，很难排除冲突和找到互相适应的版本，而使用Maven管理，这些将变的非常简单
* 管理自定义的jar包:有些jar包是我们自己开发的，我们也可以将其放在仓库里，供其他项目使用，Maven仓库使jar包的发布和管理变得简单和有效，且能有效的控制版本变更

##### 本地仓库

本地仓库是远程仓库的一个缓冲和子集，当你构建Maven项目的时候，首先会从本地仓库查找资源，如果没有，那么Maven会从远程仓库下载到本地仓库。Maven缺省的本地仓库地址为${user.home}/.m2/repository，也可以在settings.xml文件中修改该地址

 

##### 远程仓库

远程仓库：是指部署在远程的仓库，默认是Maven的中央仓库，也可以是Nexus仓库服务器，远程仓库拥有大量的jar包资源，运用Nexus可以在局域网搭建企业级的Maven仓库。也可以使使用阿里镜像。

  

#####  私服Nexus

Nexus是Maven仓库管理器，用来搭建一个本地仓库服务器，这样做的好处是便于管理，节省网络资源，速度快，还有一个非常有用的功能就是可以通过项目的SNAPSHOT版本管理，来进行模块间的高效依赖开发。虽然可以通过中央仓库来获取我们所需要的jar包，但是现实往往是存在很多问题：

* 网速慢，我们可能需要花很长的时间来下载所需要的jar
* 如果我们的公司很大，有几百甚至几千人在用Maven，那么这些人都去通过中央仓库来获取jar，那么这是一个很大的资源浪费
*  如果存在模块之间的依赖开发，我们的伙伴就不能很方便的获取快照版本。
* 在实际开发过程中，有些jar的版本可能在中央仓库里面不存在，或者更新不及时，我们是获取不到这个jar。

所有以上问题，可以通过Nexus解决。这个仓库是本地的，下载的速度很快(内网速度)。它可以为我们公司的所有Maven使用者服务，进行统一管理；它能很好的帮我们进行存在模块依赖的项目开发；可以添加公司的二方包。



**下载与安装**

```shell
#下载地址：http://www.sonatype.org/nexus/go
#执行命令：
cd C:\nexus\nexus-2.0.4\bin\jsw\windows-x86-64\
#安装：install-nexus.bat
#启动： start-nexus.bat


#解压安装包
wget https://sonatype-download.global.ssl.fastly.net/nexus/3/nexus-3.2.0-01-unix.tar.gz
tar –zxvf nexus-3.0.0-03-unix.tar.gz
#修改Jdk路径
Nexus3.0.0的版本需要JDK1.8的版本
cd /data/nexus-3.0.0-03/bin
vi nexus
#添加JDK配置
INSTALL4J_JAVA_HOME_OVERRIDE= /usr/java/jdk1.8.0_65

# 修改使用的用户
vi nexus.rc
#添加配置
run_as_user=root

#修改配置文件    
vi /home/nexus/nexus-3.0.1-01/etc/org.sonatype.nexus.cfg 

#启动nexus服务
./ nexus start

#关闭防火墙
systemctl stop firewalld
systemctl stop iptables
```



**登录和修改密码**

访问：http://localhost:8081

用户名/密码：admin/admin123 



**搜索jar和仓库介绍**



**自定义仓库**

点击add->hosted Repository

输入Repository id和Repository name然后点击save即可



**上传jar到仓库**

```xml
<!-- setting.xml配置好私服服务器地址和账户密码 -->
<!-- pom.xml配置私服地址。然后执行:mvn deploy --> 
    <distributionManagement>
        <snapshotRepository>
            <id>nexus-snapshots</id>
            <name>Internal Snapshot</name>
            <url>http://nexus.ops.yangege.cn/repository/maven-snapshots/</url>
        </snapshotRepository>
        <repository>
            <id>nexus-releases</id>
            <name>Internal Releases</name>
            <url>http://nexus.ops.yangege.cn/repository/maven-releases/</url>
        </repository>
    </distributionManagement>
```



​        在企业的私服中，会存在snapshot快照仓库和release发布仓库，snapshot快照仓库用于保存开发过程中的不稳定版本，release正式仓库则是用来保存稳定的发行版本。maven会根据模块的版本号(pom文件中的version)中是否带有“-SNAPSHOT”(注意这里必须是全部大写)来判断是快照版本还是正式版本。如果是快照版本，那么在mvn deploy时会自动发布到私服的快照版本库中;如果是正式发布版本，那么在mvn deploy时会自动发布到正式版本库中。SNAPSHOT包和release包deploy有以下特点：

* 同一版本snapshot包可以被deploy多次，包名为当前日期+后缀从1开始往上加。

* 同一版本release包只能被deploy一次。

* 默认情况，本地更新maven，不会强行更新snapshot包。

  > 解决方法：idea 配置【Always update snapshots】或 mvn clean install -U可以强制更新snapshot包。
  >
  > 并且使用第一种方法效果更优。








####   Maven常用命令

```shell
mvn compile      # 编译源代码
mvn test         # 编译源代码，测试代码，执行测试
mvn package      # 把项目打成jar或者war包
mvn install      # 将jar包放入本地仓库中
mvn clean        # 删除target目录
mvn deploy       # 部署jar包至远程仓库

#创建maven项目(目录骨架)
mvn archetype:generate  -DgroupId=com.ggj.trade -DartifactId=consign -Dversion=1.0.0 -Dpackage=com.ggj.trade.consign

#mvn常用参数
mvn -e      #显示详细错误
mvn -U      #强制更新snapshot类型的插件或依赖库（否则maven一天只会更新一次snapshot依赖）
    -pl --projects <arg>       #构建指定的模块，模块间用逗号分隔；
    -am                        #同时构建所列模块的依赖模块；

mvn -Dxxx=yyy  #指定java全局属性
mvn -Pdev      #引用profile dev

#例如：clean install -pl 3t-admin -am -Pqa -DskipTests=true

```
