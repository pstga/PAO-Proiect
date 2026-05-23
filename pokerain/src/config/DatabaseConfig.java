package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton care gestioneaza conexiunea JDBC la MySQL.
 * Credentialele pot fi modificate mai jos.
 */
public class DatabaseConfig {

    private static DatabaseConfig instance;
    private Connection connection;

    // ── Configuratie conexiune ──────────────────────────────────────────────
    private static final String URL =
            "jdbc:mysql://localhost:3306/pokerain_db" +
            "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";
    private static final String PASSWORD = "rain";
    // ────────────────────────────────────────────────────────────────────────

    private DatabaseConfig() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] Conexiune MySQL stabilita cu succes.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Driver MySQL nu a fost gasit! Adauga mysql-connector-j in classpath.");
            throw new RuntimeException(e);
        } catch (SQLException e) {
            System.err.println("[DB] Eroare la conectare: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    /** Returneaza conexiunea activa; o reface daca a fost inchisa. */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            throw new RuntimeException("[DB] Imposibil de obtinut conexiunea: " + e.getMessage(), e);
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Conexiune inchisa.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Eroare la inchiderea conexiunii: " + e.getMessage());
        }
    }
}
