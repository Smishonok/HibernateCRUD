package com.valentinnikolaev.hibernatecrud.repository.hibernate;

import com.valentinnikolaev.hibernatecrud.models.Region;
import com.valentinnikolaev.hibernatecrud.models.Role;
import com.valentinnikolaev.hibernatecrud.models.User;
import com.valentinnikolaev.hibernatecrud.repository.UserRepository;
import com.valentinnikolaev.hibernatecrud.utils.HibernateSessionFactoryUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

@Component
@Scope ("singleton")
public class HibernateUserRepositoryImpl implements UserRepository {

    private Logger log = LogManager.getLogger(this);

    @Override
    public Optional<User> add(User user) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        session.persist(user);

        Optional<User> userOptional = session
                .createQuery("from User u where u.firstName=:firstName and u.lastName=:lastName " +
                             "and u.region=:region and u.role=:role", User.class)
                .setParameter("firstName", user.getFirstName())
                .setParameter("lastName", user.getLastName())
                .setParameter("region", user.getRegion())
                .setParameter("role", user.getRole())
                .getResultStream()
                .findFirst();

        session.close();

        return userOptional;
    }

    @Override
    public Optional<User> get(Long id) {
        return get(id, false);
    }

    @Override
    public Optional<User> get(long id, boolean loadPosts) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        session.getTransaction().begin();

        User user = null;
        try {
            user = session.find(User.class, id);
        } catch (Exception e) {
            log.error("User with id:{} was no find in the database", id, e);
        }

        if (user != null && loadPosts) {
            Hibernate.initialize(user.getPosts());
        }

        session.getTransaction().commit();
        session.close();

        return user == null
               ? Optional.empty()
               : Optional.of(user);
    }

    @Override
    public Optional<User> change(User entity) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        session.getTransaction().begin();
        session.merge(entity);
        session.getTransaction().commit();
        session.close();

        Optional<User> optionalUser;
        if (entity.getPosts() instanceof HibernateProxy) {
            optionalUser = get(entity.getId(), false);
        } else {
            optionalUser = get(entity.getId(), true);
        }

        return optionalUser;
    }

    @Override
    public boolean remove(Long id) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.createQuery("delete from User where id=:id").setParameter("id", id).executeUpdate();
        session.getTransaction().commit();
        session.close();

        return ! isContains(id);
    }

    @Override
    public List<User> getAll() {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> userRoot = query.from(User.class);
        query.select(userRoot);
        List<User> users = session.createQuery(query).getResultList();
        session.getTransaction().commit();
        session.close();
        return users;
    }

    @Override
    public boolean isContains(Long id) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> userRoot = query.from(User.class);
        query.select(userRoot).where(criteriaBuilder.equal(userRoot.get("id"), id));
        boolean isEmpty = session.createQuery(query).getResultList().isEmpty();
        session.getTransaction().commit();
        session.close();
        return ! isEmpty;
    }
}
