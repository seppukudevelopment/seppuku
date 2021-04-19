package me.rigamortis.seppuku.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import net.minecraft.client.Minecraft;

/**
 * Author Seth
 * 7/27/2019 @ 7:37 PM.
 */
public final class FpsComponent extends DraggableHudComponent {

    public FpsComponent() {
        super("Fps");
        this.setH(mc.fontRenderer.FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.world != null) {
            final String fps = ChatFormatting.GRAY + "FPS " + ChatFormatting.RESET + Minecraft.getDebugFPS();
            this.setW(mc.fontRenderer.getStringWidth(fps));
            mc.fontRenderer.drawStringWithShadow(fps, this.getX(), this.getY(), -1);
        } else {
            this.setW(mc.fontRenderer.getStringWidth("(fps)"));
            mc.fontRenderer.drawStringWithShadow("(fps)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }
}
