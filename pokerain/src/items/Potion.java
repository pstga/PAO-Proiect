package items;

import entities.Creature;
import enums.StatusEffect;

public class Potion extends Item {

    private int    healAmount;
    private boolean curesStatus;

    public Potion(String name, int healAmount, boolean curesStatus, int price) {
        super(name, "Vindecă " + healAmount + " HP" + (curesStatus ? " și status" : ""), price);
        this.healAmount  = healAmount;
        this.curesStatus = curesStatus;
    }

    @Override
    public boolean use(Creature target) {
        if (!target.isAlive()) {
            System.out.printf("  → %s este leșinat și nu poate folosi %s!%n", target.getName(), name);
            return false;
        }
        target.heal(healAmount);
        System.out.printf("  → %s a recuperat %d HP. (HP acum: %d/%d)%n",
                target.getName(), healAmount, target.getHp(), target.getMaxHp());
        if (curesStatus && target.getStatusEffect() != StatusEffect.NONE) {
            StatusEffect old = target.getStatusEffect();
            target.setStatusEffect(StatusEffect.NONE);
            System.out.printf("  → Statusul %s al lui %s a fost vindecat!%n", old, target.getName());
        }
        return true;
    }

    public int     getHealAmount()  { return healAmount; }
    public boolean isCuresStatus()  { return curesStatus; }
}
