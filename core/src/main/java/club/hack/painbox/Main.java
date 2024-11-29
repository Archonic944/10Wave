package club.hack.painbox;

import club.hack.painbox.util.Animation;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import club.hack.painbox.data.BulletData;
import club.hack.painbox.data.EnemyData;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.HashMap;

public class Main implements ApplicationListener {
    // Textures and other variables...
    Texture backgroundTexture;
    Texture emptyTexture;
    Texture filledTexture;
    Animation playerAnimation;
    Animation eyeAnimation;
    Texture playerTexture;
    Texture enemyTexture;
    Texture bulletTexture;
    Texture heartTexture;
    Texture shrapnelTexture;
    Texture heartShadowTexture;
    ParticleEffect deathParticles;
    ParticleEffect healthParticles;

    Sprite playerSprite;
    Array<Sprite> enemySprites;
    Array<Sprite> bulletSprites;
    static int MAX_WAVE = 10;

    Sound hurtSound;
    Sound shootSound;
    Sound diagonalShoot;
    Music music;

    Vector2 touchPos;

    HashMap<Sprite, BulletData> bulletDataMap;
    HashMap<Sprite, EnemyData> enemyDataMap;

    // New HashMaps for enemy movement
    HashMap<Sprite, Float> enemyPhaseMap;
    HashMap<Sprite, Float> enemyOriginalXMap;

    Rectangle playerRectangle;
    Rectangle enemyRectangle;
    Rectangle bulletRectangle;

    SpriteBatch spriteBatch;
    FitViewport viewport;

    int maxPlayerHealth = 3;
    int playerHealth;

    BitmapFont font;
    GlyphLayout layout;

    ShapeRenderer shapeRenderer;

    boolean showHitboxes;
    boolean isFlippedVertically;

    int enemiesPerWave;
    int currentWave;

    private boolean isShaking;
    private float shakeDuration;
    private float shakeMagnitude;
    private float shakeTimer;

    // Snapping Variables
    private boolean isSnapping;
    private final float snapSpeed = 26.5f;

    // Enemy movement parameters
    private final float enemyAmplitude = 1.45f;   // Amplitude of sine wave
    private final float enemyFrequency = 0.5f;   // Frequency in Hz

    // Timer and Game Over Variables
    private float elapsedTime;
    private boolean isGameOver;
    private boolean isMaxWaveReached;

