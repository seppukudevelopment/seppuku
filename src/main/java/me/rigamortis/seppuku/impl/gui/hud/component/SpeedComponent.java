package me.rigamortis.seppuku.impl.gui.hud.component;

import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;

import java.text.DecimalFormat;


/**
 * Author Seth
 * 7/27/2019 @ 7:46 PM.
 */
public final class SpeedComponent extends DraggableHudComponent {

    public SpeedComponent() {
        super("Speed");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        final DecimalFormat df = new DecimalFormat("#.#");

        final double deltaX = Minecraft.getMinecraft().player.posX - Minecraft.getMinecraft().player.prevPosX;
        final double deltaZ = Minecraft.getMinecraft().player.posZ - Minecraft.getMinecraft().player.prevPosZ;
        final float tickRate = (Minecraft.getMinecraft().timer.tickLength / 1000.0f);

        final String bps = "BPS: " + df.format((MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ) / tickRate));

        this.setW(Minecraft.getMinecraft().fontRenderer.getStringWidth(bps));
        this.setH(Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT);

        //RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x90222222);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(bps, this.getX(), this.getY(), -1);
    }

}