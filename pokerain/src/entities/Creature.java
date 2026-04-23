// pokemonii: pot fi trained sau wild; au diverse statusuri 
// am innebunit cand am vazut cate chestii trebuie sa iau in considerare tbh 
package entities;

import enums.CreatureType;
import enums.StatusEffect;

import java.util.ArrayList;
import java.util.List;

public abstract class Creature implements Comparable<Creature> {

    protected String name;
    protected int hp;
    protected int maxHp;
    protected int attack;
    protected int defense;
    protected int speed;
    protected int level;
    protected int experience;
    protected CreatureType type;
    protected StatusEffect statusEffect;
    protected List<Move> moves;

    public Creature(String name, int maxHp, int attack, int defense,
                    int speed, int level, CreatureType type) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.level = level;
        this.experience = 0;
        this.type = type;
        this.statusEffect = StatusEffect.NONE;
        this.moves = new ArrayList<>();
    }

    public abstract void levelUp();
    public abstract String getDescription();

    // logica: nu poate sa aiba hp pe minus 
    public void takeDamage(int amount) {
        hp = Math.max(0, hp - amount);
    }

    // logica: nu poate sa aiba mai mult decat maxHp
    public void heal(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }

    public boolean isAlive() { return hp > 0; }

    public void addMove(Move move) {
        if (moves.size() < 4) moves.add(move);
    }

    public void applyStatus(StatusEffect effect) {
        if (statusEffect == StatusEffect.NONE) {
            statusEffect = effect;
            System.out.printf("  -> %s now has status: %s!%n", name, effect);
        }
    }

    // logica: statusul isi face efectul la fiecare turn
    public void tickStatus() {
        int dmg = statusEffect.damagePerTurn();
        if (dmg > 0) {
            takeDamage(dmg);
            System.out.printf("  -> %s takes %d damage from %s!%n", name, dmg, statusEffect);
        }
    }

    public void gainExperience(int xp) {
        experience += xp;
        System.out.printf("  -> %s gained %d XP (total: %d)%n", name, xp, experience);
        if (experience >= level * 100) {
            levelUp();
        }
    }

    @Override
    public int compareTo(Creature other) {
        return Integer.compare(other.level, this.level); // sort desc dupa nivel
    }

    // getteri + setteri
    public String getName(){ return name; }
    public int getHp(){ return hp; }
    public int getMaxHp(){ return maxHp; }
    public int getAttack(){ return attack; }
    public int getDefense(){ return defense; }
    public int getSpeed(){ return speed; }
    public int getLevel(){ return level; }
    public int getExperience(){ return experience; }
    public CreatureType getType(){ return type; }
    public StatusEffect getStatusEffect(){ return statusEffect; }
    public List<Move> getMoves(){ return moves; }

    // easter egg: mi a mancat zilele asta; ar trbui sa pun pe git in readme cum aratau notitele mele cand am fc asta :)))
    public void setStatusEffect(StatusEffect s) { this.statusEffect = s; }

    @Override
    public String toString() {
        return String.format("[%-12s | Lvl.%2d | HP: %3d/%-3d | Type: %-10s | Status: %s]",
                name, level, hp, maxHp, type, statusEffect);
    }
}
