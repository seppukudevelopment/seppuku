package me.rigamortis.seppuku.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;

/**
 * Author Seth
 * 12/5/2019 @ 3:00 PM.
 */
public final class PlayerCountComponent extends DraggableHudComponent {

    public PlayerCountComponent() {
        super("PlayerCount");
        this.setH(mc.fontRenderer.FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        if (mc.player != null) {
            final String playerCount = ChatFormatting.GRAY + "Online " + ChatFormatting.RESET + mc.player.connection.getPlayerInfoMap().size();

            this.setW(mc.fontRenderer.getStringWidth(playerCount));
            mc.fontRenderer.drawStringWithShadow(playerCount, this.getX(), this.getY(), -1);
        } else {
            this.setW(mc.fontRenderer.getStringWidth("(player count)"));
            mc.fontRenderer.drawStringWithShadow("(player count)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

}
