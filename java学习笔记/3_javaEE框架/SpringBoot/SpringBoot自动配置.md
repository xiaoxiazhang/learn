### SpringBoot自动配置

#### 1. 版本仲裁管理

每个Springboot项目为我们提供了统一的版本仲裁。只需要在指定对应的spring-boot-starter-parent版本。如果需要覆盖spring-boot默认版本，需要在自定义的父工程的dependencyManagement中定义覆盖版本。

```xml
<!-- spring boot项目的需要继承父项目，resource,plugin管理一-->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.0.4.RELEASE</version>
    <relativePath/> <!-- lookup parent from repository -->
</parent>


<!-- spring-boot-starter-parent的父项目，用于项目的版本仲裁 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-dependencies</artifactId>
    <version>2.0.4.RELEASE</version>
    <relativePath>../../spring-boot-dependencies</relativePath>
</parent>

<!--spring-boot-dependencies来真正管理Spring Boot应用里面的所有依赖版本；-->


```



#### 2. 包扫描自动配置

@SpringBootApplication: Spring Boot应用标注在某个类上说明这个类是SpringBoot的主配置类，SpringBoot 就应该运行这个类的main方法来启动SpringBoot应用；

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = {
		@Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
public @interface SpringBootApplication {
    ...
}

// 表示这是一个Spring Boot的配置类；(容器组件)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
public @interface SpringBootConfiguration {
}

// 配置类也是一个容器组件@Component
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Configuration {
    ...
}
```

**@AutoConﬁgurationPackage注解：**导入组件@Import(AutoConfigurationPackages.Registrar.class)

用于自动配置该注解所在包及其子包下所有组件。

```java
// ConfigurationClassPostProcessor会根据启动类，查找启动类基础包下面。所有@Bean , @componentScan等注解，来解析出对应bean信息。ClassPathBeanDefinitionScanner#doscan
@Override
public void registerBeanDefinitions(AnnotationMetadata metadata,
		BeanDefinitionRegistry registry) {
    // 注册该包下所有的组件（@Component...）
	register(registry, new PackageImport(metadata).getPackageName());
}
// 说明：可以通过@ComponentScan注解扩大需要扫描包的范围。 

```



#### 3. 导入自动配置类

**@EnableAutoConfiguration注解：**导入EnableAutoConﬁgurationImportSelector组件，导入自动配置类。有了自动配置类，免去了我们手动编写配置注入功能组件等的工作。

```java
public String[] selectImports(AnnotationMetadata annotationMetadata) {
	if (!isEnabled(annotationMetadata)) {
		return NO_IMPORTS;
	}
    // 扫描springboot autoconfig的META‐INF/spring-autoconfigure-metadata.properties
    // 包含自动配置的条件信息（先后条件，先决条件等）
	AutoConfigurationMetadata autoConfigurationMetadata = AutoConfigurationMetadataLoader.loadMetadata(this.beanClassLoader);
	AnnotationAttributes attributes = getAttributes(annotationMetadata);
    // 扫描所有jar包类路径下META‐INF/spring.factories 
    // 把扫描到的这些文件的内容包装成configurations对象 
	List<String> configurations = getCandidateConfigurations(annotationMetadata,
			attributes);
    
    // 获取所有的自动配置类后，然后排除掉注解上面配置了excluded对应的自动配置
	configurations = removeDuplicates(configurations);
	Set<String> exclusions = getExclusions(annotationMetadata, attributes);
	checkExcludedClasses(configurations, exclusions);
	configurations.removeAll(exclusions);
    
    // 通过自动配置条件信息过滤不配置的自动配置类
	configurations = filter(configurations, autoConfigurationMetadata);
	fireAutoConfigurationImportEvents(configurations, exclusions);
	return StringUtils.toStringArray(configurations);
}


```

得到的自动配置类如下：

![1554479661235](..\..\..\images\1554479661235.png)



**自动配置类的原理：**

@Conditional扩展注解作用：（判断是否满足当前指定条件）
@ConditionalOnJava ：系统的java版本是否符合要求
@ConditionalOnBean：容器中存在指定Bean；
@ConditionalOnMissingBean ：器中不存在指定Bean；
@ConditionalOnExpression ：满足SpEL表达式指定
@ConditionalOnClass：系统中有指定的类
@ConditionalOnMissingClass ：统中没有指定的类
@ConditionalOnSingleCandidate：容器中只有一个指定的Bean，或者这个Bean是首选Bean
@ConditionalOnProperty：系统中指定的属性是否有指定的值
@ConditionalOnResource：类路径下是否存在指定资源文件
@ConditionalOnWebApplication：当前是web环境
@ConditionalOnNotWebApplication：当前不是web环境
@ConditionalOnJndi：JNDI存在指定项



#### 4.  SpringBoot启动流程

##### 创建SpringApplication对象

```java
// 创建SpringApplication过程
public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources){
	this.resourceLoader = resourceLoader;
	Assert.notNull(primarySources, "PrimarySources must not be null");
    // 设置主程序类
	this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
    // 判断当前是否一个web应用
	this.webApplicationType = deduceWebApplicationType();
    // 从类路径下找到META‐INF/spring.factories配置的所有ApplicationContextInitializer；
    // 然后设置给SpringApplication
	setInitializers((Collection) getSpringFactoriesInstances(
			ApplicationContextInitializer.class));
    // 从类路径下找到ETA‐INF/spring.factories配置的所有ApplicationListener
	setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
    
    // 从多个配置类中找到有main方法的主配置类（通过报错堆栈信息获取main方法对应的类）
	this.mainApplicationClass = deduceMainApplicationClass();
}


