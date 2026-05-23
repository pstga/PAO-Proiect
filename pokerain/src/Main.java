import battle.BattleManager;
import config.DatabaseConfig;
import config.DatabaseInitializer;
import entities.*;
import enums.CreatureType;
import enums.MoveCategory;
import enums.StatusEffect;
import items.Pokeball;
import items.Potion;
import service.*;

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    // ────────────────────────────────────────────────────────────────────────
    //  Servicii singleton
    // ────────────────────────────────────────────────────────────────────────
    private static GameService    gameService;
    private static TrainerService trainerService;
    private static MoveService    moveService;
    private static CreatureService creatureService;
    private static AuditService   auditService;

    public static void main(String[] args) {

        System.out.println("===========================================");
        System.out.println("|           POKERAIN - Pokemon Game        |");
        System.out.println("===========================================");

        // ── Initializare baza de date ────────────────────────────────────────
        try {
            DatabaseInitializer.initialize();
        } catch (Exception e) {
            System.err.println("[WARN] Nu s-a putut conecta la MySQL: " + e.getMessage());
            System.err.println("[WARN] Aplicatia ruleaza DOAR in modul in-memory.");
        }

        // ── Instantiere servicii ─────────────────────────────────────────────
        auditService    = AuditService.getInstance();
        gameService     = new GameService();
        trainerService  = TrainerService.getInstance();
        moveService     = MoveService.getInstance();
        creatureService = CreatureService.getInstance();

        // ── Date initiale (in-memory + persistate in DB) ─────────────────────
        Move focBlast    = new Move("FireBall",      90, 85,  5, CreatureType.FIRE,     MoveCategory.SPECIAL,   StatusEffect.BURN);
        Move hydropump   = new Move("Hydropump",     95, 80,  5, CreatureType.WATER,    MoveCategory.SPECIAL,   StatusEffect.NONE);
        Move thunderbolt = new Move("Thunderbolt",   80, 100, 15, CreatureType.ELECTRIC, MoveCategory.SPECIAL,  StatusEffect.PARALYSIS);
        Move razorLeaf   = new Move("Razor Leaf",    55, 95,  25, CreatureType.GRASS,    MoveCategory.PHYSICAL,  StatusEffect.NONE);
        Move tackle      = new Move("Tackle",         40, 100, 35, CreatureType.NORMAL,  MoveCategory.PHYSICAL,  StatusEffect.NONE);
        Move iceBeam     = new Move("Ice Beam",       90, 100, 10, CreatureType.ICE,     MoveCategory.SPECIAL,   StatusEffect.SLEEP);
        Move poisonSting = new Move("Poison Sting",   15, 100, 35, CreatureType.POISON,  MoveCategory.PHYSICAL,  StatusEffect.POISON);
        Move psychic     = new Move("Brain damage",   90, 100, 10, CreatureType.PSYCHIC, MoveCategory.SPECIAL,   StatusEffect.NONE);

        // Salvam datele initiale in DB DOAR daca nu exista deja (prima rulare)
        boolean dbEmpty = true;
        try {
            dbEmpty = trainerService.findAll().isEmpty();
        } catch (Exception e) {
            dbEmpty = false; // DB indisponibil, lucram in-memory
        }

        if (dbEmpty) {
            try {
                moveService.save(focBlast); moveService.save(hydropump);
                moveService.save(thunderbolt); moveService.save(razorLeaf);
                moveService.save(tackle); moveService.save(iceBeam);
                moveService.save(poisonSting); moveService.save(psychic);
            } catch (Exception e) {
                System.err.println("[WARN] Mutarile nu au putut fi salvate in DB: " + e.getMessage());
            }
        } else {
            System.out.println("[DB] Date initiale deja existente in DB, skip seeding.");
        }


        TrainerCreature charmander = new TrainerCreature("Charmander", "Piscotel", 78, 52, 43, 65, 10, CreatureType.FIRE);
        charmander.addMove(focBlast); charmander.addMove(tackle);

        TrainerCreature squirtle = new TrainerCreature("Squirtle", "Mimi", 88, 48, 65, 43, 8, CreatureType.WATER);
        squirtle.addMove(hydropump); squirtle.addMove(tackle);

        TrainerCreature bulbasaur = new TrainerCreature("Bulbasaur", null, 90, 49, 49, 45, 9, CreatureType.GRASS);
        bulbasaur.addMove(razorLeaf); bulbasaur.addMove(poisonSting);

        TrainerCreature pikachu = new TrainerCreature("Pikachu", "Chuu", 70, 55, 40, 90, 12, CreatureType.ELECTRIC);
        pikachu.addMove(thunderbolt); pikachu.addMove(tackle);

        TrainerCreature jynx = new TrainerCreature("Jynx", null, 95, 50, 35, 50, 7, CreatureType.ICE);
        jynx.addMove(iceBeam);

        TrainerCreature alakazam = new TrainerCreature("Alakazam", "Pluaie", 65, 50, 45, 120, 15, CreatureType.PSYCHIC);
        alakazam.addMove(psychic); alakazam.addMove(tackle);

        Trainer ash   = new Trainer("Ash",   3000);
        Trainer misty = new Trainer("Misty", 2500);
        Trainer brock = new Trainer("Brock", 2200);

        gameService.registerTrainer(ash);
        gameService.registerTrainer(misty);
        gameService.registerTrainer(brock);

        // Mereu adaugam creaturile in echipa in-memory
        gameService.addCreatureToTrainer(ash,   charmander);
        gameService.addCreatureToTrainer(ash,   pikachu);
        gameService.addCreatureToTrainer(ash,   bulbasaur);
        gameService.addCreatureToTrainer(misty, squirtle);
        gameService.addCreatureToTrainer(misty, jynx);
        gameService.addCreatureToTrainer(brock, alakazam);

        // Salvam trainerii si creaturile in DB DOAR la prima rulare
        if (dbEmpty) {
            try {
                trainerService.save(ash);
                trainerService.save(misty);
                trainerService.save(brock);

                creatureService.saveTrainerCreature(charmander, ash.getId());
                creatureService.saveTrainerCreature(pikachu,    ash.getId());
                creatureService.saveTrainerCreature(bulbasaur,  ash.getId());
                creatureService.saveTrainerCreature(squirtle,   misty.getId());
                creatureService.saveTrainerCreature(jynx,       misty.getId());
                creatureService.saveTrainerCreature(alakazam,   brock.getId());
            } catch (Exception e) {
                System.err.println("[WARN] Date initiale nu au putut fi salvate complet in DB: " + e.getMessage());
            }
        }


        Potion superPotion = new Potion("Super Potion", 60,  false, 700);
        Potion fullRestore = new Potion("Full Restore",  999, true,  3000);
        Pokeball pokeball  = new Pokeball("Poke Ball",  200);
        Pokeball greatBall = new Pokeball("Great Ball",  600);

        ash.addItem(superPotion, 5);
        ash.addItem(fullRestore, 2);
        ash.addItem(pokeball,   10);
        ash.addItem(greatBall,   3);

        WildCreature wildEevee   = new WildCreature("Eevee",    65, 45, 40, 55, 6, CreatureType.NORMAL, 0.70);
        WildCreature wildMagikarp= new WildCreature("Magikarp", 30, 10, 30, 80, 3, CreatureType.WATER,  0.95);

        gameService.registerWildCreature(wildEevee);
        gameService.registerWildCreature(wildMagikarp);

        if (dbEmpty) {
            try {
                creatureService.saveWildCreature(wildEevee);
                creatureService.saveWildCreature(wildMagikarp);
            } catch (Exception e) {
                System.err.println("[WARN] Wild creatures nu au putut fi salvate in DB: " + e.getMessage());
            }
        }

        // ── Pornire server web ────────────────────────────────────────────
        try {
            new server.PokerainServer().start();
        } catch (Exception e) {
            System.err.println("[SERVER] Nu s-a putut porni serverul web: " + e.getMessage());
        }

        Scanner scanner = new Scanner(System.in);

        boolean running = true;

        while (running) {
            printMainMenu();
            String choiceStr = scanner.nextLine();
            int choice = -1;
            try {
                choice = Integer.parseInt(choiceStr.trim());
            } catch (NumberFormatException e) {
                System.out.println("Input invalid. Introduceti un numar.");
                continue;
            }

            switch (choice) {

                // ── JOC ─────────────────────────────────────────────────────
                case 1:
                    System.out.println("\n== Trainer Rankings ==");
                    gameService.printRanking();
                    auditService.log("PRINT_RANKING");
                    break;

                case 2:
                    System.out.println("\n== Sort Creatures by Level ==");
                    System.out.print("Trainer name: ");
                    Trainer t2 = gameService.findTrainer(scanner.nextLine());
                    if (t2 != null) gameService.printCreaturesSortedByLevel(t2);
                    else            System.out.println("Trainer negasit!");
                    break;

                case 3:
                    System.out.print("Creature name: ");
                    gameService.findCreatureByName(scanner.nextLine());
                    break;

                case 4:
                    System.out.print("Tip creatura (ex: FIRE, WATER, ELECTRIC): ");
                    try {
                        CreatureType type = CreatureType.valueOf(scanner.nextLine().toUpperCase());
                        gameService.filterByType(type);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Tip invalid!");
                    }
                    break;

                case 5:
                    System.out.print("Trainer name: ");
                    Trainer t5 = gameService.findTrainer(scanner.nextLine());
                    if (t5 == null) { System.out.println("Trainer negasit!"); break; }
                    t5.printBag();
                    System.out.print("Item de folosit (sau Enter pentru cancel): ");
                    String itemName = scanner.nextLine();
                    if (!itemName.trim().isEmpty()) {
                        System.out.print("Creatura tinta: ");
                        String cn = scanner.nextLine();
                        Creature target = null;
                        for (Creature c : t5.getParty()) {
                            if (c.getName().equalsIgnoreCase(cn) ||
                                (c instanceof TrainerCreature &&
                                 ((TrainerCreature)c).getNickname() != null &&
                                 ((TrainerCreature)c).getNickname().equalsIgnoreCase(cn))) {
                                target = c; break;
                            }
                        }
                        if (target != null) gameService.useItemOnCreature(t5, itemName, target);
                        else System.out.println("Creatura negasita in echipa!");
                    }
                    break;

                case 6:
                    System.out.println("\n== Battle ==");
                    System.out.print("Trainer 1: ");
                    Trainer p1 = gameService.findTrainer(scanner.nextLine());
                    System.out.print("Trainer 2: ");
                    Trainer p2 = gameService.findTrainer(scanner.nextLine());
                    if (p1 != null && p2 != null) {
                        System.out.println("  -> Final: " + gameService.conductFullBattle(p1, p2));
                    } else {
                        System.out.println("Unul sau ambii traineri nu exista!");
                    }
                    break;

                case 7:
                    System.out.print("Trainer name: ");
                    Trainer t7 = gameService.findTrainer(scanner.nextLine());
                    if (t7 != null) gameService.healTeam(t7);
                    else            System.out.println("Trainer negasit!");
                    break;

                case 8:
                    System.out.println("\n== Wild Encounter ==");
                    System.out.print("Trainer name: ");
                    Trainer trainerWild = gameService.findTrainer(scanner.nextLine());
                    if (trainerWild == null) { System.out.println("Trainer negasit!"); break; }
                    WildCreature randomWild = gameService.getRandomWildCreature();
                    if (randomWild == null) { System.out.println("Nicio creatura salbatica inregistrata!"); break; }
                    gameService.encounterWildCreature(trainerWild, randomWild, scanner);
                    break;

                // ── MANAGEMENT DB ────────────────────────────────────────────
                case 9:
                    manageTrainersMenu(scanner);
                    break;

                case 10:
                    manageMovesMenu(scanner);
                    break;

                case 11:
                    manageCreaturesMenu(scanner);
                    break;

                case 12:
                    viewAuditLog();
                    break;

                case 0:
                    running = false;
                    System.out.println("Pa pa! La revedere!");
                    DatabaseConfig.getInstance().closeConnection();
                    break;

                default:
                    System.out.println("Optiune invalida!");
            }
        }
        scanner.close();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MENIU PRINCIPAL
    // ════════════════════════════════════════════════════════════════════════

    private static void printMainMenu() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║         POKERAIN  –  MENIU           ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.println("║  JOC                                 ║");
        System.out.println("║  1. Clasament Traineri               ║");
        System.out.println("║  2. Creaturi sortate dupa nivel      ║");
        System.out.println("║  3. Cauta creatura dupa nume         ║");
        System.out.println("║  4. Filtrare creaturi dupa tip       ║");
        System.out.println("║  5. Inventar & folosire item         ║");
        System.out.println("║  6. Lupta Trainer vs Trainer         ║");
        System.out.println("║  7. Vindeca echipa                   ║");
        System.out.println("║  8. Intalnire creatura salbatica     ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.println("║  BAZA DE DATE (CRUD)                 ║");
        System.out.println("║  9.  Gestionare Traineri             ║");
        System.out.println("║  10. Gestionare Mutari               ║");
        System.out.println("║  11. Gestionare Creaturi             ║");
        System.out.println("║  12. Vezi Audit Log                  ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.println("║  0. Iesire                           ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.print("Alegere: ");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SUBMENIU: TRAINERI
    // ════════════════════════════════════════════════════════════════════════

    private static void manageTrainersMenu(Scanner sc) {
        System.out.println("\n--- Gestionare Traineri ---");
        System.out.println("1. Adauga trainer nou");
        System.out.println("2. Lista toti trainerii (din DB)");
        System.out.println("3. Cauta trainer dupa ID");
        System.out.println("4. Actualizeaza trainer (money)");
        System.out.println("5. Sterge trainer dupa ID");
        System.out.println("0. Inapoi");
        System.out.print("Alegere: ");

        try {
            int ch = Integer.parseInt(sc.nextLine().trim());
            switch (ch) {
                case 1:
                    System.out.print("Nume trainer: ");
                    String tName = sc.nextLine();
                    System.out.print("Bani: ");
                    int money = Integer.parseInt(sc.nextLine().trim());
                    Trainer newT = new Trainer(tName, money);
                    trainerService.save(newT);
                    gameService.registerTrainer(newT);
                    break;
                case 2:
                    List<Trainer> all = trainerService.findAll();
                    System.out.printf("%-4s %-15s %s%n", "ID", "Nume", "Bani");
                    all.forEach(t -> System.out.printf("%-4d %-15s %d¥%n",
                            t.getId(), t.getName(), t.getMoney()));
                    break;
                case 3:
                    System.out.print("ID trainer: ");
                    int tid = Integer.parseInt(sc.nextLine().trim());
                    trainerService.findById(tid).ifPresentOrElse(
                        t -> System.out.println("  -> " + t),
                        () -> System.out.println("  -> Negasit.")
                    );
                    break;
                case 4:
                    System.out.print("ID trainer de actualizat: ");
                    int uid = Integer.parseInt(sc.nextLine().trim());
                    Optional<Trainer> opt = trainerService.findById(uid);
                    if (opt.isEmpty()) { System.out.println("Negasit!"); break; }
                    Trainer toUpdate = opt.get();
                    System.out.print("Bani noi: ");
                    toUpdate.addMoney(Integer.parseInt(sc.nextLine().trim()) - toUpdate.getMoney());
                    trainerService.update(toUpdate);
                    break;
                case 5:
                    System.out.print("ID trainer de sters: ");
                    trainerService.delete(Integer.parseInt(sc.nextLine().trim()));
                    break;
                case 0: break;
                default: System.out.println("Optiune invalida!");
            }
        } catch (NumberFormatException e) {
            System.out.println("Input invalid.");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SUBMENIU: MUTARI
    // ════════════════════════════════════════════════════════════════════════

    private static void manageMovesMenu(Scanner sc) {
        System.out.println("\n--- Gestionare Mutari ---");
        System.out.println("1. Adauga mutare noua");
        System.out.println("2. Lista toate mutarile (din DB)");
        System.out.println("3. Cauta mutare dupa ID");
        System.out.println("4. Filtrare mutari dupa tip");
        System.out.println("5. Sterge mutare dupa ID");
        System.out.println("0. Inapoi");
        System.out.print("Alegere: ");

        try {
            int ch = Integer.parseInt(sc.nextLine().trim());
            switch (ch) {
                case 1:
                    System.out.print("Nume mutare: ");
                    String mName = sc.nextLine();
                    System.out.print("Power (0-250): ");    int power    = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("Accuracy (0-100): "); int accuracy = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("PP: ");               int pp       = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("Tip (ex: FIRE): ");   CreatureType mType = CreatureType.valueOf(sc.nextLine().toUpperCase());
                    System.out.print("Categorie (PHYSICAL/SPECIAL/STATUS): ");
                    MoveCategory cat = MoveCategory.valueOf(sc.nextLine().toUpperCase());
                    System.out.print("Side effect (NONE/BURN/POISON/PARALYSIS/SLEEP): ");
                    StatusEffect se = StatusEffect.valueOf(sc.nextLine().toUpperCase());
                    moveService.save(new Move(mName, power, accuracy, pp, mType, cat, se));
                    break;
                case 2:
                    List<Move> allMoves = moveService.findAll();
                    allMoves.forEach(m ->
                        System.out.printf("[id=%-3d] %s%n", m.getId(), m));
                    break;
                case 3:
                    System.out.print("ID mutare: ");
                    moveService.findById(Integer.parseInt(sc.nextLine().trim())).ifPresentOrElse(
                        m -> System.out.println("  -> " + m),
                        () -> System.out.println("  -> Negasita.")
                    );
                    break;
                case 4:
                    System.out.print("Tip (ex: WATER): ");
                    try {
                        CreatureType ft = CreatureType.valueOf(sc.nextLine().toUpperCase());
                        moveService.findByType(ft).forEach(m ->
                            System.out.printf("[id=%-3d] %s%n", m.getId(), m));
                    } catch (IllegalArgumentException e) {
                        System.out.println("Tip invalid!");
                    }
                    break;
                case 5:
                    System.out.print("ID mutare de sters: ");
                    moveService.delete(Integer.parseInt(sc.nextLine().trim()));
                    break;
                case 0: break;
                default: System.out.println("Optiune invalida!");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Input invalid: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SUBMENIU: CREATURI
    // ════════════════════════════════════════════════════════════════════════

    private static void manageCreaturesMenu(Scanner sc) {
        System.out.println("\n--- Gestionare Creaturi ---");
        System.out.println("1. Lista creaturi trainer (din DB)");
        System.out.println("2. Creaturi ale unui trainer (dupa trainer ID)");
        System.out.println("3. Adauga creatura salbatica noua");
        System.out.println("4. Lista creaturi salbatice (din DB)");
        System.out.println("5. Filtrare creaturi salbatice dupa catch rate");
        System.out.println("6. Sterge creatura trainer dupa ID");
        System.out.println("7. Sterge creatura salbatica dupa ID");
        System.out.println("0. Inapoi");
        System.out.print("Alegere: ");

        try {
            int ch = Integer.parseInt(sc.nextLine().trim());
            switch (ch) {
                case 1:
                    creatureService.findAllTrainerCreatures().forEach(c ->
                        System.out.printf("[id=%-3d] %s%n", c.getId(), c));
                    break;
                case 2:
                    System.out.print("Trainer ID: ");
                    int tid = Integer.parseInt(sc.nextLine().trim());
                    creatureService.findCreaturesByTrainerId(tid).forEach(c ->
                        System.out.printf("[id=%-3d] %s%n", c.getId(), c));
                    break;
                case 3:
                    System.out.print("Nume: ");         String wName = sc.nextLine();
                    System.out.print("Max HP: ");       int wHp   = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("Attack: ");       int wAtk  = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("Defense: ");      int wDef  = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("Speed: ");        int wSpd  = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("Level: ");        int wLvl  = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("Tip (ex: FIRE): "); CreatureType wType = CreatureType.valueOf(sc.nextLine().toUpperCase());
                    System.out.print("Catch rate (0.0-1.0): "); double wCr = Double.parseDouble(sc.nextLine().trim());
                    WildCreature newWild = new WildCreature(wName, wHp, wAtk, wDef, wSpd, wLvl, wType, wCr);
                    creatureService.saveWildCreature(newWild);
                    gameService.registerWildCreature(newWild);
                    break;
                case 4:
                    creatureService.findAllWildCreatures().forEach(w ->
                        System.out.printf("[id=%-3d] %s%n", w.getId(), w));
                    break;
                case 5:
                    System.out.print("Catch rate minima (ex: 0.5): ");
                    double minCr = Double.parseDouble(sc.nextLine().trim());
                    creatureService.findWildCreaturesByCatchRate(minCr).forEach(w ->
                        System.out.printf("[id=%-3d] %s%n", w.getId(), w));
                    break;
                case 6:
                    System.out.print("ID creatura trainer de sters: ");
                    creatureService.deleteTrainerCreature(Integer.parseInt(sc.nextLine().trim()));
                    break;
                case 7:
                    System.out.print("ID creatura salbatica de sters: ");
                    creatureService.deleteWildCreature(Integer.parseInt(sc.nextLine().trim()));
                    break;
                case 0: break;
                default: System.out.println("Optiune invalida!");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Input invalid: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  VIZUALIZARE AUDIT
    // ════════════════════════════════════════════════════════════════════════

    private static void viewAuditLog() {
        System.out.println("\n== Audit Log (audit.csv) ==");
        System.out.printf("%-40s %s%n", "Actiune", "Timestamp");
        System.out.println("-".repeat(60));
        try (BufferedReader br = new BufferedReader(new FileReader("audit.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    System.out.printf("%-40s %s%n", parts[0], parts[1]);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("  (fisierul audit.csv nu exista inca)");
        } catch (IOException e) {
            System.err.println("Eroare la citirea auditului: " + e.getMessage());
        }
        auditService.log("VIEW_AUDIT_LOG");
    }
}
