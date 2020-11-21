package com.ggj.center.coin.config.datasource;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.ggj.center.coin.config.XconfClientConfig;
import com.ggj.center.coin.model.properties.DruidDataSourceProperties;
import com.ggj.platform.security.datasource.SecureDruidDataSource;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.sql.SQLException;


@Slf4j
@Configuration
@AutoConfigureAfter(XconfClientConfig.class)
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
