### SpringBoot整合RedisTemplate

#### (一). Redis通信协议

Redis-cli与server端使用一种专门为redis设计的协议RESP(Redis Serialization Protocol)交互，Resp本身没有指定TCP，但redis上下文只使用TCP连接。RESP规定：

- 用 \r\n 做间隔。
- 简单的字符串，以`+`开头 例如：`+OK\r\n ` ==> OK
- 长字符串，以`$`开头，接着跟上字符串长度的数字。例如：`$6\r\nfoobar\r\n ` ==> foobar
- 整数数值以`:`开头。例如：` :100\r\n` ==> 100
- 数组以`*`开头，接上数组元素个数。例如：`*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n` ==> 数组 foo bar
- 错误消息以`-`开头 。 例如：`-ERR unknown command 'sethx'`



客户端与服务端的交互步骤如下：

```
客户端-> 服务器端: 输入命令 ==> 将命令编码成字节流 ==> 通过TCP发送到服务端
服务器端-> 客户端: 服务端解析字节流 ==> 服务端执行命令 ==>将结果编码成字节流 ==> 通过TCP发送给客户端 ==>      
                 客户端解析字节流，得到执行结果
```



比如执行set hello world,根据resp协议，需要客户端解析为下面格式字节流发送给服务端

```
*3\r\n
    $3\r\nset\r\n
    $5\r\nhello\r\n
    $5\r\nworld\r\n
```





####  (二). Jedis客户端

第一部分：Jedis对象的创建：Jedis jedis = new Jedis(); 主要是创建连接Redis服务器的客户端，在Jedis基类BinaryJedis中主要有Connection对象，创建jedis对象的时候尚未连接到redis服务器，在Connection类中,主要设置了链接Redis所使用socket的参数以及操作socket所使用的工具。

```java
public class Connection implements Closeable {

  private static final byte[][] EMPTY_ARGS = new byte[0][];

  private String host = Protocol.DEFAULT_HOST;
  private int port = Protocol.DEFAULT_PORT;
  private Socket socket;
  private RedisOutputStream outputStream;
  private RedisInputStream inputStream;
  private int pipelinedCommands = 0;
  private int connectionTimeout = Protocol.DEFAULT_TIMEOUT;
  private int soTimeout = Protocol.DEFAULT_TIMEOUT;
  private boolean broken = false;
  private boolean ssl;
  private SSLSocketFactory sslSocketFactory;
  private SSLParameters sslParameters;
  private HostnameVerifier hostnameVerifier;

  public Connection() {
  }
  ...
}

```



第二部分：在调用 执行命令`String code=jedis.set("s", "s")`的时候,才是真正创建socket连接的过程。`Client(BinaryClient).set(byte[], byte[]) `  方法参数就是把由String 字符串转换成字节数值，并调用Connection的sendCommand方法来发送Redis命令。

```java
// Jedis#set
public String set(final String key, final String value) {
    checkIsInMultiOrPipeline();
    client.set(key, value);
    return client.getStatusCodeReply();
}

// Client#set
public void set(final String key, final String value) {
    set(SafeEncoder.encode(key), SafeEncoder.encode(value));
}

// BinaryClient#set
public void set(final byte[] key, final byte[] value) {
    sendCommand(Command.SET, key, value);
}

// Connection#sendCommand
protected Connection sendCommand(final Command cmd, final byte[]... args) {
    try {
      connect(); //每次发送Redis命令都会调用Connect()方法来连接Redis远程服务器
      Protocol.sendCommand(outputStream, cmd, args);
      pipelinedCommands++;
      return this;
    } catch (JedisConnectionException ex) {
      try {
        String errorMessage = Protocol.readErrorLineIfPossible(inputStream);
        if (errorMessage != null && errorMessage.length() > 0) {
          ex = new JedisConnectionException(errorMessage, ex.getCause());
        }
      } catch (Exception e) {
        
      }
      broken = true;
      throw ex;
    }
}
```



