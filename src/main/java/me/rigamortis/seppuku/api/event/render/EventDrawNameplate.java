package me.rigamortis.seppuku.api.event.render;

import me.rigamortis.seppuku.api.event.EventCancellable;
import net.minecraft.client.gui.FontRenderer;

public class EventDrawNameplate extends EventCancellable {

    public final FontRenderer fontRenderer;
    public final String str;
    public final float x;
    public final float y;
    public final float z;
    public final int verticalShift;
    public final float viewerYaw;
    public final float viewerPitch;
    public final boolean isThirdPersonFrontal;
    public final boolean isSneaking;

    public EventDrawNameplate(FontRenderer fontRenderer, String str, float x, float y, float z, int verticalShift, float viewerYaw, float viewerPitch, boolean isThirdPersonFrontal, boolean isSneaking) {
        this.fontRenderer = fontRenderer;
        this.str = str;
        this.x = x;
        this.y = y;
        this.z = z;
        this.verticalShift = verticalShift;
        this.viewerYaw = viewerYaw;
        this.viewerPitch = viewerPitch;
        this.isThirdPersonFrontal = isThirdPersonFrontal;
        this.isSneaking = isSneaking;
    }
}
