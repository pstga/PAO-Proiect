package service;

import entities.Trainer;
import repository.TrainerRepository;

import java.util.List;
import java.util.Optional;

/**
 * Serviciu singleton pentru operatii CRUD asupra entitatii Trainer.
 * Fiecare operatie este inregistrata in fisierul de audit.
 */
public class TrainerService {

    private static TrainerService instance;
    private final TrainerRepository repository;
    private final AuditService      audit;

    private TrainerService() {
        this.repository = TrainerRepository.getInstance();
        this.audit      = AuditService.getInstance();
    }

    public static TrainerService getInstance() {
        if (instance == null) {
            instance = new TrainerService();
        }
        return instance;
    }

    // ── CREATE ──────────────────────────────────────────────────────────────

    public Trainer save(Trainer trainer) {
        Trainer saved = repository.save(trainer);
        audit.log("REGISTER_TRAINER");
        System.out.printf("[DB] Trainer '%s' salvat cu id=%d.%n", saved.getName(), saved.getId());
        return saved;
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    public Optional<Trainer> findById(int id) {
        audit.log("READ_TRAINER_BY_ID");
        return repository.findById(id);
    }

    public List<Trainer> findAll() {
        audit.log("READ_ALL_TRAINERS");
        return repository.findAll();
    }

    public Optional<Trainer> findByName(String name) {
        audit.log("READ_TRAINER_BY_NAME");
        return repository.findByName(name);
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    public Trainer update(Trainer trainer) {
        Trainer updated = repository.update(trainer);
        audit.log("UPDATE_TRAINER");
        System.out.printf("[DB] Trainer '%s' actualizat.%n", updated.getName());
        return updated;
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    public void delete(int id) {
        repository.delete(id);
        audit.log("DELETE_TRAINER");
        System.out.printf("[DB] Trainer cu id=%d sters.%n", id);
    }
}
