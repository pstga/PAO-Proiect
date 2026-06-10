package enums;

public enum StatusEffect {
    NONE, BURN, POISON, PARALYSIS, SLEEP;

    public int damagePerTurn() {
        return switch (this) {
            case BURN -> 8;
            case POISON -> 12;
            default -> 0;
        };
    }

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