#### (三). Redis自动配置









#### (四). RedisTemplate源码分析

```java
public <T> T execute(RedisCallback<T> action, boolean exposeConnection, boolean pipeline) {

	Assert.isTrue(initialized, "template not initialized; call afterPropertiesSet() before using it");
	Assert.notNull(action, "Callback object must not be null");
	
	//获取redis连接工厂，才创建redisTemplate注入的
	RedisConnectionFactory factory = getRequiredConnectionFactory();
	RedisConnection conn = null;
	try {
	
		if (enableTransactionSupport) {
			// only bind resources in case of potential transaction synchronization
			conn = RedisConnectionUtils.bindConnection(factory, enableTransactionSupport);
		} else {
		    //通过工厂获取连接。Jedis jedis = pool.getResource(); 通过jedis,pool封装创建JedisConnection
			conn = RedisConnectionUtils.getConnection(factory);
		}
	
		boolean existingConnection = TransactionSynchronizationManager.hasResource(factory);
	
		RedisConnection connToUse = preProcessConnection(conn, existingConnection);
	
		boolean pipelineStatus = connToUse.isPipelined();
		if (pipeline && !pipelineStatus) {
			connToUse.openPipeline();
		}
	
		RedisConnection connToExpose = (exposeConnection ? connToUse : createRedisConnectionProxy(connToUse));
		//执行回调函数，真正执行命令的地方，返回result
		//执行过程过，第一步先进行序列化。(keySerializer,valueSerializer创建redisTemplate可以注入默认jdk序列化方式)
		//     byte[] rawKey = rawKey(key); ==> byte[] keySerializer().serialize(key);	
		//     byte[] rawValue = rawValue(value); ==> return valueSerializer().serialize(value);	 
		//执行过程本质。创建对应的RedisXXXCommands然后通过对应的客户端执行命令
		//return Converters.stringToBoolean(connection.getJedis().setex(key, (int) seconds, value));
	
		T result = action.doInRedis(connToExpose);
	
		// close pipeline
		if (pipeline && !pipelineStatus) {
			connToUse.closePipeline();
		}
	
		// TODO: any other connection processing?
		return postProcessResult(result, connToUse, existingConnection);
	} finally {
	
	    //释放连接,归还连接池到连接池中
		RedisConnectionUtils.releaseConnection(conn, factory);
	}
}
```



#### (五). RedisTemplate配置和使用

##### RedisTemplate配置说明

```xml
<!-- mavan依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
    <exclusions>
        <exclusion>
            <groupId>io.lettuce</groupId>
            <artifactId>lettuce-core</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>2.9.3</version>
</dependency>
```


```yaml
# redis数据源配置
spring:
  redis:
    host: ${redis.platformization_trade.host}
    port: ${redis.platformization_trade.port}
    password: ${redis.platformization_trade.password}
    database: 0
    timeout: 6000ms
    jedis:
      pool:
        max-active: 100
        min-idle: 40
        max-wait: 5000ms
```

```java
// 自定义序列化方式的RedisTemplate
@Configuration
public class RedisTemplateConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory, StringSerializer stringSerializer) {
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashKeySerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
```



##### RedisTemplate API

