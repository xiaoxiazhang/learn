### SpringBoot任务机制

#### 异步任务机制

##### 1. 原理讲解

spring异步任务：@EnableAysnc、@Aysnc

**@EnableAysnc注解**

```java
// 该属性用来支持用户自定义异步注解，
// 默认扫描spring的@Async和EJB3.1@code@javax.ejb.Asynchronous 
Class<? extends Annotation> annotation() default Annotation.class;

// 标明是否需要创建CGLIB子类代理，AdviceMode=PROXY时才适用。
// 注意设置为true时，其它spring管理的bean也会升级到CGLIB子类代理 
boolean proxyTargetClass() default false;

// 标明异步通知将会如何实现，默认PROXY，
// 如需支持同一个类中非异步方法调用另一个异步方法，需要设置为ASPECTJ
AdviceMode mode() default AdviceMode.PROXY;

// 标明异步注解bean处理器应该遵循的执行顺序，
// 默认最低的优先级（Integer.MAX_VALUE，值越小优先级越高）
int order() default Ordered.LOWEST_PRECEDENCE;
```




 **@EnableAysnc注解导入核心组件@Import(AsyncConfigurationSelector.class)**

 ```java
// 根据mode类型注入组件：AspectJAsyncConfiguration(ASPECTJ)，ProxyAsyncConfiguration(JDK)
ProxyAsyncConfiguration(PROXY-JDK)
@Configuration
public class ProxyAsyncConfiguration extends AbstractAsyncConfiguration {
  ...
  // 设置后置处理器
  AsyncAnnotationBeanPostProcessor bpp = new AsyncAnnotationBeanPostProcessor(); 
  // 把配置中的executor和exceptionHandler设置给后置处理器
  bpp.configure(this.executor, this.exceptionHandler);
}


@Configuration
public abstract class AbstractAsyncConfiguration implements ImportAware {
   @Nullable
   protected AnnotationAttributes enableAsync;  //注解上面属性linkedHashmap
   protected Executor executor; // java5 executor
   protected AsyncUncaughtExceptionHandler exceptionHandler; //异常处理器

    public AbstractAsyncConfiguration() {
    }

    // 可以通过配置AsyncConfigurer对应的bean配置executor，exceptionHandler
    @Autowired(required = false)
    void setConfigurers(Collection<AsyncConfigurer> configurers) {
        if (!CollectionUtils.isEmpty(configurers)) {
            if (configurers.size() > 1) {
                throw new IllegalStateException(
                    "Only one AsyncConfigurer may exist");
            } else {
                AsyncConfigurer configurer = (AsyncConfigurer)configurers
                    .iterator().next();
                this.executor = configurer::getAsyncExecutor;
                this.exceptionHandler = configurer::
                getAsyncUncaughtExceptionHandler;
            }
        }
    }
   
 // AsyncAnnotationBeanPostProcessor后置处理器
 ```



**@Aysnc注解**：被该注解注解的bean会被AsyncAnnotationBeanPostProcessor后置处理器增强

生成代理类AopProxy（postProcessAfterInitialization()）会被AsyncAnnotionAdvisor增强

之后的逻辑调用逻辑AnnotationAsyncExecutionInterceptor拦截。（AsyncExecutionInterceptor.invoke）

