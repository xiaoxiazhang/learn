package com.ggj.center.coin.config.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author wuji
 * @Motto 他强由他强，清风拂山冈；他横任他横，明月照大江。
 * @since 2018-12-18
 */
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

    //每执行一次数据库，动态获取DataSource
    @Override
    protected Object determineCurrentLookupKey() {
        return contextHolder.get();
    }
}
