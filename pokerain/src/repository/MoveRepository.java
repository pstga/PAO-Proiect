package repository;

import config.DatabaseConfig;
import entities.Move;
import enums.CreatureType;
import enums.MoveCategory;
import enums.StatusEffect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MoveRepository implements GenericRepository<Move> {
    private static MoveRepository instance;
    private final Connection connection;

    private MoveRepository() {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }

    public static MoveRepository getInstance() {
        if (instance == null) {
            instance = new MoveRepository();
        }
        return instance;
    }

    @Override
    public Move save(Move move) {
        String sql =
            "INSERT INTO moves (name, power, accuracy, pp, max_pp, type, category, side_effect)" +
            " VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, move.getName());
            ps.setInt(2, move.getPower());
            ps.setInt(3, move.getAccuracy());
            ps.setInt(4, move.getPp());
            ps.setInt(5, move.getMaxPp());
            ps.setString(6, move.getType().name());
            ps.setString(7, move.getCategory().name());
            ps.setString(8, move.getSideEffect().name());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) move.setId(keys.getInt(1));
            return move;
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la salvarea mutarii: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Move> findById(int id) {
        String sql = "SELECT * FROM moves WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la cautarea mutarii: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Move> findAll() {
        String sql = "SELECT * FROM moves ORDER BY id";
        List<Move> list = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la listarea mutarilor: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Move> findByType(CreatureType type) {
        String sql = "SELECT * FROM moves WHERE type = ?";
        List<Move> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, type.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la filtrarea dupa tip: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Move update(Move move) {
        String sql =
            "UPDATE moves SET name=?, power=?, accuracy=?, pp=?, max_pp=?," +
            " type=?, category=?, side_effect=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, move.getName());
            ps.setInt(2, move.getPower());
            ps.setInt(3, move.getAccuracy());
            ps.setInt(4, move.getPp());
            ps.setInt(5, move.getMaxPp());
            ps.setString(6, move.getType().name());
            ps.setString(7, move.getCategory().name());
            ps.setString(8, move.getSideEffect().name());
            ps.setInt(9, move.getId());
            ps.executeUpdate();
            return move;
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la actualizarea mutarii: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM moves WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la stergerea mutarii: " + e.getMessage(), e);
        }
    }

    private Move mapRow(ResultSet rs) throws SQLException {
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
        return m;
    }
}
