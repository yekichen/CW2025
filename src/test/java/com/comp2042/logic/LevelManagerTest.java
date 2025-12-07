package com.comp2042.logic;

import com.comp2042.logic.level.LevelManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LevelManagerTest {

    @Test
    public void testInitialLevelIsOne() {
        LevelManager lm = new LevelManager();
        assertEquals(1, lm.getLevel());
    }

    @Test
    public void testLevelUpAfter10Lines() {
        LevelManager lm = new LevelManager();
        lm.addClearedLines(10);
        assertEquals(2, lm.getLevel());
    }

    @Test
    public void testFallSpeedIncreasesWithLevel() {
        LevelManager lm = new LevelManager();
        double initialSpeed = lm.getFallSpeed();

        lm.addClearedLines(20); // 升两级
        double newSpeed = lm.getFallSpeed();

        assertTrue(newSpeed > initialSpeed, "等级越高，下落速率应该越快（数值越大）");
    }
}
