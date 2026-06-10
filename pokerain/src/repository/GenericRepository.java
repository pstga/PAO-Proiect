package repository;

import java.util.List;
import java.util.Optional;

public interface GenericRepository<T> {
    T save(T entity);

    Optional<T> findById(int id);

    List<T> findAll();

    T update(T entity);

    void delete(int id);
}
