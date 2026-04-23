// fiecare mutare din battle: iar mi am blestemat zilele noroc ca mi plac pokemonii
package entities;

import enums.CreatureType;
import enums.MoveCategory;
import enums.StatusEffect;

public class Move {
    private String name;
    private int power;
    private int accuracy;      // 0–100
    private int pp;            // puncte ramase
    private int maxPp;
    private CreatureType type;
    private MoveCategory category;
    private StatusEffect sideEffect; // side effect, poate sa nu existe

    public Move(String name, int power, int accuracy, int pp, CreatureType type, MoveCategory category,
                StatusEffect sideEffect) {
        this.name = name;
        this.power = power;
        this.accuracy = accuracy;
        this.pp = pp;
        this.maxPp = pp;
        this.type = type;
        this.category = category;
        this.sideEffect = sideEffect;
    }

    // getteri
    public String getName(){ return name; }
    public int getPower(){ return power; }
    public int getAccuracy(){ return accuracy; }
    public int getPp(){ return pp; }
    public int getMaxPp(){ return maxPp; }
    public CreatureType getType(){ return type; }
    public MoveCategory getCategory(){ return category; }
    public StatusEffect getSideEffect(){ return sideEffect; }

    public boolean hasPp(){ return pp > 0; }
    public void consumePp(){ if (pp > 0) pp--; }
    public void restorePp(){ pp = maxPp; }

    @Override
    public String toString() {
        return String.format("%-14s | Power: %3d | Accuracy: %3d%% | PP: %d/%d | Type: %-10s | Category: %s",
                name, power, accuracy, pp, maxPp, type, category);
    }
}
