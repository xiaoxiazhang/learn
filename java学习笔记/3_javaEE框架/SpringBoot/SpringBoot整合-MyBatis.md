### SpringBoot整合MyBatis

#### 1. MyBatis自动配置原理

官网说明：<http://www.mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/>

##### 自动配置条件说明

```java
@org.springframework.context.annotation.Configuration
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
@ConditionalOnBean(DataSource.class)
@EnableConfigurationProperties(MybatisProperties.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class MybatisAutoConfiguration {
    // 自动配置条件
}
```



##### Mapper接口扫描原理

**方式一：@Mapper注解** （不能有@MapperScanner注解）

```java
// 1.@MapperScanner注解不存在的时候,注入组件AutoConfiguredMapperScannerRegistrar
@org.springframework.context.annotation.Configuration
@Import({ AutoConfiguredMapperScannerRegistrar.class })
@ConditionalOnMissingBean(MapperFactoryBean.class)
public static class MapperScannerRegistrarNotFoundConfiguration {

  @PostConstruct
  public void afterPropertiesSet() {
    logger.debug("No {} found.", MapperFactoryBean.class.getName());
  }
}

// 2.AutoConfiguredMapperScannerRegistrar组件在BeanDefinition注册完了之后执行扫描
// 扫描配置类所在包,扫描对应的注解@Mapper。
public static class AutoConfiguredMapperScannerRegistrar
      implements BeanFactoryAware, ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    ...
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

      ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);

      try {
        if (this.resourceLoader != null) {
          scanner.setResourceLoader(this.resourceLoader);
        }

        // 获取启动类所在包
        List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
        if (logger.isDebugEnabled()) {
          for (String pkg : packages) {
            logger.debug("Using auto-configuration base package '{}'", pkg);
          }
        }
          
        // 扫描的注解@Mapper
        scanner.setAnnotationClass(Mapper.class);
        scanner.registerFilters();
        scanner.doScan(StringUtils.toStringArray(packages));
      } 
    }
}

```



**方式二：@MapperScanner注解**

```java
//1.@MapperScan注解默认会引入组件MapperScannerRegistrar
@Import(MapperScannerRegistrar.class)
public @interface MapperScan {
    ...
}

//2.通过@MapperScan注解上面的信息扫描指定包。
public class MapperScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
  
  @Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

    // 设置注解属性信息...
    ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
    ...
 
    // 根据注解@MapperScan注解上面的value,basePackages指定包扫描。
    List<String> basePackages = new ArrayList<String>();
    for (String pkg : annoAttrs.getStringArray("value")) {
      if (StringUtils.hasText(pkg)) {
        basePackages.add(pkg);
      }
    }
    for (String pkg : annoAttrs.getStringArray("basePackages")) {
      if (StringUtils.hasText(pkg)) {
        basePackages.add(pkg);
      }
    }
    for (Class<?> clazz : annoAttrs.getClassArray("basePackageClasses")) {
      basePackages.add(ClassUtils.getPackageName(clazz));
    }
    scanner.registerFilters();
    scanner.doScan(StringUtils.toStringArray(basePackages));
  }

}

```



##### 配置SqlSessionFactory

