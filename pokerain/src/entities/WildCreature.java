package entities;

import enums.CreatureType;

public class WildCreature extends Creature {

    private double catchRate;   // 0.0 – 1.0: sansa de prindere
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
        maxHp += 10;
        hp = Math.min(hp + 10, maxHp);
        attack += 3;
        defense += 2;
        speed += 2;
        experience = 0;
        System.out.printf("  -> %s (wild) reached level %d!%n", name, level);
    }

    @Override
    public String getDescription() {
        return String.format("Wild creature: %s | Catch rate: %.0f%%", name, catchRate * 100);
    }

    // calculam sansa sa fuga;  cu cat are mai mult hp cu atat e mai posibil sa plece
    public boolean tryFlee() {
        double chance = 0.3 + (1.0 - (double) hp / maxHp) * 0.4;
        return Math.random() < chance;
    }

    // true daca prindem pookemonn gotta catch em all !!! 
    public boolean attemptCatch() {
        double hpFactor   = 1.0 - (double) hp / maxHp * 0.5; // mai usor cu HP mic; ac logica 
        double totalChance = catchRate * hpFactor;
        caught = Math.random() < totalChance;
        return caught;
    }

    public double getCatchRate() { return catchRate; }
    public boolean isCaught() { return caught; }

    @Override
    public String toString() {
        return "Wild " + super.toString() + String.format(" | CatchRate: %.0f%%", catchRate * 100);
    }
}
