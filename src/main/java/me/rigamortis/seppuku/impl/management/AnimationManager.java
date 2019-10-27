package me.rigamortis.seppuku.impl.management;

import me.rigamortis.seppuku.api.animation.Animation;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * created by noil on 8/17/2019 at 4:29 PM
 */
public final class AnimationManager {

    private final List<Animation> animations = new CopyOnWriteArrayList<>();

    public AnimationManager() {
        (new Thread() {
            public void run() {
                AnimationManager.this.update();
            }
        }).start();
    }

    private void update() {
        while (true) {
            long beforeAnimation = System.nanoTime();

            if (this.animations.size() > 0)
                this.animations.forEach(Animation::update);

            int milliseconds = (int) ((System.nanoTime() - beforeAnimation) / 1000000L);

            try {
                TimeUnit.MILLISECONDS.sleep((16 - milliseconds));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void unload() {
        this.animations.clear();
    }

    public void addAnimation(Animation animation) {
        this.animations.add(animation);
    }

    public List<Animation> getAnimations() {
        return animations;
    }
}
