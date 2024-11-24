package club.hack.painbox.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class Animation {
    private Texture[] textures;
    private float speed;
    int currentIndex = 0;
    float elapsed = 0f;
    String mode = "loop"; // "loop", "once"

    public Animation(Texture[] textures, float speed){
        this(textures, speed, "loop");
    }

    public Animation(Texture[] textures, float speed, String mode) {
        this.textures = textures;
        this.speed = speed;
        this.mode = mode;
    }

    public Texture get(){
        float delta = Gdx.graphics.getDeltaTime();
        if(mode.equals("loop")) {
            elapsed += delta;
            if (elapsed > speed) {
                currentIndex++;
                currentIndex = currentIndex % textures.length;
                elapsed = 0f;
            }
        }else if(mode.equals("once")) {
            elapsed += delta;
            if (elapsed > speed) {
                currentIndex++;
                if(currentIndex >= textures.length) {
                    currentIndex = textures.length - 1;
                }
                elapsed = 0f;
            }
        }
        return textures[currentIndex];
    }

    public void reset(){
        currentIndex = 0;
        elapsed = 0f;
    }
}
