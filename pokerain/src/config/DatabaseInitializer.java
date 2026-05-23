package config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializeaza schema bazei de date MySQL la pornirea aplicatiei.
 * Foloseste CREATE TABLE IF NOT EXISTS pentru idempotenta.
 */
public class DatabaseInitializer {

    private static final String CREATE_TRAINERS =
        "CREATE TABLE IF NOT EXISTS trainers (" +
        "  id    INT AUTO_INCREMENT PRIMARY KEY," +
        "  name  VARCHAR(100) NOT NULL UNIQUE," +
        "  money INT          NOT NULL DEFAULT 0" +
        ")";

    private static final String CREATE_MOVES =
        "CREATE TABLE IF NOT EXISTS moves (" +
        "  id          INT AUTO_INCREMENT PRIMARY KEY," +
        "  name        VARCHAR(100) NOT NULL," +
        "  power       INT," +
        "  accuracy    INT," +
        "  pp          INT," +
        "  max_pp      INT," +
        "  type        VARCHAR(50)," +
        "  category    VARCHAR(50)," +
        "  side_effect VARCHAR(50)" +
        ")";

    private static final String CREATE_TRAINER_CREATURES =
        "CREATE TABLE IF NOT EXISTS trainer_creatures (" +
        "  id            INT AUTO_INCREMENT PRIMARY KEY," +
        "  trainer_id    INT          NOT NULL," +
        "  name          VARCHAR(100)," +
        "  nickname      VARCHAR(100)," +
        "  max_hp        INT," +
        "  hp            INT," +
        "  attack        INT," +
        "  defense       INT," +
        "  speed         INT," +
        "  level         INT," +
        "  experience    INT DEFAULT 0," +
        "  loyalty       INT DEFAULT 70," +
        "  type          VARCHAR(50)," +
        "  status_effect VARCHAR(50) DEFAULT 'NONE'," +
        "  FOREIGN KEY (trainer_id) REFERENCES trainers(id) ON DELETE CASCADE" +
        ")";

    private static final String CREATE_WILD_CREATURES =
        "CREATE TABLE IF NOT EXISTS wild_creatures (" +
        "  id         INT AUTO_INCREMENT PRIMARY KEY," +
        "  name       VARCHAR(100) NOT NULL," +
        "  max_hp     INT," +
        "  attack     INT," +
        "  defense    INT," +
        "  speed      INT," +
        "  level      INT," +
        "  type       VARCHAR(50)," +
        "  catch_rate DOUBLE," +
        "  is_caught  BOOLEAN DEFAULT FALSE" +
        ")";

    private static final String CREATE_CREATURE_MOVES =
        "CREATE TABLE IF NOT EXISTS creature_moves (" +
        "  creature_id INT NOT NULL," +
        "  move_id     INT NOT NULL," +
        "  PRIMARY KEY (creature_id, move_id)," +
        "  FOREIGN KEY (creature_id) REFERENCES trainer_creatures(id) ON DELETE CASCADE," +
        "  FOREIGN KEY (move_id) REFERENCES moves(id) ON DELETE CASCADE" +
        ")";

    private static final String CREATE_ITEMS =
        "CREATE TABLE IF NOT EXISTS items (" +
        "  id          INT AUTO_INCREMENT PRIMARY KEY," +
        "  name        VARCHAR(100) NOT NULL," +
        "  price       INT NOT NULL," +
        "  description VARCHAR(255)," +
        "  category    VARCHAR(50)" +
        ")";

    private static final String CREATE_TRAINER_ITEMS =
        "CREATE TABLE IF NOT EXISTS trainer_items (" +
        "  trainer_id INT NOT NULL," +
        "  item_id    INT NOT NULL," +
        "  quantity   INT NOT NULL DEFAULT 0," +
        "  PRIMARY KEY (trainer_id, item_id)," +
        "  FOREIGN KEY (trainer_id) REFERENCES trainers(id) ON DELETE CASCADE," +
        "  FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE" +
        ")";

    private static final String SEED_ITEMS =
        "INSERT IGNORE INTO items (name, price, description, category) VALUES " +
        "('Poke Ball',    200, 'A basic ball for catching Pokemon.',       'BALL')," +
        "('Great Ball',   600, 'Better catch rate than a Poke Ball.',      'BALL')," +
        "('Ultra Ball',  1200, 'Very high catch rate.',                    'BALL')," +
        "('Potion',       300, 'Restores 20 HP to one Pokemon.',          'HEALING')," +
        "('Super Potion', 700, 'Restores 50 HP to one Pokemon.',          'HEALING')," +
        "('Revive',      1500, 'Revives a fainted Pokemon with half HP.', 'HEALING')";

    /** Creeaza toate tabelele (daca nu exista deja). */
    public static void initialize() {
        Connection conn = DatabaseConfig.getInstance().getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(CREATE_TRAINERS);
            stmt.executeUpdate(CREATE_MOVES);
            stmt.executeUpdate(CREATE_TRAINER_CREATURES);
            stmt.executeUpdate(CREATE_WILD_CREATURES);
            stmt.executeUpdate(CREATE_CREATURE_MOVES);
            stmt.executeUpdate(CREATE_ITEMS);
            stmt.executeUpdate(CREATE_TRAINER_ITEMS);
            stmt.executeUpdate(SEED_ITEMS);
            // adauga coloana hp daca nu exista (pentru baze de date vechi)
            try {
                stmt.executeUpdate("ALTER TABLE trainer_creatures ADD COLUMN hp INT AFTER max_hp");
                stmt.executeUpdate("UPDATE trainer_creatures SET hp = max_hp WHERE hp IS NULL");
                System.out.println("[DB] Coloana 'hp' adaugata la trainer_creatures.");
            } catch (SQLException ignored) { /* coloana exista deja */ }
            System.out.println("[DB] Schema initiata cu succes (tabele create/verificate).");
        } catch (SQLException e) {
            System.err.println("[DB] Eroare la initializarea schemei: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
