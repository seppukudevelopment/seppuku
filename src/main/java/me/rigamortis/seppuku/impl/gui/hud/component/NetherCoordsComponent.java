package me.rigamortis.seppuku.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import net.minecraft.client.Minecraft;

import java.text.DecimalFormat;

/**
 * Author Seth
 * 7/27/2019 @ 7:45 PM.
 */
public final class NetherCoordsComponent extends DraggableHudComponent {

    public NetherCoordsComponent() {
        super("NetherCoords");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        final DecimalFormat df = new DecimalFormat("#.#");

        final String coords = ChatFormatting.GRAY + "x " + ChatFormatting.DARK_GREEN +
                df.format(Minecraft.getMinecraft().player.posX * 8) + ChatFormatting.DARK_GREEN + "," +
                ChatFormatting.GRAY + " y " + ChatFormatting.DARK_GREEN + df.format(Minecraft.getMinecraft().player.posY) + ChatFormatting.DARK_GREEN + "," +
                ChatFormatting.GRAY + " z " + ChatFormatting.DARK_GREEN + df.format(Minecraft.getMinecraft().player.posZ * 8) + ChatFormatting.RESET;

        final String nether = ChatFormatting.GRAY + "x " + ChatFormatting.RED +
                df.format(Minecraft.getMinecraft().player.posX / 8) + ChatFormatting.RED + "," +
                ChatFormatting.GRAY + " y " + ChatFormatting.RED + df.format(Minecraft.getMinecraft().player.posY) + ChatFormatting.RED + "," +
                ChatFormatting.GRAY + " z " + ChatFormatting.RED + df.format(Minecraft.getMinecraft().player.posZ / 8) + ChatFormatting.RESET;

        this.setW(Minecraft.getMinecraft().fontRenderer.getStringWidth(Minecraft.getMinecraft().player.dimension == -1 ? coords : nether));
        this.setH(Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT);

        //RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x90222222);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(Minecraft.getMinecraft().player.dimension == -1 ? coords : nether, this.getX(), this.getY(), -1);
    }

}