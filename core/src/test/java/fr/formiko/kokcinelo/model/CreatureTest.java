package fr.formiko.kokcinelo.model;

import fr.formiko.kokcinelo.Controller;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import com.badlogic.gdx.math.Vector2;

public class CreatureTest extends Assertions {
    @Test
    void testCreature() {
        Creature c = new CreatureX();
        assertNotNull(c);
    }

    @Test
    void testSee() {
        Creature c = new CreatureX(10, 20);
        c.setCenter(0, 0);
        Creature c2 = new CreatureX(10, 20);
        c2.setCenter(20, 20);
        Creature c3 = new CreatureX(10, 50);
        c3.setCenter(40, 40);

        assertTrue(c.see(c2));
        assertTrue(c2.see(c));
        assertTrue(c3.see(c2));
        assertTrue(c2.see(c3));
        assertTrue(c3.see(c2));
        assertTrue(c2.see(c3));

        assertTrue(c3.see(c));
        assertFalse(c.see(c3));
    }
    @Test
    void testCreatureMove() {
        Creature c = new CreatureX();
        // c.setMovingSpeed(1f);
        c.setCenter(0, 0);
        c.setRotation(0);
        c.getActor().moveFront(1f);
        almostEquals(0, c.getCenterX());
        almostEquals(1, c.getCenterY());
    }
    @Test
    void testCreatureMoveWithSpeed() {
        Creature c = new CreatureX();
        c.setMovingSpeed(1f);
        c.setCenter(0, 0);
        c.setRotation(0);
        c.moveFront(1f);
        almostEquals(0, c.getCenterX());
        almostEquals(1, c.getCenterY());
    }
    @Test
    void testCreatureMoveWithSpeed2() {
        Creature c = new CreatureX();
        c.setMovingSpeed(1f);
        c.setCenter(0, 0);
        c.setRotation(0);
        c.moveFront(0.2f);
        almostEquals(0, c.getCenterX());
        almostEquals(0.2f, c.getCenterY());
    }
    @Test
    void testCreatureMoveWithSpeed3() {
        Creature c = new CreatureX();
        c.setMovingSpeed(4.5f);
        c.setCenter(0, 0);
        c.setRotation(0);
        c.moveFront(0.2f);
        almostEquals(0, c.getCenterX());
        almostEquals(0.9f, c.getCenterY());
    }
    @Test
    void testCreatureMoveWithSpeedY() {
        Creature c = new CreatureX();
        c.setMovingSpeed(4.5f);
        c.setCenter(0, 0);
        c.setRotation(90);
        c.moveFront(0.2f);
        almostEquals(-0.9f, c.getCenterX());
        almostEquals(0, c.getCenterY());
    }
    @Test
    void testCreatureMoveWithSpeedXY() {
        Creature c = new CreatureX();
        c.setMovingSpeed(5f);
        c.setCenter(0, 0);
        c.setRotation(45);
        c.moveFront(0.2f);
        almostEquals(-0.70711f, c.getCenterX());
        almostEquals(0.70711f, c.getCenterY());
    }

    @Test
    void testGoTo() {
        Creature c = new CreatureX();
        c.setMovingSpeed(5f);
        c.setCenter(0, 0);
        c.setRotation(0);
        c.goTo(new Vector2(1, 1));
        assertEquals(45f, c.getWantedRotation());
    }
    @Test
    void testGoTo2() {
        Creature c = new CreatureX();
        c.setMovingSpeed(5f);
        c.setCenter(0, 0);
        c.setRotation(0);
        c.goTo(new Vector2(-2, 1));
        almostEquals(296.56506f, c.getWantedRotation());
    }

    @Test
    void testRunAwayFrom() {
        Creature c = new CreatureX();
        c.setMovingSpeed(5f);
        c.setCenter(0, 0);
        c.setRotation(0);
        c.runAwayFrom(new LinkedList<Float>(), new Vector2(1, 1));
        almostEquals(225f, c.getWantedRotation());
    }

    @Test
    void testAnt() {
        new Ant();
        new RedAnt();
        new Ladybug();
    }

    void createGameStateWithAphidLadybugAnt(int aphid, int ladybug, int ant) {
        GameState gs = GameState.builder().setAphidNumber(100).setMapHeight(2000).setMapWidth(2000).setLadybugNumber(ladybug)
                .setAphidNumber(aphid).setRedAntNumber(ant).build();
        Controller.setController(new Controller(null));
        Controller.getController().setGameState(gs);
        Controller.setDebug(false);
    }

    @Test
    void testGetVisibleCreatureHuntedBy() {
        createGameStateWithAphidLadybugAnt(0, 1, 1);
        Ant a = Controller.getController().getGameState().getAnts().get(0);
        Ladybug l = Controller.getController().getGameState().getLadybugs().get(0);

        a.setCenter(0, 0);
        l.setCenter(1, 0);

        assertFalse(a.getVisibleCreatureHuntedBy().contains(l));
        assertTrue(l.getVisibleCreatureHuntedBy().contains(a));
    }

    @Test
    void testGetVisibleCreatureHuntedBy2() {
        createGameStateWithAphidLadybugAnt(0, 1, 1);
        Ant a = Controller.getController().getGameState().getAnts().get(0);
        Ladybug l = Controller.getController().getGameState().getLadybugs().get(0);

        a.setCenter(0, 0);
        l.setCenter(1500, 0);

        assertFalse(a.getVisibleCreatureHuntedBy().contains(l));
        // assertFalse(l.getVisibleCreatureHuntedBy().contains(a));
    }

