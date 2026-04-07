import battle.BattleManager;
import entities.*;
import enums.CreatureType;
import enums.MoveCategory;
import enums.StatusEffect;
import items.Pokeball;
import items.Potion;
import service.GameService;

public class Main {

    public static void main(String[] args) {

        System.out.println("╔═══════════════════════════════════════════╗");
        System.out.println("║     POKÉGAME — Sistem Turn-Based RPG      ║");
        System.out.println("╚═══════════════════════════════════════════╝");

        GameService service = new GameService();

        // ─────────────────────────────────────────────────────────────────
        // SECȚIUNEA 1: Creare mutări (Move)
        // ─────────────────────────────────────────────────────────────────
        System.out.println("\n══ 1. Creare mutări ══");

        Move focBlast     = new Move("Foc Blast",    90, 85, 5,  CreatureType.FIRE,     MoveCategory.SPECIAL,  StatusEffect.BURN);
        Move hydropump    = new Move("Hydropump",    95, 80, 5,  CreatureType.WATER,    MoveCategory.SPECIAL,  StatusEffect.NONE);
        Move thunderbolt  = new Move("Thunderbolt",  80, 100, 15, CreatureType.ELECTRIC, MoveCategory.SPECIAL,  StatusEffect.PARALYSIS);
        Move razorLeaf    = new Move("Razor Leaf",   55, 95, 25, CreatureType.GRASS,    MoveCategory.PHYSICAL, StatusEffect.NONE);
        Move tackle       = new Move("Tackle",       40, 100, 35, CreatureType.NORMAL,  MoveCategory.PHYSICAL, StatusEffect.NONE);
        Move iceBeam      = new Move("Ice Beam",     90, 100, 10, CreatureType.ICE,     MoveCategory.SPECIAL,  StatusEffect.SLEEP);
        Move poisonSting  = new Move("Poison Sting", 15, 100, 35, CreatureType.POISON,  MoveCategory.PHYSICAL, StatusEffect.POISON);
        Move psychic      = new Move("Psychic",      90, 100, 10, CreatureType.PSYCHIC, MoveCategory.SPECIAL,  StatusEffect.NONE);

        System.out.println("  → Mutări create: " + focBlast.getName() + ", " + hydropump.getName()
                + ", " + thunderbolt.getName() + ", " + razorLeaf.getName());
        System.out.println("  → " + focBlast);

        // ─────────────────────────────────────────────────────────────────
        // SECȚIUNEA 2: Creare creaturi (TrainerCreature)
        // ─────────────────────────────────────────────────────────────────
        System.out.println("\n══ 2. Creare creaturi trainer ══");

        TrainerCreature charmander = new TrainerCreature("Charmander", "Flăcărel",
                78, 52, 43, 65, 10, CreatureType.FIRE);
        charmander.addMove(focBlast);
        charmander.addMove(tackle);

        TrainerCreature squirtle = new TrainerCreature("Squirtle", "Pluton",
                88, 48, 65, 43, 8, CreatureType.WATER);
        squirtle.addMove(hydropump);
        squirtle.addMove(tackle);

        TrainerCreature bulbasaur = new TrainerCreature("Bulbasaur", null,
                90, 49, 49, 45, 9, CreatureType.GRASS);
        bulbasaur.addMove(razorLeaf);
        bulbasaur.addMove(poisonSting);

        TrainerCreature pikachu = new TrainerCreature("Pikachu", "Sparky",
                70, 55, 40, 90, 12, CreatureType.ELECTRIC);
        pikachu.addMove(thunderbolt);
        pikachu.addMove(tackle);

        TrainerCreature jynx = new TrainerCreature("Jynx", null,
                95, 50, 35, 50, 7, CreatureType.ICE);
        jynx.addMove(iceBeam);

        TrainerCreature alakazam = new TrainerCreature("Alakazam", "Mindstorm",
                65, 50, 45, 120, 15, CreatureType.PSYCHIC);
        alakazam.addMove(psychic);
        alakazam.addMove(tackle);

        System.out.println("  → " + charmander);
        System.out.println("  → " + squirtle);
        System.out.println("  → " + pikachu);

        // ─────────────────────────────────────────────────────────────────
        // SECȚIUNEA 3: Creare traineri
        // ─────────────────────────────────────────────────────────────────
        System.out.println("\n══ 3. Creare și înregistrare traineri ══");

        Trainer ash    = new Trainer("Ash",    3000);
        Trainer misty  = new Trainer("Misty",  2500);
        Trainer brock  = new Trainer("Brock",  2200);

        service.registerTrainer(ash);
        service.registerTrainer(misty);
        service.registerTrainer(brock);

        // ─────────────────────────────────────────────────────────────────
        // SECȚIUNEA 4: Adăugare creaturi în echipe
        // ─────────────────────────────────────────────────────────────────
        System.out.println("\n══ 4. Adăugare creaturi în echipă ══");

        service.addCreatureToTrainer(ash,   charmander);
        service.addCreatureToTrainer(ash,   pikachu);
        service.addCreatureToTrainer(ash,   bulbasaur);
        service.addCreatureToTrainer(misty, squirtle);
        service.addCreatureToTrainer(misty, jynx);
        service.addCreatureToTrainer(brock, alakazam);

        ash.printParty();

        // ─────────────────────────────────────────────────────────────────
        // SECȚIUNEA 5: Afișare creaturi sortate după nivel (Operație 3)
        // ─────────────────────────────────────────────────────────────────
        System.out.println("\n══ 5. Sortare creaturi după nivel ══");
        service.printCreaturesSortedByLevel(ash);

        // ─────────────────────────────────────────────────────────────────
        // SECȚIUNEA 6: Căutare creatură (Operație 4)
        // ─────────────────────────────────────────────────────────────────
        System.out.println("\n══ 6. Căutare creatură ══");
        service.findCreatureByName("Sparky");
        service.findCreatureByName("Mewtwo");

        // ─────────────────────────────────────────────────────────────────
        // SECȚIUNEA 7: Filtrare după tip (Operație 5)
        // ─────────────────────────────────────────────────────────────────
        System.out.println("\n══ 7. Filtrare după tip ══");
        service.filterByType(CreatureType.WATER);
        service.filterByType(CreatureType.FIRE);

        // ─────────────────────────────────────────────────────────────────
        // SECȚIUNEA 8: Item-uri și folosire (Operații 6 & 8)
        // ─────────────────────────────────────────────────────────────────
        System.out.println("\n══ 8. Inventar și utilizare item-uri ══");

        Potion   superPotion = new Potion("Super Potion", 60, false, 700);
        Potion   fullRestore = new Potion("Full Restore", 999, true, 3000);
        Pokeball pokeball    = new Pokeball("Poke Ball",   1.0, 200);
        Pokeball greatBall   = new Pokeball("Great Ball",  1.5, 600);

        ash.addItem(superPotion, 5);
        ash.addItem(fullRestore, 2);
        ash.addItem(pokeball,    10);
        ash.addItem(greatBall,   3);
        ash.printBag();

        // Simulăm daune și folosim potion
        charmander.takeDamage(40);
        System.out.printf("%n  → %s după daune: HP %d/%d%n",
                charmander.getName(), charmander.getHp(), charmander.getMaxHp());
        service.useItemOnCreature(ash, "Super Potion", charmander);

        // ─────────────────────────────────────────────────────────────────
        // SECȚIUNEA 9: Luptă trainer vs trainer (Operație 7)
        // ─────────────────────────────────────────────────────────────────
        System.out.println("\n══ 9. Luptă completă: Ash vs Misty ══");
        String result = service.conductFullBattle(ash, misty);
        System.out.println("  → Rezultat final: " + result);

        // ─────────────────────────────────────────────────────────────────
        // SECȚIUNEA 10: Vindecare echipă (Operație 9)
        // ─────────────────────────────────────────────────────────────────
        System.out.println("\n══ 10. Vindecare echipă ══");
        service.healTeam(ash);

        // ─────────────────────────────────────────────────────────────────
        // SECȚIUNEA 11: Creaturi sălbatice și prindere (Operație 10)
        // ─────────────────────────────────────────────────────────────────
        System.out.println("\n══ 11. Creaturi sălbatice și prindere ══");

        WildCreature wildEevee = new WildCreature("Eevee", 65, 45, 40, 55, 6,
                CreatureType.NORMAL, 0.7);
        WildCreature wildMagikarp = new WildCreature("Magikarp", 30, 10, 30, 80, 3,
                CreatureType.WATER, 0.95);

        service.registerWildCreature(wildEevee);
        service.registerWildCreature(wildMagikarp);
        service.printWildRegistry();

        // Simulăm daune ca să fie mai ușor de prins
        wildEevee.takeDamage(45);
        System.out.printf("%n  → Eevee după daune: HP %d/%d%n",
                wildEevee.getHp(), wildEevee.getMaxHp());

        // Încercăm prinderea
        BattleManager catchBattle = new BattleManager(ash, wildEevee);
        catchBattle.startBattle();
        boolean caught = catchBattle.tryCatch(greatBall);
        if (caught) {
            TrainerCreature newEevee = new TrainerCreature(
                    wildEevee.getName(), "Eevy",
                    wildEevee.getMaxHp(), wildEevee.getAttack(),
                    wildEevee.getDefense(), wildEevee.getSpeed(),
                    wildEevee.getLevel(), wildEevee.getType());
            newEevee.addMove(tackle);
            service.addCreatureToTrainer(ash, newEevee);
        }

        // ─────────────────────────────────────────────────────────────────
        // SECȚIUNEA 12: Brock câștigă un badge, clasament final (Operație 8)
        // ─────────────────────────────────────────────────────────────────
        System.out.println("\n══ 12. Acordare badge și clasament ══");
        brock.addBadge("Boulder Badge");
        brock.addBadge("Cascade Badge");
        ash.addBadge("Thunder Badge");

        service.printRanking();

        // ─────────────────────────────────────────────────────────────────
        // SECȚIUNEA 13: LevelUp demonstrativ (Operație 6 — evoluție)
        // ─────────────────────────────────────────────────────────────────
        System.out.println("\n══ 13. Level Up ══");
        System.out.println("  → Înainte: " + pikachu);
        pikachu.gainExperience(1200); // forțăm level-up
        System.out.println("  → După:    " + pikachu);

        // ─────────────────────────────────────────────────────────────────
        // SECȚIUNEA 14: TypeChart demonstrativ
        // ─────────────────────────────────────────────────────────────────
        System.out.println("\n══ 14. Eficacitate tipuri ══");
        printTypeEffect(CreatureType.FIRE,     CreatureType.GRASS);
        printTypeEffect(CreatureType.WATER,    CreatureType.FIRE);
        printTypeEffect(CreatureType.ELECTRIC, CreatureType.WATER);
        printTypeEffect(CreatureType.GRASS,    CreatureType.FIRE);

        System.out.println("\n╔═══════════════════════════════════════════╗");
        System.out.println("║         Demonstrație completă! ✓          ║");
        System.out.println("╚═══════════════════════════════════════════╝");
    }

    private static void printTypeEffect(CreatureType atk, CreatureType def) {
        double mult = battle.TypeChart.getMultiplier(atk, def);
        String eff  = battle.TypeChart.effectiveness(mult);
        System.out.printf("  %s → %s : x%.1f  %s%n", atk, def, mult,
                eff.isEmpty() ? "(normal)" : eff);
    }
}
