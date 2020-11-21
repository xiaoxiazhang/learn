package com.ggj.center.coin.config.datasource;

import com.ggj.center.coin.constants.DataSourceEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author wuji
 * @Motto 他强由他强，清风拂山冈；他横任他横，明月照大江。
 * @since 2018-12-18
 */
@Slf4j
@Aspect
@Order(-1)//保证在@Transactional之前执行
@Component
public class DynamicDataSourceAspect {


    //改变数据源
    @Before("@annotation(targetDataSource)")
    public void changeDataSource(JoinPoint joinPoint, TargetDataSource targetDataSource) {
        String datasourceName = targetDataSource.name();
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