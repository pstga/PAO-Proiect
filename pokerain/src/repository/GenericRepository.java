package repository;

import java.util.List;
import java.util.Optional;

/**
 * Interfata generica pentru operatii CRUD in baza de date.
 *
 * @param <T> tipul entitatii gestionate
 */
public interface GenericRepository<T> {

    /** Salveaza o entitate noua si returneaza entitatea cu ID-ul populat. */
    T save(T entity);

    /** Cauta o entitate dupa ID primar. */
    Optional<T> findById(int id);

    /** Returneaza toate entitatile din tabel. */
    List<T> findAll();

    /** Actualizeaza o entitate existenta (cautata dupa ID). */
    T update(T entity);

    /** Sterge entitatea cu ID-ul dat. */
    void delete(int id);
}
