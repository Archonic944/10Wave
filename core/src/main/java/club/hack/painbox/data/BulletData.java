package club.hack.painbox.data;

import com.badlogic.gdx.math.Vector2;

/**
 * Stores bullet speed (Vector2d) and
 */
public class BulletData {
    public Vector2 speed;
    public int health;
    public boolean isShrapnel;
    public BulletData(Vector2 speed, int health, boolean isShrapnel) {
        this.speed = speed;
        this.health = health;
        this.isShrapnel = isShrapnel;
    }

    public BulletData(Vector2 speed, int health) {
        this(speed, health, false);
    }

    public void reduceHealth() {
        health--;
    }
}
