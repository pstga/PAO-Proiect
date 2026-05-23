package server;

import com.sun.net.httpserver.*;
import battle.BattleManager;
import battle.TypeChart;
import entities.*;
import enums.*;
import service.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

/**
 * Server HTTP embedded (com.sun.net.httpserver – inclus in JDK, fara dependente extra).
 * Expune API JSON + serveste dashboard-ul web din web/index.html.
 * Pornit automat la startup; ruleaza in parallel cu consola.
 */
public class PokerainServer {

    private static final int PORT = 8080;

    private final TrainerService  trainerService  = TrainerService.getInstance();
    private final MoveService     moveService     = MoveService.getInstance();
    private final CreatureService creatureService = CreatureService.getInstance();
    private final AuditService    audit           = AuditService.getInstance();

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/trainers",  this::handleTrainers);
        server.createContext("/api/moves",     this::handleMoves);
        server.createContext("/api/creatures", this::handleCreatures);
        server.createContext("/api/wild",      this::handleWild);
        server.createContext("/api/audit",     this::handleAudit);
        server.createContext("/api/battle",    this::handleBattle);
        server.createContext("/api/heal",      this::handleHeal);
        server.createContext("/api/cmoves",    this::handleCreatureMoves);
        server.createContext("/api/shop",      this::handleShop);
        server.createContext("/api/inventory", this::handleInventory);
        server.createContext("/",              this::handleStatic);

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println();
        System.out.println("[SERVER] ╔══════════════════════════════════════════════╗");
        System.out.printf ("[SERVER] ║   Dashboard web: http://localhost:%-7d    ║%n", PORT);
        System.out.println("[SERVER] ╚══════════════════════════════════════════════╝");
        System.out.println();
    }

    // ── Static file ───────────────────────────────────────────────────────────

    private void handleStatic(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.startsWith("/api")) { sendJson(ex, 404, "{\"error\":\"not found\"}"); return; }
        File f = new File("web/index.html");
        if (!f.exists()) {
            sendJson(ex, 503, "{\"error\":\"web/index.html not found. Run from project root.\"}");
            return;
        }
        byte[] bytes = java.nio.file.Files.readAllBytes(f.toPath());
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        send(ex, 200, bytes);
    }

    // ── Trainers ──────────────────────────────────────────────────────────────

    private void handleTrainers(HttpExchange ex) throws IOException {
        setCors(ex);
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();
        if ("OPTIONS".equals(method)) { send(ex, 204, new byte[0]); return; }

        int id = tailId(path, "/api/trainers/");

        if ("GET".equals(method)) {
            if (id > 0) {
                trainerService.findById(id).ifPresentOrElse(
                    t  -> sendJsonQ(ex, 200, trainerJson(t)),
                    () -> sendJsonQ(ex, 404, "{\"error\":\"not found\"}")
                );
            } else {
                sendJson(ex, 200, arr(trainerService.findAll(), this::trainerJson));
            }
        } else if ("POST".equals(method)) {
            String body = body(ex);
            Trainer t = new Trainer(str(body, "name"), num(body, "money"));
            trainerService.save(t);
            sendJson(ex, 201, trainerJson(t));
        } else if ("DELETE".equals(method) && id > 0) {
            trainerService.delete(id);
            sendJson(ex, 200, "{\"ok\":true}");
        } else {
            sendJson(ex, 405, "{\"error\":\"method not allowed\"}");
        }
    }

    // ── Moves ─────────────────────────────────────────────────────────────────

    private void handleMoves(HttpExchange ex) throws IOException {
        setCors(ex);
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();
        if ("OPTIONS".equals(method)) { send(ex, 204, new byte[0]); return; }

        int id = tailId(path, "/api/moves/");

        if ("GET".equals(method)) {
            sendJson(ex, 200, arr(moveService.findAll(), this::moveJson));
        } else if ("POST".equals(method)) {
            String body = body(ex);
            try {
                Move m = new Move(
                    str(body, "name"), num(body, "power"), num(body, "accuracy"), num(body, "pp"),
                    CreatureType.valueOf(str(body, "type").toUpperCase()),
                    MoveCategory.valueOf(str(body, "category").toUpperCase()),
                    StatusEffect.valueOf(str(body, "sideEffect").toUpperCase())
                );
                moveService.save(m);
                sendJson(ex, 201, moveJson(m));
            } catch (IllegalArgumentException e) {
                sendJson(ex, 400, "{\"error\":\"" + esc(e.getMessage()) + "\"}");
            }
        } else if ("DELETE".equals(method) && id > 0) {
            moveService.delete(id);
            sendJson(ex, 200, "{\"ok\":true}");
        } else {
            sendJson(ex, 405, "{\"error\":\"method not allowed\"}");
        }
    }

    // ── Creatures ─────────────────────────────────────────────────────────────

    private void handleCreatures(HttpExchange ex) throws IOException {
        setCors(ex);
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();
        if ("OPTIONS".equals(method)) { send(ex, 204, new byte[0]); return; }

        if ("GET".equals(method)) {
            if (path.contains("/trainer/")) {
                int tid = tailId(path, "/trainer/");
                sendJson(ex, 200, arr(creatureService.findCreaturesByTrainerId(tid), this::creatureJson));
            } else {
                sendJson(ex, 200, arr(creatureService.findAllTrainerCreatures(), this::creatureJson));
            }
        } else if ("DELETE".equals(method)) {
            int id = tailId(path, "/api/creatures/");
            if (id > 0) { creatureService.deleteTrainerCreature(id); sendJson(ex, 200, "{\"ok\":true}"); }
            else          sendJson(ex, 400, "{\"error\":\"missing id\"}");
        } else {
            sendJson(ex, 405, "{\"error\":\"method not allowed\"}");
        }
    }

    // ── Wild ──────────────────────────────────────────────────────────────────

    private void handleWild(HttpExchange ex) throws IOException {
        setCors(ex);
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();
        if ("OPTIONS".equals(method)) { send(ex, 204, new byte[0]); return; }

        int id = tailId(path, "/api/wild/");

        if ("GET".equals(method)) {
            sendJson(ex, 200, arr(creatureService.findAllWildCreatures(), this::wildJson));
        } else if ("POST".equals(method)) {
            String body = body(ex);
            try {
                WildCreature w = new WildCreature(
                    str(body, "name"), num(body, "maxHp"), num(body, "attack"),
                    num(body, "defense"), num(body, "speed"), num(body, "level"),
                    CreatureType.valueOf(str(body, "type").toUpperCase()),
                    dbl(body, "catchRate")
                );
                creatureService.saveWildCreature(w);
                sendJson(ex, 201, wildJson(w));
            } catch (IllegalArgumentException e) {
                sendJson(ex, 400, "{\"error\":\"" + esc(e.getMessage()) + "\"}");
            }
        } else if ("DELETE".equals(method) && id > 0) {
            creatureService.deleteWildCreature(id);
            sendJson(ex, 200, "{\"ok\":true}");
        } else {
            sendJson(ex, 405, "{\"error\":\"method not allowed\"}");
        }
    }

    // ── Audit ─────────────────────────────────────────────────────────────────

    private void handleAudit(HttpExchange ex) throws IOException {
        setCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) { send(ex, 204, new byte[0]); return; }

        List<String[]> entries = new ArrayList<>();
        File f = new File("audit.csv");
        if (f.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(",", 2);
                    if (p.length == 2) entries.add(p);
                }
            }
        }
        // Most recent first, max 200
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        int start = Math.max(0, entries.size() - 200);
        for (int i = entries.size() - 1; i >= start; i--) {
            if (!first) sb.append(",");
            first = false;
            sb.append(String.format("{\"action\":\"%s\",\"timestamp\":\"%s\"}",
                esc(entries.get(i)[0].trim()), esc(entries.get(i)[1].trim())));
        }
        sb.append("]");
        sendJson(ex, 200, sb.toString());
    }

    // ── Battle ────────────────────────────────────────────────────────────────

    private void handleBattle(HttpExchange ex) throws IOException {
        setCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) { send(ex, 204, new byte[0]); return; }
        if (!"POST".equals(ex.getRequestMethod())) {
            sendJson(ex, 405, "{\"error\":\"POST only\"}"); return;
        }

        String body = body(ex);
        String mode = str(body, "mode");

        try {
            if ("pvp".equals(mode)) {
                int playerId = num(body, "playerId");
                int rivalId  = num(body, "rivalId");
                if (playerId == rivalId) {
                    sendJson(ex, 400, "{\"error\":\"A trainer can't battle themselves!\"}"); return;
                }
                Trainer player = trainerService.findById(playerId).orElse(null);
                Trainer rival  = trainerService.findById(rivalId).orElse(null);
                if (player == null || rival == null) {
                    sendJson(ex, 404, "{\"error\":\"Trainer not found\"}"); return;
                }

                // incarca creaturile si mutarile
                loadTrainerForBattle(player, playerId);
                loadTrainerForBattle(rival, rivalId);

                if (player.getParty().isEmpty() || rival.getParty().isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"Both trainers need at least 1 creature\"}"); return;
                }

                // ruleaza lupta cu captura output
                String[] result = runBattle(player, rival);
                audit.log("CONDUCT_BATTLE");

                sendJson(ex, 200, String.format(
                    "{\"result\":\"%s\",\"log\":\"%s\",\"player\":\"%s\",\"rival\":\"%s\"}",
                    esc(result[0]), esc(result[1]), esc(player.getName()), esc(rival.getName())));

            } else if ("wild".equals(mode)) {
                int trainerId = num(body, "trainerId");
                int wildId    = num(body, "wildId");
                Trainer player = trainerService.findById(trainerId).orElse(null);
                WildCreature wild = creatureService.findWildCreatureById(wildId).orElse(null);
                if (player == null || wild == null) {
                    sendJson(ex, 404, "{\"error\":\"Trainer or wild creature not found\"}"); return;
                }

                loadTrainerForBattle(player, trainerId);
                if (player.getParty().isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"Trainer needs at least 1 creature\"}"); return;
                }

                // da mutare default wild-ului daca nu are
                if (wild.getMoves().isEmpty()) {
                    wild.addMove(new Move("Tackle", 40, 100, 35,
                        wild.getType(), MoveCategory.PHYSICAL, StatusEffect.NONE));
                }

                String[] result = runBattleWild(player, wild);
                audit.log("CONDUCT_BATTLE");

                sendJson(ex, 200, String.format(
                    "{\"result\":\"%s\",\"log\":\"%s\",\"player\":\"%s\",\"wild\":\"%s\"}",
                    esc(result[0]), esc(result[1]), esc(player.getName()), esc(wild.getName())));
            } else if ("wild-turn".equals(mode)) {
                // lupte interactive cu creaturi salbatice (o tura per request)
                int trainerId = num(body, "trainerId");
                int wildId    = num(body, "wildId");
                String action = str(body, "action"); // attack, catch, flee
                int wHp       = num(body, "wildHp");
                int pHp       = num(body, "playerHp");

                Trainer player = trainerService.findById(trainerId).orElse(null);
                WildCreature wild = creatureService.findWildCreatureById(wildId).orElse(null);
                if (player == null || wild == null) {
                    sendJson(ex, 404, "{\"error\":\"Not found\"}"); return;
                }
                loadTrainerForBattle(player, trainerId);
                if (player.getParty().isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"No creatures\"}"); return;
                }
                TrainerCreature active = player.getActiveCreature();
                if (wild.getMoves().isEmpty()) {
                    wild.addMove(new Move("Tackle", 40, 100, 35,
                        wild.getType(), MoveCategory.PHYSICAL, StatusEffect.NONE));
                }
                // prima tura: folosim maxHp
                if (wHp <= 0) wHp = wild.getMaxHp();
                if (pHp <= 0) pHp = active.getMaxHp();

                StringBuilder log = new StringBuilder();
                String result = "continue";
                boolean done = false;
                Random rng = new Random();

                if ("attack".equals(action)) {
                    Move pm = active.getMoves().get(0);
                    double tm = TypeChart.getMultiplier(pm.getType(), wild.getType());
                    double aa = active.getAttack() * active.getLoyaltyBonus();
                    int dmg = Math.max(1, (int)((pm.getPower() * aa * tm) / Math.max(1, wild.getDefense())));
                    wHp = Math.max(0, wHp - dmg);
                    log.append(String.format("%s uses %s! Deals %d damage!\n", active.getNickname(), pm.getName(), dmg));
                    String eff = TypeChart.effectiveness(tm);
                    if (!eff.isEmpty()) log.append("  -> ").append(eff).append("\n");
                    if (wHp <= 0) {
                        log.append(String.format("\nWild %s fainted! You win!\n", wild.getName()));
                        result = "WIN"; done = true;
                    } else {
                        // wild-ul contraataca
                        Move wm = wild.getMoves().get(rng.nextInt(wild.getMoves().size()));
                        double tm2 = TypeChart.getMultiplier(wm.getType(), active.getType());
                        int dmg2 = Math.max(1, (int)((wm.getPower() * wild.getAttack() * tm2) / Math.max(1, active.getDefense())));
                        pHp = Math.max(0, pHp - dmg2);
                        log.append(String.format("Wild %s uses %s! Deals %d damage!\n", wild.getName(), wm.getName(), dmg2));
                        eff = TypeChart.effectiveness(tm2);
                        if (!eff.isEmpty()) log.append("  -> ").append(eff).append("\n");
                        if (pHp <= 0) {
                            log.append(String.format("\n%s fainted! You lost!\n", active.getNickname()));
                            result = "LOSE"; done = true;
                        }
                    }
                } else if ("catch".equals(action)) {
                    double hpFactor = 1.0 - (double)wHp / wild.getMaxHp() * 0.5;
                    double chance = wild.getCatchRate() * hpFactor;
                    if (rng.nextDouble() < chance) {
                        log.append(String.format("Gotcha! %s was caught!\n", wild.getName()));
                        // verifica daca trainerul are loc in echipa (max 6)
                        int partySize = creatureService.findCreaturesByTrainerId(trainerId).size();
                        if (partySize >= 6) {
                            log.append("But your party is full! The creature was released.\n");
                            result = "CAUGHT"; done = true;
                        } else {
                            result = "CAUGHT"; done = true;
                            TrainerCreature caught = new TrainerCreature(
                                wild.getName(), wild.getName(),
                                wild.getMaxHp(), wild.getAttack(), wild.getDefense(),
                                wild.getSpeed(), wild.getLevel(), wild.getType());
                            creatureService.saveTrainerCreature(caught, trainerId);
                            creatureService.deleteWildCreature(wild.getId());
                            log.append(String.format("%s was added to your party! (%d/6)\n", wild.getName(), partySize + 1));
                        }
                    } else {
                        log.append("Oh no! The Pokemon broke free!\n");
                        Move wm = wild.getMoves().get(0);
                        double tm2 = TypeChart.getMultiplier(wm.getType(), active.getType());
                        int dmg = Math.max(1, (int)((wm.getPower() * wild.getAttack() * tm2) / Math.max(1, active.getDefense())));
                        pHp = Math.max(0, pHp - dmg);
                        log.append(String.format("Wild %s attacks! %d damage!\n", wild.getName(), dmg));
                        if (pHp <= 0) { result = "LOSE"; done = true; log.append("Your Pokemon fainted!\n"); }
                    }
                } else if ("flee".equals(action)) {
                    if (rng.nextDouble() < 0.5) {
                        log.append("Got away safely!\n");
                        result = "FLED"; done = true;
                    } else {
                        log.append("Can't escape!\n");
                        Move wm = wild.getMoves().get(0);
                        double tm2 = TypeChart.getMultiplier(wm.getType(), active.getType());
                        int dmg = Math.max(1, (int)((wm.getPower() * wild.getAttack() * tm2) / Math.max(1, active.getDefense())));
                        pHp = Math.max(0, pHp - dmg);
                        log.append(String.format("Wild %s attacks! %d damage!\n", wild.getName(), dmg));
                        if (pHp <= 0) { result = "LOSE"; done = true; log.append("Your Pokemon fainted!\n"); }
                    }
                } else {
                    sendJson(ex, 400, "{\"error\":\"action must be attack, catch or flee\"}"); return;
                }

                // persist hp-ul creaturii in DB dupa fiecare tura
                int hpDiff = active.getHp() - pHp;
                if (hpDiff > 0) active.takeDamage(hpDiff);
                else if (hpDiff < 0) active.heal(-hpDiff);
                creatureService.updateTrainerCreature(active);

                audit.log("WILD_ENCOUNTER_TURN");
                sendJson(ex, 200, String.format(
                    "{\"log\":\"%s\",\"result\":\"%s\",\"done\":%b,\"wildHp\":%d,\"wildMaxHp\":%d," +
                    "\"playerHp\":%d,\"playerMaxHp\":%d,\"wildName\":\"%s\",\"playerName\":\"%s\"}",
                    esc(log.toString()), esc(result), done, wHp, wild.getMaxHp(),
                    pHp, active.getMaxHp(), esc(wild.getName()), esc(active.getNickname())));

            } else {
                sendJson(ex, 400, "{\"error\":\"mode must be pvp, wild or wild-turn\"}");
            }
        } catch (Exception e) {
            sendJson(ex, 500, "{\"error\":\"" + esc(e.getMessage()) + "\"}");
        }
    }

    private void loadTrainerForBattle(Trainer trainer, int trainerId) {
        List<TrainerCreature> creatures = creatureService.findCreaturesByTrainerId(trainerId);
        for (TrainerCreature c : creatures) {
            List<Move> moves = creatureService.getCreatureMoves(c.getId());
            if (moves.isEmpty()) {
                c.addMove(new Move("Tackle", 40, 100, 35,
                    CreatureType.NORMAL, MoveCategory.PHYSICAL, StatusEffect.NONE));
            } else {
                for (Move m : moves) c.addMove(m);
            }
            trainer.addToParty(c);
        }
    }

    private String[] runBattle(Trainer player, Object opponent) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream capture = new PrintStream(baos);
        PrintStream original = System.out;
        System.setOut(capture);
        String result;
        try {
            BattleManager bm = new BattleManager(player, opponent);
            bm.startBattle();
            int turn = 0;
            while (!bm.isBattleOver() && turn < 20) {
                bm.executeTurn(0);
                turn++;
            }
            result = bm.getResult();
            if (result.isEmpty()) result = "DRAW";
        } finally {
            System.setOut(original);
        }
        return new String[]{ result, baos.toString() };
    }

    private String[] runBattleWild(Trainer player, WildCreature wild) {
        return runBattle(player, wild);
    }

    // ── Heal ──────────────────────────────────────────────────────────────────

    private void handleHeal(HttpExchange ex) throws IOException {
        setCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) { send(ex, 204, new byte[0]); return; }
        if (!"POST".equals(ex.getRequestMethod())) {
            sendJson(ex, 405, "{\"error\":\"POST only\"}"); return;
        }

        int trainerId = tailId(ex.getRequestURI().getPath(), "/api/heal/");
        if (trainerId <= 0) {
            sendJson(ex, 400, "{\"error\":\"missing trainer id\"}"); return;
        }

        List<TrainerCreature> creatures = creatureService.findCreaturesByTrainerId(trainerId);
        for (TrainerCreature c : creatures) {
            c.heal(c.getMaxHp());
            c.setStatusEffect(StatusEffect.NONE);
            creatureService.updateTrainerCreature(c);
        }
        audit.log("HEAL_TEAM");
        sendJson(ex, 200, String.format("{\"ok\":true,\"healed\":%d}", creatures.size()));
    }

    // ── Creature Moves ────────────────────────────────────────────────────────

    private void handleCreatureMoves(HttpExchange ex) throws IOException {
        setCors(ex);
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();
        if ("OPTIONS".equals(method)) { send(ex, 204, new byte[0]); return; }

        // /api/cmoves/123 sau /api/cmoves/123/456
        String[] parts = path.replace("/api/cmoves/", "").split("/");
        int creatureId = 0;
        int moveId = 0;
        try {
            if (parts.length >= 1 && !parts[0].isEmpty()) creatureId = Integer.parseInt(parts[0]);
            if (parts.length >= 2 && !parts[1].isEmpty()) moveId = Integer.parseInt(parts[1]);
        } catch (NumberFormatException ignored) {}

        if (creatureId <= 0) {
            sendJson(ex, 400, "{\"error\":\"missing creature id\"}"); return;
        }

        if ("GET".equals(method)) {
            List<Move> moves = creatureService.getCreatureMoves(creatureId);
            sendJson(ex, 200, arr(moves, this::moveJson));
        } else if ("POST".equals(method)) {
            String body = body(ex);
            int mId = num(body, "moveId");
            if (mId <= 0) { sendJson(ex, 400, "{\"error\":\"missing moveId\"}"); return; }
            try {
                creatureService.assignMove(creatureId, mId);
                sendJson(ex, 200, "{\"ok\":true}");
            } catch (RuntimeException e) {
                sendJson(ex, 400, "{\"error\":\"" + esc(e.getMessage()) + "\"}");
            }
        } else if ("DELETE".equals(method) && moveId > 0) {
            creatureService.removeMove(creatureId, moveId);
            sendJson(ex, 200, "{\"ok\":true}");
        } else {
            sendJson(ex, 405, "{\"error\":\"method not allowed\"}");
        }
    }

    // ── JSON serializers ──────────────────────────────────────────────────────

    private String trainerJson(Trainer t) {
        return String.format("{\"id\":%d,\"name\":\"%s\",\"money\":%d}",
                t.getId(), esc(t.getName()), t.getMoney());
    }

    private String moveJson(Move m) {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"power\":%d,\"accuracy\":%d," +
            "\"pp\":%d,\"maxPp\":%d,\"type\":\"%s\",\"category\":\"%s\",\"sideEffect\":\"%s\"}",
            m.getId(), esc(m.getName()), m.getPower(), m.getAccuracy(),
            m.getPp(), m.getMaxPp(), m.getType(), m.getCategory(), m.getSideEffect());
    }

    private String creatureJson(TrainerCreature c) {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"nickname\":\"%s\",\"level\":%d," +
            "\"hp\":%d,\"maxHp\":%d,\"attack\":%d,\"defense\":%d,\"speed\":%d," +
            "\"type\":\"%s\",\"loyalty\":%d,\"status\":\"%s\"}",
            c.getId(), esc(c.getName()), esc(c.getNickname()), c.getLevel(),
            c.getHp(), c.getMaxHp(), c.getAttack(), c.getDefense(), c.getSpeed(),
            c.getType(), c.getLoyalty(), c.getStatusEffect());
    }

    private String wildJson(WildCreature w) {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"level\":%d,\"maxHp\":%d," +
            "\"attack\":%d,\"defense\":%d,\"speed\":%d," +
            "\"type\":\"%s\",\"catchRate\":%.2f,\"caught\":%b}",
            w.getId(), esc(w.getName()), w.getLevel(), w.getMaxHp(),
            w.getAttack(), w.getDefense(), w.getSpeed(),
            w.getType(), w.getCatchRate(), w.isCaught());
    }

    @FunctionalInterface private interface Fn<T> { String apply(T t); }
    private <T> String arr(List<T> list, Fn<T> fn) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) { if (i > 0) sb.append(","); sb.append(fn.apply(list.get(i))); }
        return sb.append("]").toString();
    }

    // ── HTTP helpers ──────────────────────────────────────────────────────────

    private void sendJson(HttpExchange ex, int code, String json) throws IOException {
        byte[] b = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        send(ex, code, b);
    }
    private void sendJsonQ(HttpExchange ex, int code, String json) {
        try { sendJson(ex, code, json); } catch (IOException ignored) {}
    }
    private void send(HttpExchange ex, int code, byte[] body) throws IOException {
        ex.sendResponseHeaders(code, body.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(body); }
    }
    private void setCors(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }
    private String body(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) { return new String(is.readAllBytes(), StandardCharsets.UTF_8); }
    }
    private int tailId(String path, String prefix) {
        String last = path.substring(path.lastIndexOf('/') + 1);
        try { return Integer.parseInt(last); } catch (NumberFormatException e) { return -1; }
    }

    // ── Simple JSON parsing (no external library) ─────────────────────────────

    private String str(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return m.find() ? m.group(1) : "";
    }
    private int num(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+)").matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }
    private double dbl(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*([\\d.]+)").matcher(json);
        return m.find() ? Double.parseDouble(m.group(1)) : 0.0;
    }
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n","\\n").replace("\r","");
    }

    // ── Shop & Inventory ─────────────────────────────────────────────────────

    private void handleShop(HttpExchange ex) throws IOException {
        setCors(ex);
        String method = ex.getRequestMethod();
        if ("OPTIONS".equals(method)) { send(ex, 204, new byte[0]); return; }

        Connection conn = config.DatabaseConfig.getInstance().getConnection();

        if ("GET".equals(method)) {
            // listeaza toate itemele din magazin
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM items ORDER BY category, price")) {
                StringBuilder sb = new StringBuilder("[");
                boolean first = true;
                while (rs.next()) {
                    if (!first) sb.append(","); first = false;
                    sb.append(String.format(
                        "{\"id\":%d,\"name\":\"%s\",\"price\":%d,\"description\":\"%s\",\"category\":\"%s\"}",
                        rs.getInt("id"), esc(rs.getString("name")), rs.getInt("price"),
                        esc(rs.getString("description")), esc(rs.getString("category"))));
                }
                sb.append("]");
                sendJson(ex, 200, sb.toString());
            } catch (SQLException e) {
                sendJson(ex, 500, "{\"error\":\"" + esc(e.getMessage()) + "\"}");
            }
        } else if ("POST".equals(method)) {
            // cumpara un item
            String body = body(ex);
            int trainerId = num(body, "trainerId");
            int itemId    = num(body, "itemId");
            int qty       = num(body, "quantity");
            if (qty <= 0) qty = 1;

            try {
                // obtine pretul
                PreparedStatement ps = conn.prepareStatement("SELECT price, name FROM items WHERE id = ?");
                ps.setInt(1, itemId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) { sendJson(ex, 404, "{\"error\":\"Item not found\"}"); return; }
                int price = rs.getInt("price");
                String itemName = rs.getString("name");
                int total = price * qty;

                // verifica banii
                Trainer t = trainerService.findById(trainerId).orElse(null);
                if (t == null) { sendJson(ex, 404, "{\"error\":\"Trainer not found\"}"); return; }
                if (t.getMoney() < total) {
                    sendJson(ex, 400, "{\"error\":\"Not enough money! Need " + total + " but have " + t.getMoney() + "\"}"); return;
                }

                // scade banii
                t.setMoney(t.getMoney() - total);
                trainerService.update(t);

                // adauga in inventar
                PreparedStatement upsert = conn.prepareStatement(
                    "INSERT INTO trainer_items (trainer_id, item_id, quantity) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE quantity = quantity + ?");
                upsert.setInt(1, trainerId);
                upsert.setInt(2, itemId);
                upsert.setInt(3, qty);
                upsert.setInt(4, qty);
                upsert.executeUpdate();

                audit.log("BUY_ITEM");
                sendJson(ex, 200, String.format(
                    "{\"ok\":true,\"item\":\"%s\",\"qty\":%d,\"spent\":%d,\"remaining\":%d}",
                    esc(itemName), qty, total, t.getMoney()));
            } catch (SQLException e) {
                sendJson(ex, 500, "{\"error\":\"" + esc(e.getMessage()) + "\"}");
            }
        } else {
            sendJson(ex, 405, "{\"error\":\"GET or POST only\"}");
        }
    }

    private void handleInventory(HttpExchange ex) throws IOException {
        setCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) { send(ex, 204, new byte[0]); return; }
        if (!"GET".equals(ex.getRequestMethod())) {
            sendJson(ex, 405, "{\"error\":\"GET only\"}"); return;
        }

        int trainerId = tailId(ex.getRequestURI().getPath(), "/api/inventory/");
        if (trainerId <= 0) { sendJson(ex, 400, "{\"error\":\"missing trainer id\"}"); return; }

        Connection conn = config.DatabaseConfig.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT i.id, i.name, i.price, i.description, i.category, ti.quantity " +
                "FROM trainer_items ti JOIN items i ON ti.item_id = i.id " +
                "WHERE ti.trainer_id = ? AND ti.quantity > 0 ORDER BY i.category, i.name")) {
            ps.setInt(1, trainerId);
            ResultSet rs = ps.executeQuery();
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) sb.append(","); first = false;
                sb.append(String.format(
                    "{\"id\":%d,\"name\":\"%s\",\"price\":%d,\"description\":\"%s\"," +
                    "\"category\":\"%s\",\"quantity\":%d}",
                    rs.getInt("id"), esc(rs.getString("name")), rs.getInt("price"),
                    esc(rs.getString("description")), esc(rs.getString("category")),
                    rs.getInt("quantity")));
            }
            sb.append("]");
            sendJson(ex, 200, sb.toString());
        } catch (SQLException e) {
            sendJson(ex, 500, "{\"error\":\"" + esc(e.getMessage()) + "\"}");
        }
    }
}

