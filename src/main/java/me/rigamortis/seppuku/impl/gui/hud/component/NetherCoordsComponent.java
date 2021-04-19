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
            final DecimalFormat df = new DecimalFormat("0.0");

            final String coords = ChatFormatting.GRAY + "Coords [" + ChatFormatting.DARK_GREEN +
                    df.format(mc.player.posX * 8) + ", " + df.format(mc.player.posZ * 8) + ChatFormatting.GRAY + "]";

            final String nether = ChatFormatting.GRAY + "Nether [" + ChatFormatting.RED +
                    df.format(mc.player.posX / 8) + ", " + df.format(mc.player.posZ / 8) + ChatFormatting.GRAY + "]";

            this.setW(mc.fontRenderer.getStringWidth(mc.player.dimension == -1 ? coords : nether));
            mc.fontRenderer.drawStringWithShadow(mc.player.dimension == -1 ? coords : nether, this.getX(), this.getY(), -1);
        } else {
            this.setW(mc.fontRenderer.getStringWidth("(nether coords)"));
            mc.fontRenderer.drawStringWithShadow("(nether coords)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

}