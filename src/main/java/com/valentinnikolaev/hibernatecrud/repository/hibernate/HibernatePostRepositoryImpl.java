package com.valentinnikolaev.hibernatecrud.repository.hibernate;

import com.valentinnikolaev.hibernatecrud.models.Post;
import com.valentinnikolaev.hibernatecrud.models.User;
import com.valentinnikolaev.hibernatecrud.repository.PostRepository;
import com.valentinnikolaev.hibernatecrud.utils.HibernateSessionFactoryUtil;
import javafx.geometry.Pos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

@Component
@Scope ("singleton")
public class HibernatePostRepositoryImpl implements PostRepository {

    private Logger log = LogManager.getLogger(HibernatePostRepositoryImpl.class);

    @Override
    public Optional<Post> add(Post post) {
        if (post.getUser() == null) {
            log.warn("User is not defined, post can`t be added into database!");
            return Optional.empty();
        }

        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        session.persist(post);
        Optional<Post> postOptional = session
                .createQuery("from Post p where p.user=:user and p.content=:content and p" +
                             ".created=:created",Post.class)
                .setParameter("user", post.getUser())
                .setParameter("content", post.getContent())
                .setParameter("created", post.getDateOfCreation())
                .getResultStream()
                .findFirst();
        session.close();

        return postOptional;
    }

    @Override
    public Optional<Post> get(Long id) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
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
                r.get("userId"), userId);

        return getPosts(restriction);
    }

    @Override
    public Optional<Post> change(Post entity) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        session.merge(entity);
        session.close();

        return get(entity.getId());
    }

    @Override
    public boolean remove(Long id) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
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
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
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

    private List<Post> getPosts(BiFunction<CriteriaBuilder, Root<Post>, Predicate> restriction) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Post> query = criteriaBuilder.createQuery(Post.class);
        Root<Post> root = query.from(Post.class);
        query.select(root).where(restriction.apply(criteriaBuilder, root));
        List<Post> posts = session.createQuery(query).getResultList();
        session.getTransaction().commit();
        session.close();
        return posts;
    }
}
