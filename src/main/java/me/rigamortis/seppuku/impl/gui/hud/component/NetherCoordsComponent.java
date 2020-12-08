package me.rigamortis.seppuku.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;

import java.text.DecimalFormat;

/**
 * Author Seth
 * 7/27/2019 @ 7:45 PM.
 */
public final class NetherCoordsComponent extends DraggableHudComponent {

    public NetherCoordsComponent() {
        super("NetherCoords");
        this.setH(mc.fontRenderer.FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.player != null && mc.world != null) {
            final DecimalFormat df = new DecimalFormat("#.#");

            final String coords = ChatFormatting.GRAY + "x " + ChatFormatting.DARK_GREEN +
                    df.format(mc.player.posX * 8) + ChatFormatting.DARK_GREEN + "," +
                    ChatFormatting.GRAY + " y " + ChatFormatting.DARK_GREEN + df.format(mc.player.posY) + ChatFormatting.DARK_GREEN + "," +
                    ChatFormatting.GRAY + " z " + ChatFormatting.DARK_GREEN + df.format(mc.player.posZ * 8) + ChatFormatting.RESET;

            final String nether = ChatFormatting.GRAY + "x " + ChatFormatting.RED +
                    df.format(mc.player.posX / 8) + ChatFormatting.RED + "," +
                    ChatFormatting.GRAY + " y " + ChatFormatting.RED + df.format(mc.player.posY) + ChatFormatting.RED + "," +
                    ChatFormatting.GRAY + " z " + ChatFormatting.RED + df.format(mc.player.posZ / 8) + ChatFormatting.RESET;

            this.setW(mc.fontRenderer.getStringWidth(mc.player.dimension == -1 ? coords : nether));
            mc.fontRenderer.drawStringWithShadow(mc.player.dimension == -1 ? coords : nether, this.getX(), this.getY(), -1);
        } else {
            this.setW(mc.fontRenderer.getStringWidth("(nether coords)"));
            mc.fontRenderer.drawStringWithShadow("(nether coords)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

}