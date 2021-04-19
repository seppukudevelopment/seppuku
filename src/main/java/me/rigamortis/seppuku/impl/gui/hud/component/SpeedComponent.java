package me.rigamortis.seppuku.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import net.minecraft.util.math.MathHelper;

import java.text.DecimalFormat;


/**
 * Author Seth
 * 7/27/2019 @ 7:46 PM.
 */
public final class SpeedComponent extends DraggableHudComponent {

    public SpeedComponent() {
        super("Speed");
        this.setH(mc.fontRenderer.FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.player != null) {
            final DecimalFormat df = new DecimalFormat("0.0");

            final double deltaX = mc.player.posX - mc.player.prevPosX;
            final double deltaZ = mc.player.posZ - mc.player.prevPosZ;
            final float tickRate = (mc.timer.tickLength / 1000.0f);

            final String bps = ChatFormatting.GRAY + "BPS " + ChatFormatting.RESET + df.format((MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ) / tickRate));

            this.setW(mc.fontRenderer.getStringWidth(bps));
            mc.fontRenderer.drawStringWithShadow(bps, this.getX(), this.getY(), -1);
        } else {
            this.setW(mc.fontRenderer.getStringWidth("(bps)"));
            mc.fontRenderer.drawStringWithShadow("(bps)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

}