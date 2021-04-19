package me.rigamortis.seppuku.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.Objects;

/**
 * Author Seth
 * 7/28/2019 @ 9:41 AM.
 */
public final class PingComponent extends DraggableHudComponent {

    public PingComponent() {
        super("Ping");
        this.setH(mc.fontRenderer.FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.world == null || mc.player == null) {
            this.setW(mc.fontRenderer.getStringWidth("(ping)"));
            mc.fontRenderer.drawStringWithShadow("(ping)", this.getX(), this.getY(), 0xFFAAAAAA);
            return;
        }

        if (mc.player.connection == null)
            return;

        final NetworkPlayerInfo playerInfo = mc.player.connection.getPlayerInfo(mc.player.getUniqueID());
        if (Objects.nonNull(playerInfo)) {
            final String ms = playerInfo.getResponseTime() != 0 ? playerInfo.getResponseTime() + "ms" : "?";
            final String ping = ChatFormatting.GRAY + "Ping " + ChatFormatting.RESET + ms;

            this.setW(mc.fontRenderer.getStringWidth(ping));
            mc.fontRenderer.drawStringWithShadow(ping, this.getX(), this.getY(), -1);
        }
    }
}