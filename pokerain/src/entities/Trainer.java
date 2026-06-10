
package entities;

import items.Item;

import java.util.*;

public class Trainer {
    private int id; 
    private String name;
    private List<TrainerCreature> party; 
    private Map<String, Integer> bag; 
    private List<Item> itemObjects; 
    private int money;

    public Trainer(String name, int money) {
        this.name = name;
        this.money = money;
        this.party = new ArrayList<>();
        this.bag = new LinkedHashMap<>();
        this.itemObjects = new ArrayList<>();
    }

    public boolean addToParty(TrainerCreature creature) {
        if (party.size() >= 6) {
            System.out.println("  -> Team is already full! (max 6 creatures)");
            return false;
        }
        party.add(creature);
        System.out.printf("  -> %s added %s to the team!%n", name, creature.getNickname());
        return true;
    }

    public TrainerCreature getActiveCreature() {
        for (TrainerCreature c : party) {
            if (c.isAlive()) return c;
        }
        return null;
    }

    public boolean hasAliveCreatures() {
        return party.stream().anyMatch(Creature::isAlive);
    }

    public void healAllCreatures() {
        System.out.printf("  -> %s Heals all creatures!%n", name);
        for (TrainerCreature c : party) {
            c.heal(c.getMaxHp());
            c.setStatusEffect(enums.StatusEffect.NONE);
        }
    }

    public void addItem(Item item, int quantity) {
        bag.merge(item.getName(), quantity, Integer::sum);

        boolean exists = itemObjects.stream().anyMatch(i -> i.getName().equals(item.getName()));
        if (!exists) itemObjects.add(item);
        System.out.printf("  -> %s received %dx %s.%n", name, quantity, item.getName());
    }

    public boolean useItem(String itemName, Creature target) {
        String actualItemName = null;
        for (String key : bag.keySet()) {
            if (key.equalsIgnoreCase(itemName.trim())) {
                actualItemName = key;
                break;
            }
        }

        if (actualItemName == null || bag.get(actualItemName) <= 0) {
            System.out.printf("  -> %s does not have %s in inventory!%n", name, itemName);
            return false;
        }

        String finalActualItemName = actualItemName;
        Item item = itemObjects.stream()
                .filter(i -> i.getName().equals(finalActualItemName))
                .findFirst().orElse(null);
        if (item == null) return false;

        boolean used = item.use(target);
        if (used) {
            bag.merge(actualItemName, -1, Integer::sum);
            if (bag.get(actualItemName) <= 0) bag.remove(actualItemName);
        }
        return used;
    }
    public int getId(){ return id; }
    public void setId(int id){ this.id = id; }
    public String getName(){ return name; }
    public List<TrainerCreature> getParty(){ return party; }
    public Map<String, Integer> getBag() { return bag; }
    public List<Item> getItemObjects() { return itemObjects; }
    public int getMoney() { return money; }
    public void setMoney(int money) { this.money = money; }
    public void addMoney(int amount) { money += amount; }
    public boolean spendMoney(int amount) {
        if (money < amount) return false;
        money -= amount;
        return true;
    }

    public void printParty() {
        System.out.printf("%n  %s's Team:%n", name);
        if (party.isEmpty()) { System.out.println("    (empty)"); return; }
        for (int i = 0; i < party.size(); i++) {
            System.out.printf("    %d. %s%n", i + 1, party.get(i));
        }
    }

    public void printBag() {
        System.out.printf("%n  %s's Inventory:%n", name);
        if (bag.isEmpty()) { System.out.println("    (empty)"); return; }
        bag.forEach((k, v) -> System.out.printf("    %-14s x%d%n", k, v));
    }

    @Override
    public String toString() {
        return String.format("Trainer %-12s | Money: %d¥ | Creatures: %d/6",
                name, money, party.size());
    }
}
