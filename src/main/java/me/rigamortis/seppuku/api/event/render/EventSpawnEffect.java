package me.rigamortis.seppuku.api.event.render;

import me.rigamortis.seppuku.api.event.EventCancellable;

/**
 * @author noil
 */
public class EventSpawnEffect extends EventCancellable {

    private int particleID;

    public EventSpawnEffect(int particleID) {
        this.particleID = particleID;
    }

    public int getParticleID() {
        return particleID;
    }

    public void setParticleID(int particleID) {
        this.particleID = particleID;
    }
}