    @Test
    void testGetVisibleCreatureHuntedBy3() {
        createGameStateWithAphidLadybugAnt(0, 2, 1);
        Ant a = Controller.getController().getGameState().getAnts().get(0);
        Ladybug l = Controller.getController().getGameState().getLadybugs().get(0);
        Ladybug l2 = Controller.getController().getGameState().getLadybugs().get(1);

        a.setCenter(0, 0);
        l.setCenter(1, 0);
        l2.setCenter(1, 0);

        assertFalse(a.getVisibleCreatureHuntedBy().contains(l));
        assertTrue(l.getVisibleCreatureHuntedBy().contains(a));
        assertTrue(l2.getVisibleCreatureHuntedBy().contains(a));
    }

    @Test
    void testGetVisibleCreatureHuntedByAphid() {
        createGameStateWithAphidLadybugAnt(2, 2, 0);
        Ladybug l = Controller.getController().getGameState().getLadybugs().get(0);
        Ladybug l2 = Controller.getController().getGameState().getLadybugs().get(1);
        Aphid a = Controller.getController().getGameState().getAphids().get(0);
        Aphid a2 = Controller.getController().getGameState().getAphids().get(1);

        a.setCenter(0, 0);
        a2.setCenter(50, 0);
        l.setCenter(100, 0);
        l2.setCenter(150, 0);

        assertTrue(l.getVisibleCreatureHuntedBy().isEmpty());
        assertTrue(a.getVisibleCreatureHuntedBy().contains(l));
        assertTrue(a2.getVisibleCreatureHuntedBy().contains(l));
        assertTrue(a.getVisibleCreatureHuntedBy().contains(l2)); // because of Ladybug size
        assertTrue(a2.getVisibleCreatureHuntedBy().contains(l2));
    }
    @Test
    void testGetVisibleCreatureHuntedByAphid2() {
        createGameStateWithAphidLadybugAnt(2, 2, 0);
        Ladybug l = Controller.getController().getGameState().getLadybugs().get(0);
        Ladybug l2 = Controller.getController().getGameState().getLadybugs().get(1);
        Aphid a = Controller.getController().getGameState().getAphids().get(0);
        Aphid a2 = Controller.getController().getGameState().getAphids().get(1);

        a.setCenter(0, 0);
        a2.setCenter(50, 0);
        l.setCenter(100, 0);
        l2.setCenter(150 + l2.getHitRadius(), 0);

        assertTrue(l.getVisibleCreatureHuntedBy().isEmpty());
        assertTrue(a.getVisibleCreatureHuntedBy().contains(l));
        assertTrue(a2.getVisibleCreatureHuntedBy().contains(l));
        assertFalse(a.getVisibleCreatureHuntedBy().contains(l2)); // because of Ladybug size
        assertTrue(a2.getVisibleCreatureHuntedBy().contains(l2));
    }
    @Test
    void testGetClosestVisibleCreatureToHunt() {
        createGameStateWithAphidLadybugAnt(2, 3, 1);
        Ladybug l = Controller.getController().getGameState().getLadybugs().get(0);
        Ladybug l2 = Controller.getController().getGameState().getLadybugs().get(1);
        Ladybug l3 = Controller.getController().getGameState().getLadybugs().get(2);
        Aphid a = Controller.getController().getGameState().getAphids().get(0);
        Aphid a2 = Controller.getController().getGameState().getAphids().get(1);

        a.setCenter(0, 0);
        a2.setCenter(50, 0);
        l.setCenter(100, 0);
        l2.setCenter(150, 0);
        l3.setCenter(1500, 0);

        assertEquals(a2, l.getClosestVisibleCreatureToHunt());
        assertEquals(a2, l2.getClosestVisibleCreatureToHunt());
        assertNull(l3.getClosestVisibleCreatureToHunt());
        assertNull(a.getClosestVisibleCreatureToHunt());
    }

    // Test for runAwayFrom are mostly graphics test


    @Test
    void testRunAwayFromSeveralEnemies() {
        Creature c = new CreatureX();
        c.setMovingSpeed(5f);
        c.setCenter(0, 0);
        c.setRotation(0);
        c.runAwayFrom(new LinkedList<Float>(), new Vector2(1, 1), new Vector2(-1, 1));
        almostEquals(0, c.getWantedRotation());
    }

    @ParameterizedTest
    // @formatter:off
    @CsvSource({
        // 1 wall
        "5, 100, 20, 14.47f, 165.52f",
        "1, 100, 20, 2.85f, 177.13f",
        "0, 100, 20, 0f, 180f",
        "1999, 100, 20, 182.87f, 357.15f",
        "2000, 100, 20, 0f, 180f",
        // 2 walls
        //TODO
    })
    // @formatter:on
    public void testGetWallsAngles(float x, float y, int visionRadius, float angle1, float angle2) {
        createGameStateWithAphidLadybugAnt(1, 1, 0);
        Creature c = new CreatureX(10, visionRadius);
        c.setCenter(x, y);
        List<Float> wallsAngles = c.getWallsAngles();
        assertEquals(2, wallsAngles.size());
        almostEqualsAngle(angle1, wallsAngles.get(0));
        almostEqualsAngle(angle2, wallsAngles.get(1));
    }


    class CreatureX extends Creature {
        public CreatureX(int hitRadius, int visionRadius) {
            super("x");
            this.hitRadius = hitRadius;
            this.visionRadius = visionRadius;
        }
        public CreatureX() { this(10, 20); }
    }
    void almostEquals(float f1, float f2) { assertTrue(java.lang.Math.abs(f1 - f2) < 0.1f, f1 + " ≃ " + f2); }
    void almostEqualsAngle(float f1, float f2) {
        if (java.lang.Math.abs(f1 - f2) > 180) {
            if (f1 > f2) {
                f1 -= 360;
            } else {
                f2 -= 360;
            }
        }
        almostEquals(f1, f2);
    }
}