```

设置对应的初始化器Initializer：

![1554558732151](..\..\..\images\1554558732151.png)



设置对应的监听器Listener：

![1554558845751](..\..\..\images\1554558845751.png)



##### 运行run方法

```java
// 运行的主流程方法
public ConfigurableApplicationContext run(String... args) {
	StopWatch stopWatch = new StopWatch();
	stopWatch.start();
	ConfigurableApplicationContext context = null;
	Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
	configureHeadlessProperty();
    // 获取SpringApplicationRunListeners；从类路径下META‐INF/spring.factories 
	SpringApplicationRunListeners listeners = getRunListeners(args);
     // 回调所有的获取SpringApplicationRunListener.starting()方法 
	listeners.starting();
	try { 
        // 封装命令行参数
		ApplicationArguments applicationArguments = new DefaultApplicationArguments(
				args);
        /*
         * 准备环境: 
         * 先创建环境，并设置环境（如：获取active环境spring.profiles.active）
         * 创建环境完成后回调SpringApplicationRunListener.environmentPrepared()；
         * 表示环境准备完成
         */
		ConfigurableEnvironment environment = prepareEnvironment(listeners,
				applicationArguments);
		configureIgnoreBeanInfo(environment);
        // 打印banner,如果没有设置spring.banner.location,默认使用SpringBootBanner打印
		Banner printedBanner = printBanner(environment);
        // 创建ApplicationContext；决定创建web的ioc还是普通的ioc
		context = createApplicationContext();
		exceptionReporters = getSpringFactoriesInstances(
				SpringBootExceptionReporter.class,
				new Class[] { ConfigurableApplicationContext.class }, context);
        // 准备上下文环境;将environment保存到ioc中；而且applyInitializers()； 
        // applyInitializers()：回调所有的ApplicationContextInitializer的initialize方法 
        // 回调所有的SpringApplicationRunListener的contextPrepared()； 
        // prepareContext运行完成以后回调所有的SpringApplicationRunListener的contextLoaded（）；
		prepareContext(context, environment, listeners, applicationArguments,
				printedBanner);
        
        //刷新容器；ioc容器初始化（如果是web应用还会创建嵌入式的Tomcat）；
        //扫描，创建，加载所有组件的地方；（配置类，组件，自动配置） 
		refreshContext(context);
        
		afterRefresh(context, applicationArguments);
		stopWatch.stop();
		if (this.logStartupInfo) {
			new StartupInfoLogger(this.mainApplicationClass)
					.logStarted(getApplicationLog(), stopWatch);
		}
        // 回调所有的SpringApplicationRunListener的started()；
		listeners.started(context);
        
        //从ioc容器中获取所有的ApplicationRunner和CommandLineRunner进行回调         
        //ApplicationRunner先回调，CommandLineRunner再回调
		callRunners(context, applicationArguments);
	}
	catch (Throwable ex) {
		handleRunFailure(context, ex, exceptionReporters, listeners);
		throw new IllegalStateException(ex);
	}

	try {
        // 回调所有的SpringApplicationRunListener的running()；
		listeners.running(context);
	}
	catch (Throwable ex) {
        
        // 回调所有的SpringApplicationRunListener的failed()；
		handleRunFailure(context, ex, exceptionReporters, null);
		throw new IllegalStateException(ex);
	}
	return context;
}


```



##### 事件监听机制

**ApplicationContextInitializer**：需要配置在META-INF/spring.factories 

**SpringApplicationRunListener**：需要配置在META-INF/spring.factories 

```properties
org.springframework.context.ApplicationContextInitializer=\ com.ggj.springboot.listener.HelloApplicationContextInitializer 
org.springframework.boot.SpringApplicationRunListener=\ com.ggj.springboot.listener.HelloSpringApplicationRunListener

```



**ApplicationRunner/CommandLineRunner**：只需要放在ioc容器中

```java
@Component
public class CoinApplicationRunner implements ApplicationRunner {

    @Autowired
    private DictDataService dictDataService;

    @Autowired
    private RedisCacheService redisCacheService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<DictDataDO> dictDataDOS = dictDataService.listAllCode();
        Map<String, DictDataDO> dictDataMap = dictDataDOS.stream().collect(Collectors.toMap(DictDataDO::getCode, e -> e));
        redisCacheService.multiSet(dictDataMap, CacheConstants.ONE_DAYS);
    }
}
```





#### 5. 自定义自动配置类 (starter)

