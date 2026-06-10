package service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditService {
    private static AuditService instance;

    private static final String AUDIT_FILE = "audit.csv";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AuditService() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(AUDIT_FILE, true))) {
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

    public void log(String actionName) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        try (PrintWriter pw = new PrintWriter(new FileWriter(AUDIT_FILE, true))) {
            pw.println(actionName + "," + timestamp);
        } catch (IOException e) {
            System.err.println("[AUDIT] Eroare la scriere in audit.csv: " + e.getMessage());
        }
    }
}
