package service;

import battle.BattleManager;
import entities.*;
import enums.CreatureType;
import enums.StatusEffect;
import items.Item;
import items.Pokeball;

import java.util.*;
import java.util.stream.Collectors;

public class GameService {

    private List<Trainer> trainerRanking;
    private Map<String, WildCreature> wildCreatureRegistry;

    public GameService() {
        this.trainerRanking = new ArrayList<>();
        this.wildCreatureRegistry = new HashMap<>();
    }

    // inregistrare trainer
    public void registerTrainer(Trainer trainer) {
        boolean exists = trainerRanking.stream()
                .anyMatch(t -> t.getName().equalsIgnoreCase(trainer.getName()));
        if (!exists) {
            trainerRanking.add(trainer);
            System.out.printf("[SERVICE] Trainer '%s' registered.%n", trainer.getName());
        } else {
            System.out.printf("[SERVICE] Trainer '%s' is already registered.%n", trainer.getName());
        }
    }

    // adaugare creatura in echipa unui trainer
    public boolean addCreatureToTrainer(Trainer trainer, TrainerCreature creature) {
        return trainer.addToParty(creature);
    }

    // afisare toate creaturile unui trainer, sortate dupa nivel
    public void printCreaturesSortedByLevel(Trainer trainer) {
        System.out.printf("%n[SERVICE] %s's creatures (sorted by level):%n", trainer.getName());
        TreeSet<TrainerCreature> sorted = new TreeSet<>(trainer.getParty());
        
        int i = 1;
        for (TrainerCreature c : sorted) {
            System.out.printf("  %d. %s%n", i++, c);
        }
    }

    // cautare creatura dupa nume
    public TrainerCreature findCreatureByName(String name) {
        System.out.printf("%n[SERVICE] Searching for creature: '%s'...%n", name);
        for (Trainer t : trainerRanking) {
            for (TrainerCreature c : t.getParty()) {
                if (c.getName().equalsIgnoreCase(name) ||
                        (c.getNickname() != null && c.getNickname().equalsIgnoreCase(name))) {
                    System.out.printf("  -> Found at trainer '%s': %s%n", t.getName(), c);
                    return c;
                }
            }
        }
        System.out.println("  -> Not found.");
        return null;
    }

