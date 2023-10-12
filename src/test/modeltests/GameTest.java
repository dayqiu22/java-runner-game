package modeltests;

import model.*;
import model.Character;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GameTest {
    private Game testGame;
    private Block testBlock1;
    private Block testBlock2;
    private Block testBlock3;
    private Block testBlock4;
    private Block testBlock5;
    private Block testBlock6;
    private PowerUp testPowerUp1;
    private PowerUp testPowerUp2;
    private PowerUp testPowerUp3;
    private PowerUp testPowerUp4;
    private Hazard testHazard;

    @BeforeEach
    void runBefore() {
        testGame = new Game(30,30);
        testBlock1 = new Block(new Position(11, 20));
        testBlock2 = new Block(new Position(8,20));
        testBlock3 = new Block(new Position(10,22));
        testBlock4 = new Block(new Position(10,21));
        testBlock5 = new Block(new Position(12,18));
        testBlock6 = new Block(new Position(10,18));
        testPowerUp1 = new PowerUp(new Position(11, 18), "speedup");
        testPowerUp2 = new PowerUp(new Position(9, 18), "invulnerable");
        testPowerUp3 = new PowerUp(new Position(13, 18), "speedup");
        testPowerUp4 = new PowerUp(new Position(7, 18), "invulnerable");
        testHazard = new Hazard(new Position(10,17));
    }

    @Test
    void testConstructor() {
        assertEquals(30, testGame.getMaxX());
        assertEquals(30, testGame.getMaxY());
        Character player = testGame.getCharacter();
        assertEquals(10, player.getPosition().getPositionX());
        assertEquals(20, player.getPosition().getPositionY());
        assertEquals(0, testGame.getInventory().size());
        assertEquals(3, testGame.getAvailableKeys().size());
        assertEquals(0, testGame.getTime());
        assertEquals(0, testGame.getInvulnerabilityEnd());
        assertEquals(0, testGame.getSpeedEnd());
        assertFalse(testGame.isEnded());
    }

    @Test
    void testMoveResolveCollisionsX() {
        testGame.addBlock(testBlock1);
        testGame.addBlock(testBlock2);
        Character character = testGame.getCharacter();

        character.setVelocityX(1);
        testGame.moveResolveCollisions();
        assertEquals(10, character.getPosition().getPositionX());

        character.setVelocityX(-2);
        testGame.moveResolveCollisions();
        assertEquals(9, character.getPosition().getPositionX());
    }

    @Test
    void testMoveResolveCollisionsY() {
        testGame.addBlock(testBlock4);
        testGame.addBlock(testHazard);
        Character character = testGame.getCharacter();

        character.setVelocityY(2);
        testGame.moveResolveCollisions();
        assertEquals(20, character.getPosition().getPositionY());

        character.setVelocityY(-2);
        testGame.setInvulnerabilityEnd(1);
        testGame.moveResolveCollisions();
        testGame.moveResolveCollisions();
        assertEquals(16, character.getPosition().getPositionY());

        character.setVelocityY(1);
        testGame.setTime(2);
        testGame.moveResolveCollisions();
        assertTrue(testGame.isEnded());
    }

    @Test
    void testMoveResolveCollisionsDiagonal() {
        testGame.addBlock(testBlock5);
        Character character = testGame.getCharacter();

        character.setVelocityX(2);
        character.setVelocityY(-2);
        testGame.moveResolveCollisions();
        assertEquals(11, character.getPosition().getPositionX());
        assertEquals(18, character.getPosition().getPositionY());

        testGame.addBlock(testBlock6);
        character.setPosition(new Position(10, 20));
        testGame.moveResolveCollisions();
        assertEquals(12, character.getPosition().getPositionX());
        assertEquals(17, character.getPosition().getPositionY());
    }

    @Test
    void testCheckCollisionList() {
        testGame.addBlock(testBlock1);
        testGame.addBlock(testBlock2);
        assertEquals(0, testGame.checkCollisionList(testGame.getCharacter().getPosition()).size());
        assertEquals(1, testGame.checkCollisionList(new Position(11, 20)).size());
        assertTrue(testGame.checkCollisionList(new Position(11, 20)).contains(testBlock1));
        assertEquals(1, testGame.checkCollisionList(new Position(8, 20)).size());
        assertTrue(testGame.checkCollisionList(new Position(8, 20)).contains(testBlock2));
    }

    @Test
    void testOnPlatform() {
        assertFalse(testGame.onPlatform(testGame.getCharacter().getPosition()));
        testGame.addBlock(testBlock3);
        assertFalse(testGame.onPlatform(testGame.getCharacter().getPosition()));
        testGame.addBlock(testBlock4);
        assertTrue(testGame.onPlatform(testGame.getCharacter().getPosition()));
    }

    @Test
    void testAtBoundary() {
        assertFalse(testGame.atBoundary(testGame.getCharacter().getPosition()));

        assertTrue(testGame.atBoundary(new Position(0, 20)));
        assertTrue(testGame.atBoundary(new Position(testGame.getMaxX(), 10)));
        assertTrue(testGame.atBoundary(new Position(10, 0)));

        assertFalse(testGame.atBoundary(new Position(1, 20)));
        assertFalse(testGame.atBoundary(new Position(testGame.getMaxX() - 1, 10)));
        assertFalse(testGame.atBoundary(new Position(10, 1)));

        assertTrue(testGame.atBoundary(new Position(-1, 20)));
        assertTrue(testGame.atBoundary(new Position(testGame.getMaxX() + 1, 10)));
        assertTrue(testGame.atBoundary(new Position(10, -1)));
    }

    @Test
    void testAtBottomBoundary() {
        assertFalse(testGame.atBottomBoundary(testGame.getCharacter().getPosition()));

        assertTrue(testGame.atBottomBoundary(new Position(10, testGame.getMaxY())));
        assertFalse(testGame.atBottomBoundary(new Position(10, testGame.getMaxY() - 1)));
        assertTrue(testGame.atBottomBoundary(new Position(10, testGame.getMaxY() + 1)));
    }

    @Test
    void testCollectPowerUp() {
        testGame.addBlock(testPowerUp1);
        testGame.addBlock(testPowerUp2);
        testGame.addBlock(testPowerUp3);
        testGame.addBlock(testPowerUp4);

        assertTrue(testGame.collectPowerUp(testPowerUp1));
        assertEquals(1,testGame.getInventory().size());
        assertEquals(3,testGame.getBlocks().size());
        assertEquals("1",testPowerUp1.getKeyAssignment());
        assertFalse(testGame.getAvailableKeys().contains("1"));

        assertTrue(testGame.collectPowerUp(testPowerUp2));
        assertEquals(2,testGame.getInventory().size());
        assertEquals(2,testGame.getBlocks().size());
        assertEquals("2",testPowerUp2.getKeyAssignment());
        assertFalse(testGame.getAvailableKeys().contains("2"));

        assertTrue(testGame.collectPowerUp(testPowerUp3));
        assertEquals(3,testGame.getInventory().size());
        assertEquals(1,testGame.getBlocks().size());
        assertEquals("3",testPowerUp3.getKeyAssignment());
        assertFalse(testGame.getAvailableKeys().contains("3"));

        assertFalse(testGame.collectPowerUp(testPowerUp4));
        assertEquals(3,testGame.getInventory().size());
        assertEquals(1,testGame.getBlocks().size());
        assertNull(testPowerUp4.getKeyAssignment());
    }

    @Test
    void testUseItem() {
        testGame.collectPowerUp(testPowerUp1);
        testGame.collectPowerUp(testPowerUp2);
        testGame.collectPowerUp(testPowerUp3);

        testGame.usePowerUp(testPowerUp1);
        testGame.usePowerUp(testPowerUp2);
        assertEquals(30, testGame.getSpeedEnd());
        assertEquals(30, testGame.getInvulnerabilityEnd());
        assertNull(testPowerUp1.getKeyAssignment());
        assertNull(testPowerUp2.getKeyAssignment());
        assertEquals(2,testGame.getAvailableKeys().size());
        assertEquals(1,testGame.getInventory().size());
        assertEquals(2, testGame.getCharacter().getVelocityXMultiplier());

        testGame.usePowerUp(testPowerUp3);
        assertEquals(60, testGame.getSpeedEnd());
        assertNull(testPowerUp3.getKeyAssignment());
        assertEquals(3,testGame.getAvailableKeys().size());
        assertEquals(0,testGame.getInventory().size());

        testGame.collectPowerUp(testPowerUp4);
        assertEquals("1",testPowerUp4.getKeyAssignment());
        assertEquals(60, testGame.getInvulnerabilityEnd());
        assertNull(testPowerUp4.getKeyAssignment());
        assertEquals(3,testGame.getAvailableKeys().size());
        assertEquals(0,testGame.getInventory().size());
    }

    @Test
    void testAddBlock() {
        testGame.addBlock(testBlock1);
        assertTrue(testGame.getBlocks().contains(testBlock1));
        assertEquals(1, testGame.getBlocks().size());

        testGame.addBlock(testBlock2);
        testGame.addBlock(testBlock3);
        assertTrue(testGame.getBlocks().contains(testBlock2));
        assertTrue(testGame.getBlocks().contains(testBlock3));
        assertEquals(3, testGame.getBlocks().size());
    }
}