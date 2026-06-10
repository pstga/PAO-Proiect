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
    private Trainer player;
    private Object opponent;
    private Random rng;
    private boolean battleOver;
    private String result;

    public BattleManager(Trainer player, Object opponent) {
        this.player = player;
        this.opponent = opponent;
        this.rng = new Random();
        this.battleOver = false;
        this.result = "";
    }

    public void startBattle() {
        System.out.println(" Start Fight!");
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
        System.out.printf("  Fight: %s uses %s and deals %d damage to %s!%n",
                attacker.getName(), move.getName(), damage, defender.getName());

        double typeMultiplier = TypeChart.getMultiplier(move.getType(), defender.getType());
        String eff = TypeChart.effectiveness(typeMultiplier);
        if (!eff.isEmpty()) {
            System.out.println("    -> " + eff);
        }
    }

    public int calculateDamage(Creature attacker, Creature defender, Move move) {
        double actualAttack = attacker.getAttack();
        if (attacker instanceof TrainerCreature tc) {
            actualAttack *= tc.getLoyaltyBonus();
        }

        double typeMultiplier = TypeChart.getMultiplier(move.getType(), defender.getType());

        int damage = (int) ((move.getPower() * actualAttack * typeMultiplier) / Math.max(1, defender.getDefense()));
        return Math.max(1, damage);
    }

    public boolean tryCatch(Pokeball ball) {
        if (!(opponent instanceof WildCreature wild)) {
            System.out.println("  -> You can't catch trained creatures!");
            return false;
        }
        boolean success = ball.use(wild);
        if (success) {
            battleOver = true;
            result = "CAUGHT";
        }
        return success;
    }

    public boolean tryFlee() {
        if (rng.nextBoolean()) {
            System.out.println("  -> Escape successful!");
            battleOver = true;
            result = "FLEE";
            return true;
        } else {
            System.out.println("  -> Couldn't escape!");
            return false;
        }
    }

    private void checkBattleEnd() {
        if (opponent instanceof Trainer oppTrainer) {
            if (!oppTrainer.hasAliveCreatures()) {
                System.out.printf("%n  Congratulations! You defeated Trainer %s!%n", oppTrainer.getName());
                battleOver = true;
                result = "WIN";
            }
        } else if (opponent instanceof WildCreature wild) {
            if (!wild.isAlive()) {
                System.out.printf("%n  Congratulations! %s defeated!%n", wild.getName());
                battleOver = true;
                result = "WIN";
            }
        }

        if (!player.hasAliveCreatures()) {
            System.out.println("\n  You lost!");
            battleOver = true;
            result = "LOSE";
        }
    }

    private Creature getOpponentCreature() {
        if (opponent instanceof WildCreature w) return w;
        if (opponent instanceof Trainer t) return t.getActiveCreature();
        return null;
    }
    public boolean isBattleOver() { return battleOver; }
    public String getResult() { return result; }
}
