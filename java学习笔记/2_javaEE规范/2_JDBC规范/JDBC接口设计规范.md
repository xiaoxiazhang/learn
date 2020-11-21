### JDBC接口设计规范

JDBC（Java Database Connectivity）：是一个独立于特定数据库管理系统、通用的SQL数据库存取和操作的公共接口（一组API）。定义了用来访问数据库的标准Java类库，使用这个类库可以以一种标准的方法、方便地访问数据库资源。JDBC驱动程动是各个数据库厂商【如Oricle, MySQL, DB2, MS SQLServer】根据JDBC的规范制作的 JDBC 实现类的类库，JDBC驱动程序总共有四种类型：

* 第一类：JDBC-ODBC桥。
* 第二类：部分本地API部分Java的驱动程序。 
* 第三类：JDBC网络纯Java驱动程序。
* 第四类：本地协议的纯 Java 驱动程序。 


​     

#### (一). JDBC接口规范和使用

##### 1. 接口规范设计

```java
-- Driver接口 ==> jdbc驱动类
  核心接口：Connection connect(String url, java.util.Properties info) ==> 获取连接

-- DriverManager类
  属性：final static CopyOnWriteArrayList<DriverInfo> registeredDrivers  ==> 保存注册的Driver
  核心方法：
    loadInitialDrivers静态代码块中调用 ==> 通过 ServiceLoader.load(Driver.class); 把对应的Driver注册到DriverManager中
    public static Connection getConnection(String url,String user, String password) ==> 获取连接，通过调用注册Driver类来获取连接
    
// JDBC规定: 驱动类在被加载时,需要主动把自己注册到DriverManger中:)
public class Driver extends NonRegisteringDriver implements java.sql.Driver {
    public Driver() throws SQLException {
    }
    static {
        try {
            DriverManager.registerDriver(new Driver());
        } catch (SQLException var1) {
            throw new RuntimeException("Can't register driver!");
        }
    }
}


-- Connection接口 ==> 代表数据库连接,每个Connection代表一个物理连接会话
  核心方法：
    // 创建不同statement对象
    Statement createStatement()
    reparedStatement prepareStatement(String sql)
    CallableStatement prepareCall(String sql)
    
    // 设置事务的隔离级别，是否自动提交事务，事务回滚和事务提交
    void setTransactionIsolation(int level)
    void setAutoCommit(boolean autoCommit)
    void rollback()
    void rollback(Savepoint savepoint)
    void commit()
    
    // 返回数据库元信息
    DatabaseMetaData getMetaData()	


-- DatabaseMetaData接口 ==> 获得数据源的各种信息
  核心方法：
    String getURL()	      // 返回数据库的URL
    String getUserName()	// 返回连接数据库当前用户名
    boolean isReadOnly()	// 是否只允许读操作。
    String getDatabaseProductName()	   // 返回数据库的产品名称
    String getDatabaseProductVersion() // 返回数据库的版本号
    String getDriverName()	    // 返回驱动驱动程序的名称
    String getDriverVersion()	  // 返回驱动程序的版本号


-- Statement接口 ==> 用于执行DDL/DML/DCL语句
  核心接口：
    boolean execute(String sql)	       // 可以用于执行所有的SQL语句
    ResultSet executeQuery(String sql) // 执行查询返回resultSet
    int executeUpdate(String sql)	     // 执行DDL / DML语句
    ResultSet getResultSet()	// 获取execute方法返回结果集
    int getUpdateCount()	    // 获取execute方法(DML)影响行数

  -- PreparedStatement接口 ==> 预处理Statment,可以防止SQL注入
    核心接口：setXXX(int parameterIndex, XXX x) ==> 设置占位符值，参数索引从1开始

    -- CallableStatement接口 ==> 存储过程Statement
       入参：CallableStatement#setXxx(int parameterIndex/String parameterName, X x);
       回参：CallableStatement#registerOutParameter(int parameterIndex, int sqlType)
       获取回参值：CallableStatement#getXxx(int parameterIndex/String parameterName)


-- ResultSet接口 ==> 结果集对象,可以通过列索引/列名来读数据
  核心接口：
    boolean next()	向后移动指针，如果有值返回true
    getXXX(String columnLabel)
    getXXX(int int columnIndex)	通过列名或者列索引（从1开始）获取查询列值XXX对应其类型
    ResultSetMetaData getMetaData()	获取结果集元信息


-- ResultSetMetaData接口 
  核心接口：
    String getColumnName(int column)   // 获取指定列的名称
    int getColumnCount()               // 返回当前 ResultSet 对象中的列数。
    int getColumnType(int column)	   // 检索指定列的数据库特定类型名称。
    int isNullable(int column)         // 指定列中的值是否可以为 null。
    boolean isAutoIncrement(int column)// 是否自增列


-- DataSource接口
  核心接口：Connection getConnection()

```



