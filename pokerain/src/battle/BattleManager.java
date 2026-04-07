package battle;

import entities.Creature;
import entities.TrainerCreature;
import entities.Trainer;
import entities.WildCreature;
import items.Pokeball;
import entities.Move;

import java.util.List;
import java.util.Random;

public class BattleManager {

    private Trainer     player;
    private Object      opponent;
    private Random      rng;
    private boolean     battleOver;
    private String      result;

    public BattleManager(Trainer player, Object opponent) {
        this.player     = player;
        this.opponent   = opponent;
        this.rng        = new Random();
        this.battleOver = false;
        this.result     = "";
    }

    public void startBattle() {
        System.out.println("\n" + "═".repeat(55));
        System.out.println(" LUPTĂ INIȚIATĂ!");
        System.out.println("═".repeat(55));
    }

    public void executeTurn(int moveIndex) {
        if (battleOver) return;

        Creature attacker = player.getActiveCreature();
        Creature defender = getOpponentCreature();

        if (attacker == null || defender == null) return;

        Move playerMove = attacker.getMoves().get(moveIndex);

        doAttack(attacker, defender, playerMove);
        if (defender.isAlive()) {
            List<Move> oppMoves = defender.getMoves();
            if (!oppMoves.isEmpty()) {
                Move oppMove = oppMoves.get(rng.nextInt(oppMoves.size()));
                doAttack(defender, attacker, oppMove);
            }
        }

        checkBattleEnd();
    }

    private void doAttack(Creature attacker, Creature defender, Move move) {
        int damage = calculateDamage(attacker, defender, move);
        defender.takeDamage(damage);
        System.out.printf("  ⚔  %s folosește %s și dă %d daune lui %s!%n",
                attacker.getName(), move.getName(), damage, defender.getName());
    }

    public int calculateDamage(Creature attacker, Creature defender, Move move) {
        int damage = (move.getPower() * attacker.getAttack()) / Math.max(1, defender.getDefense());
        return Math.max(1, damage);
    }

    public boolean tryCatch(Pokeball ball) {
        if (!(opponent instanceof WildCreature wild)) {
            System.out.println("  → Nu poți prinde creaturi antrenate!");
            return false;
        }
        boolean success = ball.use(wild);
        if (success) {
            battleOver = true;
            result     = "CAUGHT";
        }
        return success;
    }

    public boolean tryFlee() {
        System.out.println("  → Ai fugit cu succes!");
        battleOver = true;
        result     = "FLEE";
        return true;
    }

    private void checkBattleEnd() {
        Creature opponentCreature = getOpponentCreature();

        if (opponentCreature != null && !opponentCreature.isAlive()) {
            System.out.printf("%n  ✓ %s a fost înfrânt!%n", opponentCreature.getName());
            battleOver = true;
            result = "WIN";
        }

        if (!player.hasAliveCreatures()) {
            System.out.println("\n  ✗ Ai pierdut!");
            battleOver = true;
            result = "LOSE";
        }
    }

    private Creature getOpponentCreature() {
        if (opponent instanceof WildCreature w) return w;
        if (opponent instanceof Trainer t)    return t.getActiveCreature();
        return null;
    }

    public boolean isBattleOver() { return battleOver; }
    public String  getResult()    { return result; }
}
