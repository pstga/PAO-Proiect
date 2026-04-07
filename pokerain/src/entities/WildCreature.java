package entities;

import enums.CreatureType;

public class WildCreature extends Creature {

    private double catchRate;   // 0.0 – 1.0: șansa de prindere
    private boolean caught;

    public WildCreature(String name, int maxHp, int attack, int defense,
                        int speed, int level, CreatureType type, double catchRate) {
        super(name, maxHp, attack, defense, speed, level, type);
        this.catchRate = catchRate;
        this.caught    = false;
    }

    @Override
    public void levelUp() {
        level++;
        maxHp    += 10;
        hp        = Math.min(hp + 10, maxHp);
        attack   += 3;
        defense  += 2;
        speed    += 2;
        experience = 0;
        System.out.printf("  → %s (sălbatic) a ajuns la nivelul %d!%n", name, level);
    }

    @Override
    public String getDescription() {
        return String.format("Creatură sălbatică: %s | Rată prindere: %.0f%%", name, catchRate * 100);
    }

    /** Încearcă să fugă; cu cât are mai mult HP, cu atât e mai greu. */
    public boolean tryFlee() {
        double chance = 0.3 + (1.0 - (double) hp / maxHp) * 0.4;
        return Math.random() < chance;
    }

    /** Returnează true dacă prinderea a reușit. */
    public boolean attemptCatch(double ballBonus) {
        double hpFactor   = 1.0 - (double) hp / maxHp * 0.5; // mai ușor cu HP mic
        double totalChance = catchRate * ballBonus * hpFactor;
        caught = Math.random() < totalChance;
        return caught;
    }

    public double  getCatchRate() { return catchRate; }
    public boolean isCaught()    { return caught; }

    @Override
    public String toString() {
        return "Sălbatic " + super.toString() + String.format(" | CatchRate: %.0f%%", catchRate * 100);
    }
}
