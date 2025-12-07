package com.comp2042.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ScoreTest {

    @Test
    public void testInitialScoreIsZero() {
        Score score = new Score();
        assertEquals(0, score.scoreProperty().get(), "初始分数应该为 0");
    }

    @Test
    public void testAddScore() {
        Score score = new Score();
        score.add(100);
        assertEquals(100, score.scoreProperty().get(), "加 100 分后应该是 100");
    }

    @Test
    public void testAddMultipleTimes() {
        Score score = new Score();
        score.add(50);
        score.add(70);
        assertEquals(120, score.scoreProperty().get(), "50 + 70 应该等于 120");
    }

    @Test
    public void testResetScore() {
        Score score = new Score();
        score.add(200);
        score.reset();
        assertEquals(0, score.scoreProperty().get(), "重置后应该回到 0");
    }
}
