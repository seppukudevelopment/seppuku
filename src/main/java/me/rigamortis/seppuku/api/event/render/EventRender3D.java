package me.rigamortis.seppuku.api.event.render;

/**
 * Author Seth
 * 4/6/2019 @ 1:20 PM.
 */
public class EventRender3D {

    private float partialTicks;

    public EventRender3D(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }
}
