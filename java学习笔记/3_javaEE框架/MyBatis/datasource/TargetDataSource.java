package com.ggj.center.coin.config.datasource;

import java.lang.annotation.*;

/**
 * @author wuji
 * @Motto 他强由他强，清风拂山冈；他横任他横，明月照大江。
 * @since 2018-12-18
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TargetDataSource {
    String name();
}
