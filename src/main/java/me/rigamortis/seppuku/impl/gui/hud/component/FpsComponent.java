package me.rigamortis.seppuku.impl.gui.hud.component;

import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import net.minecraft.client.Minecraft;

/**
 * Author Seth
 * 7/27/2019 @ 7:37 PM.
 */
public final class FpsComponent extends DraggableHudComponent {

    public FpsComponent() {
        super("Fps");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        final String framerate = "FPS: " + Minecraft.getDebugFPS();

        this.setW(Minecraft.getMinecraft().fontRenderer.getStringWidth(framerate));
        this.setH(Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT);

        //RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x90222222);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(framerate, this.getX(), this.getY(), -1);
    }

}
