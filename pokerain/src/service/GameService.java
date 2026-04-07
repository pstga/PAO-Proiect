package service;

import battle.BattleManager;
import battle.TypeChart;
import entities.*;
import enums.CreatureType;
import items.Item;
import items.Pokeball;
import items.Potion;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Clasa serviciu centrală — expune toate cele 10 operații ale sistemului.
 */
public class GameService {

    // ── Colecție 1: TreeSet cu traineri, sortați după badge-uri (descrescător) ──
    private TreeSet<Trainer> trainerRanking;

    // ── Colecție 2: HashMap creature_name → WildCreature (acces rapid) ─────────
    private Map<String, WildCreature> wildCreatureRegistry;

    public GameService() {
        // Comparator: mai multe badge-uri = rang mai bun; egal → după nume
        this.trainerRanking = new TreeSet<>(
                Comparator.comparingInt(Trainer::getBadgeCount).reversed()
                        .thenComparing(Trainer::getName)
        );
        this.wildCreatureRegistry = new HashMap<>();
    }

    // ════════════════════════════════════════════════════════════════════════
    // 1. Înregistrare trainer
    // ════════════════════════════════════════════════════════════════════════
    public void registerTrainer(Trainer trainer) {
        trainerRanking.add(trainer);
        System.out.printf("[SERVICE] Trainer '%s' înregistrat.%n", trainer.getName());
    }

    // ════════════════════════════════════════════════════════════════════════
    // 2. Adăugare creatură în echipa unui trainer
    // ════════════════════════════════════════════════════════════════════════
    public boolean addCreatureToTrainer(Trainer trainer, TrainerCreature creature) {
        boolean ok = trainer.addToParty(creature);
        if (ok) refreshTrainer(trainer);
        return ok;
    }

    // ════════════════════════════════════════════════════════════════════════
    // 3. Afișare toate creaturile unui trainer, sortate după nivel (TreeSet)
    // ════════════════════════════════════════════════════════════════════════
    public void printCreaturesSortedByLevel(Trainer trainer) {
        System.out.printf("%n[SERVICE] Creaturile lui %s (sortate după nivel):%n", trainer.getName());
        TreeSet<TrainerCreature> sorted = new TreeSet<>(trainer.getParty());
        int i = 1;
        for (TrainerCreature c : sorted) {
            System.out.printf("  %d. %s%n", i++, c);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // 4. Căutare creatură după nume (în toți trainerii înregistrați)
    // ════════════════════════════════════════════════════════════════════════
    public Optional<TrainerCreature> findCreatureByName(String name) {
        System.out.printf("%n[SERVICE] Căutare creatură: '%s'...%n", name);
        for (Trainer t : trainerRanking) {
            Optional<TrainerCreature> found = t.getParty().stream()
                    .filter(c -> c.getName().equalsIgnoreCase(name)
                              || c.getNickname().equalsIgnoreCase(name))
                    .findFirst();
            if (found.isPresent()) {
                System.out.printf("  → Găsit la trainer '%s': %s%n", t.getName(), found.get());
                return found;
            }
        }
        System.out.println("  → Nu a fost găsit.");
        return Optional.empty();
    }

    // ════════════════════════════════════════════════════════════════════════
    // 5. Filtrare creaturi după tip (toate creaturile din toți trainerii)
    // ════════════════════════════════════════════════════════════════════════
    public List<TrainerCreature> filterByType(CreatureType type) {
        System.out.printf("%n[SERVICE] Creaturi de tip %s:%n", type);
        List<TrainerCreature> result = trainerRanking.stream()
                .flatMap(t -> t.getParty().stream())
                .filter(c -> c.getType() == type)
                .collect(Collectors.toList());
        result.forEach(c -> System.out.println("  → " + c));
        if (result.isEmpty()) System.out.println("  (niciuna)");
        return result;
    }

    // ════════════════════════════════════════════════════════════════════════
    // 6. Aplicarea unui atac simulat (un singur tur de luptă)
    // ════════════════════════════════════════════════════════════════════════
    public void executeBattleTurn(Trainer player, Object opponent, int moveIndex) {
        System.out.println("\n[SERVICE] Executare tur de luptă...");
        BattleManager bm = new BattleManager(player, opponent);
        bm.startBattle();
        bm.executeTurn(moveIndex);
        if (bm.isBattleOver()) {
            System.out.printf("  → Rezultat: %s%n", bm.getResult());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // 7. Luptă completă trainer vs trainer
    // ════════════════════════════════════════════════════════════════════════
    public String conductFullBattle(Trainer player, Trainer rival) {
        System.out.println("\n[SERVICE] Luptă completă...");
        BattleManager bm = new BattleManager(player, rival);
        bm.startBattle();
        int turn = 0;
        while (!bm.isBattleOver() && turn < 20) {
            bm.executeTurn(0); // simplu: mereu prima mutare
            turn++;
        }
        String result = bm.getResult();
        if ("WIN".equals(result)) {
            player.addBadge("Badge de la " + rival.getName());
            refreshTrainer(player);
            refreshTrainer(rival);
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════════════
    // 8. Folosire item din inventar
    // ════════════════════════════════════════════════════════════════════════
    public boolean useItemOnCreature(Trainer trainer, String itemName, Creature target) {
        System.out.printf("%n[SERVICE] %s folosește %s pe %s...%n",
                trainer.getName(), itemName, target.getName());
        return trainer.useItem(itemName, target);
    }

    // ════════════════════════════════════════════════════════════════════════
    // 9. Vindecare echipă completă
    // ════════════════════════════════════════════════════════════════════════
    public void healTeam(Trainer trainer) {
        System.out.printf("%n[SERVICE] Vindecare echipă '%s'...%n", trainer.getName());
        trainer.healAllCreatures();
        trainer.printParty();
    }

    // ════════════════════════════════════════════════════════════════════════
    // 10. Clasamentul trainerilor după badge-uri
    // ════════════════════════════════════════════════════════════════════════
    public void printRanking() {
        System.out.println("\n[SERVICE] ══ CLASAMENT TRAINERI ══");
        int rank = 1;
        for (Trainer t : trainerRanking) {
            System.out.printf("  %2d. %-15s | Badge-uri: %d | Creaturi: %d%n",
                    rank++, t.getName(), t.getBadgeCount(), t.getParty().size());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // BONUS: Registru creaturi sălbatice
    // ════════════════════════════════════════════════════════════════════════
    public void registerWildCreature(WildCreature wc) {
        wildCreatureRegistry.put(wc.getName().toLowerCase(), wc);
        System.out.printf("[SERVICE] Creatură sălbatică '%s' înregistrată în registru.%n", wc.getName());
    }

    public void printWildRegistry() {
        System.out.println("\n[SERVICE] ══ REGISTRU CREATURI SĂLBATICE ══");
        wildCreatureRegistry.values().forEach(wc -> System.out.println("  " + wc));
    }

    // ── Helper intern: TreeSet nu se auto-actualizează la modificări ──────────
    private void refreshTrainer(Trainer trainer) {
        trainerRanking.remove(trainer);
        trainerRanking.add(trainer);
    }
}
