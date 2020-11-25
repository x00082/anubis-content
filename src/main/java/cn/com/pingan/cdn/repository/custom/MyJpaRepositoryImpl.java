package cn.com.pingan.cdn.repository.custom;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Iterator;

/**
 * @Classname MySimpleJpaRepository
 * @Description TODO
 * @Date 2020/11/24 11:51
 * @Created by Luj
 */

public class MyJpaRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID> implements MyJpaRepository<T, ID> {

    private static final int BATCH_SIZE = 50;

    private final EntityManager em;


    public MyJpaRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.em = entityManager;
    }

/*
    public MyJpaRepositoryImpl(Class<T> domainClass, EntityManager em) {
        super(domainClass, em);
        this.em = em;
    }
    */


    @Override
    @Transactional
    @Qualifier("mysqlEntityManager")
    public <S extends T> Iterable<S> batchSave(Iterable<S> entities) {
        Iterator<S> iterator = entities.iterator();
        int index = 0;
        while (iterator.hasNext()){
            em.persist(iterator.next());
            index++;
            if (index % BATCH_SIZE == 0){
                em.flush();
                em.clear();
            }
        }
        if (index % BATCH_SIZE != 0){
            em.flush();
            em.clear();
        }
        return entities;

    }

    @Override
    @Transactional
    @Qualifier("mysqlEntityManager")
    public <S extends T> Iterable<S> batchUpdate(Iterable<S> entities) {
        Iterator<S> iterator = entities.iterator();
        int index = 0;
        while (iterator.hasNext()){
            em.merge(iterator.next());
            index++;
            if (index % BATCH_SIZE == 0){
                em.flush();
                em.clear();
            }
        }
        if (index % BATCH_SIZE != 0){
            em.flush();
            em.clear();
        }
        return entities;
    }
}
