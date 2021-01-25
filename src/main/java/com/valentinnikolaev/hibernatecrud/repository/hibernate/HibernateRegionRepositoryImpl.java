package com.valentinnikolaev.hibernatecrud.repository.hibernate;

import com.valentinnikolaev.hibernatecrud.models.Region;
import com.valentinnikolaev.hibernatecrud.repository.RegionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Scope ("singleton")
public class HibernateRegionRepositoryImpl implements RegionRepository {

    private Logger log = LogManager.getLogger(HibernateRegionRepositoryImpl.class);
    private SessionFactory sessionFactory;

    public HibernateRegionRepositoryImpl(@Autowired SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Region> add(Region region) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.persist(region);
        session.flush();
        Optional<Region> regionOptional = session
                .createQuery("from Region r where r.name=:name", Region.class)
                .setParameter("name", region.getName())
                .getResultStream()
                .findFirst();
        session.getTransaction().commit();
        session.close();

        return regionOptional;
    }

    @Override
    public Optional<Region> get(Long id) {
        Session session = sessionFactory.openSession();
        Region region = session.find(Region.class, id);
        session.close();

        return region == null
               ? Optional.empty()
               : Optional.of(region);
    }

    @Override
    public Optional<Region> change(Region entity) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try  {
            session.update(entity);
            session.getTransaction().commit();
        } catch (Exception e) {
            transaction.rollback();
            log.error("Illegal type of entity or entity was removed.");
        }

        return get(entity.getId());
    }

    @Override
    public boolean remove(Long id) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        int numberOfChangedEntities = session
                .createQuery("delete from Region r where r.id=:id")
                .setParameter("id", id)
                .executeUpdate();
        session.getTransaction().commit();
        session.close();

        return numberOfChangedEntities > 0;
    }

    @Override
    public List<Region> getAll() {
        Session session = sessionFactory.openSession();
        List<Region> regionsList = session.createQuery("from Region", Region.class).getResultList();
        session.close();
        return regionsList;
    }

    @Override
    public boolean isContains(Long id) {
        Session session = sessionFactory.openSession();
        List<Region> regionsList = session.createQuery("from Region", Region.class).getResultList();
        session.close();
        return regionsList.size() == 1;
    }
}
