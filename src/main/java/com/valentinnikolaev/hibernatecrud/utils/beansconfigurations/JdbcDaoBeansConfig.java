package com.valentinnikolaev.hibernatecrud.utils.beansconfigurations;

import com.valentinnikolaev.hibernatecrud.repository.PostRepository;
import com.valentinnikolaev.hibernatecrud.repository.RegionRepository;
import com.valentinnikolaev.hibernatecrud.repository.UserRepository;
import com.valentinnikolaev.hibernatecrud.repository.jdbc.JdbcPostRepositoryImpl;
import com.valentinnikolaev.hibernatecrud.repository.jdbc.JdbcRegionRepositoryImpl;
import com.valentinnikolaev.hibernatecrud.repository.jdbc.JdbcUserRepositoryImpl;
import org.springframework.context.annotation.*;

@Configuration
@ComponentScan (basePackages = "com.valentinnikolaev.hibernatecrud.repository")
public class JdbcDaoBeansConfig {

    @Bean
    @Scope("singleton")
    public UserRepository userRepository() {
        return new JdbcUserRepositoryImpl( postRepository());
    }

    @Bean
    @Scope("singleton")
    public PostRepository postRepository() {
        return new JdbcPostRepositoryImpl();
    }

    @Bean
    @Scope("singleton")
    public RegionRepository regionRepository() {
        return new JdbcRegionRepositoryImpl();
    }
}
