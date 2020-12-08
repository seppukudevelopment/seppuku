package me.rigamortis.seppuku.impl.gui.hud.component;

import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import net.minecraft.client.network.NetworkPlayerInfo;

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

        if (mc.player.connection == null || mc.getCurrentServerData() == null)
            return;

        final NetworkPlayerInfo playerInfo = mc.player.connection.getPlayerInfo(mc.player.getUniqueID());
        final String ping = "Ping: " + playerInfo.getResponseTime() + "ms";

        this.setW(mc.fontRenderer.getStringWidth(ping));
        mc.fontRenderer.drawStringWithShadow(ping, this.getX(), this.getY(), -1);
    }
}