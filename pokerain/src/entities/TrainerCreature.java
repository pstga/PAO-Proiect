package entities;

import enums.CreatureType;

public class TrainerCreature extends Creature {

    private String nickname;
    private int    loyalty;     // 0–255

    public TrainerCreature(String name, String nickname, int maxHp, int attack,
                           int defense, int speed, int level, CreatureType type) {
        super(name, maxHp, attack, defense, speed, level, type);
        this.nickname = (nickname != null && !nickname.isEmpty()) ? nickname : name;
        this.loyalty  = 70;
    }

    @Override
    public void levelUp() {
        level++;
        maxHp += 12;
        hp = Math.min(hp + 12, maxHp);
        attack += 4;
        defense += 3;
        speed += 3;
        loyalty = Math.min(255, loyalty + 5);
        experience = 0;
        System.out.printf("  Yay! %s grew to level %d! (Loyalty: %d)%n", nickname, level, loyalty);
    }

    @Override
    public String getDescription() {
        return String.format("Trainer creature: %s (%s) | Loyalty: %d/255", nickname, name, loyalty);
    }

    // bonus de atac!
    public double getLoyaltyBonus() {
        return 1.0 + (loyalty / 255.0) * 0.20;
    }

    public String getNickname() { return nickname; }
    public int getLoyalty()  { return loyalty; }

    public void setNickname(String nickname) { this.nickname = nickname; }
    public void increaseLoyalty(int amount)  { loyalty = Math.min(255, loyalty + amount); }
    public void decreaseLoyalty(int amount)  { loyalty = Math.max(0,   loyalty - amount); }

    @Override
    public String toString() {
        String display = nickname.equals(name) ? name : nickname + " (" + name + ")";
        return String.format("[%-18s | Lvl.%2d | HP: %3d/%-3d | Type: %-10s | Loyalty: %3d]",
                display, level, hp, maxHp, type, loyalty);
    }
}
