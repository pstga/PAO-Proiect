package items;

import entities.Creature;
import entities.WildCreature;

public class Pokeball extends Item {

    private double catchBonus;  // multiplicator față de catchRate de bază

    public Pokeball(String name, double catchBonus, int price) {
        super(name, "Minge de prindere (bonus: x" + catchBonus + ")", price);
        this.catchBonus = catchBonus;
    }

    @Override
    public boolean use(Creature target) {
        if (!(target instanceof WildCreature wild)) {
            System.out.println("  → Poți prinde doar creaturi sălbatice!");
            return false;
        }
        System.out.printf("  → Arunci %s spre %s...%n", name, target.getName());
        boolean success = wild.attemptCatch(catchBonus);
        if (success) {
            System.out.printf("  → %s a fost prins! 🎉%n", target.getName());
        } else {
            System.out.printf("  → %s a scăpat din %s!%n", target.getName(), name);
        }
        return success;
    }

    public double getCatchBonus() { return catchBonus; }
}
