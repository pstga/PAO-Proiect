package repository;

import config.DatabaseConfig;
import entities.TrainerCreature;
import enums.CreatureType;
import enums.StatusEffect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrainerCreatureRepository implements GenericRepository<TrainerCreature> {
    private static TrainerCreatureRepository instance;
    private final Connection connection;

    private TrainerCreatureRepository() {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }

    public static TrainerCreatureRepository getInstance() {
        if (instance == null) {
            instance = new TrainerCreatureRepository();
        }
        return instance;
    }

    public TrainerCreature save(TrainerCreature creature, int trainerId) {
        String sql =
            "INSERT INTO trainer_creatures" +
            " (trainer_id, name, nickname, max_hp, hp, attack, defense, speed," +
            "  level, experience, loyalty, type, status_effect)" +
            " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, trainerId);
            ps.setString(2, creature.getName());
            ps.setString(3, creature.getNickname());
            ps.setInt(4, creature.getMaxHp());
            ps.setInt(5, creature.getHp());
            ps.setInt(6, creature.getAttack());
            ps.setInt(7, creature.getDefense());
            ps.setInt(8, creature.getSpeed());
            ps.setInt(9, creature.getLevel());
            ps.setInt(10, creature.getExperience());
            ps.setInt(11, creature.getLoyalty());
            ps.setString(12, creature.getType().name());
            ps.setString(13, creature.getStatusEffect().name());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) creature.setId(keys.getInt(1));
            return creature;
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la salvarea creaturii: " + e.getMessage(), e);
        }
    }

    @Override
    public TrainerCreature save(TrainerCreature creature) {
        return save(creature, 0);
    }

    @Override
    public Optional<TrainerCreature> findById(int id) {
        String sql = "SELECT * FROM trainer_creatures WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la cautarea creaturii: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<TrainerCreature> findAll() {
        String sql = "SELECT * FROM trainer_creatures ORDER BY id";
        List<TrainerCreature> list = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la listarea creaturilor: " + e.getMessage(), e);
        }
        return list;
    }

    public List<TrainerCreature> findByTrainerId(int trainerId) {
        String sql = "SELECT * FROM trainer_creatures WHERE trainer_id = ? ORDER BY id";
        List<TrainerCreature> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, trainerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la cautarea dupa trainer: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public TrainerCreature update(TrainerCreature creature) {
        String sql =
            "UPDATE trainer_creatures SET name=?, nickname=?, max_hp=?, hp=?, attack=?," +
            " defense=?, speed=?, level=?, experience=?, loyalty=?, type=?, status_effect=?" +
            " WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, creature.getName());
            ps.setString(2, creature.getNickname());
            ps.setInt(3, creature.getMaxHp());
            ps.setInt(4, creature.getHp());
            ps.setInt(5, creature.getAttack());
            ps.setInt(6, creature.getDefense());
            ps.setInt(7, creature.getSpeed());
            ps.setInt(8, creature.getLevel());
            ps.setInt(9, creature.getExperience());
            ps.setInt(10, creature.getLoyalty());
            ps.setString(11, creature.getType().name());
            ps.setString(12, creature.getStatusEffect().name());
            ps.setInt(13, creature.getId());
            ps.executeUpdate();
            return creature;
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la actualizarea creaturii: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM trainer_creatures WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la stergerea creaturii: " + e.getMessage(), e);
        }
    }

    private TrainerCreature mapRow(ResultSet rs) throws SQLException {
        TrainerCreature tc = new TrainerCreature(
            rs.getString("name"),
            rs.getString("nickname"),
            rs.getInt("max_hp"),
            rs.getInt("attack"),
            rs.getInt("defense"),
            rs.getInt("speed"),
            rs.getInt("level"),
            CreatureType.valueOf(rs.getString("type"))
        );
        tc.setId(rs.getInt("id"));
        tc.setStatusEffect(StatusEffect.valueOf(rs.getString("status_effect")));

        int dbHp = rs.getInt("hp");
        if (dbHp > 0) {
            tc.takeDamage(tc.getMaxHp() - dbHp); 
        }
        return tc;
    }
}