```java
@Override
@Nullable
public Object invoke(final MethodInvocation invocation) throws Throwable {
	Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);
	Method specificMethod = ClassUtils.getMostSpecificMethod(invocation.getMethod(), targetClass);
	final Method userDeclaredMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
    // 获取异步的AsyncTaskExecutor
	AsyncTaskExecutor executor = determineAsyncExecutor(userDeclaredMethod);
	if (executor == null) {
		throw new IllegalStateException(
				"No executor specified and no default executor set on AsyncExecutionInterceptor either");
	}

	Callable<Object> task = () -> {
		try {
			Object result = invocation.proceed();
			if (result instanceof Future) {
				return ((Future<?>) result).get();
			}
		}
		catch (ExecutionException ex) {
			handleError(ex.getCause(), userDeclaredMethod, invocation.getArguments());
		}
		catch (Throwable ex) {
			handleError(ex, userDeclaredMethod, invocation.getArguments());
		}
		return null;
	};

	return doSubmit(task, executor, invocation.getMethod().getReturnType());
}


/**
 * 1. 使用AsyncExecutionInterceptor中的异步线程
 * 2. 如果1不存在，那么使用@Async中指定TaskExecutor对应的bean。
 * 3. 如果上面都不存在，使用spring容器中的TaskExecutor，如果容器中也没有使用
 *    默认的SimpleAsyncTaskExecutor。
*/
protected AsyncTaskExecutor determineAsyncExecutor(Method method) {
	AsyncTaskExecutor executor = this.executors.get(method);
	if (executor == null) {
		Executor targetExecutor;
		String qualifier = getExecutorQualifier(method);
		if (StringUtils.hasLength(qualifier)) {
			targetExecutor = findQualifiedExecutor(this.beanFactory, qualifier);
		}
		else {
			targetExecutor = this.defaultExecutor.get();
		}
		if (targetExecutor == null) {
			return null;
		}
		executor = (targetExecutor instanceof AsyncListenableTaskExecutor ?
				(AsyncListenableTaskExecutor) targetExecutor : new TaskExecutorAdapter(targetExecutor));
		this.executors.put(method, executor);
	}
	return executor;
}

// 有返回值的异常会被重新抛出
// 如果没有设置AsyncUncaughtExceptionHandler会使用默认的实现AsyncUncaughtExceptionHandler
// 处理，只是简单的打印error日志
protected void handleError(Throwable ex, Method method, Object... params)
    throws Exception {
	if (Future.class.isAssignableFrom(method.getReturnType())) {
		ReflectionUtils.rethrowException(ex);
	}
	else {
		// Could not transmit the exception to the caller with default executor
		try {
			this.exceptionHandler.obtain()
                .handleUncaughtException(ex, method, params);
		}
		catch (Throwable ex2) {
			logger.warn("Exception handler for async method '" 
                        + method.toGenericString() +
					"' threw unexpected exception itself", ex2);
		}
	}
}
```



##### 2. 使用方式

**第一步：添加配置**

```java
@Slf4j
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    private static final int MAX_POOL_SIZE = 50;

    private static final int CORE_POOL_SIZE = 20;

    @Override
    @Bean("taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

        taskExecutor.setMaxPoolSize(MAX_POOL_SIZE);
        taskExecutor.setCorePoolSize(CORE_POOL_SIZE);
        taskExecutor.setQueueCapacity(CORE_POOL_SIZE * 10);
        taskExecutor.setThreadNamePrefix("coin-task-ex");
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setAwaitTerminationSeconds(60 * 10);
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return taskExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> log.error("invoke async method occurs error. method: {}, params: {}",
            method.getName(), JSON.toJSONString(params), ex);
    }

}


```



**第二步：标识异步方法和调用**

```java
// 无返回值的方法直接加上注解即可。 
@Async
public void method1() {
  ...
}

// 有返回值的方法需要修改返回值。
@Async
public Future<Object> method2() {
  ...
  return new AsyncResult<>(Object);
}    

Future<Boolean> test = asyncTaskService.test();
try {
    System.out.println(test.get());
} catch (InterruptedException| ExecutionException e) {
    //
}

//直接注入taskExecutor调用execute
public void method3(){
    taskExecutor.execute(()->{
      ...
	});
}

```



##### 3. 使用建议

* @Async可以指定方法执行的Executor，用法：@Async("MyTaskExecutor")。推荐指定Executor，这样可以避免因为Executor配置没有生效而Spring使用默认的Executor的问题。
* 实现接口AsyncConfigurer的时候，方法getAsyncExecutor()必须要使用@Bean，并指定Bean的name。

* 由于其本质上还是基于代理实现的，所以如果一个类中有A、B两个异步方法，而A中存在对B的调用，那么调用A方法的时候，B方法不会去异步执行的。

* 在异步方法上标注@Transactional是无效的。

* future.get()的时候，最好使用get(long timeout, TimeUnit unit)方法，避免长时间阻塞。

* ListenableFuture和CompletableFuture也是推荐使用的，他们相比Future，提供了对异步调用的各个阶段或过程进行介入的能力。



#### 定时任务

##### 1. 原理讲解

定时任务：@EnableScheduling、@Scheduled

**@EnableScheduling注解**：会创建ScheduledAnnotationBeanPostProcessor后置处理器