```java
// 创建SqlSessionFactoryBean，根据配置设置dataSource,SpringBootVFS,ConfigLocation,
// plugins,databaseIdProvider,typeAliasesPackage, typeHandlersPackage,mapperLocations
@Bean
  @ConditionalOnMissingBean
  public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
    SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
    factory.setDataSource(dataSource);
    factory.setVfs(SpringBootVFS.class);
    if (StringUtils.hasText(this.properties.getConfigLocation())) {
            factory.setConfigLocation(this.resourceLoader.getResource(this.properties.getConfigLocation()));
    }
    Configuration configuration = this.properties.getConfiguration();
    if (configuration == null && !StringUtils.hasText(this.properties.getConfigLocation())) {
      configuration = new Configuration();
    }
    if (configuration != null && !CollectionUtils.isEmpty(this.configurationCustomizers)) {
      for (ConfigurationCustomizer customizer : this.configurationCustomizers) {
        customizer.customize(configuration);
      }
    }
    factory.setConfiguration(configuration);
    if (this.properties.getConfigurationProperties() != null) {
      factory.setConfigurationProperties(this.properties.getConfigurationProperties());
    }
    if (!ObjectUtils.isEmpty(this.interceptors)) {
      factory.setPlugins(this.interceptors);
    }
    if (this.databaseIdProvider != null) {
      factory.setDatabaseIdProvider(this.databaseIdProvider);
    }
    if (StringUtils.hasLength(this.properties.getTypeAliasesPackage())) {
      factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
    }
    if (StringUtils.hasLength(this.properties.getTypeHandlersPackage())) {
      factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
    }
    if (!ObjectUtils.isEmpty(this.properties.resolveMapperLocations())) {
      factory.setMapperLocations(this.properties.resolveMapperLocations());
    }

    return factory.getObject();
  }

// 可以通过拓展ConﬁgurationCustomizer，来自定义configuration属性
@Bean
ConfigurationCustomizer mybatisConfigurationCustomizer() {
  return new ConfigurationCustomizer() {
    @Override
    public void customize(Configuration configuration) {
      // customize ...
       configuration.setMapUnderscoreToCamelCase(true); 
    }
  };
}

```



#### 2. MyBatis配置和使用

##### maven依赖

```xml
<!-- druid连接池 -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.1.16</version>
</dependency>
<!-- mybatis自动配置包 -->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.0.1</version>
</dependency>
```



##### 配置数据源

```java
@Slf4j
@Configuration
@EnableTransactionManagement  //开启注解事物
public class DataSourceConfig {

    @Resource
    private DruidDataSourceProperties druidDataSourceProperties;

    private static final String MYSQL_SUFFIX = "?useUnicode=true&characterEncoding=UTF8&zeroDateTimeBehavior=convertToNull&autoReconnect=true&allowMultiQueries=true&tinyInt1isBit=false";

    @Bean(name="dataSource",initMethod = "init")
    public DruidDataSource writeDataSource() {
        try (SecureDruidDataSource datasource = new SecureDruidDataSource()) {
            datasource.setUrl(druidDataSourceProperties.getWriteDbUrl() + MYSQL_SUFFIX);
            datasource.setUsername(druidDataSourceProperties.getWriteDbUsername());
            datasource.setPassword(druidDataSourceProperties.getWriteDbPassword());
            datasource.setDriverClassName(druidDataSourceProperties.getDriverClassName());
            commonConfig(datasource);
            log.info("---------  datasource init success  ---------");
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



##### 扫描接口

```java
// 扫描Mapper接口对应的包
@MapperScan("com.ggj.trade.consign.mapper")
```



##### 配置属性

```yaml
mybatis:
  configLocation: classpath:/mybatis.xml #mybatis全局配置	
  mapperLocations:  classpath:/mapper/*/*Mapper.xml  #mapper文件位置
  type-aliases-package: com.ggj.trade.consign.model.entity 
  configuration:
    map-underscore-to-camel-case: true  #是否开启驼峰式
```



#### 3. PageHelper配置和使用

文档地址：<https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/HowToUse.md>

##### 配置原理

```java
@Configuration
@ConditionalOnBean(SqlSessionFactory.class)
@EnableConfigurationProperties(PageHelperProperties.class)
@AutoConfigureAfter(MybatisAutoConfiguration.class)
public class PageHelperAutoConfiguration {

    @Autowired
    private List<SqlSessionFactory> sqlSessionFactoryList;

    @Autowired
    private PageHelperProperties properties;
 
    @Bean
    @ConfigurationProperties(prefix = PageHelperProperties.PAGEHELPER_PREFIX)
    public Properties pageHelperProperties() {
        return new Properties();
    }

    @PostConstruct
    public void addPageInterceptor() {
        PageInterceptor interceptor = new PageInterceptor();
        Properties properties = new Properties();
        //先把一般方式配置的属性放进去
        properties.putAll(pageHelperProperties());
        //在把特殊配置放进去，由于close-conn 利用上面方式时，属性名就是 close-conn 而不是 closeConn，所以需要额外的一步
        properties.putAll(this.properties.getProperties());
        interceptor.setProperties(properties);
  
        // 把插件设置到SqlSessionFactory中去
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
            sqlSessionFactory.getConfiguration().addInterceptor(interceptor);
        }
    }
}

```



##### 使用详解

**maven依赖**

```xml
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper-spring-boot-starter</artifactId>
    <version>1.2.10</version>
</dependency>
```



**springboot配置**

```yaml
pagehelper:
  helperDialect: mysql
  reasonable: true
  supportMethodsArguments: true
  params: count=countSql
```



**项目中使用：**需要进行分页的 MyBatis 查询方法前调用 PageHelper.startPage 静态方法即可，紧跟在这个方法后的第一个MyBatis 查询方法会被进行分页。

```java
PageHelper.startPage(1, 10); //获取第1页，10条内容，默认查询总数count
List<Country> list = countryMapper.selectIf(1); //紧跟着的第一个select方法会被分页
PageInfo<Country> page = new PageInfo(list); //用PageInfo对结果进行包装
```



#### 4. MyBatis-Generator使用

官网地址：<http://www.mybatis.org/generator/>

##### 自定义lombok插件

```java
public class LombokPlugin extends PluginAdapter {

    /**
     * 校验为真，其他方法才会被执行
     */
    @Override
    public boolean validate(List<String> list) {
        return true;
    }

    /**
     * 不生成set方法
     */
    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return false;
    }

    /**
     * 不生成get方法
     */
    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return false;
    }

    /*
     * 类上生成所需注解
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        topLevelClass.addImportedType("lombok.Data");
        topLevelClass.addImportedType("lombok.EqualsAndHashCode");
        topLevelClass.addImportedType("lombok.experimental.Accessors");

        topLevelClass.addAnnotation("@Data");
        topLevelClass.addAnnotation("@EqualsAndHashCode(callSuper = false)");
        topLevelClass.addAnnotation("@Accessors(chain = true)");

        return true;
    }

    /*
     * 生成属性注释
     */
    @Override
    public boolean modelFieldGenerated(Field field,
                                       TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                       IntrospectedTable introspectedTable,
                                       Plugin.ModelClassType modelClassType) {

        StringBuilder sb = new StringBuilder();
        field.addJavaDocLine("/**");
        sb.append(" * ");
        sb.append(introspectedColumn.getRemarks());
        field.addJavaDocLine(sb.toString().replace("\n", " "));
        field.addJavaDocLine(" */");
        return true;
    }

}

