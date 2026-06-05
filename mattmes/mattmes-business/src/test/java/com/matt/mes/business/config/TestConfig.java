package com.matt.mes.business.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import com.matt.mes.common.config.MybatisPlusConfig;

/**
 * 测试配置类
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@MapperScan("com.matt.mes.business.mapper")
@ComponentScan("com.matt.mes.business")
@Import(MybatisPlusConfig.class)
public class TestConfig {
}
