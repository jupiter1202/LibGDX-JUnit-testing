package de.jupiter1202.test;

import com.badlogic.gdx.math.Vector2;

/** A small domain object used to demonstrate platform-independent unit tests. */
public final class Player {
    public static final int MAX_HEALTH = 100;

    private final Vector2 position;
    private int health = MAX_HEALTH;

    public Player(float x, float y) {
        position = new Vector2(x, y);
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public int getHealth() {
        return health;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void moveBy(float x, float y) {
        position.add(x, y);
    }

    public void takeDamage(int damage) {
        if (damage < 0) {
            throw new IllegalArgumentException("damage must not be negative");
        }
        health = Math.max(0, health - damage);
    }
}
