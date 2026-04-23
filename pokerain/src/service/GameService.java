package service;

import battle.BattleManager;
import entities.*;
import enums.CreatureType;

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
        if (result.isEmpty()) System.out.println("  (none)");
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

    public void printWildRegistry() {
        System.out.println("\n[SERVICE] == WILD CREATURE REGISTRY ==");
        wildCreatureRegistry.values().forEach(wc -> System.out.println("  " + wc));
    }

}