```java
/**
 * 操作key (redisTemplate)
 */

//ant匹配key ==> keys pattern
public Set<K> keys(K pattern)

//判断key是否存在 ==> exists key
public Boolean hasKey(K key)

//设置key过期时间 ==> expire key seconds
public Boolean expire(K key, final long timeout, final TimeUnit unit)

//删除key,返回删除个数 ==> del key [key ...]
public Long delete(Collection<K> keys)

//获取key，具体的类型 ==> type key
public DataType type(K key)

    
    
/**
 * 操作String ==> redisTemplate.opsForValue()
 */
    
//获取字符串长度 ==> strlen key
public Long size(K key)

//追加字符串,返回字符串长度 ==> append key value
public Integer append(K key, String value)

//设置key,value。 ==> set key value EX seconds
public void set(K key, V value)
public void set(K key, V value, long timeout, TimeUnit unit)
  
  
//如果key不存在，才插入 ==> setnx key value
public Boolean setIfAbsent(K key, V value)
public Boolean setIfAbsent(K key, V value, long timeout, TimeUnit unit);

//自增自减key,key不存在，默认先设置0，返回处理后的结果 ==> incr/decr key 或者 incrby/decrby key increment/decriment
public Long increment(K key, long delta) 

//批量设置key,value ==> mset key value [key value...]
public void multiSet(Map<? extends K, ? extends V> m)
//批量获取keys,返回集合，中间可能为null ==> mget key [key ...]
List<V> multiGet(Collection<K> keys)

   
    
/** 
 * 操作list ==> redisTemplate.opsForList() 
 */
    
//从左.右插入key,返回插入个数 ==> lpush/rpush key value [value ...]
public Long leftPush(K key, V value)
public Long rightPush(K key, V value)

//从左.右弹出首个value,返回弹出的值 ==> lpop/rpop key 
public V leftPop(K key) 
public V rightPop(K key)
//截取key的值，[start,end]  ==> ltrim key start end
public void trim(K key, long start, long end)
//设置指定index的value值。超过范围为报错。  ==> lset key index value
public void set(K key, long index, V value)
//删除指定值指定个数，返回删除个数。 ==> lrem key count value
public Long remove(K key, long count, Object value)

//获取指定索引范围的value值，[start,end] ==> lrange key start end
public List<V> range(K key, long start, long end)
//获取指定位置的value值 ==> lindex key index
public V index(K key, long index)
//获取指定key,列表长度 ==> llen key
public Long size(K key)

    

/**
 * 无序不重复
 * 操作set ==> redisTemplate.opsForSet()
 *
 */

//添加元素 ==> zadd key value [value ...}
Long add(K key, V... values);
//弹出栈首元素个数count ==> spop key [count]
public List<V> pop(K key, long count) 
//删除key中的value  ==> srem key value [value ...]
public Long remove(K key, Object... values)

//判断是否包含元素 ==> sismember key value 
Boolean isMember(K key, Object o);
//获取key所有的元素 ==> smembers key
public Set<V> members(K key);
//获取key中集合个数 ==> scard key
public Long size(K key)

//取两个结合的差集，A-B  ==> sdiff key [key...]
public Set<V> difference(K key, K otherKey)
//取两个结合的交集，A∩B  ==> sinter key [key...]
public Set<V> intersect(K key, Collection<K> otherKeys)
//取两个结合的交集，A∪B  ==> sinter key [key...]
public Set<V> union(K key, Collection<K> otherKeys);



/**
 * 有序不重复
 * 操作zset ==> redisTemplate.opsForZset()
 */

// 添加元素
Boolean add(K key, V value, double score);
Long add(K key, Set<TypedTuple<V>> tuples);

// 获取集合元素个数
Long zCard(K key);

// 获取集合元素分值
Double score(K key, Object o)
  
// 遍历集合
Set<V> range(K key, long start, long end)
Set<V> reverseRange(K key, long start, long end)
Set<V> rangeByScore(K key, double min, double max)
Set<V> rangeByScore(K key, double min, double max, long offset, long count)
  
Set<V> reverseRangeByScore(K key, double min, double max)
Set<V> reverseRangeByScore(K key, double min, double max, long offset, long count)  
  
// 删除集合元素
Long remove(K key, Object... values);
Long removeRange(K key, long start, long end);


/**
 * 操作hash ==> redisTemplate.opsForHash()
 */

//hash添加一个键值对 ==> hset key field value
public void put(K key, HK hashKey, HV value)
//hash添加一个键值对中如果不存在field，则添加对应键值对 ==> hsetnx key field value
public Boolean putIfAbsent(K key, HK hashKey, HV value)
//hash添加一个键值对中添加多个键值对。 ==> hmset key field value [field value ...]
public void putAll(K key, Map<? extends HK, ? extends HV> m)

//hash添加一个键值对中删除其中field ==> hdel key filed [filed ...]
public Long delete(K key, Object... hashKeys)

//hash中是否包含某个field  ==> hexists key filed
public Boolean hasKey(K key, Object hashKey)
//hash中filed个数 ==> hlen key 
public Long size(K key)
//hash中某个filed的值 ==> hget key filed
public HV get(K key, Object hashKey)
//hash中多个filed的值 ==> hmget key filed [filed ...]
public List<HV> multiGet(K key, Collection<HK> fields)

//hash中所有的filed名
public Set<HK> keys(K key)
//hash中所有的filed值
public List<HV> values(K key)


    
/**
 * redis事务 ==> redisTemplate
 * 事务原理: multi命令,会将之后的命令存储在一个队列中，如果命令有误则则直接报错；执行exec则会将
 *    队列中命令一个一个执行，直到命令都执行完或者报错。
 * watch指令的作用：事务执行前监听对应key, 在执行exec的时候，如果监听的key发生变化则报错。
 */

// 监听key ==> watch key [key...]
// 如果key后面发生变化，会放弃事务执行  
public void watch(Collection<K> keys)
// 开启事物 ==> multi
public void multi()
... //执行命令
// 取消执行 ==> discard
public void discard()
// 执行队列命令（如果watch的变量被修改则队列命令不会被执行）
public List<Object> exec()
// 取消监听 ==> unwatch
public void unwatch() 
    

/**
 * redis事件的发布和订阅
 */
public void convertAndSend(String channel, Object message);


    
/**
 * lua脚本 ==> redisTemplate
 */

//执行lua脚本 ==> EVAL script numkeys key [key ...] arg [arg ...]
public <T> T execute(RedisScript<T> script, List<K> keys, Object... args)

举例：
DefaultRedisScript<Long> script = new DefaultRedisScript<>();
script.setScriptSource(new StaticScriptSource("redis.call('set',KEYS[1],'BAR')"));
Long result = redisTemplate.execute(script, Lists.newArrayList("hello"));

```



