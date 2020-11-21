### Mybatis Plus使用详解

在 MyBatis 的基础上只做增强不做改变，为简化开发、提高效率而生。

主要功能说明：代码生成器，通用CRUD，条件查询，分页查询，ActiveRecord 模式(**了解**)

官方文档地址：http://mp.baomidou.com/guide

#### 1. SpringBoot集成

**引入依赖**

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.0.7.1</version>
</dependency>
注意：引入 MyBatis-Plus 之后请不要再次引入MyBatis以及MyBatis-Spring以避免因版本差异导致的问题。

```



**自动配置MyBatis-Plus**

```java
@EnableTransactionManagement
@Configuration
@MapperScan("com.ggj.platform.promotion.mapper")
@AutoConfigureAfter(DataSourceConfig.class)
public class MybatisPlusConfig {
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

}
```



**自动配置详解**

```java
@org.springframework.context.annotation.Configuration
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
@ConditionalOnSingleCandidate(DataSource.class)
@EnableConfigurationProperties(MybatisPlusProperties.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class MybatisPlusAutoConfiguration {
//@ConditionalOnSingleCandidate(DataSource.class)说明只能有一个Datasource Bean配置才生效
//多数据源的时候需要自己装配sqlSessionFactory


@Bean
@ConditionalOnMissingBean
public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
    MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
    factory.setDataSource(dataSource);   //设置数据源
    factory.setVfs(SpringBootVFS.class); //设置mybatis对应spring boot环境的配置
    //MyBatis配置文件位置
    if (StringUtils.hasText(this.properties.getConfigLocation())) {
        factory.setConfigLocation(this.resourceLoader.getResource(this.properties.getConfigLocation()));
    }
    applyConfiguration(factory); //通过配置文件内容，设置configuration对象
    
    //配置configurationProperties
    if (this.properties.getConfigurationProperties() != null) {
        factory.setConfigurationProperties(this.properties.getConfigurationProperties());
    }
    //配置插件
    if (!ObjectUtils.isEmpty(this.interceptors)) {
        factory.setPlugins(this.interceptors);
    }
    if (this.databaseIdProvider != null) {
        factory.setDatabaseIdProvider(this.databaseIdProvider);
    }
     //配置别名包
    if (StringUtils.hasLength(this.properties.getTypeAliasesPackage())) {
        factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
    }
  
    if (StringUtils.hasLength(this.properties.getTypeEnumsPackage())) {
        factory.setTypeEnumsPackage(this.properties.getTypeEnumsPackage());
    }
    if (this.properties.getTypeAliasesSuperType() != null) {
        factory.setTypeAliasesSuperType(this.properties.getTypeAliasesSuperType());
    }
    //配置类型处理包
    if (StringUtils.hasLength(this.properties.getTypeHandlersPackage())) {
        factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
    }
    //配置mapper扫描包位置
    if (!ObjectUtils.isEmpty(this.properties.resolveMapperLocations())) {
        factory.setMapperLocations(this.properties.resolveMapperLocations());
    }
    GlobalConfig globalConfig;
    if (!ObjectUtils.isEmpty(this.properties.getGlobalConfig())) {
        globalConfig = this.properties.getGlobalConfig();
    } else {
        globalConfig = new GlobalConfig();
    }
    //注入填充器（spring上下文创建的MetaObjectHandler 类型bean会被设置进来）
    if (this.applicationContext.getBeanNamesForType(MetaObjectHandler.class,
        false, false).length > 0) {
        MetaObjectHandler metaObjectHandler = this.applicationContext.getBean(MetaObjectHandler.class);
        globalConfig.setMetaObjectHandler(metaObjectHandler);
    }
    //注入主键生成器（spring上下文创建的IKeyGeneratorr 类型bean会被设置进来）
    if (this.applicationContext.getBeanNamesForType(IKeyGenerator.class, false,
        false).length > 0) {
        IKeyGenerator keyGenerator = this.applicationContext.getBean(IKeyGenerator.class);
        globalConfig.getDbConfig().setKeyGenerator(keyGenerator);
    }
    //注入sql注入器（spring上下文创建的ISqlInjector 类型bean会被设置进来）
    if (this.applicationContext.getBeanNamesForType(ISqlInjector.class, false,
        false).length > 0) {
        ISqlInjector iSqlInjector = this.applicationContext.getBean(ISqlInjector.class);
        globalConfig.setSqlInjector(iSqlInjector);
    }
    //设置mybatis-plus全局配置
    factory.setGlobalConfig(globalConfig);
    return factory.getObject();
}
```



#### 2. 通用代码生成器

**需要引入依赖**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
    <version>2.1.1.RELEASE</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>2.1.1.RELEASE</version>
</dependency>

<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.4</version>
</dependency>

<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-generator</artifactId>
    <version>3.0.7</version>
</dependency>

<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.47</version>
</dependency>

<dependency>
    <groupId>org.apache.velocity</groupId>
    <artifactId>velocity-engine-core</artifactId>
    <version>2.0</version>
</dependency>
```



**通过main方法生成对应的代码**

```java
//替换自己的配置后，可直接使用。
//具体配置说明，请查看官方说明：https://mp.baomidou.com/guide/generator.html
public class GeneratorCodeHelper {

    public static void main(String[] args) {
        //全局配置
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setAuthor("wuji") //作者
                .setOutputDir("D:\\git_dir\\coin-center\\coin-generator\\src\\main\\java") //生成路径
                .setEnableCache(false)
                .setBaseColumnList(true)
                .setBaseResultMap(true)
                .setDateType(DateType.ONLY_DATE)
                .setEntityName("%sDO")
                //.setSwagger2(true)
                .setFileOverride(true)//文件覆盖
                .setIdType(IdType.AUTO); //主键策略

        //数据源配置
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setDbType(DbType.MYSQL)
                .setUrl("jdbc:mysql://rm-bp11d50m31tbdlv0h2o.mysql.rds.aliyuncs.com:3306/ggj_coin?useUnicode=true&useSSL=false&characterEncoding=utf8")
                .setDriverName("com.mysql.jdbc.Driver")
                .setUsername("test_write")
                .setPassword("Fs4210gFGS1*(s1");

        //策略配置
        StrategyConfig stConfig = new StrategyConfig();
        stConfig.setCapitalMode(true) // 全局大写命名
                .setTablePrefix("")  //都需要指定，会给对应实体显示生成@TableName注解
                .setNaming(NamingStrategy.underline_to_camel) // 数据库表映射到实体的命名策略
                .setEntityLombokModel(true)
                .setRestControllerStyle(false)
                //表名：不指定表示为所有表生成通用代码
                .setInclude(

                );

        //包名策略
        PackageConfig packageConfig = new PackageConfig();
        packageConfig.setParent("com.ggj.center.coin") //指定父包
                .setEntity("domain")  //entity包路径
                .setService("service")  //service包路径
                .setController("controller");

        AutoGenerator autoGenerator = new AutoGenerator()
                .setGlobalConfig(globalConfig)
                .setDataSource(dataSourceConfig)
                .setStrategy(stConfig)
                .setPackageInfo(packageConfig);
        autoGenerator.execute();
    }

}
```



#### 3. 通用CURD使用

**通用CRUD使用方式**

>基于 Mybatis
>
>* 需要编写 EmployeeMapper 接口，并手动编写 CRUD 方法
>
>* 提供 EmployeeMapper.xml 映射文件，并手动编写每个方法对应的 SQL 语句.
>
>基于 MP
>
>* 只需要创建 EmployeeMapper 接口, 并继承 BaseMapper 接口.这就是使用 MP
>
>* 需要完成的所有操作，甚至不需要创建 SQL 映射文件。
>
>  原理：(Basemapper中在应用启动的时候，就实现了BaseMapper接口中对应的方法)



**插入操作**

**MP注解说明**

@TableId注解：@TableId(value = "id", type = IdType.AUTO)  ==>指定id类型

@TableName注解：@TableName("dict_data")  ==>指定表名

@TableField注解：指定数据库的字段名

**MP全局配置说明**

dbColumnUnderline：用于配置数据库字段下划线到java属性驼峰转换（默认配置为true）

idType：用于说明默认id自增策略，默认配置配置0.数据库ID自增--详情参考IdType枚举

```java
//支持主键自增的数据库插入数据获取主键值
//Mybatis: 需要通过 useGeneratedKeys 以及 keyProperty 来设置
//MP: 自动将主键值回写到实体类中
int insert(T entity); 

```



**更新操作**

```java
/**
 * @param entity 实体对象
 */
int updateById(@Param(Constants.ENTITY) T entity);
```



**删除操作**

```java
/**
 * @param id 主键ID
 */
int deleteById(Serializable id);

/**
 * @param idList 主键ID列表(不能为 null 以及 empty)
 */
int deleteBatchIds(@Param(Constants.COLLECTION) Collection<? extends Serializable> idList);
```



**查询操作**

```java
/**
 * @param id 主键ID
 */
T selectById(Serializable id);

/**
 * @param idList 主键ID列表(不能为 null 以及 empty)
 */
List<T> selectBatchIds(@Param(Constants.COLLECTION) Collection<? extends Serializable> idList);

 /**
  * <p>
  * 查询（根据 columnMap 条件）
  * </p>
  * @param columnMap 表字段 map 对象
  */
List<T> selectByMap(@Param(Constants.COLUMN_MAP) Map<String, Object> columnMap);
	
```



#### 4. 条件分页查询

**AbstractWrapper**

QueryWrapper(LambdaQueryWrapper) 和 UpdateWrapper(LambdaUpdateWrapper) 的父类
用于生成 sql 的 where 条件, entity 属性也用于生成 sql 的 where 条件

| AbstractWrapper条件语句                                      | 说明                                                         |
| :----------------------------------------------------------- | :----------------------------------------------------------- |
| allEq(Map<R, V> params)                                      | `allEq({id:1,name:"老王",age:null}, false)`==><br />`id = 1 and name = '老王'` |
| eq(R column, Object val)                                     | `eq("name", "老王")`==>`name = '老王'`                       |
| ne(R column, Object val)                                     | `ne("name", "老王")`==>`name <> '老王'`                      |
| gt(R column, Object val)                                     | `gt("age", 18)`==>`age > 18`                                 |
| ge(R column, Object val)                                     | ge("age", 18)`==>`age >= 18                                  |
| lt(R column, Object val)                                     | `lt("age", 18)`==>`age < 18`                                 |
| le(R column, Object val)                                     | `le("age", 18)`==>`age <= 18`                                |
| between(R column, Object val1, Object val2)                  | `between("age", 18, 30)`==>`age between 18 and 30`           |
| notBetween(R column, Object val1, Object val2)               | `notBetween("age", 18, 30)`==>`age not between 18 and 30`    |
| like(R column, Object val)<br />notLike(R column, Object val)<br />likeLeft(R column, Object val)<br />likeRight(R column, Object val) | `like("name", "王")`==>`name like '%王%'`                    |
| isNull(R column)                                             | `isNull("name")`==>`name is null`                            |
| isNotNull(R column)                                          | `isNotNull("name")`==>`name is not null`                     |
| in(R column, Collection<?> value)                            | `in("age", 1, 2, 3)`==>`age in (1,2,3)`                      |
| notIn(R column, Collection<?> value)                         |                                                              |
| groupBy(R... columns)                                        | `groupBy("id", "name")`==>`group by id,name`                 |
| orderByAsc(R... columns)                                     | `orderByAsc("id", "name")`==>`order by id ASC,name ASC`      |
| orderByDesc(R... columns)                                    | `orderByDesc("id", "name")`==>`order by id DESC,name DESC`   |
| having(String sqlHaving, Object... params)                   | `having("sum(age) > 10")`--->`having sum(age) > 10`          |
| and(Function<This, This> func)<br />or(Function<This, This> func) | `and(i -> i.eq("name", "李白").ne("status", "活着"))`==>`and (name = '李白' and status <> '活着')` |

| QueryWrapper                | 说明                        |
| --------------------------- | --------------------------- |
| select(String... sqlSelect) | select("id", "name", "age") |


| UpdateWrapper                  | 说明                  |
| ------------------------------ | --------------------- |
| set(String column, Object val) | set("name", "老李头") |


注意: entity 生成的 where 条件与 使用各个 api 生成的 where 条件**没有任何关联行为**



**条件查询**

```java
//条件查询
T selectOne(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);
List<T> selectList(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);
Integer selectCount(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

//条件修改
int update(@Param(Constants.ENTITY) T entity, @Param(Constants.WRAPPER) Wrapper<T> updateWrapper);

//条件删除
int delete(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);
```



**条件分页查询**

```java
/**
 * <p>
 * 根据 entity 条件，查询全部记录（并翻页）
 * </p>
 * @param page         分页查询条件（可以为 RowBounds.DEFAULT）
 * @param queryWrapper 实体对象封装操作类（可以为 null）
 */
IPage<T> selectPage(IPage<T> page, @Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

注意：如果注入了PaginationInterceptor，使用的就是真分页。
```



**使用分页插件**

```java
//第一步：注入分页插件
@Bean
public PaginationInterceptor paginationInterceptor() {
    return new PaginationInterceptor();
}

//第二步：使用分页插件
IPage<CouponEntity> listCouponCriteria(Page<CouponEntity> page, @Param("couponSearch") CouponSearch couponSearch)
//注意：分页插件page参数必须是第一个；分页返回的对象与传入的对象是同一个

```





#### 5. idea插件的安装和使用

MybatisX 是一款基于 IDEA 的快速开发插件，为效率而生。（mybatis-plus作者开发）

安装方法：打开 IDEA，进入 File -> Settings -> Plugins -> Browse Repositories，输入 `mybatisx` 搜索并安装。

![1546775530884](..\..\..\images\1546775530884.png)



**功能**

- Java 与 XML 调回跳转
- Mapper 方法自动生成 XML 



#### 6. Mybatis-Plus源码分析

**CRUD注入分析**

```java
@ConditionalOnSingleCandidate(DataSource.class) //单个DataSource
@AutoConfigureAfter(DataSourceAutoConfiguration.class)

第一步：实例化MybatisConfiguration（打印Banner）

第二步：MybatisPlusAutoConfiguration自动配置SqlSessionFactory
public SqlSessionFactory sqlSessionFactory(DataSource dataSource)
  //factory置configLocation,interceptors,databaseIdProvider
  //typeAliasesPackage,typeEnumsPackage,typeAliasesSuperType
  //typeHandlersPackage,MapperLocations(mapper-xml路径)，globalConfig
  //IKeyGenerator,MetaObjectHandler,ISqlInjector
  ...
  return factory.getObject(); //MybatisSqlSessionFactoryBean
 
第三步：调用MybatisSqlSessionFactoryBean 的 afterPropertiesSet方法;
this.sqlSessionFactory = buildSqlSessionFactory(); //构造sqlSessionFactory
protected SqlSessionFactory buildSqlSessionFactory(){
    //MybatisConfiguration对象继承Configuration
    //Configuration设置vfs,typeAliasesPackage,typeAliasesSuperType,typeEnumsPackage
    //TypeHandlerRegistry,typeAliases,plugins,typeHandlersPackage,typeHandlers
    //databaseIdProvider,cache,transactionFactory,Environment,globalConfig
    ...
    
    //通过Configuration构造sqlSessioFactory
    SqlSessionFactory sqlSessionFactory = this.sqlSessionFactoryBuilder.build(configuration);
    
    //循环处理mapperLocation（配置的mapper-xml文件）
    for (Resource mapperLocation : this.mapperLocations) {               
                    XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(mapperLocation.getInputStream(),
                        configuration, mapperLocation.toString(), configuration.getSqlFragments());
                    //解析mapper-xml文件
                    xmlMapperBuilder.parse();
                
    }
}

第四步：循环解析Mapper
//xmlMapperBuilder.parse();
public void parse() {
    if (!configuration.isResourceLoaded(resource)) {
      configurationElement(parser.evalNode("/mapper"));
      configuration.addLoadedResource(resource);
      
      //绑定通用CRUD MapperStatement
      bindMapperForNamespace();
    }

    parsePendingResultMaps();
    parsePendingCacheRefs();
    parsePendingStatements();
}

第五步：绑定通用CRUD MapperStatement
bindMapperForNamespace();
//MybatisMapperAnnotationBuilder.parse()
public void parse() {
           
    if (BaseMapper.class.isAssignableFrom(type)) {
        //如果没有注入SqlInjector,则创建并注入 new DefaultSqlInjector()
        //并且调用inspectInject注入SQL
        GlobalConfigUtils.getSqlInjector(configuration).inspectInject(assistant, type);
     }
    for (Method method : methods) {
        if (!method.isBridge()) {
            parseStatement(method);
        }
    }
    parsePendingMethods();
}

第六步：注入DefaultSqlInjector的SQL
public void inspectInject(MapperBuilderAssistant builderAssistant, Class<?> mapperClass) {

    List<AbstractMethod> methodList = this.getMethodList();
    Assert.notEmpty(methodList, "No effective injection method was found.");
    // 循环注入自定义方法
    methodList.forEach(m -> m.inject(builderAssistant, mapperClass));
    mapperRegistryCache.add(className);
    /**
     * 初始化 SQL 解析
     */
    if (GlobalConfigUtils.getGlobalConfig(builderAssistant.getConfiguration()).isSqlParserCache()) {
        SqlParserHelper.initSqlParserInfoCache(mapperClass);
    }
}

//注入默认以下SQL
public List<AbstractMethod> getMethodList() {
        return Stream.of(
            new Insert(),
            new Delete(),
            new DeleteByMap(),
            new DeleteById(),
            new DeleteBatchByIds(),
            new Update(),
            new UpdateById(),
            new SelectById(),
            new SelectBatchByIds(),
            new SelectByMap(),
            new SelectOne(),
            new SelectCount(),
            new SelectMaps(),
            new SelectMapsPage(),
            new SelectObjs(),
            new SelectList(),
            new SelectPage()
        ).collect(Collectors.toList());
    }

第七步：注入SQL到MapperedStatement过程
//AbstractMethod.inject注入自定义方法
public void inject(MapperBuilderAssistant builderAssistant, Class<?> mapperClass) {
    if (null != modelClass) {
        /**
         * 注入自定义方法
         */
        TableInfo tableInfo = TableInfoHelper.initTableInfo(builderAssistant, modelClass);
        injectMappedStatement(mapperClass, modelClass, tableInfo);
    }
}
//具体SQL（SelectById）构造出MappedStatement（SqlMethod==>对应具体SQL）
public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
    SqlMethod sqlMethod = SqlMethod.SELECT_BY_ID;
    //构造出对应的SqlSource
    SqlSource sqlSource = new RawSqlSource(configuration, String.format(sqlMethod.getSql(), this.sqlSelectColumns(tableInfo, false),
        tableInfo.getTableName(), tableInfo.getKeyColumn(), tableInfo.getKeyProperty()), Object.class);
    return this.addSelectMappedStatement(mapperClass, sqlMethod.getMethod(), sqlSource, modelClass, tableInfo);
}

//把MappedStatement设置到Configration中
builderAssistant.addMappedStatement
configuration.addMappedStatement(statement);

```





**分页插件分析**

```java
@Bean
public PaginationInterceptor paginationInterceptor() {
    return new PaginationInterceptor();
}

//PaginationInterceptor拦截的是StatementHandler的prepare方法
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class PaginationInterceptor extends AbstractSqlParserHandler implements Interceptor {
    
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);

        // SQL 解析
        this.sqlParser(metaObject);

        // 先判断是不是SELECT操作
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        if (!SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())) {
            return invocation.proceed();
        }

        // 针对定义了rowBounds，做为mapper接口方法的参数
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        Object paramObj = boundSql.getParameterObject();

        // 判断参数里是否有page对象
        IPage page = null;
        if (paramObj instanceof IPage) {
            page = (IPage) paramObj;
        } else if (paramObj instanceof Map) {
            for (Object arg : ((Map) paramObj).values()) {
                if (arg instanceof IPage) {
                    page = (IPage) arg;
                    break;
                }
            }
        }

        /**
         * 不需要分页的场合，如果 size 小于 0 返回结果集
         */
        if (null == page || page.getSize() < 0) {
            return invocation.proceed();
        }

        String originalSql = boundSql.getSql();
        Connection connection = (Connection) invocation.getArgs()[0];
        DbType dbType = StringUtils.isNotEmpty(dialectType) ? DbType.getDbType(dialectType)
            : JdbcUtils.getDbType(connection.getMetaData().getURL());

        boolean orderBy = true;
        if (page.isSearchCount()) {
            //构造select count(*) 
            SqlInfo sqlInfo = SqlParserUtils.getOptimizeCountSql(page.optimizeCountSql(), sqlParser, originalSql);
            orderBy = sqlInfo.isOrderBy();
            //执行count操作SQL
            this.queryTotal(overflow, sqlInfo.getSql(), mappedStatement, boundSql, page, connection);
            if (page.getTotal() <= 0) {
                return invocation.proceed();
            }
        }

        String buildSql = concatOrderBy(originalSql, page, orderBy);
        //构造分页SQL，对于mysql就是Limit
        DialectModel model = DialectFactory.buildPaginationSql(page, buildSql, dbType, dialectClazz);
        Configuration configuration = mappedStatement.getConfiguration();
        List<ParameterMapping> mappings = new ArrayList<>(boundSql.getParameterMappings());
        Map<String, Object> additionalParameters = (Map<String, Object>) metaObject.getValue("delegate.boundSql.additionalParameters");
        model.consumers(mappings, configuration, additionalParameters);
        metaObject.setValue("delegate.boundSql.sql", model.getDialectSql());
        metaObject.setValue("delegate.boundSql.parameterMappings", mappings);

        /*
         * <p> 禁用内存分页 </p>
         * <p> 内存分页会查询所有结果出来处理（这个很吓人的），如果结果变化频繁这个数据还会不准。</p>
         */
        metaObject.setValue("delegate.rowBounds.offset", RowBounds.NO_ROW_OFFSET);
        metaObject.setValue("delegate.rowBounds.limit", RowBounds.NO_ROW_LIMIT);
        return invocation.proceed();
    }
    
}
    
//mysql构造分页语句
public class MySqlDialect implements IDialect {
    @Override
    public DialectModel buildPaginationSql(String originalSql, long offset, long limit) {
        String sql = originalSql + " LIMIT " + FIRST_MARK + StringPool.COMMA + SECOND_MARK;
        return new DialectModel(sql, offset, limit).setConsumerChain();
    }
}

```











