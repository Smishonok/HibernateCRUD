package com.valentinnikolaev.hibernatecrud.utils.beansconfigurations;

import com.valentinnikolaev.hibernatecrud.models.Post;
import com.valentinnikolaev.hibernatecrud.models.Region;
import com.valentinnikolaev.hibernatecrud.models.User;
import com.valentinnikolaev.hibernatecrud.repository.PostRepository;
import com.valentinnikolaev.hibernatecrud.repository.RegionRepository;
import com.valentinnikolaev.hibernatecrud.repository.UserRepository;
import com.valentinnikolaev.hibernatecrud.repository.hibernate.HibernatePostRepositoryImpl;
import com.valentinnikolaev.hibernatecrud.repository.hibernate.HibernateRegionRepositoryImpl;
import com.valentinnikolaev.hibernatecrud.repository.hibernate.HibernateUserRepositoryImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@ComponentScan (basePackages = "com.valentinnikolaev.hibernatecrud.repository")
public class DaoBeansConfig {

    private static Logger log = LogManager.getLogger(DaoBeansConfig.class);

    @Bean
    @Scope ("singleton")
    public SessionFactory sessionFactory() {
        try {
            org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration().configure();
            configuration.addAnnotatedClass(User.class);
            configuration.addAnnotatedClass(Post.class);
            configuration.addAnnotatedClass(Region.class);
            StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(
                    configuration.getProperties());
            return configuration.buildSessionFactory(builder.build());
        } catch (HibernateException e) {
            log.error("Session factory can`t be created.", e);
            throw new HibernateException(e);
        }
    }

    @Bean
    @Scope("singleton")
    public UserRepository userRepository() {
        return new HibernateUserRepositoryImpl(sessionFactory());
    }

    @Bean
    @Scope("singleton")
    public PostRepository postRepository() {
        return new HibernatePostRepositoryImpl(sessionFactory());
    }

    @Bean
    @Scope("singleton")
    public RegionRepository regionRepository() {
        return new HibernateRegionRepositoryImpl(sessionFactory());
    }
}
