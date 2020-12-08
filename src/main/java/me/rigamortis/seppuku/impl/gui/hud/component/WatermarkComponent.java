package me.rigamortis.seppuku.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.impl.fml.SeppukuMod;
import net.minecraft.client.Minecraft;

/**
 * Author Seth
 * 7/25/2019 @ 4:55 AM.
 */
public final class WatermarkComponent extends DraggableHudComponent {

    private final String WATERMARK = ChatFormatting.ITALIC + "Seppuku " + ChatFormatting.GRAY + SeppukuMod.VERSION;

    public WatermarkComponent() {
        super("Watermark");
        this.setH(Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        this.setW(Minecraft.getMinecraft().fontRenderer.getStringWidth(WATERMARK));
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(WATERMARK, this.getX(), this.getY(), -1);
    }

}
