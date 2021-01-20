package com.valentinnikolaev.hibernatecrud.utils.beansconfigurations;

import com.valentinnikolaev.hibernatecrud.repository.PostRepository;
import com.valentinnikolaev.hibernatecrud.repository.RegionRepository;
import com.valentinnikolaev.hibernatecrud.repository.UserRepository;
import com.valentinnikolaev.hibernatecrud.repository.hibernate.HibernatePostRepositoryImpl;
import com.valentinnikolaev.hibernatecrud.repository.hibernate.HibernateRegionRepositoryImpl;
import com.valentinnikolaev.hibernatecrud.repository.hibernate.HibernateUserRepositoryImpl;
import org.springframework.context.annotation.*;

@Configuration
@ComponentScan (basePackages = "com.valentinnikolaev.hibernatecrud.repository")
public class DaoBeansConfig {

    @Bean
    @Scope("singleton")
    public UserRepository userRepository() {
        return new HibernateUserRepositoryImpl();
    }

    @Bean
    @Scope("singleton")
    public PostRepository postRepository() {
        return new HibernatePostRepositoryImpl();
    }

    @Bean
    @Scope("singleton")
    public RegionRepository regionRepository() {
        return new HibernateRegionRepositoryImpl();
    }
}
