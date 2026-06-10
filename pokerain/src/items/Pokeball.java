package items;

import entities.Creature;
import entities.WildCreature;

public class Pokeball extends Item {
    public Pokeball(String name, int price) {
        super(name, "Catching ball", price);
    }

    @Override
    public boolean use(Creature target) {
        if (!(target instanceof WildCreature wild)) {
            System.out.println("  -> You can only catch wild creatures!");
            return false;
        }
        System.out.printf("  -> Throwing %s at %s...%n", name, target.getName());
        boolean success = wild.attemptCatch();
        if (success) {
            System.out.printf("  -> %s was caught!%n", target.getName());
        } else {
            System.out.printf("  -> Darn it! %s escaped!%n", target.getName());
        }
        return success;
    }
}
