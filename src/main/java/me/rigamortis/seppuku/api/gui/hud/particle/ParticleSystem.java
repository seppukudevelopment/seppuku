package me.rigamortis.seppuku.api.gui.hud.particle;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.ScaledResolution;

import javax.vecmath.Vector2f;
import java.util.List;

public final class ParticleSystem {

    private final int PARTS = 100;
    private final List<Particle> particles = Lists.newArrayListWithCapacity(PARTS);

    private final ScaledResolution scaledResolution;

    public ParticleSystem(ScaledResolution scaledResolution) {
        this.scaledResolution = scaledResolution;
        for (int i = 0; i < PARTS; i++) {
            this.particles.add(new Particle(new Vector2f((float) (Math.random() * scaledResolution.getScaledWidth()), (float) (Math.random() * scaledResolution.getScaledHeight()))));
        }
    }

    public void update() {
        for (Particle particle : this.particles) {
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
        for (Particle particle : this.particles) {
            particle.render(mouseX, mouseY);
        }
    }
}
