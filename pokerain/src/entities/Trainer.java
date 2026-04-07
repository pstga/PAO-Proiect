package entities;

import items.Item;
import items.Potion;

import java.util.*;

public class Trainer {

    private String             name;
    private List<TrainerCreature> party;      // max 6, List ordonată
    private Map<String, Integer>  bag;        // item_name → cantitate
    private List<Item>            itemObjects;// obiectele efective
    private List<String>          badges;
    private int                   money;

    public Trainer(String name, int money) {
        this.name        = name;
        this.money       = money;
        this.party       = new ArrayList<>();
        this.bag         = new LinkedHashMap<>();
        this.itemObjects = new ArrayList<>();
        this.badges      = new ArrayList<>();
    }

    // ── Echipă ───────────────────────────────────────────────────────────────
    public boolean addToParty(TrainerCreature creature) {
        if (party.size() >= 6) {
            System.out.println("  → Echipa e plină! (max 6 creaturi)");
            return false;
        }
        party.add(creature);
        System.out.printf("  → %s a adăugat pe %s în echipă!%n", name, creature.getNickname());
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
        System.out.printf("  → %s vindecă toată echipa!%n", name);
        for (TrainerCreature c : party) {
            c.heal(c.getMaxHp());
            c.setStatusEffect(enums.StatusEffect.NONE);
        }
    }

    // ── Inventar ──────────────────────────────────────────────────────────────
    public void addItem(Item item, int quantity) {
        bag.merge(item.getName(), quantity, Integer::sum);
        // adaugă obiectul dacă nu există deja
        boolean exists = itemObjects.stream().anyMatch(i -> i.getName().equals(item.getName()));
        if (!exists) itemObjects.add(item);
        System.out.printf("  → %s a primit %dx %s.%n", name, quantity, item.getName());
    }

    public boolean useItem(String itemName, Creature target) {
        if (!bag.containsKey(itemName) || bag.get(itemName) <= 0) {
            System.out.printf("  → %s nu are %s în inventar!%n", name, itemName);
            return false;
        }
        Item item = itemObjects.stream()
                .filter(i -> i.getName().equals(itemName))
                .findFirst().orElse(null);
        if (item == null) return false;

        boolean used = item.use(target);
        if (used) {
            bag.merge(itemName, -1, Integer::sum);
            if (bag.get(itemName) <= 0) bag.remove(itemName);
        }
        return used;
    }

    // ── Badge-uri ─────────────────────────────────────────────────────────────
    public void addBadge(String badge) {
        badges.add(badge);
        System.out.printf("  ★ %s a câștigat badge-ul: %s! (Total: %d)%n", name, badge, badges.size());
    }

    // ── Getteri ───────────────────────────────────────────────────────────────
    public String                getName()        { return name; }
    public List<TrainerCreature> getParty()       { return party; }
    public Map<String, Integer>  getBag()         { return bag; }
    public List<String>          getBadges()      { return badges; }
    public int                   getMoney()       { return money; }
    public int                   getBadgeCount()  { return badges.size(); }

    public void addMoney(int amount) { money += amount; }
    public boolean spendMoney(int amount) {
        if (money < amount) return false;
        money -= amount;
        return true;
    }

    public void printParty() {
        System.out.printf("%n  Echipa lui %s:%n", name);
        if (party.isEmpty()) { System.out.println("    (goală)"); return; }
        for (int i = 0; i < party.size(); i++) {
            System.out.printf("    %d. %s%n", i + 1, party.get(i));
        }
    }

    public void printBag() {
        System.out.printf("%n  Inventarul lui %s:%n", name);
        if (bag.isEmpty()) { System.out.println("    (gol)"); return; }
        bag.forEach((k, v) -> System.out.printf("    %-14s x%d%n", k, v));
    }

    @Override
    public String toString() {
        return String.format("Trainer %-12s | Badge-uri: %d | Bani: %d¥ | Creaturi: %d/6",
                name, badges.size(), money, party.size());
    }
}
