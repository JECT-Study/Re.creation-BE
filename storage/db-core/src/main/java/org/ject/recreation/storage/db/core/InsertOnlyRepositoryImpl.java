package org.ject.recreation.storage.db.core;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class InsertOnlyRepositoryImpl<T> implements InsertOnlyRepository<T> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void persistOnly(T entity) {
        em.persist(entity);
    }
}