##### 2. CRUD API使用

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.40</version>
</dependency>
```



```java
public class JDBCTest {
    public static Connection getConnection(String file) {
        try {
            Properties properties = new Properties();
            properties.load(ClassLoader.getSystemResourceAsStream(file));
            Class.forName(properties.getProperty("jdbc.driver"));
            String url = properties.getProperty("jdbc.url");
            String username = properties.getProperty("jdbc.user");
            String password = properties.getProperty("jdbc.password");
            return DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            log.error("Create connection error");
            return null;
        }
    }
  

    @Test
    public void test() throws Exception{
        Connection connection = getConnection("app.properties");

        // Statement
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM cart_item limit 2");
        int columnCount = result.getMetaData().getColumnCount();
        while (result.next()) {
            for (int i = 1; i <= columnCount; ++i) {
                System.out.printf("%s\t", result.getObject(i));
            }
            System.out.println();
        }


        // PrepareStatement
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM cart_item where number=?");
        ps.setLong(1,1805164L);
        ResultSet psResult = ps.executeQuery();
        while (psResult.next()) {
            for (int i = 1; i <= columnCount; ++i) {
                System.out.printf("%s\t", psResult.getObject(i));
            }
            System.out.println();
        }


        // CallableStatement
        CallableStatement cs = connection.prepareCall("{CALL countEmployee(?,?)}");
        cs.setString(1,"test");
        cs.registerOutParameter(2,Types.INTEGER); // 设置入参
        cs.execute();
        System.out.println(cs.getInt(2)); // 获取出参


        // ddl support
        Statement dmlStatement = connection.createStatement();
        int res = dmlStatement.executeUpdate("CREATE TABLE t_ddl(id INT auto_increment PRIMARY KEY) " );
          
    }
}

```





##### 3. 事务支持

事务是由数据库操作序列组成的逻辑执行单元，这些操作要么全部执行, 要么全部不执行。 MySQL事务功能需要有InnoDB存储引擎的支持。事务的ACID特性：

* 原子性（A: Atomicity）：事务是不可再分的最小逻辑执行体;
* 一致性（C: Consistency）：事务执行的结果，必须使数据库从一个一致性状态，变为另一个一致性状态。
* 隔离性（I: Isolation）： 各个事务的执行互不干扰，任意一个事务的内部操作对其他并发事务都是隔离的（并发执行的事务之间不能看到对方的中间状态，不能互相影响)
* 持续性（D: Durability）：也称持久性(Persistence)，指事务一旦提交, 对数据所做的任何改变都要记录到永久存储器【通常指物理数据库】



事务的隔离级别：用于处理并发问题【脏读，不可重复读，幻读】

* read_uncommited：读未提交   

* read_commited：读已提交  ==> 解决脏读问题

* repeatable_read：可重复读 ==> 解决脏读，不可重复读问题

* serialize：串行化 ==> 没有并发问题，但是性能最低



Commit/Rollback，当事务所包含的全部操作都成功执行后提交事务,使操作永久生效,事务提交有两种方式:

* 显式提交: 使用commit;
* 自动提交: 执行DDL/DCL语句或程序正常退出;



当事务所包含的任意一个操作执行失败后应该回滚事务, 使该事务中所做的修改全部失效, 事务回滚也有两种方式

* 显式回滚: 使用rollback;
* 自动回滚: 系统错误或强行退出.

注意: 同一事务中所有的操作,都必须使用同一个Connection.

```java
Test
public void test() throws Exception {
    Connection connection = getConnection("db.properties");
    boolean autoCommitFlag = connection.getAutoCommit();
    // 关闭自动提交, 开启事务
    connection.setAutoCommit(false);
    PreparedStatement ps = null;
    try{
         ps = connection.prepareStatement("UPDATE `employee` SET `last_name`=? WHERE `id`=?");
         ps.setString(1,"haha");
         ps.setLong(2,1l);
         ps.executeUpdate();
         // if (true)  throw new RuntimeException("报错啦");
         connection.commit();
    } catch (Exception e){
        connection.rollback();
    } finally{
        ps.close();
        connection.close();
    }
}
```



 

#####  4. 批处理

多条SQL语句被当做同一批操作同时执行。调用Statement对象的addBatch(String sql)方法将多条SQL语句收集起来，然后调executeBatch()同时执行。为了让批量操作可以正确进行, 必须把批处理视为单个事务, 如果在执行过程中失败, 则让事务回滚到批处理开始前的状态。

对于批处理，可以使用PreparedStatement，但是建议使用Statement。因为PreparedStatement的预编译空间有限，当数据量过大时可能会引起内存溢出。  MySQL默认也没有打开批处理功能,需要在URL中设置rewriteBatchedStatements=true参数打开。

```java
@Test
public void test() throws Exception {
    Connection connection = getConnection("db.properties");
    connection.setAutoCommit(false);
    Statement statement = connection.createStatement();
    for (int i = 0; i < 10; ++i) {
        //添加批处理语句
        statement.addBatch("insert into employee(last_name,gender,email) values(MD5(RAND() * 10000) , 'm','h861336327@163.com')");
    }
    //执行批处理语句
    statement.executeBatch();
    connection.commit();
    statement.close();
    connection.close();
}

