### MyBatis-Plus多数据源配置

#### 1. 配置多数据源

```java
@Configuration
public class DataSourceConfig {

    @Resource
    private DruidDataSourceProperties druidDataSourceProperties;

    private static final String MYSQL_SUFFIX = "?useUnicode=true&characterEncoding=UTF8&zeroDateTimeBehavior=convertToNull&autoReconnect=true&allowMultiQueries=true&tinyInt1isBit=false";


    @Bean(name="writeDataSource",initMethod = "init")
    public DruidDataSource writeDataSource() {
        try (SecureDruidDataSource datasource = new SecureDruidDataSource()) {
            datasource.setUrl(druidDataSourceProperties.getWriteDbUrl() + MYSQL_SUFFIX);
            datasource.setUsername(druidDataSourceProperties.getWriteDbUsername());
            datasource.setPassword(druidDataSourceProperties.getWriteDbPassword());
            datasource.setDriverClassName(druidDataSourceProperties.getDriverClassName());
            commonConfig(datasource);
            log.info("-------------------- write datasource init success ---------------------");
            return datasource;
        }
    }

    @Bean(name="readDataSource",initMethod = "init")
    public DruidDataSource readDataSource() {
        try (SecureDruidDataSource datasource = new SecureDruidDataSource()) {
            datasource.setUrl(druidDataSourceProperties.getReadDbUrl() + MYSQL_SUFFIX);
            datasource.setUsername(druidDataSourceProperties.getReadDbUsername());
            datasource.setPassword(druidDataSourceProperties.getReadDbPassword());
            datasource.setDriverClassName(druidDataSourceProperties.getDriverClassName());
            commonConfig(datasource);
            log.info("-------------------- read datasource init success ---------------------");
            return datasource;
        }
    }

    private void commonConfig(DruidDataSource datasource)  {
        //配置连接池的初始化大小，最大值，最小值
        datasource.setInitialSize(druidDataSourceProperties.getInitSize());
        datasource.setMaxActive(druidDataSourceProperties.getMaxActive());
        datasource.setMinIdle(druidDataSourceProperties.getMinIdle());
        //配置获取连接等待超时的时间
        datasource.setMaxWait(60000);
        //配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
        datasource.setTimeBetweenEvictionRunsMillis(60000);
        //配置一个连接在池中最小生存的时间，单位是毫秒
        datasource.setMinEvictableIdleTimeMillis(300000);

        //用来检测连接是否有效的sql，要求是一个查询语句，常用select 'x'。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会起作用。
        datasource.setValidationQuery("SELECT 'x'");
        //申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
        datasource.setTestOnBorrow(false);
        //建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
        datasource.setTestWhileIdle(true);
        //归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。
        datasource.setTestOnReturn(false);
        //是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大，比如说oracle。在mysql下建议关闭
        datasource.setPoolPreparedStatements(false);
        //要启用PSCache，必须配置大于0，当大于0时，poolPreparedStatements自动触发修改为true。在Druid中，不会存在Oracle下PSCache占用内存过多的问题，可以把这个数值配置大一些，比如说100
        datasource.setMaxPoolPreparedStatementPerConnectionSize(-1);
        //慢sql的记录
        try {
            datasource.setFilters("stat");
        } catch (SQLException e) {
            log.error("druid configuration initialization filter:",e);
        }
        StatFilter statFilter = new StatFilter();
        statFilter.setSlowSqlMillis(50);
        statFilter.setLogSlowSql(true);
        datasource.setProxyFilters(Lists.newArrayList(statFilter));
    }
}
```



