package club.hack.painbox.data;

import com.badlogic.gdx.math.MathUtils;

public class EnemyData {
    private int health;

    // Movement variables for wiggling effect
    private float time; // Keeps track of time to drive oscillation
    private final float wiggleSpeed; // Speed of the wiggle
    private final float wiggleAmplitude; // Amplitude of the wiggle (how far they move)

    public EnemyData(int health) {
        this.health = health;
        this.time = MathUtils.random(0f, 1f); // Random start to avoid enemies moving in sync
        this.wiggleSpeed = MathUtils.random(1.5f, 3.0f); // Randomized speed of movement for variation
        this.wiggleAmplitude = MathUtils.random(0.1f, 0.3f); // Randomized wiggle amplitude for variation
    }

    /**
     * Reduces the enemy's health by 1.
     * @return true if the enemy is still alive, false if health <= 0
     */
    public boolean takeDamage() {
        health--;
        return health > 0;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    // Update the time for the wiggle effect
    public void update(float delta) {
        time += delta; // Increment the time for smooth movement
    }

    // Get the X-axis movement offset for wiggling
    public float getXMovementOffset() {
        return MathUtils.sin(time * wiggleSpeed) * wiggleAmplitude; // Oscillate left-right
    }

    // Get the Y-axis movement offset for wiggling
    public float getYMovementOffset() {
        return MathUtils.cos(time * wiggleSpeed) * wiggleAmplitude; // Oscillate up-down
    }
}