    // cautare trainer dupa nume
    public Trainer findTrainer(String name) {
        for (Trainer t : trainerRanking) {
            if (t.getName().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return null;
    }

    // filtrare creaturi dupa tip
    public List<TrainerCreature> filterByType(CreatureType type) {
        System.out.printf("%n[SERVICE] Creatures of type %s:%n", type);
        List<TrainerCreature> result = trainerRanking.stream()
                .flatMap(t -> t.getParty().stream())
                .filter(c -> c.getType() == type)
                .collect(Collectors.toList());
        result.forEach(c -> System.out.println("  -> " + c));
        if (result.isEmpty())
            System.out.println("  (none)");
        return result;
    }

    // lupta completa trainer vs trainer
    public String conductFullBattle(Trainer player, Trainer rival) {
        System.out.println("\n[SERVICE] Full battle...");
        BattleManager bm = new BattleManager(player, rival);
        bm.startBattle();
        int turn = 0;
        while (!bm.isBattleOver() && turn < 20) {
            bm.executeTurn(0); // simplu: mereu prima mutare
            turn++;
        }
        return bm.getResult();
    }

    // folosire item din inventar
    public boolean useItemOnCreature(Trainer trainer, String itemName, Creature target) {
        System.out.printf("%n[SERVICE] %s uses %s on %s...%n",
                trainer.getName(), itemName, target.getName());
        return trainer.useItem(itemName, target);
    }

    // vindecare echipa completa
    public void healTeam(Trainer trainer) {
        System.out.printf("%n[SERVICE] Healing team '%s'...%n", trainer.getName());
        trainer.healAllCreatures();
        trainer.printParty();
    }

    // clasamentul trainerilor
    public void printRanking() {
        System.out.println("\n[SERVICE] == TRAINER RANKINGS ==");
        trainerRanking.sort(Comparator.comparing(Trainer::getName));
        int rank = 1;
        for (Trainer t : trainerRanking) {
            System.out.printf("  %2d. %-15s | Creatures: %d%n",
                    rank++, t.getName(), t.getParty().size());
        }
    }

    // registru creaturi salbatice
    public void registerWildCreature(WildCreature wc) {
        wildCreatureRegistry.put(wc.getName().toLowerCase(), wc);
        System.out.printf("[SERVICE] Wild creature '%s' registered.%n", wc.getName());
    }

    // returns a random wild creature from the registry (full HP reset)
    public WildCreature getRandomWildCreature() {
        if (wildCreatureRegistry.isEmpty()) return null;
        List<WildCreature> pool = new ArrayList<>(wildCreatureRegistry.values());
        WildCreature chosen = pool.get(new Random().nextInt(pool.size()));
        chosen.heal(chosen.getMaxHp()); // reset HP for a fresh encounter
        return chosen;
    }

    // wild pokemon encounter
    public void encounterWildCreature(Trainer trainer, WildCreature wild, Scanner scanner) {
        System.out.printf("%n[ENCOUNTER] A wild %s appeared!%n", wild.getDescription());
        System.out.printf("  %s%n", wild);

        TrainerCreature active = trainer.getActiveCreature();
        if (active == null) {
            System.out.println("  -> You have no healthy creatures to fight with!");
            return;
        }
        System.out.printf("  -> %s sends out %s!%n%n", trainer.getName(), active.getNickname());

        BattleManager bm = new BattleManager(trainer, wild);
        bm.startBattle();

        while (!bm.isBattleOver() && wild.isAlive() && !wild.isCaught()) {
            System.out.printf("  [Wild] %s%n", wild);
            System.out.printf("  [Your] %s%n", active);
            System.out.println("    1. Attack");
            System.out.println("    2. Throw Pokeball");
            System.out.println("    3. Flee");
            System.out.print("  Choice: ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    bm.executeTurn(0);
                    // dupa atac wild-ul poate incerca sa fuga
                    if (!bm.isBattleOver() && wild.isAlive() && wild.tryFlee()) {
                        System.out.printf("  -> The wild %s fled!%n", wild.getName());
                    }
                    break;

                case "2":
                    // incerc sa folosesc pokeball daca am 
                    Pokeball ball = null;
                    for (Map.Entry<String, Integer> e : trainer.getBag().entrySet()) {
                        Item itm = trainer.getItemObjects().stream()
                                .filter(i -> i.getName().equals(e.getKey()))
                                .findFirst().orElse(null);
                        if (itm instanceof Pokeball && e.getValue() > 0) {
                            ball = (Pokeball) itm;
                            break;
                        }
                    }
                    if (ball == null) {
                        System.out.println("  -> You have no Pokeballs left!");
                        break;
                    }
                    // scadem din bag
                    trainer.getBag().merge(ball.getName(), -1, Integer::sum);
                    if (trainer.getBag().getOrDefault(ball.getName(), 0) <= 0)
                        trainer.getBag().remove(ball.getName());

                    if (bm.tryCatch(ball)) {
                        TrainerCreature captured = new TrainerCreature(
                                wild.getName(), null,
                                wild.getMaxHp(), wild.getAttack(), wild.getDefense(),
                                wild.getSpeed(), wild.getLevel(), wild.getType());
                        captured.heal(wild.getHp());
                        if (addCreatureToTrainer(trainer, captured))
                            System.out.printf("  -> %s was added to %s's team!%n", wild.getName(), trainer.getName());
                        else
                            System.out.printf("  -> Team is full; %s was released.%n", wild.getName());
                    }
                    break;

                case "3":
                    // daca nu mi iese flee ul are avantaj (atac in plus) wild creature ul
                    if (!bm.tryFlee()) {
                        int rawDmg = Math.max(1, wild.getAttack() - active.getDefense());
                        active.takeDamage(rawDmg);
                        System.out.printf("  -> The wild %s attacks back! %s takes %d damage.%n",
                                wild.getName(), active.getNickname(), rawDmg);
                    }
                    break;

                default:
                    System.out.println("  Invalid choice, please enter 1, 2 or 3.");
            }
        }

        System.out.printf("[ENCOUNTER] Ended. Result: %s%n",
                bm.getResult().isEmpty() ? "Fainted/Fled" : bm.getResult());
    }

}
