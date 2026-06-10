package repository;

import config.DatabaseConfig;
import entities.WildCreature;
import enums.CreatureType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WildCreatureRepository implements GenericRepository<WildCreature> {
    private static WildCreatureRepository instance;
    private final Connection connection;

    private WildCreatureRepository() {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }

    public static WildCreatureRepository getInstance() {
        if (instance == null) {
            instance = new WildCreatureRepository();
        }
        return instance;
    }

    @Override
    public WildCreature save(WildCreature wild) {
        if (wild.getCatchRate() < 0.0 || wild.getCatchRate() > 1.0) {
            throw new IllegalArgumentException("Catch rate must be between 0.0 and 1.0 (0% - 100%)");
        }
        String sql =
            "INSERT INTO wild_creatures (name, max_hp, attack, defense, speed, level, type, catch_rate, is_caught)" +
            " VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, wild.getName());
            ps.setInt(2, wild.getMaxHp());
            ps.setInt(3, wild.getAttack());
            ps.setInt(4, wild.getDefense());
            ps.setInt(5, wild.getSpeed());
            ps.setInt(6, wild.getLevel());
            ps.setString(7, wild.getType().name());
            ps.setDouble(8, wild.getCatchRate());
            ps.setBoolean(9, wild.isCaught());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) wild.setId(keys.getInt(1));
            return wild;
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la salvarea creaturii salbatice: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<WildCreature> findById(int id) {
        String sql = "SELECT * FROM wild_creatures WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la cautarea creaturii salbatice: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<WildCreature> findAll() {
        String sql = "SELECT * FROM wild_creatures ORDER BY id";
        List<WildCreature> list = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la listarea creaturilor salbatice: " + e.getMessage(), e);
        }
        return list;
    }

    public List<WildCreature> findByCatchRateAbove(double minRate) {
        String sql = "SELECT * FROM wild_creatures WHERE catch_rate >= ? ORDER BY catch_rate DESC";
        List<WildCreature> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, minRate);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la filtrarea dupa catch rate: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public WildCreature update(WildCreature wild) {
        if (wild.getCatchRate() < 0.0 || wild.getCatchRate() > 1.0) {
            throw new IllegalArgumentException("Catch rate must be between 0.0 and 1.0 (0% - 100%)");
        }
        String sql =
            "UPDATE wild_creatures SET name=?, max_hp=?, attack=?, defense=?," +
            " speed=?, level=?, type=?, catch_rate=?, is_caught=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, wild.getName());
            ps.setInt(2, wild.getMaxHp());
            ps.setInt(3, wild.getAttack());
            ps.setInt(4, wild.getDefense());
            ps.setInt(5, wild.getSpeed());
            ps.setInt(6, wild.getLevel());
            ps.setString(7, wild.getType().name());
            ps.setDouble(8, wild.getCatchRate());
            ps.setBoolean(9, wild.isCaught());
            ps.setInt(10, wild.getId());
            ps.executeUpdate();
            return wild;
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la actualizarea creaturii salbatice: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM wild_creatures WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la stergerea creaturii salbatice: " + e.getMessage(), e);
        }
    }

    private WildCreature mapRow(ResultSet rs) throws SQLException {
        WildCreature wc = new WildCreature(
            rs.getString("name"),
            rs.getInt("max_hp"),
            rs.getInt("attack"),
            rs.getInt("defense"),
            rs.getInt("speed"),
            rs.getInt("level"),
            CreatureType.valueOf(rs.getString("type")),
            rs.getDouble("catch_rate")
        );
        wc.setId(rs.getInt("id"));
        return wc;
    }
}