```



#### (二). Java数据库连接池

通过DriverManger获得Connection，Connection对应一个实际的物理连接。每次操作都需要打开物理连接, 使用完后立即关闭；这样频繁的打开/关闭连接会造成不必要的数据库系统性能消耗.。

数据库连接池提供的解决方案：当应用启动时，主动建立足够的数据库连接，并将这些连接组织成连接池。每次请求连接时，无须重新打开连接；而是从池中取出已有连接，使用完后并不实际关闭连接，而是归还给池。

JDBC数据库连接池使用javax.sql.DataSource表示，DataSource只是一个接口。 其实现通常由服务器提供商（如WebLogic, WebShere）或开源组织（如DBCP,C3P0和HikariCP）提供.数据库连接池的常用参数如下: 

* 数据库初始连接数;
* 连接池最大连接数;
* 连接池最小连接数;



##### 1. Druid配置和使用

官方文档：[https://github.com/alibaba/druid/wiki/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98](https://github.com/alibaba/druid/wiki/常见问题)

| **配置**                      | **缺省** | **说明**                                                     |
| ----------------------------- | -------- | ------------------------------------------------------------ |
| name                          |          | 默认格式是：”DataSource-” + System.identityHashCode(this)    |
| url                           |          | 数据库URL                                                    |
| username                      |          | 数据库用户名                                                 |
| password                      |          | 数据库的密码                                                 |
| driverClassName               |          | 驱动类名称，例如：com.mysql.jdbc.Driver                      |
| initialSize                   | 0        | 配置连接池的初始化大小                                       |
| maxActive                     | 8        | 最大连接池数量                                               |
| maxIdle                       | 8        | 已废弃                                                       |
| minIdle                       |          | 最小连接池数量                                               |
| maxWait                       |          | 获取连接等待超时的时间，单位毫秒。配置了maxWait之后，缺省启用公平锁，并发效率会有所下降，如果需要可以通过配置useUnfairLock属性为true使用非公平锁。 |
| poolPreparedStatements        | false    | 是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大，比如说oracle。在mysql下建议关闭 |
| maxOpenPreparedStatements     | -1       | 要启用PSCache，必须配置大于0，当大于0时，poolPreparedStatements自动触发修改为true。在Druid中，不会存在Oracle下PSCache占用内存过多的问题，可以把这个数值配置大一些，比如说100 |
| validationQuery               |          | 检测连接是否有效的sql，要求是一个查询语句，常用select 'x'。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会其作用。 |
| testOnBorrow                  | true     | 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。 |
| testOnReturn                  | false    | 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能 |
| testWhileIdle                 | false    | 建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。 |
| timeBetweenEvictionRunsMillis |          | 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒。 |
| minEvictableIdleTimeMillis    |          | 配置一个连接在池中最小生存的时间，单位是毫秒                 |
| connectionInitSqls            |          | 物理连接初始化的时候执行的sql                                |
| exceptionSorter               |          | 根据dbType自动识别 当数据库抛出一些不可恢复的异常时，抛弃连接 |
| filters                       |          | 属性类型是字符串，通过别名的方式配置扩展插件，常用的插件有： 监控统计用的filter:stat日志用的filter:log4j防御sql注入的filter:wall |
| proxyFilters                  |          | 类型是List，如果同时配置了filters和proxyFilters，是组合关系，并非替换关系 |

```java
   // 配置举例
    @Bean(name = "tradePlatformDataSource", initMethod = "init")
    public DruidDataSource tradePlatformDataSource() {
        try (SecureDruidDataSource datasource = new SecureDruidDataSource()) {
            datasource.setUrl(druidDataSourceProp.getTradePlatformDbUrl() + MYSQL_SUFFIX);
            datasource.setUsername(druidDataSourceProp.getTradePlatformDbUsername());
            datasource.setPassword(druidDataSourceProp.getTradePlatformDbPassword());
            datasource.setDriverClassName(druidDataSourceProp.getDriverClassName());
            commonConfig(datasource);
            log.info("------------------- trade platform datasource init success -----------------");
            return datasource;
        }
    }

    private void commonConfig(DruidDataSource datasource)  {
        //配置连接池的初始化大小，最大值，最小值
        datasource.setInitialSize(druidDataSourceProp.getInitSize());
        datasource.setMaxActive(druidDataSourceProp.getMaxActive());
        datasource.setMinIdle(druidDataSourceProp.getMinIdle());
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
        StatFilter statFilter = new StatFilter();
        statFilter.setSlowSqlMillis(200);
        statFilter.setLogSlowSql(true);
        datasource.setProxyFilters(Lists.newArrayList(statFilter));
    }
