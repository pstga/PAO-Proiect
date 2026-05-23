package service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Serviciu singleton de audit.
 * Scrie in fisierul audit.csv o linie de forma:
 *   nume_actiune,timestamp
 * de fiecare data cand este apelata metoda log().
 */
public class AuditService {

    private static AuditService instance;

    private static final String AUDIT_FILE = "audit.csv";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AuditService() {
        // Cream fisierul (daca nu exista) fara a-l suprascrie
        try (PrintWriter pw = new PrintWriter(new FileWriter(AUDIT_FILE, true))) {
            // fisierul e gata de utilizare
        } catch (IOException e) {
            System.err.println("[AUDIT] Nu s-a putut initializa fisierul de audit: " + e.getMessage());
        }
    }

    public static AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    /**
     * Inregistreaza o actiune in fisierul CSV.
     *
     * @param actionName numele actiunii (ex: REGISTER_TRAINER)
     */
    public void log(String actionName) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        try (PrintWriter pw = new PrintWriter(new FileWriter(AUDIT_FILE, true))) {
            pw.println(actionName + "," + timestamp);
        } catch (IOException e) {
            System.err.println("[AUDIT] Eroare la scriere in audit.csv: " + e.getMessage());
        }
    }
}
