package com.valentinnikolaev.hibernatecrud.repository.hibernate;

import com.valentinnikolaev.hibernatecrud.models.Post;
import com.valentinnikolaev.hibernatecrud.models.User;
import com.valentinnikolaev.hibernatecrud.repository.PostRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.*;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;


@Component
@Scope ("singleton")
public class HibernatePostRepositoryImpl implements PostRepository {

    private Logger log = LogManager.getLogger(HibernatePostRepositoryImpl.class);
    private SessionFactory sessionFactory;

    public HibernatePostRepositoryImpl(@Autowired SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Post> add(Post post) {
        if (post.getUser() == null) {
            log.warn("User is not defined, post can`t be added into database!");
            return Optional.empty();
        }

        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.persist(post);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            log.error("Post not added into database.", e);
        }
        session.close();

        List<BiFunction<CriteriaBuilder, Root<Post>, Predicate>> restrictions = new ArrayList<>();
        restrictions.add((cb, r)->cb.equal(r.get("user"), post.getUser()));
        restrictions.add((cb, r)->cb.equal(r.get("content"), post.getContent()));
        restrictions.add((cb, r)->cb.equal(r.get("created"), post.getDateOfCreation()));

        return getPosts(restrictions.stream().toArray(BiFunction[]::new)).stream().findFirst();
    }

    @Override
    public Optional<Post> add(Long userId, String content, Clock clock) {
        boolean isTransactionCommitted = true;

        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        Post post = new Post.PostBuilder().withContent(content).withClock(clock).build();
        try {
            User loadedUser = session.load(User.class, userId, LockOptions.UPGRADE);
            post.setUser(loadedUser);
            loadedUser.addPost(post);
            session.persist(loadedUser);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            isTransactionCommitted = false;
            log.error("Post not added into database.", e);
        }
        session.close();

        Optional<Post> postFromDb = Optional.empty();
        if (isTransactionCommitted) {
            List<BiFunction<CriteriaBuilder, Root<Post>, Predicate>> restrictions = new ArrayList<>();
            restrictions.add((cb, r)->cb.equal(r.get("user").get("id"), userId));
            restrictions.add((cb, r)->cb.equal(r.get("content"), content));
            restrictions.add((cb, r)->cb.equal(r.get("created"), post.getDateOfCreation()));
            postFromDb = getPosts(restrictions.stream().toArray(BiFunction[]::new))
                    .stream()
                    .findFirst();
        }

        return postFromDb;
    }


    @Override
    public Optional<Post> get(Long id) {
        Session session = sessionFactory.openSession();
        Optional<Post> postOptional = Optional.empty();
        try {
            postOptional = Optional.of(session.find(Post.class, id));
        } catch (Exception e) {
            log.error("Post with id: {} is not contains in database", id, e);
        }
        session.close();

        return postOptional;
    }

    @Override
    public List<Post> getPostsByUserId(Long userId) {
        BiFunction<CriteriaBuilder, Root<Post>, Predicate> restriction = (cb, r)->cb.equal(
                r.get("user").get("id"), userId);

        return getPosts(restriction);
    }

    @Override
    public Optional<Post> change(Post entity) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.update(entity);
            transaction.commit();
            session.close();
        } catch (Exception e) {
            transaction.rollback();
            session.close();
            log.error("Illegal entity type. Post was not changed.");
        }

        return get(entity.getId());
    }

    @Override
    public boolean remove(Long id) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session
                .createQuery("delete from Post p where p.id=:id")
                .setParameter("id", id)
                .executeUpdate();
        session.getTransaction().commit();
        session.close();
        return ! isContains(id);
    }

    @Override
    public boolean removePostsByUserId(Long userId) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session
                .createQuery("delete from Post p where p.user.id=:userId")
                .setParameter("userId", userId)
                .executeUpdate();
        session.getTransaction().commit();
        session.close();

        return getPostsByUserId(userId).isEmpty();
    }

    @Override
    public List<Post> getAll() {
        BiFunction<CriteriaBuilder, Root<Post>, Predicate> restriction = (cb, r)->cb.greaterThan(
                r.get("id"), 0);
        return getPosts(restriction);
    }

    @Override
    public boolean isContains(Long id) {
        BiFunction<CriteriaBuilder, Root<Post>, Predicate> restriction = (cb, r)->cb.equal(
                r.get("id"), id);

        return ! getPosts(restriction).isEmpty();
    }

    private List<Post> getPosts(
            BiFunction<CriteriaBuilder, Root<Post>, Predicate>... restrictions) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Post> query = criteriaBuilder.createQuery(Post.class);
        Root<Post> root = query.from(Post.class);
        root.fetch("user", JoinType.LEFT).fetch("region", JoinType.LEFT);
        query
                .select(root)
                .where(List
                               .of(restrictions)
                               .stream()
                               .map(r->r.apply(criteriaBuilder, root))
                               .toArray(Predicate[]::new));

        List<Post> posts = session.createQuery(query).getResultList();

        session.getTransaction().commit();
        session.close();
        return posts;
    }
}