```java
private void finishRegistration() {
	if (this.scheduler != null) {
		this.registrar.setScheduler(this.scheduler);
	}

if (this.beanFactory instanceof ListableBeanFactory) {
    //1.查找类型为SchedulingConfigurer的bean，，为this.registrar赋值,初始化taskScheduler
	Map<String, SchedulingConfigurer> beans =
			((ListableBeanFactory) this.beanFactory)
        .getBeansOfType(SchedulingConfigurer.class);
	List<SchedulingConfigurer> configurers = new ArrayList<>(beans.values());
	AnnotationAwareOrderComparator.sort(configurers);
	for (SchedulingConfigurer configurer : configurers) {
		configurer.configureTasks(this.registrar);
	}
}

if (this.registrar.hasTasks() && this.registrar.getScheduler() == null) {
	Assert.state(this.beanFactory != null, "BeanFactory must be set to find scheduler by type");
	try {
		//2. 查找 TaskScheduler类型bean
		this.registrar.setTaskScheduler(resolveSchedulerBean(beanFactory, TaskScheduler.class, false));
	}
	catch (NoUniqueBeanDefinitionException ex) {
		logger.debug("Could not find unique TaskScheduler bean", ex);
		try {
		    //3. 存在多个TaskScheduler类型，查找bean名字为taskScheduler的bean
			this.registrar.setTaskScheduler(resolveSchedulerBean(beanFactory, TaskScheduler.class, true));
		}
		catch (NoSuchBeanDefinitionException ex2) {
		    //存在多个TaskScheduler类型，不存在bean名字为taskScheduler的bean 
			if (logger.isInfoEnabled()) {
				logger.info("More than one TaskScheduler bean exists within the context, and " +
						"none is named 'taskScheduler'. Mark one of them as primary or name it 'taskScheduler' " +
						"(possibly as an alias); or implement the SchedulingConfigurer interface and call " +
						"ScheduledTaskRegistrar#setScheduler explicitly within the configureTasks() callback: " +
						ex.getBeanNamesFound());
			}
		}
	}
	catch (NoSuchBeanDefinitionException ex) {
		logger.debug("Could not find default TaskScheduler bean", ex);
		//4. 查找ScheduledExecutorService类型Bean
		try {
			this.registrar.setScheduler(resolveSchedulerBean(beanFactory, ScheduledExecutorService.class, false));
		}
		catch (NoUniqueBeanDefinitionException ex2) {
			logger.debug("Could not find unique ScheduledExecutorService bean", ex2);
			try {
			    //5. 存在多个ScheduledExecutorService类型，查找bean名字为taskScheduler的bean
				this.registrar.setScheduler(resolveSchedulerBean(beanFactory, ScheduledExecutorService.class, true));
			}
			catch (NoSuchBeanDefinitionException ex3) {
				if (logger.isInfoEnabled()) {
					logger.info("More than one ScheduledExecutorService bean exists within the context, and " +
							"none is named 'taskScheduler'. Mark one of them as primary or name it 'taskScheduler' " +
							"(possibly as an alias); or implement the SchedulingConfigurer interface and call " +
							"ScheduledTaskRegistrar#setScheduler explicitly within the configureTasks() callback: " +
							ex2.getBeanNamesFound());
				}
			}
		}
		catch (NoSuchBeanDefinitionException ex2) {
		    //存在多个ScheduledExecutorService类型，不存在bean名字为taskScheduler的bean
			logger.debug("Could not find default ScheduledExecutorService bean", ex2);
			// Giving up -> falling back to default scheduler within the registrar...
			logger.info("No TaskScheduler/ScheduledExecutorService bean found for scheduled processing");
		}
	}
}

this.registrar.afterPropertiesSet();


}
// 6.taskScheduler不存在，设置Executors.newSingleThreadScheduledExecutor();
// 这个一个单个线程的线程池
if (this.taskScheduler == null) {
	this.localExecutor = Executors.newSingleThreadScheduledExecutor();
	this.taskScheduler = new ConcurrentTaskScheduler(this.localExecutor);
}
```


##### 2. cron语法说明

**cron格式：**0 * * * * MON-FRI

字段	允许值	允许的特殊字符
秒	        0-59	, - * /
分	        0-59	, - * /
小时	0-23	, - * /
日期	1-31	, - * ? / L W C
月份	1-12	, - * /
星期	0-7或SUN-SAT 0,7是SUN	, - * ? / L C #



特殊字符	代表含义
,	        枚举
\-	        区间
\*	        任意
/	        步长
?	        日/星期冲突匹配
L	        最后
W	       工作日
C	        和calendar联系后计算过的值
\#	        星期，4#2，第2个星期四



举例：
0 0-5 14 * * ?  	每天14点到14:05期间的每1分钟触发一次事件
0 10,44 14 ? 3 WED	每年3月的星期三的14:10和14:44触发一次事件



##### 3. 使用说明
第一步：启动类@EnableScheduling
第二步：设置taskSchedule配置
```java
@Configuration
public class SchedulingConfig implements SchedulingConfigurer {
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);
        taskScheduler.initialize();
        return taskScheduler;
    }
}
```
第三步：service方法中定义(会生成对应的代理bean) ==>  @Scheduled(cron="0 * * * * ?")  