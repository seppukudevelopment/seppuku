package me.rigamortis.seppuku.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import net.minecraft.client.Minecraft;

import java.text.DecimalFormat;

/**
 * Author Seth
 * 7/27/2019 @ 7:44 PM.
 */
public final class CoordsComponent extends DraggableHudComponent {

    public CoordsComponent() {
        super("Coords");
        this.setH(Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.player != null && mc.world != null) {
            final DecimalFormat df = new DecimalFormat("0.0");

            final String nether = this.isRclicked() && mc.player.dimension == -1 ? ChatFormatting.GRAY + " [" + ChatFormatting.RESET
                    + df.format(mc.player.posX * 8) + ", "
                    + df.format(mc.player.posZ * 8) + ChatFormatting.GRAY + "]"
                    : this.isRclicked() && mc.player.dimension != -1 ? ChatFormatting.GRAY + " [" + ChatFormatting.RESET
                    + df.format(mc.player.posX / 8) + ", "
                    + df.format(mc.player.posZ / 8) + ChatFormatting.GRAY + "]"
                    : "";

            final String coords = ChatFormatting.GRAY + "XYZ " + ChatFormatting.RESET
                    + df.format(mc.player.posX) + ", "
                    + df.format(mc.player.posY) + ", "
                    + df.format(mc.player.posZ) + nether;

            this.setW(Minecraft.getMinecraft().fontRenderer.getStringWidth(coords));
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(coords, this.getX(), this.getY(), -1);
        } else {
            this.setW(Minecraft.getMinecraft().fontRenderer.getStringWidth("(coords)"));
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("(coords)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

}