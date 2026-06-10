package items;

import entities.Creature;
import enums.StatusEffect;

public class Potion extends Item {
    private int healAmount;
    private boolean curesStatus;

    public Potion(String name, int healAmount, boolean curesStatus, int price) {
        super(name, "Heals " + healAmount + " HP" + (curesStatus ? " and status" : ""), price);
        this.healAmount = healAmount;
        this.curesStatus = curesStatus;
    }

    @Override
    public boolean use(Creature target) {
        if (!target.isAlive()) {
            System.out.printf("  -> %s has fainted!%n", target.getName());
            return false;
        }
        target.heal(healAmount);
        System.out.printf("  -> %s recovered %d HP. (HP now: %d/%d)%n",
                target.getName(), healAmount, target.getHp(), target.getMaxHp());
        if (curesStatus && target.getStatusEffect() != StatusEffect.NONE) {
            StatusEffect old = target.getStatusEffect();
            target.setStatusEffect(StatusEffect.NONE);
            System.out.printf("  -> %s's %s status was cured!%n", target.getName(), old);
        }
        return true;
    }
    public int getHealAmount() { return healAmount; }
    public boolean isCuresStatus() { return curesStatus; }
}
