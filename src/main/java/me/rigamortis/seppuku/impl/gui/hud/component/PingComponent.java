package me.rigamortis.seppuku.impl.gui.hud.component;

import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

/**
 * Author Seth
 * 7/28/2019 @ 9:41 AM.
 */
public final class PingComponent extends DraggableHudComponent {

    public PingComponent() {
        super("Ping");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.world == null || mc.player == null || mc.player.getUniqueID() == null)
            return;

        final NetworkPlayerInfo playerInfo = mc.player.connection.getPlayerInfo(mc.player.getUniqueID());

        if (playerInfo == null)
            return;

        final String ping = "MS: " + playerInfo.getResponseTime();

        this.setW(Minecraft.getMinecraft().fontRenderer.getStringWidth(ping));
        this.setH(Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT);

        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(ping, this.getX(), this.getY(), -1);
    }
}