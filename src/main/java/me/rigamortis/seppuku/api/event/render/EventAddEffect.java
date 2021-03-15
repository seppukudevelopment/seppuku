package me.rigamortis.seppuku.api.event.render;

import me.rigamortis.seppuku.api.event.EventCancellable;
import net.minecraft.client.particle.Particle;

/**
 * @author noil
 */
public class EventAddEffect extends EventCancellable {

    private Particle particle;

    public EventAddEffect(Particle particle) {
        this.particle = particle;
    }

    public Particle getParticle() {
        return particle;
    }

    public void setParticle(Particle particle) {
        this.particle = particle;
    }
}
