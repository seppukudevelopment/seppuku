package me.rigamortis.seppuku.api.gui.hud.particle;

import net.minecraft.client.gui.ScaledResolution;

import javax.vecmath.Vector2f;

public final class ParticleSystem {

    private final int PARTS = 100;
    private final Particle[] particles = new Particle[PARTS];

    private final ScaledResolution scaledResolution;

    public ParticleSystem(ScaledResolution scaledResolution) {
        this.scaledResolution = scaledResolution;
        for (int i = 0; i < PARTS; i++) {
            this.particles[i] = new Particle(new Vector2f((float) (Math.random() * scaledResolution.getScaledWidth()), (float) (Math.random() * scaledResolution.getScaledHeight())));
        }
    }

    public void update() {
        for (int i = 0; i < PARTS; i++) {
            final Particle particle = this.particles[i];
            if (this.scaledResolution != null) {
                final boolean isOffScreenX = particle.getPos().x > this.scaledResolution.getScaledWidth() || particle.getPos().x < 0;
                final boolean isOffScreenY = particle.getPos().y > this.scaledResolution.getScaledHeight() || particle.getPos().y < 0;
                if (isOffScreenX || isOffScreenY) {
                    particle.respawn(this.scaledResolution);
                }
            }
            particle.update();
        }
    }

    public void render(int mouseX, int mouseY) {
        for (int i = 0; i < PARTS; i++) {
            final Particle particle = this.particles[i];
            particle.render(mouseX, mouseY);
        }
    }
}
