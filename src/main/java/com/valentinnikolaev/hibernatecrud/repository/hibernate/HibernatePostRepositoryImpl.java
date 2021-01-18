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
    public Optional<Post> add(Post entity) {
        if (entity.getUser() == null) {
            log.warn("User is not defined, post can`t be added into database!");
            return Optional.empty();
        }

        try (Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            User user = session.load(User.class, entity.getUser().getId());
            user.getPosts().add(entity);
            session.merge(user);
            session.getTransaction().commit();
        } catch (Exception e) {
            log.error("Post was not added into database. User with id: {} is not contains in " +
                      "database.", entity.getUser().getId(), e);
            return Optional.empty();
        }

        return get(entity.getUser(), entity.getContent(), entity.getDateOfCreation());
    }

    @Override
    public Optional<Post> get(Long aLong) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        session.beginTransaction();
        Optional<Post> postOptional = Optional.empty();
        try {
            postOptional = Optional.of(session.find(Post.class, aLong));
        } catch (Exception e) {
            log.error("Post with id: {} is not contains in database", aLong, e);
        }
        session.getTransaction().commit();
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
        session.beginTransaction();
        session.merge(entity);
        session.getTransaction().commit();
        session.close();

        return get(entity.getId());
    }

    @Override
    public boolean remove(Long aLong) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session
                .createQuery("delete from Post p where p.id=:id")
                .setParameter("id", aLong)
                .executeUpdate();
        session.getTransaction().commit();
        session.close();
        return ! isContains(aLong);
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
    public boolean isContains(Long aLong) {
        BiFunction<CriteriaBuilder, Root<Post>, Predicate> restriction = (cb, r)->cb.equal(
                r.get("id"), aLong);

        return ! getPosts(restriction).isEmpty();
    }

    private Optional<Post> get(User user, String content, LocalDateTime dateOfCreating) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        session.beginTransaction();
        Optional<Post> optionalPost = session
                .createQuery("from Post p join User u on p.user.id=u.id where u=:user and p" +
                             ".content=:content and p.created=:created")
                .setParameter("user", user)
                .setParameter("content", content)
                .setParameter("created", dateOfCreating)
                .getResultStream()
                .findFirst();
        session.getTransaction().commit();
        session.close();
        return optionalPost;
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
