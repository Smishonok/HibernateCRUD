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
@Scope("singleton")
public class HibernateUserRepositoryImpl implements UserRepository {

    private Logger log = LogManager.getLogger(this);

    @Override
    public Optional<User> add(User entity) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        session.getTransaction().begin();
        session.persist(entity);
        session.flush();
        session.getTransaction().commit();
        session.close();

        return getUserByProperties(entity.getFirstName(), entity.getLastName(), entity.getRegion(),
                                   entity.getRole(), false);
    }

    private Optional<User> getUserByProperties(String firstName, String lastName, Region region,
                                               Role role, boolean loadPosts) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        session.getTransaction().begin();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        root.fetch("region", JoinType.LEFT);

        if (loadPosts) {
            root.fetch("posts", JoinType.INNER);
        }

        query.select(root);
        query.where(criteriaBuilder.equal(root.get(firstName), firstName),
                    criteriaBuilder.equal(root.get(lastName), lastName),
                    criteriaBuilder.equal(root.get(region.getName()), region.getName()),
                    criteriaBuilder.equal(root.get(role.toString()), role.toString()));
        TypedQuery<User> resultQuery = session.createQuery(query);

        session.getTransaction().commit();
        session.close();
        return resultQuery.getResultStream().findFirst();
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
