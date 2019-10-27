package me.rigamortis.seppuku.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import net.minecraft.client.Minecraft;

/**
 * Author Seth
 * 7/25/2019 @ 7:44 AM.
 */
public final class TpsComponent extends DraggableHudComponent {

    public TpsComponent() {
        super("Tps");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final String tickrate = String.format(ChatFormatting.WHITE + "TPS: %.2f", Seppuku.INSTANCE.getTickRateManager().getTickRate());

        this.setW(Minecraft.getMinecraft().fontRenderer.getStringWidth(tickrate));
        this.setH(Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT);

        //RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x90222222);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(tickrate, this.getX(), this.getY(), -1);
    }

}
