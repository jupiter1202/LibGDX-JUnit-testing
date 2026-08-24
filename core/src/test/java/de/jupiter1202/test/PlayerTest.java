package de.jupiter1202.test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PlayerTest {
    @Test
    void movesRelativeToItsCurrentPosition() {
        Player player = new Player(10f, 20f);

        player.moveBy(3f, -5f);

        assertAll(
            () -> assertEquals(13f, player.getX()),
            () -> assertEquals(15f, player.getY())
        );
    }

    @Test
    void damageNeverReducesHealthBelowZero() {
        Player player = new Player(0f, 0f);

        player.takeDamage(Player.MAX_HEALTH + 25);

        assertAll(
            () -> assertEquals(0, player.getHealth()),
            () -> assertFalse(player.isAlive())
        );
    }

    @Test
    void rejectsNegativeDamage() {
        Player player = new Player(0f, 0f);

        assertThrows(IllegalArgumentException.class, () -> player.takeDamage(-1));
    }
}
