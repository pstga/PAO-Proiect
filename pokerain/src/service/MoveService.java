package service;

import entities.Move;
import enums.CreatureType;
import repository.MoveRepository;

import java.util.List;
import java.util.Optional;

/**
 * Serviciu singleton pentru operatii CRUD asupra entitatii Move.
 * Fiecare operatie este inregistrata in fisierul de audit.
 */
public class MoveService {

    private static MoveService instance;
    private final MoveRepository repository;
    private final AuditService   audit;

    private MoveService() {
        this.repository = MoveRepository.getInstance();
        this.audit      = AuditService.getInstance();
    }

    public static MoveService getInstance() {
        if (instance == null) {
            instance = new MoveService();
        }
        return instance;
    }

    // ── CREATE ──────────────────────────────────────────────────────────────

    public Move save(Move move) {
        Move saved = repository.save(move);
        audit.log("SAVE_MOVE");
        System.out.printf("[DB] Mutare '%s' salvata cu id=%d.%n", saved.getName(), saved.getId());
        return saved;
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    public Optional<Move> findById(int id) {
        audit.log("READ_MOVE_BY_ID");
        return repository.findById(id);
    }

    public List<Move> findAll() {
        audit.log("READ_ALL_MOVES");
        return repository.findAll();
    }

    public List<Move> findByType(CreatureType type) {
        audit.log("READ_MOVES_BY_TYPE");
        return repository.findByType(type);
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    public Move update(Move move) {
        Move updated = repository.update(move);
        audit.log("UPDATE_MOVE");
        System.out.printf("[DB] Mutare '%s' actualizata.%n", updated.getName());
        return updated;
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    public void delete(int id) {
        repository.delete(id);
        audit.log("DELETE_MOVE");
        System.out.printf("[DB] Mutare cu id=%d stearsa.%n", id);
    }
}