```java
@Configuration
@EnableTransactionManagement
@MapperScan("com.ggj.center.coin.mapper")
public class MutiDataSourceConfig {


    @Autowired
    private DruidDataSource writeDataSource;

    @Autowired
    private DruidDataSource readDataSource;


    //配置动态数据源
    @Bean
    public DynamicDataSource dynamicDataSource(){
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        //设置目标数据源map
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(CoinConstants.COIN_WRITE,writeDataSource);
        targetDataSources.put(CoinConstants.COIN_SLAVE,readDataSource);
        dynamicDataSource.setTargetDataSources(targetDataSources);
        //设置默认的数据源（写库）
        dynamicDataSource.setDefaultTargetDataSource(writeDataSource);
        return dynamicDataSource;
    }


    @Bean(name = "transactionManager")
    public DataSourceTransactionManager transactionManagers() {
        return new DataSourceTransactionManager(dynamicDataSource());
    }

    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

    @Bean("sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        MybatisSqlSessionFactoryBean sqlSessionFactory = new MybatisSqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(dynamicDataSource());
        sqlSessionFactory.setVfs(SpringBootVFS.class);
        sqlSessionFactory.setTypeAliasesPackage("com.ggj.center.coin.domain");
        sqlSessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:/mapper/*/*Mapper.xml"));

        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(false);
        sqlSessionFactory.setConfiguration(configuration);

        sqlSessionFactory.setPlugins(new Interceptor[]{
                paginationInterceptor() //添加分页功能
        });
        sqlSessionFactory.setGlobalConfig(globalConfig());
        return sqlSessionFactory.getObject();
    }

    @Bean
    public GlobalConfig globalConfig() {
        GlobalConfig config = new GlobalConfig();
        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        dbConfig.setDbType(DbType.MYSQL);
        dbConfig.setIdType(IdType.AUTO);
        dbConfig.setTableUnderline(true);
        config.setDbConfig(dbConfig);
        return config;
    }

}
```



#### 2. 动态数据源路由

```java
public class DynamicDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();

    //设置数据源
    static void setDataSourceType(String dataSource) {
        contextHolder.set(dataSource);
    }

    //清除数据源
    static void clearDataSourceType() {
        contextHolder.remove();
    }

    //获取当前线程的数据源的key
    @Override
    protected Object determineCurrentLookupKey() {
        return contextHolder.get();
    }
}
```





#### 3. AOP切面实现动态数据源

```java
//注解在需要切换数据源的代码上
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TargetDataSource {
    String name();
}

//切面(TargetDataSource注解)
@Slf4j
@Aspect
@Order(-1)//保证在@Transactional之前执行
@Component
public class DynamicDataSourceAspect {

    //改变数据源
    @Before("@annotation(targetDataSource)")
    public void changeDataSource(JoinPoint joinPoint, TargetDataSource targetDataSource) {
        String datasourceName = targetDataSource.name();
        //注解为空或者不正确的时候，使用默认数据源
        if (StringUtils.isEmpty(datasourceName)) {
            log.debug("use defaut datasource");
        }else if (DataSourceEnum.getByName(datasourceName) == null) {
            String method = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
            log.error("method==>{},datesource==>{} is not exists,system will use defaut data source。" + method, datasourceName);
        } else {
            log.debug("use datasource：" + datasourceName);
            DynamicDataSource.setDataSourceType(datasourceName);
        }
    }

    @After("@annotation(targetDataSource)")
    public void clearDataSource(JoinPoint joinPoint, TargetDataSource targetDataSource) {
        log.debug("clear datasource " + targetDataSource.name() + " !");
        DynamicDataSource.clearDataSourceType();
    }
}
```



#### 4.源码解析

```java
//动态路由
public abstract class AbstractRoutingDataSource{
    
    protected DataSource determineTargetDataSource() {
	Assert.notNull(this.resolvedDataSources, "DataSource router not initialized");
	Object lookupKey = determineCurrentLookupKey();
    //通过key获取对应的数据源
	DataSource dataSource = this.resolvedDataSources.get(lookupKey);
    //数据源不存在，但是lookupKey为空,或者lenientFallback=true使用默认数据源
	if (dataSource == null && (this.lenientFallback || lookupKey == null)) {
		dataSource = this.resolvedDefaultDataSource;
	}
    //否则，数据源不存在，直接报错
	if (dataSource == null) {
		throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + lookupKey + "]");
	}
	return dataSource;
}


  @Nullable
  //获取数据源key
  protected abstract Object determineCurrentLookupKey();
     
}

//重写获取数据源key
public class DynamicDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return contextHolder.get();
    }
}

//执行流程
1. @Transactional之前执行，设置动态DataSource lookupKey
2. 根据lookupKey,获取目标DataSource,执行事务/查询.
3. 清除当前线程绑定的lookupKey
```







