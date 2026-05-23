package service;

import config.DatabaseConfig;
import entities.Move;
import entities.TrainerCreature;
import entities.WildCreature;
import enums.CreatureType;
import enums.MoveCategory;
import enums.StatusEffect;
import repository.TrainerCreatureRepository;
import repository.WildCreatureRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Serviciu singleton pentru operatii CRUD asupra entitatilor
 * TrainerCreature si WildCreature.
 * Fiecare operatie este inregistrata in fisierul de audit.
 */
public class CreatureService {

    private static CreatureService instance;
    private final TrainerCreatureRepository trainerCreatureRepo;
    private final WildCreatureRepository    wildCreatureRepo;
    private final AuditService              audit;

    private CreatureService() {
        this.trainerCreatureRepo = TrainerCreatureRepository.getInstance();
        this.wildCreatureRepo    = WildCreatureRepository.getInstance();
        this.audit               = AuditService.getInstance();
    }

    public static CreatureService getInstance() {
        if (instance == null) {
            instance = new CreatureService();
        }
        return instance;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  TrainerCreature CRUD
    // ════════════════════════════════════════════════════════════════════════

    public TrainerCreature saveTrainerCreature(TrainerCreature creature, int trainerId) {
        TrainerCreature saved = trainerCreatureRepo.save(creature, trainerId);
        audit.log("ADD_CREATURE");
        System.out.printf("[DB] TrainerCreature '%s' salvata cu id=%d.%n",
                saved.getNickname(), saved.getId());
        return saved;
    }

    public Optional<TrainerCreature> findTrainerCreatureById(int id) {
        audit.log("READ_TRAINER_CREATURE_BY_ID");
        return trainerCreatureRepo.findById(id);
    }

    public List<TrainerCreature> findAllTrainerCreatures() {
        audit.log("READ_ALL_TRAINER_CREATURES");
        return trainerCreatureRepo.findAll();
    }

    public List<TrainerCreature> findCreaturesByTrainerId(int trainerId) {
        audit.log("READ_CREATURES_BY_TRAINER");
        return trainerCreatureRepo.findByTrainerId(trainerId);
    }

    public TrainerCreature updateTrainerCreature(TrainerCreature creature) {
        TrainerCreature updated = trainerCreatureRepo.update(creature);
        audit.log("UPDATE_CREATURE");
        System.out.printf("[DB] TrainerCreature '%s' actualizata.%n", updated.getNickname());
        return updated;
    }

    public void deleteTrainerCreature(int id) {
        trainerCreatureRepo.delete(id);
        audit.log("DELETE_CREATURE");
        System.out.printf("[DB] TrainerCreature cu id=%d stearsa.%n", id);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  WildCreature CRUD
    // ════════════════════════════════════════════════════════════════════════

    public WildCreature saveWildCreature(WildCreature wild) {
        WildCreature saved = wildCreatureRepo.save(wild);
        audit.log("SAVE_WILD_CREATURE");
        System.out.printf("[DB] WildCreature '%s' salvata cu id=%d.%n",
                saved.getName(), saved.getId());
        return saved;
    }

    public Optional<WildCreature> findWildCreatureById(int id) {
        audit.log("READ_WILD_CREATURE_BY_ID");
        return wildCreatureRepo.findById(id);
    }

    public List<WildCreature> findAllWildCreatures() {
        audit.log("READ_ALL_WILD_CREATURES");
        return wildCreatureRepo.findAll();
    }

    public WildCreature updateWildCreature(WildCreature wild) {
        WildCreature updated = wildCreatureRepo.update(wild);
        audit.log("UPDATE_WILD_CREATURE");
        System.out.printf("[DB] WildCreature '%s' actualizata.%n", updated.getName());
        return updated;
    }

    public void deleteWildCreature(int id) {
        wildCreatureRepo.delete(id);
        audit.log("DELETE_WILD_CREATURE");
        System.out.printf("[DB] WildCreature cu id=%d stearsa.%n", id);
    }

    public List<WildCreature> findWildCreaturesByCatchRate(double minRate) {
        audit.log("READ_WILD_CREATURES_BY_CATCH_RATE");
        return wildCreatureRepo.findByCatchRateAbove(minRate);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Creature Moves (join table creature_moves)
    // ════════════════════════════════════════════════════════════════════════

    /** Atribuie o mutare unei creaturi (max 4). */
    public void assignMove(int creatureId, int moveId) {
        Connection conn = DatabaseConfig.getInstance().getConnection();
        try {
            // verifica limita de 4 mutari
            PreparedStatement cnt = conn.prepareStatement(
                "SELECT COUNT(*) FROM creature_moves WHERE creature_id = ?");
            cnt.setInt(1, creatureId);
            ResultSet rs = cnt.executeQuery();
            rs.next();
            if (rs.getInt(1) >= 4) {
                throw new RuntimeException("Maximum 4 moves per creature!");
            }
            // insereaza
            PreparedStatement ps = conn.prepareStatement(
                "INSERT IGNORE INTO creature_moves (creature_id, move_id) VALUES (?, ?)");
            ps.setInt(1, creatureId);
            ps.setInt(2, moveId);
            ps.executeUpdate();
            audit.log("ASSIGN_MOVE");
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la atribuirea mutarii: " + e.getMessage(), e);
        }
    }

    /** Returneaza mutarile atribuite unei creaturi. */
    public List<Move> getCreatureMoves(int creatureId) {
        Connection conn = DatabaseConfig.getInstance().getConnection();
        List<Move> moves = new ArrayList<>();
        String sql = "SELECT m.* FROM moves m " +
                     "JOIN creature_moves cm ON m.id = cm.move_id " +
                     "WHERE cm.creature_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, creatureId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Move m = new Move(
                    rs.getString("name"),
                    rs.getInt("power"),
                    rs.getInt("accuracy"),
                    rs.getInt("pp"),
                    CreatureType.valueOf(rs.getString("type")),
                    MoveCategory.valueOf(rs.getString("category")),
                    StatusEffect.valueOf(rs.getString("side_effect"))
                );
                m.setId(rs.getInt("id"));
                moves.add(m);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la citirea mutarilor: " + e.getMessage(), e);
        }
        return moves;
    }

    /** Sterge legatura creatura-mutare. */
    public void removeMove(int creatureId, int moveId) {
        Connection conn = DatabaseConfig.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM creature_moves WHERE creature_id = ? AND move_id = ?")) {
            ps.setInt(1, creatureId);
            ps.setInt(2, moveId);
            ps.executeUpdate();
            audit.log("REMOVE_MOVE");
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la stergerea mutarii: " + e.getMessage(), e);
        }
    }
}