##### Redis Lua脚本的使用

Redis内置了对LUA脚本的支持，并且在计算过程中保证了脚本中执行的原子性。
**命令**：EVAL script numkeys key [key ...] arg [arg ...]　

**参数说明:** 

- 键名参数可以在 Lua中通过全局变量KEYS数组，用1为基址的形式访问( KEYS[1] ， KEYS[2] ，以此类推)。

- 参数 arg [arg ...] ，可以在Lua中通过全局变量ARGV数组访问，访问的形式和KEYS变量类似( ARGV[1] 、 ARGV[2] ，诸如此类)。

  > 注意：所有键都应该由 KEYS 数组来传递。例如：eval "return redis.call('set',KEYS[1],'bar')" 1 foo

  

**Lua 脚本执行redis命令函数**：

- redis.call() ：执行过程中发生错误时，脚本会停止执行，并返回一个脚本错误，错误的输出信息会说明错误造成的原因。`eval "return redis.call('set',KEYS[1],'bar')" 1 foo`
- redis.pcall() ：出错时并不引发(raise)错误，而是返回一个带 err 域的 Lua 表(table)，用于表示错误







#### (六). Redisson分布式锁使用

Redis官方文档：https://github.com/redisson/redisson/wiki

##### maven依赖

```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson</artifactId>
    <version>${redisson.version}</version>
</dependency>
```



##### Redisson配置

```java


```





##### Redisson使用







#### (七). 缓存使用原则





##### 缓存读写策略

CacheA

##### 

##### 缓存穿透

缓存穿透说：是大量请求的 key 根本不存在于缓存中，导致请求直接到了数据库上，根本没有经过缓存这一层。



##### 缓存雪崩

缓存雪崩：缓存在同一时间大面积失效，后面的请求都直接落到了数据库上，造成数据库短时间内承受大量请求。

