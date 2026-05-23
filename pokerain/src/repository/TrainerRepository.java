package repository;

import config.DatabaseConfig;
import entities.Trainer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository singleton pentru entitatea Trainer.
 * Expune operatii CRUD si cautare dupa nume.
 */
public class TrainerRepository implements GenericRepository<Trainer> {

    private static TrainerRepository instance;
    private final Connection connection;

    private TrainerRepository() {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }

    public static TrainerRepository getInstance() {
        if (instance == null) {
            instance = new TrainerRepository();
        }
        return instance;
    }

    // ── CREATE ──────────────────────────────────────────────────────────────

    @Override
    public Trainer save(Trainer trainer) {
        String sql = "INSERT INTO trainers (name, money) VALUES (?, ?)";
        try (PreparedStatement ps =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, trainer.getName());
            ps.setInt(2, trainer.getMoney());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                trainer.setId(keys.getInt(1));
            }
            return trainer;
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la salvarea trainerului: " + e.getMessage(), e);
        }
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    @Override
    public Optional<Trainer> findById(int id) {
        String sql = "SELECT * FROM trainers WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la cautarea trainerului: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Trainer> findAll() {
        String sql = "SELECT * FROM trainers ORDER BY name";
        List<Trainer> list = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la listarea trainerilor: " + e.getMessage(), e);
        }
        return list;
    }

    public Optional<Trainer> findByName(String name) {
        String sql = "SELECT * FROM trainers WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la cautarea dupa nume: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    @Override
    public Trainer update(Trainer trainer) {
        String sql = "UPDATE trainers SET name = ?, money = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, trainer.getName());
            ps.setInt(2, trainer.getMoney());
            ps.setInt(3, trainer.getId());
            ps.executeUpdate();
            return trainer;
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la actualizarea trainerului: " + e.getMessage(), e);
        }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM trainers WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la stergerea trainerului: " + e.getMessage(), e);
        }
    }

    // ── Mapare ResultSet → Trainer ───────────────────────────────────────────

    private Trainer mapRow(ResultSet rs) throws SQLException {
        Trainer t = new Trainer(rs.getString("name"), rs.getInt("money"));
        t.setId(rs.getInt("id"));
        return t;
    }
}