```



```xml
<!--打印慢SQL logback配置 -->

<!-- SLOW_SQL 日志 APPENDER  -->
<appender name="SlowLogAppender" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <File>${LOG_HOME}/slow.log</File>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <FileNamePattern>${LOG_HOME}/archive/slow.%d{yyyy-MM-dd}.log</FileNamePattern>
        <maxHistory>10</maxHistory>
    </rollingPolicy>
    <encoder>
        <Pattern>${pattern}</Pattern>
    </encoder>
    <filter class="ch.qos.logback.classic.filter.LevelFilter">
        <level>ERROR</level>
        <onMatch>ACCEPT</onMatch>
        <onMismatch>DENY</onMismatch>
    </filter>
</appender>

<logger name="com.alibaba.druid.filter.stat.StatFilter" additivity="false" level="error">
    <appender-ref ref="SlowLogAppender"/>
</logger>

```





##### 2. HikariCP配置和使用
| **配置**            | **缺省 **       | **说明**                                                     |
| ------------------- | --------------- | ------------------------------------------------------------ |
| pool-name           | auto-genenrated | 连接池的名字。                                               |
| jdbcUrl             |                 | 数据库URL                                                    |
| username            |                 | 数据库用户名                                                 |
| password            |                 | 数据库的密码                                                 |
| driverClassName     |                 | 驱动类名称，例如：com.mysql.jdbc.Driver                      |
| maxPoolSize         | 10              | 最大连接池数量                                               |
| minIdle             | 10              | 最小连接池数量                                               |
| autoCommit          | True            | 连接池中连接是否自动提交。如果为false，需要应用层手动提交事物。 |
| connection-timeout  | 30s             | 连接超时时间。可以接收的最小超时时间为250ms。                |
| idleTimeout         | 10min           | 连接池空闲时间。仅在minimum-idle小于maximum-poop-size的时候才会起作用，可自定义设置较长时间。 |
| maxLifetime         | 30min           | 连接池中连接的最大生命周期。当连接一致处于闲置状态时，数据库可能会主动断开连接。为了防止大量的同一时间处于空闲连接因为数据库方的闲置超时策略断开连接，一般将这个值设置的比数据库的“闲置超时时间”小几秒，以便这些连接断开后，HikariCP能迅速的创建新一轮的连接。 |
| connectionTestQuery |                 | 连接测试查询语句                                             |



```java
    // 配置举例：默认不会提前创建连接，而是在使用的时候创建。   
    @Bean
    public DataSource dataSource(DataSourceProp dataSourceProp){
        HikariConfig hikariConfig = new HikariConfig();

        // datasource配置
        hikariConfig.setDriverClassName(dataSourceProp.getDriverClassName());
        hikariConfig.setJdbcUrl(dataSourceProp.getUrl());
        hikariConfig.setUsername(dataSourceProp.getUsername());
        hikariConfig.setPassword(dataSourceProp.getPassword());

        // 连接池配置
        hikariConfig.setPoolName("Cart_HikariCP");
        hikariConfig.setMaximumPoolSize(dataSourceProp.getMaxActive());
        hikariConfig.setMinimumIdle(dataSourceProp.getMinIdle());
        hikariConfig.setConnectionTestQuery("select 1");

        // 数据库连接超时时间,30s
        hikariConfig.setConnectionTimeout(30 * 1000);

        // 空闲连接存活最大时间 10分钟
        hikariConfig.setIdleTimeout(10 * 60 * 1000);

        hikariConfig.setAutoCommit(true);

        // 连接的生命时长（毫秒），超时而且没被使用则被释放（retired），默认:30分钟 1800000ms，
        // 建议设置比数据库超时时长少60秒，参考MySQL wait_timeout参数（show variables like '%timeout%';）
        hikariConfig.setMaxLifetime(8 * 60 * 60 * 1000 - 60 * 1000);

        return new HikariDataSource(hikariConfig);
    }

    @Data
    @Component
    @ConfigurationProperties(prefix = "datasource")
    static class DataSourceProp {
        // 驱动className
        private String driverClassName;

        // 初始化创建的连接数
        private Integer initSize;

        // 最大连接数量
        private Integer maxActive;

        // 最小空闲连接
        private Integer minIdle;

        private String url;

        private String username;

        private String password;

    }
```



**原理说明**


