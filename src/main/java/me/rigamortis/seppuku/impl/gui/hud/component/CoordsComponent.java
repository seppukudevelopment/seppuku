package me.rigamortis.seppuku.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import net.minecraft.client.Minecraft;

import java.text.DecimalFormat;

/**
 * Author Seth
 * 7/27/2019 @ 7:44 PM.
 */
public final class CoordsComponent extends DraggableHudComponent {

    public CoordsComponent() {
        super("Coords");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        final DecimalFormat df = new DecimalFormat("#.#");

        final String coords = ChatFormatting.GRAY + "x " + ChatFormatting.RESET +
                df.format(Minecraft.getMinecraft().player.posX) + ChatFormatting.RESET + "," +
                ChatFormatting.GRAY + " y " + ChatFormatting.RESET + df.format(Minecraft.getMinecraft().player.posY) + ChatFormatting.RESET + "," +
                ChatFormatting.GRAY + " z " + ChatFormatting.RESET + df.format(Minecraft.getMinecraft().player.posZ) + ChatFormatting.RESET;

        this.setW(Minecraft.getMinecraft().fontRenderer.getStringWidth(coords));
        this.setH(Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT);

        //RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0xAA202020);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(coords, this.getX(), this.getY(), -1);
    }

}