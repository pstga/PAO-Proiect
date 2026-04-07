package items;

import entities.Creature;

public abstract class Item {
    protected String name;
    protected String description;
    protected int    price;

    public Item(String name, String description, int price) {
        this.name        = name;
        this.description = description;
        this.price       = price;
    }

    public abstract boolean use(Creature target);

    public String getName()        { return name; }
    public String getDescription() { return description; }
    public int    getPrice()       { return price; }

    @Override
    public String toString() {
        return String.format("%-12s | %s (Preț: %d¥)", name, description, price);
    }
}
