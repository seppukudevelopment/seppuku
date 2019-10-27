package me.rigamortis.seppuku.api.event.render;

import net.minecraft.client.gui.ScaledResolution;

/**
 * Author Seth
 * 4/6/2019 @ 1:08 PM.
 */
public class EventRender2D {

    private float partialTicks;
    private ScaledResolution scaledResolution;

    public EventRender2D(float partialTicks, ScaledResolution scaledResolution) {
        this.partialTicks = partialTicks;
        this.scaledResolution = scaledResolution;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public ScaledResolution getScaledResolution() {
        return scaledResolution;
    }

    public void setScaledResolution(ScaledResolution scaledResolution) {
        this.scaledResolution = scaledResolution;
    }
}
