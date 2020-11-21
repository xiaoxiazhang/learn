### Spring工具类使用

#### 1. StopWatch

```java
// 1.spring stopWatch数据结构 
// 每个StopWatch 对应的ID，任务集合
private final String id;
private boolean keepTaskList = true;
private final List<TaskInfo> taskList = new LinkedList<>();

// 当前任务的开始时间和任务名称
private long startTimeNanos;
private String currentTaskName;
private TaskInfo lastTaskInfo;

// 总任务数和总时间
private int taskCount;
private long totalTimeNanos;

// 2.API使用 

// 创建-启动-结束
public StopWatch(String id)
public void start(String taskName) throws IllegalStateException
public void stop() throws IllegalStateException
  
// 获取最后一次时间任务时间毫秒数，和总任务的时间毫秒数
public long getLastTaskTimeMillis() 
public long getTotalTimeMillis()  
  
// 格式化输出  
public String prettyPrint()
  
  
// 3.使用姿势
@Test
public void testStopWatch() throws Exception {
    StopWatch stopWatch = new StopWatch("计数器01");

    stopWatch.start("job-01");
    TimeUnit.SECONDS.sleep(2);
    stopWatch.stop();
    System.out.println("job-01 cost: " + stopWatch.getLastTaskTimeMillis() + "ms" );

    stopWatch.start("job-02");
    TimeUnit.SECONDS.sleep(3);
    stopWatch.stop();
    System.out.println("job-02 cost: " + stopWatch.getLastTaskTimeMillis() + "ms" );

    System.out.println("total: " + stopWatch.getTotalTimeMillis() + "ms");

    System.out.println(stopWatch.prettyPrint());
}

```





#### 2. Assert

```java
// Assert 断言工具类

//不满足条件，throw new IllegalStateException(message)
public static void state(boolean expression, String message)  //断言转态

//不满足条件throw new IllegalArgumentException(message);
public static void isTrue(boolean expression, String message) //断言为true
public static void isNull(Object object, String message)      //断言为空
public static void notNull(Object object, String message) //断言不为空
public static void hasLength(String text, String message) //断言字符串length>0
public static void notEmpty(Object[] array, String message) //断言数组length>0
public static void noNullElements(Object[] array, String message)  //断言数组元素都不为Null
public static void notEmpty(Collection<?> collection, String message) //断言集合size()>0
public static void notEmpty(Map<?, ?> map, String message)  //断言map size()>0
public static void isInstanceOf(Class<?> type, Object obj, String message) //断言对象类型
  
```



####  3. StringUtils

```java
// 字符非空和包含字符串判断
public static boolean isEmpty(@Nullable Object str)
public static boolean hasText(@Nullable String str)
  
// 首字母大写 
public static String capitalize(String str) 

// 获取文件名
public static String getFilename(@Nullable String path)
// 获取文件后缀
public static String getFilenameExtension(@Nullable String path)
// 去除文件后缀
public static String stripFilenameExtension(String path)
  
```



#### 4. CollectionUtils

```java
// 判空处理
public static boolean isEmpty(Collection<?> collection)
public static boolean isEmpty(Map<?, ?> map)
  
// 数组元素加到集合中
public static <E> void mergeArrayIntoCollection(Object array, Collection<E> collection) 

```



#### 5. StreamUtils

```java
// 输入流 ==> 字节数组
public static byte[] copyToByteArray(@Nullable InputStream in) 

// 输入流 + 字符编码 ==> 字符串
public static String copyToString(@Nullable InputStream in, Charset charset) 
 
// 流拷贝
public static int copy(InputStream in, OutputStream out)
	
```





#### 6. FileCopyUtils

```java
// 拷贝方法
public static int copy(File in, File out)  
public static void copy(byte[] in, File out)  
public static int copy(InputStream in, OutputStream out) 
public static int copy(Reader in, Writer out) throws IOException  
public static void copy(String in, Writer out) throws IOException 

// 流转换成字节数组和字符串
public static byte[] copyToByteArray(InputStream in) throws IOException 
public static byte[] copyToByteArray(File in) throws IOException 
public static String copyToString(@Nullable Reader in)
  
```





#### 7. ResourceUtils

```java 
-- Resource接口
  核心接口：public InputStream getInputStream() 
  -- FileSystemResource类     ==> 以文件系统绝对路径的方式进行访问； 
  -- ClassPathResource类      ==> 以类路径的方式进行访问；
  -- ServletContextResource类 ==> 以相对于 Web 应用根目录的方式进行访问。
  
// ClassPathResource原理说明
this.clazz.getResourceAsStream(this.path);
this.classLoader.getResourceAsStream(this.path);
ClassLoader.getSystemResourceAsStream(this.path);
// 使用姿势
ClassPathResource resource = new ClassPathResource("test.properties");


-- ResourceLoader接口
   核心接口：Resource getResource(String location);
   -- DefaultResourceLoader类  
     -- AbstractApplicationContext类
       原理：底层通过创建PathMatchingResourcePatternResolver实现getResources
   -- ResourcePatternResolver接口
     核心接口：public Resource[] getResources(String locationPattern)
     -- PathMatchingResourcePatternResolver类 ==> ant默认匹配查找资源文件
 
// 使用姿势
DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
Resource resource = resourceLoader.getResource("classpath:test.properties");

PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
Resource[] resources = resolver.getResources("classpath*:*.properties");


// 从指定的地址加载文件资源: ResourceUtils#getFile
public static File getFile(String resourceLocation) throws FileNotFoundException 
  
```





#### 8. Base64Utils

