import battle.BattleManager;
import entities.*;
import enums.CreatureType;
import enums.MoveCategory;
import enums.StatusEffect;
import items.Pokeball;
import items.Potion;
import service.GameService;

import java.util.Scanner;
// todo: mecanica de level up si de cumparat chestii !!
public class Main {

    public static void main(String[] args) {

        System.out.println("===========================================");
        System.out.println("|           POKERAIN - Pokemon Game         |");
        System.out.println("===========================================");

        GameService service = new GameService();

        // nu am inca baza de date; le voi simula acum aici adaugand cateva de test;
        // cand voi avea db o sa am optiunea de a adauga traineri si chestii in plus :) 

        // adaugam mutari
        Move focBlast = new Move("FireBall", 90, 85, 5, CreatureType.FIRE, MoveCategory.SPECIAL, StatusEffect.BURN);
        Move hydropump = new Move("Hydropump", 95, 80, 5, CreatureType.WATER, MoveCategory.SPECIAL, StatusEffect.NONE);
        Move thunderbolt = new Move("Thunderbolt", 80, 100, 15, CreatureType.ELECTRIC, MoveCategory.SPECIAL, StatusEffect.PARALYSIS);
        Move razorLeaf = new Move("Razor Leaf", 55, 95, 25, CreatureType.GRASS, MoveCategory.PHYSICAL, StatusEffect.NONE);
        Move tackle = new Move("Tackle", 40, 100, 35, CreatureType.NORMAL, MoveCategory.PHYSICAL, StatusEffect.NONE);
        Move iceBeam = new Move("Ice Beam", 90, 100, 10, CreatureType.ICE, MoveCategory.SPECIAL, StatusEffect.SLEEP);
        Move poisonSting  = new Move("Poison Sting", 15, 100, 35, CreatureType.POISON,  MoveCategory.PHYSICAL, StatusEffect.POISON);
        Move psychic = new Move("Brain damage", 90, 100, 10, CreatureType.PSYCHIC, MoveCategory.SPECIAL, StatusEffect.NONE);

        // adaugam pokemoni
        TrainerCreature charmander = new TrainerCreature("Charmander", "Piscotel", 78, 52, 43, 65, 10, CreatureType.FIRE);
        charmander.addMove(focBlast);
        charmander.addMove(tackle);

        TrainerCreature squirtle = new TrainerCreature("Squirtle", "Mimi", 88, 48, 65, 43, 8, CreatureType.WATER);
        squirtle.addMove(hydropump);
        squirtle.addMove(tackle);

        TrainerCreature bulbasaur = new TrainerCreature("Bulbasaur", null, 90, 49, 49, 45, 9, CreatureType.GRASS);
        bulbasaur.addMove(razorLeaf);
        bulbasaur.addMove(poisonSting);

        TrainerCreature pikachu = new TrainerCreature("Pikachu", "Chuu", 70, 55, 40, 90, 12, CreatureType.ELECTRIC);
        pikachu.addMove(thunderbolt);
        pikachu.addMove(tackle);

        TrainerCreature jynx = new TrainerCreature("Jynx", null, 95, 50, 35, 50, 7, CreatureType.ICE);
        jynx.addMove(iceBeam);

        TrainerCreature alakazam = new TrainerCreature("Alakazam", "Pluaie", 65, 50, 45, 120, 15, CreatureType.PSYCHIC);
        alakazam.addMove(psychic);
        alakazam.addMove(tackle);

        // traineri
        Trainer ash = new Trainer("Ash", 3000);
        Trainer misty = new Trainer("Misty", 2500);
        Trainer brock = new Trainer("Brock", 2200);

        service.registerTrainer(ash);
        service.registerTrainer(misty);
        service.registerTrainer(brock);

        // adaugam niste creaturi trainerilor, idk le impartim cum o fi acum
        service.addCreatureToTrainer(ash, charmander);
        service.addCreatureToTrainer(ash, pikachu);
        service.addCreatureToTrainer(ash, bulbasaur);
        service.addCreatureToTrainer(misty, squirtle);
        service.addCreatureToTrainer(misty, jynx);
        service.addCreatureToTrainer(brock, alakazam);

        // cateva iteme
        Potion superPotion = new Potion("Super Potion", 60, false, 700);
        Potion fullRestore = new Potion("Full Restore", 999, true, 3000);
        Pokeball pokeball = new Pokeball("Poke Ball", 200);
        Pokeball greatBall = new Pokeball("Great Ball", 600);

        ash.addItem(superPotion, 5);
        ash.addItem(fullRestore, 2);
        ash.addItem(pokeball, 10);
        ash.addItem(greatBall, 3);

        // bagam si niste wild creatures pentru a le PRINDEEEE
        WildCreature wildEevee = new WildCreature("Eevee", 65, 45, 40, 55, 6, CreatureType.NORMAL, 0.7);
        WildCreature wildMagikarp = new WildCreature("Magikarp", 30, 10, 30, 80, 3, CreatureType.WATER, 0.95);

        service.registerWildCreature(wildEevee);
        service.registerWildCreature(wildMagikarp);

        // test
        System.out.println("Game setup complete! Welcome to the interactive menu.");

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Print Trainer Rankings");             // ii luam in functie de bani
            System.out.println("2. Sort Trainer's Creatures by Level");  // team ul fiecarui trainer
            System.out.println("3. Find Creature by Name");              // cautam un pokemon si vedem daca e in echipa cuiva
            System.out.println("4. Filter Creatures by Type");           // cautam pokemonii dupa tip
            System.out.println("5. View Inventory & Use Item");          // pt traineri, use item
            System.out.println("6. Conduct Full Battle");                // simulam un fight
            System.out.println("7. Heal Team");                          // vindecam post-fight
            System.out.println("8. Encounter Wild Creature");            // simulam prinderea de pokemoni wild
            System.out.println("9. Exit");                               // papa
            System.out.print("Enter your choice: ");

            String choiceStr = scanner.nextLine();
            int choice = -1;
            try {
                choice = Integer.parseInt(choiceStr.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.println("\n== Trainer Rankings ==");
                    service.printRanking();
                    break;
                case 2:
                    System.out.println("\n== Sort Creatures by Level ==");
                    System.out.print("Enter trainer name: ");
                    String trainerName2 = scanner.nextLine();
                    Trainer t2 = service.findTrainer(trainerName2);
                    if (t2 != null) {
                        service.printCreaturesSortedByLevel(t2);
                    } else {
                        System.out.println("Trainer not found!");
                    }
                    break;
                case 3:
                    System.out.print("Enter creature name to search: ");
                    String name = scanner.nextLine();
                    service.findCreatureByName(name);
                    break;
                case 4:
                    System.out.print("Enter creature type (e.g. FIRE, WATER, ELECTRIC): ");
                    String typeStr = scanner.nextLine().toUpperCase();
                    try {
                        CreatureType type = CreatureType.valueOf(typeStr);
                        service.filterByType(type);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid type!");
                    }
                    break;
                case 5:
                    System.out.print("Enter trainer name: ");
                    String trainerName5 = scanner.nextLine();
                    Trainer t5 = service.findTrainer(trainerName5);
                    if (t5 == null) {
                        System.out.println("Trainer not found!");
                        break;
                    }
                    t5.printBag();
                    System.out.print("Enter item name to use (e.g. Super Potion) or leave empty to cancel: ");
                    String itemName = scanner.nextLine();
                    if (!itemName.trim().isEmpty()) {
                        System.out.print("Enter creature name to use it on: ");
                        String creatureName = scanner.nextLine();
                        Creature target = null;
                        for (Creature c : t5.getParty()) {
                            if (c.getName().equalsIgnoreCase(creatureName) || 
                               (c instanceof TrainerCreature && ((TrainerCreature)c).getNickname() != null && ((TrainerCreature)c).getNickname().equalsIgnoreCase(creatureName))) {
                                target = c;
                                break;
                            }
                        }
                        if (target != null) {
                            service.useItemOnCreature(t5, itemName, target);
                        } else {
                            System.out.println("Creature not found in " + t5.getName() + "'s party!");
                        }
                    }
                    break;
                case 6:
                    System.out.println("\n== Battle ==");
                    System.out.print("Enter Player 1 Trainer name: ");
                    String p1Name = scanner.nextLine();
                    Trainer p1 = service.findTrainer(p1Name);
                    System.out.print("Enter Player 2 Trainer name: ");
                    String p2Name = scanner.nextLine();
                    Trainer p2 = service.findTrainer(p2Name);
                    
                    if (p1 != null && p2 != null) {
                        String result = service.conductFullBattle(p1, p2);
                        System.out.println("  -> Final Result: " + result);
                    } else {
                        System.out.println("One or both trainers not found!");
                    }
                    break;
                case 7:
                    System.out.println("\n== Heal Team ==");
                    System.out.print("Enter trainer name: ");
                    String trainerName7 = scanner.nextLine();
                    Trainer t7 = service.findTrainer(trainerName7);
                    if (t7 != null) {
                        service.healTeam(t7);
                    } else {
                        System.out.println("Trainer not found!");
                    }
                    break;
                case 8:
                    System.out.println("\n== Wild Encounter ==");
                    System.out.print("Enter trainer name: ");
                    String trainerName = scanner.nextLine();
                    Trainer trainer = service.findTrainer(trainerName);
                    if (trainer == null) {
                        System.out.println("Trainer not found!");
                        break;
                    }

                    WildCreature randomWild = service.getRandomWildCreature();
                    if (randomWild == null) {
                        System.out.println("No wild creatures are registered!");
                        break;
                    }

                    service.encounterWildCreature(trainer, randomWild, scanner);
                    break;
                case 9:
                    running = false;
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
        }
        scanner.close();
    }
}