    @Override
    public void create() {
        // Initialize resources
        backgroundTexture = new Texture("Background.png");
        playerAnimation = new Animation(new Texture[]{new Texture("Player-1.png"), new Texture("Player-2.png")}, 0.2f);
        playerTexture = new Texture("Player.png");
        eyeAnimation = new Animation(new Texture[]{
            new Texture("Eye-1.png"),
            new Texture("Eye-2.png"),
            new Texture("Eye-3.png"),
            new Texture("Eye-4.png"),
            new Texture("Eye-4.png"),
            new Texture("Eye-5.png")
        }, 0.4f, "once");
        enemyTexture = new Texture("Enemy.png");
        bulletTexture = new Texture("Bullet.png");
        shrapnelTexture = new Texture("Shrapnel.png");
        heartTexture = new Texture("HeartFull.png");
        heartShadowTexture = new Texture("HeartShadow.png");

        deathParticles = new ParticleEffect();
        deathParticles.load(Gdx.files.internal("Skulls.p"), Gdx.files.internal(""));
        deathParticles.allowCompletion();
        healthParticles = new ParticleEffect();
        healthParticles.load(Gdx.files.internal("HealthParticle.p"), Gdx.files.internal(""));
        healthParticles.allowCompletion();
        emptyTexture = new Texture("EmptyContainer.png");
        filledTexture = new Texture("FilledContainer.png");

        hurtSound = Gdx.audio.newSound(Gdx.files.internal("Hurt.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("Music.mp3"));
        shootSound = Gdx.audio.newSound(Gdx.files.internal("Shoot.mp3"));
        diagonalShoot = Gdx.audio.newSound(Gdx.files.internal("DiagonalShoot.mp3"));

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(16 / 1.2f, 10 / 1.2f);
        touchPos = new Vector2();

        font = new BitmapFont();
        font.setUseIntegerPositions(false);
        font.getData().setScale(viewport.getWorldHeight() / Gdx.graphics.getHeight());
        layout = new GlyphLayout();
        shapeRenderer = new ShapeRenderer();

        music.setLooping(true);
        music.setVolume(0.5f);
        music.play();

        // Web-specific config
        Gdx.input.setCatchKey(Input.Keys.UP, true);
        Gdx.input.setCatchKey(Input.Keys.LEFT, true);
        Gdx.input.setCatchKey(Input.Keys.RIGHT, true);

        playerRectangle = new Rectangle();
        enemyRectangle = new Rectangle();
        bulletRectangle = new Rectangle();

        newGame(); // Start a new game after initializing resources
    }

    void newGame() {
        // Initialize or reset game variables
        playerHealth = maxPlayerHealth;
        isShaking = false;
        shakeDuration = 0f;
        shakeMagnitude = 0.5f;
        shakeTimer = 0f;
        isFlippedVertically = false;
        isSnapping = false;
        moving = false;
        currentWave = 1;
        enemiesPerWave = 5;
        isGameOver = false;
        isMaxWaveReached = false;
        elapsedTime = 0f;

        enemySprites = new Array<>();
        bulletSprites = new Array<>();
        bulletDataMap = new HashMap<>();
        enemyDataMap = new HashMap<>();
        enemyPhaseMap = new HashMap<>();
        enemyOriginalXMap = new HashMap<>();

        playerSprite = new Sprite(playerAnimation.get());
        playerSprite.setSize(1, 1);
        playerSprite.setPosition((viewport.getWorldWidth() - playerSprite.getWidth()) / 2, 0);

        spawnWave(); // Spawn the first wave of enemies
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        input();
        logic(delta);
        draw();
    }

    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void dispose() {
        // Dispose resources
        backgroundTexture.dispose();
        enemyTexture.dispose();
        bulletTexture.dispose();
        shrapnelTexture.dispose();
        emptyTexture.dispose();
        filledTexture.dispose();
        hurtSound.dispose();
        shootSound.dispose();
        diagonalShoot.dispose();
        music.dispose();
        spriteBatch.dispose();
        font.dispose();
        shapeRenderer.dispose();
        deathParticles.dispose();
        healthParticles.dispose();
    }

    private void spawnWave() {
        if (currentWave > MAX_WAVE) {
            isMaxWaveReached = true;
            System.out.println("Maximum wave reached. No more enemies will spawn.");
            return;
        }

        for (int i = 0; i < enemiesPerWave; i++) {
            Sprite enemySprite = new Sprite(enemyTexture);
            enemySprite.setSize(1, 1);
            float x = MathUtils.random(0f, viewport.getWorldWidth() - enemySprite.getWidth());
            float y = MathUtils.random(2f, viewport.getWorldHeight() - enemySprite.getHeight() - 2f);
            enemySprite.setPosition(x, y);
            enemySprites.add(enemySprite);
            enemyDataMap.put(enemySprite, new EnemyData(1));

            // Initialize movement parameters for the enemy
            enemyOriginalXMap.put(enemySprite, enemySprite.getX());
            enemyPhaseMap.put(enemySprite, MathUtils.random(0f, 2 * MathUtils.PI));
        }

        System.out.println("Wave " + currentWave + " spawned with " + enemiesPerWave + " enemies.");
        enemiesPerWave = enemiesPerWave + 1;
        eyeAnimation.reset();
    }

    boolean moving = false;

    private void input() {
        if (isGameOver || isMaxWaveReached) {
            // Handle input for restarting the game
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                newGame();
                System.out.println("Game restarted.");
            }
            return; // Skip other inputs when game is over
        }

        float speed = 6.75f;
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            playerSprite.translateX(speed * delta);
            moving = true;
        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            playerSprite.translateX(-speed * delta);
            moving = true;
        } else {
            moving = false;
        }


        // Handle Snapping
        if(!isSnapping) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                snapToTop();
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                snapToBottom();
            }


