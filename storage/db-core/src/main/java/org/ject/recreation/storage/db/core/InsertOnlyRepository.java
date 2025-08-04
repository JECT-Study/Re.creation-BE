package org.ject.recreation.storage.db.core;

public interface InsertOnlyRepository<T> {
    void persistOnly(T entity);
}
