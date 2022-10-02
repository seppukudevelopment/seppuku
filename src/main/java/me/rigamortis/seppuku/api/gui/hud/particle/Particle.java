package me.rigamortis.seppuku.api.gui.hud.particle;

import me.rigamortis.seppuku.api.util.ColorUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import javax.vecmath.Vector2f;
import java.util.concurrent.ThreadLocalRandom;

public class Particle {

    private final int maxAlpha;
    private Vector2f pos;
    private Vector2f velocity;
    private Vector2f acceleration;
    private int alpha;
    private float size;

    public Particle(Vector2f pos) {
        this.pos = pos;
        int lowVel = -1;
        int highVel = 1;
        float resultXVel = lowVel + ThreadLocalRandom.current().nextFloat() * (highVel - lowVel);
        float resultYVel = lowVel + ThreadLocalRandom.current().nextFloat() * (highVel - lowVel);
        this.velocity = new Vector2f(resultXVel, resultYVel);
        this.acceleration = new Vector2f(0, 0.35f);
        this.alpha = 0;
        this.maxAlpha = ThreadLocalRandom.current().nextInt(32, 192);
        this.size = 0.5f + ThreadLocalRandom.current().nextFloat() * (2.0f - 0.5f);
    }

    public void respawn(ScaledResolution scaledResolution) {
        this.pos = new Vector2f((float) (Math.random() * scaledResolution.getScaledWidth()), (float) (Math.random() * scaledResolution.getScaledHeight()));
    }

    public void update() {
        if (this.alpha < this.maxAlpha) {
            this.alpha += 8;
        }

        if (this.acceleration.getX() > 0.35f) {
            this.acceleration.setX(this.acceleration.getX() * 0.975f);
        } else if (this.acceleration.getX() < -0.35f) {
            this.acceleration.setX(this.acceleration.getX() * 0.975f);
        }

        if (this.acceleration.getY() > 0.35f) {
            this.acceleration.setY(this.acceleration.getY() * 0.975f);
        } else if (this.acceleration.getY() < -0.35f) {
            this.acceleration.setY(this.acceleration.getY() * 0.975f);
        }

        this.pos.add(acceleration);
        this.pos.add(velocity);
    }

    public void render(int mouseX, int mouseY) {
        if (Mouse.isButtonDown(0)) {
            float deltaXToMouse = mouseX - this.pos.getX();
            float deltaYToMouse = mouseY - this.pos.getY();
            if (Math.abs(deltaXToMouse) < 50 && Math.abs(deltaYToMouse) < 50) {
                this.acceleration.setX(this.acceleration.getX() + (deltaXToMouse * 0.0015f));
                this.acceleration.setY(this.acceleration.getY() + (deltaYToMouse * 0.0015f));
            }
        }

        RenderUtil.drawRect(this.pos.x, this.pos.y, this.pos.x + this.size, this.pos.y + this.size, ColorUtil.changeAlpha(0xFF9900EE, this.alpha));
    }

    public Vector2f getPos() {
        return pos;
    }

    public void setPos(Vector2f pos) {
        this.pos = pos;
    }

    public Vector2f getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2f velocity) {
        this.velocity = velocity;
    }

    public Vector2f getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Vector2f acceleration) {
        this.acceleration = acceleration;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }
}