            // Handle Shooting
            // Player cannot shoot while snapping
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                if (!isFlippedVertically) {
                    createBullet(0, 1, false);
                    shootSound.play(0.3f);
                } else {
                    createBullet(0, -1, false);
                    shootSound.play(0.3f);
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                if (!isFlippedVertically) {
                    createBullet(-1, 1, false);
                    diagonalShoot.play(0.3f);
                } else {
                    createBullet(-1, -1, false);
                    diagonalShoot.play(0.3f);
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                if (!isFlippedVertically) {
                    createBullet(1, 1, false);
                    diagonalShoot.play(0.3f);
                } else {
                    createBullet(1, -1, false);
                    diagonalShoot.play(0.3f);
                }
            }
        }

//        // Toggle hitbox rendering with the H key
//        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
//            showHitboxes = !showHitboxes;
//            System.out.println("Hitbox Rendering: " + (showHitboxes ? "ON" : "OFF"));
//        }
    }

    private void snapToTop() {
        isSnapping = true;
        if (!isFlippedVertically) {
            playerSprite.setFlip(false, true);
            isFlippedVertically = true;
            System.out.println("Player snapping to top.");
        }
    }

    private void snapToBottom() {
        isSnapping = true;
        if (isFlippedVertically) {
            playerSprite.setFlip(false, false);
            isFlippedVertically = false;
            System.out.println("Player snapping to bottom.");
        }
    }

    /**
     * Updates snap. Call every frame no matter what.
     */
    private void updateSnap(float delta){
        if(isSnapping) {
            if (isFlippedVertically) {
                float newY = playerSprite.getY() + snapSpeed * delta;
                if (newY > viewport.getWorldHeight()) {
                    playerSprite.setY(viewport.getWorldHeight() - playerSprite.getHeight());
                    isSnapping = false;
                }else playerSprite.setY(newY);
            }else{
                float newY = playerSprite.getY() - snapSpeed * delta;
                if (newY < 0) {
                    playerSprite.setY(0);
                    isSnapping = false;
                }else playerSprite.setY(newY);
            }
        }
    }

    private void logic(float delta) {
        if (isGameOver) {
            return; // Skip game logic if game is over
        }

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        // Handle Snapping Movement
        updateSnap(delta);

        // Clamp Player Position Horizontally
        float playerWidth = playerSprite.getWidth();
        float playerHeight = playerSprite.getHeight();
        playerSprite.setX(MathUtils.clamp(playerSprite.getX(), 0, worldWidth - playerWidth));

        // Update Player Rectangle for Collision Detection
        playerRectangle.setWidth(playerSprite.getWidth() * 0.4f);
        playerRectangle.setHeight(playerSprite.getHeight());
        playerRectangle.setCenter(playerSprite.getX() + playerSprite.getWidth() / 2, playerSprite.getY() + playerSprite.getHeight() / 2);

        // Update Timer if Max Wave Not Reached
        if (!isMaxWaveReached) {
            elapsedTime += delta;
        }

        // Update Bullets
        for (int i = bulletSprites.size - 1; i >= 0; i--) {
            Sprite bulletSprite = bulletSprites.get(i);
            BulletData bulletData = bulletDataMap.get(bulletSprite);
            if (bulletData == null) {
                bulletSprites.removeIndex(i);
                continue;
            }

            float bulletSpeed = MathUtils.random(8f, 10f);
            bulletSprite.translate(bulletData.speed.x * bulletSpeed * delta,
                bulletData.speed.y * bulletSpeed * delta);

            bulletRectangle.set(bulletSprite.getX(), bulletSprite.getY(),
                bulletSprite.getWidth(), bulletSprite.getHeight());

            boolean bounced = false;

            if (bulletSprite.getX() < 0) {
                bulletSprite.setX(0);
                if (!bulletData.isShrapnel)
                    bulletData.speed.x = -bulletData.speed.x + MathUtils.random(-1f, 1f);
                bounced = true;
            }

            if (bulletSprite.getX() + bulletSprite.getWidth() > worldWidth) {
                bulletSprite.setX(worldWidth - bulletSprite.getWidth());
                if (!bulletData.isShrapnel)
                    bulletData.speed.x = -bulletData.speed.x + MathUtils.random(-1f, 1f);
                bounced = true;
            }

            if (bulletSprite.getY() + bulletSprite.getHeight() > worldHeight) {
                bulletSprite.setY(worldHeight - bulletSprite.getHeight());
                if (!bulletData.isShrapnel)
                    bulletData.speed.y = -bulletData.speed.y + MathUtils.random(-0.3f, 0.5f);
                bounced = true;
            }

            if (bulletSprite.getY() < 0) {
                bulletSprite.setY(0);
                if (!bulletData.isShrapnel)
                    bulletData.speed.y = -bulletData.speed.y + MathUtils.random(-0.3f, 0.5f);
                bounced = true;
            }

            if (bounced) {
                bulletData.reduceHealth();
                if (bulletData.health <= 0 || bulletData.isShrapnel) {
                    bulletSprites.removeIndex(i);
                    bulletDataMap.remove(bulletSprite);
                    if (!bulletData.isShrapnel) {
                        spawnShrapnel(bulletSprite.getX(), bulletSprite.getY());
                    }
                    System.out.println("Bullet removed after exceeding bounces.");
                }
            }
        }

        // Handle Bullet-Enemy Collisions
        for (int i = bulletSprites.size - 1; i >= 0; i--) {
            Sprite bulletSprite = bulletSprites.get(i);
            BulletData bulletData = bulletDataMap.get(bulletSprite);
            if (bulletData == null) {
                System.out.println("BulletData is null, continuing");
                continue;
            }

            bulletRectangle.set(bulletSprite.getX(), bulletSprite.getY(),
                bulletSprite.getWidth(), bulletSprite.getHeight());

            for (int j = enemySprites.size - 1; j >= 0; j--) {
                Sprite enemySprite = enemySprites.get(j);
                enemyRectangle.setWidth(enemySprite.getWidth() * 0.6f);
                enemyRectangle.setHeight(enemySprite.getHeight() * 0.8f);
                enemyRectangle.setCenter(enemySprite.getX() + enemySprite.getWidth() / 2, enemySprite.getY() + enemySprite.getHeight() / 2);

                if (bulletRectangle.overlaps(enemyRectangle) && !bulletData.isShrapnel) {
                    EnemyData enemyData = enemyDataMap.get(enemySprite);
                    if (enemyData != null) {
                        boolean isAlive = enemyData.takeDamage();
                        if (!isAlive) {
                            enemySprites.removeIndex(j);
                            enemyDataMap.remove(enemySprite);
                            enemyPhaseMap.remove(enemySprite);
                            enemyOriginalXMap.remove(enemySprite);
                            deathParticles.setPosition(enemySprite.getX() + enemySprite.getWidth() / 2, enemySprite.getY() + enemySprite.getHeight() / 2);
                            deathParticles.setDuration(2);
                            deathParticles.start();
                            System.out.println("Enemy defeated!");
                        }
                    }
                    bulletSprites.removeIndex(i);
                    bulletDataMap.remove(bulletSprite);
                    hurtSound.play(0.3f);
                    break;
                }
            }
        }

        if (!isMaxWaveReached) {
            // Handle Bullet-Player Collisions
            for (int i = bulletSprites.size - 1; i >= 0; i--) {
                Sprite bulletSprite = bulletSprites.get(i);
                BulletData bulletData = bulletDataMap.get(bulletSprite);
                bulletRectangle.set(bulletSprite.getX(), bulletSprite.getY(),
                    bulletSprite.getWidth(), bulletSprite.getHeight());
                if (bulletData == null) {
                    continue;
                }

                if (bulletRectangle.overlaps(playerRectangle)) {
                    playerHealth--;
                    Vector2 pos = heartLocation(playerHealth);
                    healthParticles.setPosition(pos.x, pos.y);
                    healthParticles.start();
                    healthParticles.setDuration(2);
                    hurtSound.play(0.3f);
                    bulletSprites.removeIndex(i);
                    bulletDataMap.remove(bulletSprite);

                    System.out.println("Player hit by bullet! Remaining Health: " + playerHealth);

                    startScreenShake(0.3f, 0.2f);

                    if (playerHealth <= 0) {
                        isGameOver = true;
                        System.out.println("Player has been defeated!");
                    }
                }
            }
        }

        // Check if All Enemies are Defeated to Spawn New Wave
        if (enemySprites.size == 0 && !isMaxWaveReached) {
            currentWave++;
            spawnWave();
            if (currentWave > MAX_WAVE) {
                isMaxWaveReached = true;
                System.out.println("Reached Wave 10. Timer frozen and no more enemies will spawn.");
            }
        }

        // Update Enemies' Positions for Sinusoidal Movement
        for (Sprite enemySprite : enemySprites) {
            Float currentPhase = enemyPhaseMap.get(enemySprite);
            Float originalX = enemyOriginalXMap.get(enemySprite);

            if (currentPhase == null || originalX == null) {
                // Safety check: initialize if missing
                originalX = enemySprite.getX();
                currentPhase = MathUtils.random(0f, 2 * MathUtils.PI);
                enemyOriginalXMap.put(enemySprite, originalX);
                enemyPhaseMap.put(enemySprite, currentPhase);
            }

            // Update phase based on frequency and delta time
            currentPhase += 2 * MathUtils.PI * enemyFrequency * delta;

            // Calculate new X position
            float newX = originalX + enemyAmplitude * MathUtils.sin(currentPhase);

            // Optionally, clamp to screen bounds to prevent enemies from moving off-screen
            newX = MathUtils.clamp(newX, 0, worldWidth - enemySprite.getWidth());

            enemySprite.setX(newX);
            enemyPhaseMap.put(enemySprite, currentPhase);
        }

        // Handle Screen Shake
        if (isShaking) {
            shakeTimer += delta;
            if (shakeTimer < shakeDuration) {
                float shakeOffsetX = MathUtils.random(-shakeMagnitude, shakeMagnitude);
                float shakeOffsetY = MathUtils.random(-shakeMagnitude, shakeMagnitude);
                viewport.getCamera().position.set(viewport.getWorldWidth() / 2 + shakeOffsetX,
                    viewport.getWorldHeight() / 2 + shakeOffsetY,
                    0);
                viewport.getCamera().update();
            } else {
                viewport.getCamera().position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
                viewport.getCamera().update();
                isShaking = false;
            }
        }
    }

    /**
     * Creates a bullet with the specified direction.
     *
     * @param directionX Horizontal direction component (-1, 0, 1)
     * @param directionY Vertical direction component (-1, 0, 1)
     * @param isShrapnel Indicates whether the bullet is shrapnel
     */
    private void createBullet(float directionX, float directionY, boolean isShrapnel) {
        float bulletWidth = isShrapnel ? 0.15f : 0.35f;
        float bulletHeight = isShrapnel ? 0.15f : 0.35f;

        Sprite bulletSprite = new Sprite(isShrapnel ? shrapnelTexture : bulletTexture);
        bulletSprite.setSize(bulletWidth, bulletHeight);
        bulletDataMap.put(bulletSprite, new BulletData(new Vector2(directionX, directionY).nor(), 3, isShrapnel));
        bulletSprites.add(bulletSprite);

        // Set bullet starting position based on player's position
        if (!isFlippedVertically) {
            // Player is at the bottom; fire upwards
            bulletSprite.setCenter(
                playerSprite.getX() + playerSprite.getWidth() / 2,
                playerSprite.getY() + playerSprite.getHeight() + 0.5f
            );
        } else {
            // Player is at the top; fire downwards
            bulletSprite.setCenter(
                playerSprite.getX() + playerSprite.getWidth() / 2,
                playerSprite.getY() - 0.5f
            );
        }
    }

    private void spawnShrapnel(float x, float y) {
        // 8 pieces of shrapnel spawn and go outwards
        for (int i = 0; i < 8; i++) {
            float angle = i * 45f; // 360 / 8 = 45 degrees between each shrapnel
            Vector2 direction = new Vector2(MathUtils.cosDeg(angle), MathUtils.sinDeg(angle)).nor();
            Sprite shrapnelSprite = new Sprite(shrapnelTexture);
            shrapnelSprite.setSize(0.15f, 0.15f);
            shrapnelSprite.setCenter(x, y);
            bulletDataMap.put(shrapnelSprite, new BulletData(direction, 1, true));
            bulletSprites.add(shrapnelSprite);
        }
    }

    void refreshAnimations() {
        if (moving) {
            updateSprite(playerAnimation, playerSprite);
        } else {
            playerSprite.setTexture(playerTexture);
        }
        enemySprites.forEach((s) -> updateSprite(eyeAnimation, s));
    }

    void updateSprite(Animation a, Sprite s) {
        Texture t = a.get();
        if (s.getTexture() != t) s.setTexture(t);
    }

    private void draw() {
        refreshAnimations();
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin();

        // Draw Background
        spriteBatch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        // Draw Player
        playerSprite.draw(spriteBatch);

        // Draw Enemies
        for (Sprite enemySprite : enemySprites) {
            enemySprite.draw(spriteBatch);
        }

        // Draw Bullets
        for (Sprite bulletSprite : bulletSprites) {
            bulletSprite.draw(spriteBatch);
        }

        // Draw Health Hearts
        for (int i = 0; i < maxPlayerHealth; i++) {
            Vector2 heartLocation = heartLocation(i);
            spriteBatch.draw(heartShadowTexture, heartLocation.x + 0.05f, heartLocation.y - 0.05f, 0.8f, 0.8f);
            if (i < playerHealth) {
                spriteBatch.draw(heartTexture, heartLocation.x, heartLocation.y, 0.8f, 0.8f);
            }
        }

        // Draw Wave Containers (Top Center)
        for (int i = 0; i < 10; i++) {
            if (i < currentWave - 1) {
                spriteBatch.draw(filledTexture, 7f + i * 0.6f, viewport.getWorldHeight() - 0.6f, 0.5f, 0.5f);
            } else {
                spriteBatch.draw(emptyTexture, 7f + i * 0.6f, viewport.getWorldHeight() - 0.6f, 0.5f, 0.5f);
            }
        }

        // Draw Timer at Top Center (seconds and minutes both zero padded, ##:##)
        String timerText = zeroPadded((int) elapsedTime / 60) + ":" + zeroPadded((int) elapsedTime % 60);
        layout.setText(font, timerText);
        font.setColor(isMaxWaveReached ? Color.GREEN : Color.WHITE);
        font.getData().setScale(0.1f);
        font.draw(spriteBatch, timerText,
            (viewport.getWorldWidth() - layout.width) / 2,
            viewport.getWorldHeight() - 0.2f); // Slightly below the top edge
        // Draw "Press R to Restart" below "Game Over"
        if(isMaxWaveReached) drawRestartText();

        // Draw Particles
        deathParticles.draw(spriteBatch, Gdx.graphics.getDeltaTime());
        healthParticles.draw(spriteBatch, Gdx.graphics.getDeltaTime());

        // Draw "Game Over" and "Press R to Restart" if applicable
        if (isGameOver && !isMaxWaveReached) {
            String gameOverText = "Game   Over   :(";
            layout.setText(font, gameOverText);
            font.setColor(Color.RED);
            font.getData().setScale(0.1f); // Reduced font scale for visibility
            font.draw(spriteBatch, gameOverText,
                (viewport.getWorldWidth() - layout.width) / 2,
                (viewport.getWorldHeight() + layout.height) / 2);

            // Draw "Press R to Restart" below "Game Over"
            drawRestartText();

            font.getData().setScale(1f); // Reset font scale
        }

        spriteBatch.end();

        // Render Hitboxes if Enabled
        if (showHitboxes) {
            renderHitboxes();
        }
    }

    private void drawRestartText() {
        String restartText = "Press R to Restart";
        layout.setText(font, restartText);
        font.setColor(Color.YELLOW);
        font.getData().setScale(0.08f); // Slightly smaller font scale
        font.draw(spriteBatch, restartText,
            (viewport.getWorldWidth() - layout.width) / 2,
            (viewport.getWorldHeight() + layout.height) / 2 - layout.height * 1.5f);
    }

    private Vector2 heartLocation(int i) {
        return new Vector2(0.1f + i * 0.8f, viewport.getWorldHeight() - 0.9f);
    }

    private void renderHitboxes() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);

        // Player Hitbox
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(playerRectangle.x, playerRectangle.y, playerRectangle.width, playerRectangle.height);

        // Enemy Hitboxes
        shapeRenderer.setColor(Color.RED);
        for (Sprite enemySprite : enemySprites) {
            EnemyData enemyData = enemyDataMap.get(enemySprite);
            if (enemyData != null) {
                Rectangle enemyRect = new Rectangle(enemySprite.getX(), enemySprite.getY(),
                    enemySprite.getWidth(), enemySprite.getHeight());
                shapeRenderer.rect(enemyRect.x, enemyRect.y, enemyRect.width, enemyRect.height);
            }
        }

        // Bullet Hitboxes
        shapeRenderer.setColor(Color.BLUE);
        for (Sprite bulletSprite : bulletSprites) {
            Rectangle bulletRect = new Rectangle(bulletSprite.getX(), bulletSprite.getY(),
                bulletSprite.getWidth(), bulletSprite.getHeight());
            shapeRenderer.rect(bulletRect.x, bulletRect.y, bulletRect.width, bulletRect.height);
        }

        shapeRenderer.end();
    }

    private String zeroPadded(int i) {
        if (i < 10) return "0" + i;
        return "" + i;
    }

    private void startScreenShake(float duration, float magnitude) {
        isShaking = true;
        shakeDuration = duration;
        shakeMagnitude = magnitude;
        shakeTimer = 0f;
    }
}
