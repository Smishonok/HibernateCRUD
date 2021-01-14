package com.valentinnikolaev.hibernatecrud.utils.beansconfigurations;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan (basePackages = "com.valentinnikolaev.hibernatecrud.controller")
@Import(JdbcDaoBeansConfig.class)
public class ControllersBeansConfig {
}
