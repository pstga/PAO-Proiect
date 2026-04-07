package battle;

import enums.CreatureType;

import java.util.HashMap;
import java.util.Map;

public class TypeChart {

    // [attackType][defendType] → multiplicator
    private static final Map<CreatureType, Map<CreatureType, Double>> CHART = new HashMap<>();

    static {
        // FIRE
        put(CreatureType.FIRE,     CreatureType.GRASS,    2.0);
        put(CreatureType.FIRE,     CreatureType.ICE,      2.0);
        put(CreatureType.FIRE,     CreatureType.WATER,    0.5);
        put(CreatureType.FIRE,     CreatureType.FIRE,     0.5);
        // WATER
        put(CreatureType.WATER,    CreatureType.FIRE,     2.0);
        put(CreatureType.WATER,    CreatureType.GRASS,    0.5);
        put(CreatureType.WATER,    CreatureType.WATER,    0.5);
        // GRASS
        put(CreatureType.GRASS,    CreatureType.WATER,    2.0);
        put(CreatureType.GRASS,    CreatureType.FIRE,     0.5);
        put(CreatureType.GRASS,    CreatureType.GRASS,    0.5);
        // ELECTRIC
        put(CreatureType.ELECTRIC, CreatureType.WATER,    2.0);
        put(CreatureType.ELECTRIC, CreatureType.GRASS,    0.5);
        put(CreatureType.ELECTRIC, CreatureType.ELECTRIC, 0.5);
        // ICE
        put(CreatureType.ICE,      CreatureType.GRASS,    2.0);
        put(CreatureType.ICE,      CreatureType.WATER,    0.5);
        // POISON
        put(CreatureType.POISON,   CreatureType.GRASS,    2.0);
        put(CreatureType.POISON,   CreatureType.POISON,   0.5);
        // PSYCHIC
        put(CreatureType.PSYCHIC,  CreatureType.POISON,   2.0);
    }

    private static void put(CreatureType atk, CreatureType def, double multiplier) {
        CHART.computeIfAbsent(atk, k -> new HashMap<>()).put(def, multiplier);
    }

    public static double getMultiplier(CreatureType atkType, CreatureType defType) {
        return CHART.getOrDefault(atkType, Map.of()).getOrDefault(defType, 1.0);
    }

    public static String effectiveness(double mult) {
        if (mult >= 2.0) return "Super eficace!";
        if (mult <= 0.5) return "Nu e prea eficace...";
        return "";
    }
}