```



##### generator文件配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration
        PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
        "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">
<generatorConfiguration>
    <!-- MyBatis3Simple MyBatis3-->
    <context id="DB2Tables" targetRuntime="MyBatis3">


        <plugin type="org.mybatis.generator.plugins.SerializablePlugin"/>
        <plugin type="com.ggj.trade.consign.plugin.LombokPlugin" />
        <!--<plugin type="org.mybatis.generator.plugins.ToStringPlugin" />-->
        <!--<plugin type="org.mybatis.generator.plugins.EqualsHashCodePlugin" />-->

        <!-- 忽略mybatis生成的注释 -->
        <commentGenerator>
            <property name="suppressAllComments" value="true"/>
        </commentGenerator>

        <!-- 配置数据库连接 -->
        <jdbcConnection driverClass="com.mysql.jdbc.Driver"
                        connectionURL="jdbc:mysql://drdshbga3gjsy83cpublic.drds.aliyuncs.com:3306/seller_trade?useUnicode=true&amp;useSSL=false&amp;characterEncoding=utf8"
                        userId="p_trade_rw"
                        password="rS2l53UEA1sQdEYA">
        </jdbcConnection>

        <javaTypeResolver>
            <property name="forceBigDecimals" value="false"/>
        </javaTypeResolver>

        <!-- 指定javaBean生成的位置 -->
        <javaModelGenerator
                targetPackage="com.ggj.trade.consign.model.entity"
                targetProject="trade-service-consign-generator/src/main/java">
            <property name="enableSubPackages" value="true"/>
            <property name="trimStrings" value="true"/>
        </javaModelGenerator>

        <!--指定sql映射文件生成的位置 -->
        <sqlMapGenerator
                targetPackage="mapper"
                targetProject="trade-service-consign-generator/src/main/resources">
            <property name="enableSubPackages" value="true"/>
        </sqlMapGenerator>

        <!-- 指定mapper接口生成的位置 -->
        <javaClientGenerator
                type="XMLMAPPER"
                targetPackage="com.ggj.trade.consign.dal.mapper"
                targetProject="trade-service-consign-generator/src/main/java">
            <property name="enableSubPackages" value="true"/>
        </javaClientGenerator>


        <!-- table指定每个表的生成策略 -->
        <table tableName="consign_order" domainObjectName="ConsignOrderDO"
               mapperName="ConsignOrderMapper"  enableCountByExample="false"
               enableUpdateByExample="false" enableDeleteByExample="false"
               enableSelectByExample="false" selectByExampleQueryId="false">

            <generatedKey column="id" sqlStatement="MySql" identity="true" type="post" />
            <columnOverride column="is_handwork"  jdbcType="INTEGER" javaType="java.lang.Integer" />
        </table>

        <table tableName="consign_order_sku" domainObjectName="ConsignOrderSkuDO"
               mapperName="ConsignOrderSkuMapper" enableCountByExample="false"
               enableUpdateByExample="false" enableDeleteByExample="false"
               enableSelectByExample="false" selectByExampleQueryId="false">

            <generatedKey column="id" sqlStatement="MySql" identity="true" type="post" />
            <columnOverride column="is_handwork"  jdbcType="INTEGER" javaType="java.lang.Integer"/>
        </table>

        <table tableName="consign_package" domainObjectName="ConsignPackageDO"
               mapperName="ConsignPackageMapper" enableCountByExample="false"
               enableUpdateByExample="false" enableDeleteByExample="false"
               enableSelectByExample="false" selectByExampleQueryId="false">

            <generatedKey column="id" sqlStatement="MySql" identity="true" type="post" />
            <columnOverride column="is_handwork"  jdbcType="INTEGER" javaType="java.lang.Integer"/>
        </table>

        <table tableName="consign_package_shop_relation" domainObjectName="ConsignPackageShopRelationDO"
               mapperName="ConsignPackageShopRelationMapper" enableCountByExample="false"
               enableUpdateByExample="false" enableDeleteByExample="false"
               enableSelectByExample="false" selectByExampleQueryId="false">

            <generatedKey column="id" sqlStatement="MySql" identity="true" type="post" />
        </table>

        <table tableName="deliver_time_config" domainObjectName="DeliverTimeConfigDO"
               mapperName="DeliverTimeConfigMapper" enableCountByExample="false"
               enableUpdateByExample="false" enableDeleteByExample="false"
               enableSelectByExample="false" selectByExampleQueryId="false">

            <generatedKey column="id" sqlStatement="MySql" identity="true" type="post" />
            <columnOverride column="is_deleted"  jdbcType="INTEGER" javaType="java.lang.Integer"/>
        </table>

        <!-- <table tableName="%"
               enableCountByExample="false"
               enableDeleteByExample="false"
               enableSelectByExample="false"
               enableUpdateByExample="false">

            <generatedKey column="id" sqlStatement="Mysql" identity="true"/>
            <columnOverride column="address"  javaType="java.lang.String" jdbcType="VARCHAR" />
            <columnOverride column="is_deleted"  jdbcType="INTEGER" javaType="java.lang.Integer"/>
        </table> -->
    </context>
</generatorConfiguration>
```



##### java方法生成代码

```java
public class GeneratorCodeHelper {

    public static void main(String[] args) throws Exception{
        GeneratorCodeHelper app = new GeneratorCodeHelper();
        app.generator();
    }

    private void generator() throws Exception{
        List<String> warnings = new ArrayList<String>();
        boolean overwrite = true;
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("generatorConfig.xml");
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(resourceAsStream);
        DefaultShellCallback callback = new DefaultShellCallback(overwrite);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);

        for(String warning:warnings){
            System.out.println(warning);
        }
    }
}
```

