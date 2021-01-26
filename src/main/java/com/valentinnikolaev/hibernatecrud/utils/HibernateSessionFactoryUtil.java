package com.valentinnikolaev.hibernatecrud.utils;

import com.valentinnikolaev.hibernatecrud.models.Post;
import com.valentinnikolaev.hibernatecrud.models.Region;
import com.valentinnikolaev.hibernatecrud.models.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import java.awt.*;
import java.util.function.Function;

public class HibernateSessionFactoryUtil {

    private static Logger log = LogManager.getLogger(HibernateSessionFactoryUtil.class);

    private static SessionFactory sessionFactory;

    private HibernateSessionFactoryUtil() {}

    public static SessionFactory getSessionFactory() throws HeadlessException {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration().configure();
                configuration.setProperty("hibernate.connection.url", "jdbc:postgresql://localhost:5532/postgres");
                configuration.addAnnotatedClass(User.class);
                configuration.addAnnotatedClass(Post.class);
                configuration.addAnnotatedClass(Region.class);
                StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(
                        configuration.getProperties());
                sessionFactory = configuration.buildSessionFactory(builder.build());
            } catch (HibernateException e) {
                log.error("Session factory can`t be created.", e);
            }
        }
        return sessionFactory;
    }

    public static <T> T processTransaction(Function<Session, T> function) {
        Session session = getSessionFactory().openSession();
        session.beginTransaction();
        T requestResult = function.apply(session);
        session
                .getTransaction()
                .commit();
        session.close();
        return requestResult;
    }
}
