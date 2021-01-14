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

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

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
        User user = session.find(User.class, id);

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

        session.update(entity);
        session.flush();

        return Optional.empty();
    }

    @Override
    public boolean remove(Long aLong) {
        return false;
    }

    @Override
    public List<User> getAll() {
        return null;
    }

    @Override
    public boolean removeAll() {
        return false;
    }

    @Override
    public boolean isContains(Long aLong) {
        return false;
    }
}
