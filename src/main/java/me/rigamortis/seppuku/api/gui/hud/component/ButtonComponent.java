package me.rigamortis.seppuku.api.gui.hud.component;

import me.rigamortis.seppuku.api.util.RenderUtil;
import net.minecraft.client.Minecraft;

/**
 * @author noil
 */
public class ButtonComponent extends HudComponent {

    public boolean enabled;
    public ComponentListener mouseClickListener;

    public ButtonComponent(String name) {
        super(name);
        this.enabled = false;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (isMouseInside(mouseX, mouseY))
            RenderUtil.drawGradientRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x30909090, 0x00101010);

        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), this.enabled ? 0x45002E00 : 0x452E0000);
        Minecraft.getMinecraft().fontRenderer.drawString(this.getName(), (int) this.getX() + 1, (int) this.getY() + 1, this.enabled ? 0xFF55FF55 : 0xFFFF5555);
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int button) {
        super.mouseRelease(mouseX, mouseY, button);

        if (!this.isMouseInside(mouseX, mouseY) || button != 0)
            return;

        this.enabled = !enabled;

        if (mouseClickListener != null)
            mouseClickListener.onComponentEvent();
    }
}