```java
// base64 加解密字节数组
public static byte[] encode(byte[] src)
public static byte[] decode(byte[] src)
  
// 字节数组 base64加密成字符串
public static String encodeToString(byte[] src)
// 字符串base64 解密成字节数组
public static byte[] decodeFromString(String src) 

```





#### 9. DigestUtils

```java
// 通过字节数组或者输入流生成md5摘要
public static String md5DigestAsHex(byte[] bytes)
public static String md5DigestAsHex(InputStream inputStream) 

```





#### 10. SerializationUtils

```java
// 序列化对象成字节数组
public static byte[] serialize(@Nullable Object object)
  
// 反序列化字节数组成对象
public static Object deserialize(@Nullable byte[] bytes)

```





#### 11. BeanUtils

**Java自省机制**：Java中的反射机制是通过名称得到类的方法和属性，对于一切Java类都是适用的。而JavaBean是一种特殊的Java类，遵守JavaBean的规范，即所有的成员都是私有成员，且每个成员都有公开的读取和设定的方法（getter和setter），且这些方法都遵守命名的规范。就是因为JavaBean有这些的特性，sun推出了一种专门对JavaBean成员进行访问的技术，方便对其的访问，就是内省技术。

```java
-- Introspector类：获取某个对象的BeanInfo信息
  // 获取BeanInfo
  public static BeanInfo getBeanInfo(Class<?> beanClass)

  -- BeanInfo接口：定义获取javabean 属性、方法等信息
  BeanDescriptor getBeanDescriptor();            // javabean信息： 名称，类
  PropertyDescriptor[] getPropertyDescriptors(); // javabean属性数组： 属性名，属性对应的读写方法
  MethodDescriptor[] getMethodDescriptors();     // javabean方法数组： 方法名，参数等

-- PropertyDescriptor类：属性的描述
  public PropertyDescriptor(String propertyName, Class<?> beanClass) // 构造器
  public synchronized Method getReadMethod()                         // 获取get属性方法
  public synchronized Method getWriteMethod()                        // 获取set属性方法


```



**BeanUtils源码分析**：通过自省机制，遍历目标属性描述PropertyDescriptor[]，通过source中查找匹配的PropertyDescriptor，然后read 目标属性值set到源对象属性中。

```java
// BeanUtils#copyProperties：属性名和属性类型相同的属性会拷贝，属性名相同和属性类型不同会报异常。
public static void copyProperties(Object source, Object target) throws BeansException 


// 核心代码分析【缓存对象属性描述信息PropertyDescriptor[]】
public final class CachedIntrospectionResults {
 
   // 类自省结果信息缓存
  static final ConcurrentMap<Class<?>, CachedIntrospectionResults> softClassCache =
			new ConcurrentReferenceHashMap<>(64);
  
  // 获取类自省缓存结果信息并加入全局缓存中
  static CachedIntrospectionResults forClass(Class<?> beanClass) throws BeansException {
    CachedIntrospectionResults results = softClassCache.get(beanClass);
		if (results != null) {
			return results;
		
		results = new CachedIntrospectionResults(beanClass);
    CachedIntrospectionResults existing = softClassCache.putIfAbsent(beanClass, results);
		return (existing != null ? existing : results);
  }
    
  // 构造器，创建类自省缓存结果。
  private CachedIntrospectionResults(Class<?> beanClass) throws BeansException {
     this.propertyDescriptorCache = new LinkedHashMap<>();

			// This call is slow so we do it once.
			PropertyDescriptor[] pds = this.beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor pd : pds) {
				pd = buildGenericTypeAwarePropertyDescriptor(beanClass, pd);
				this.propertyDescriptorCache.put(pd.getName(), pd);
			}
     
      ...   
  }
}  
```



#### 12. Spring 泛型工具

```java
java反射和泛型类结构
-- Member接口 ==> 反射成员信息
  核心方法：getDeclaringClass, getName ,getModifiers, isSynthetic
  -- Filed, Executable, Method, Constructor

-- AnnotatedElement接口 ==> 注解相关信息
  核心方法：isAnnotationPresent, getAnnotation, getAnnotations, getDeclaredAnnotation
  -- GenericDeclaration接口
    核心方法：getTypeParameters
    -- Class类 ==> 类信息

  -- AccessibleObject  ==> 访问信息
    核心方法：isAccessible, setAccessible
    -- Filed类  ==> 字段信息
    -- Executable抽象类
      -- Method类 ==> 方法信息
      -- Constructor类  ==> 构造器信息


-- Type接口 【Class实现该接口】
  -- GenericArrayType  ==> 泛型数组类型
    核心方法: 
  -- ParameterizedType ==> 泛型参数类型: AbstractList<E>
    核心方法: getRawType(); getActualTypeArguments()
  -- TypeVariable ==> 泛型类型参数 E
    核心方法: getName, getBounds
  -- WildcardType ==>  泛型通配类型?
    核心方法: 

MethodParameter类
  -- parameter, parameterIndex, executable


  
// ResolvableType工具类
  
// 1. 工厂方法: for*
public static ResolvableType forField(Field field) 
public static ResolvableType forClass(@Nullable Class<?> clazz)
    
// 2. 转换方法 as*
public ResolvableType asMap()

// 3. 处理方法: resolve*方法 
public Class<?> resolve()
    
// 使用案例
ResolvableType resolvableType = ResolvableType.forField(ClassTest.class.getField("myMap"));
System.out.println(resolvableType.getType());
System.out.println(resolvableType.resolve());
System.out.println(resolvableType.asMap().getType());

ResolvableType[] generics = resolvableType.getGenerics();
for (ResolvableType generic : generics) {
    System.out.println(generic.getType());
}    

```

