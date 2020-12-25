package me.rigamortis.seppuku.api.gui.hud.component;

import me.rigamortis.seppuku.api.util.RenderUtil;
import net.minecraft.client.Minecraft;

/**
 * @author noil
 */
public class ButtonComponent extends HudComponent {

    public boolean enabled, rightClickEnabled;
    public ComponentListener mouseClickListener, rightClickListener;

    public ButtonComponent(String name) {
        super(name);
        this.enabled = false;
        this.rightClickEnabled = false;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (isMouseInside(mouseX, mouseY))
            RenderUtil.drawGradientRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x30909090, 0x00101010);

        // draw bg
        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), this.enabled ? 0x45002E00 : 0x452E0000);

        if (this.rightClickListener != null) {
            // draw right click box
            if (this.rightClickEnabled) {
                RenderUtil.drawRect(this.getX() + this.getW() - 7, this.getY() + 2, this.getX() + this.getW() - 2, this.getY() + this.getH() - 2, 0xFF6D55FF);
            } else {
                RenderUtil.drawRect(this.getX() + this.getW() - 7, this.getY() + 2, this.getX() + this.getW() - 2, this.getY() + this.getH() - 2, 0x45909090);
            }
            RenderUtil.drawBorderedRect(this.getX() + this.getW() - 7, this.getY() + 2, this.getX() + this.getW() - 2, this.getY() + this.getH() - 2, 1.0f, 0x00000000, 0x7506002A);
        }

        // draw text
        Minecraft.getMinecraft().fontRenderer.drawString(this.getName(), (int) this.getX() + 1, (int) this.getY() + 1, this.enabled ? 0xFF55FF55 : 0xFFFF5555);
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int button) {
        super.mouseRelease(mouseX, mouseY, button);

        if (!this.isMouseInside(mouseX, mouseY))
            return;

        if (button == 0) {
            this.enabled = !this.enabled;

            if (this.mouseClickListener != null)
                this.mouseClickListener.onComponentEvent();
        } else if (button == 1) {
            if (this.rightClickListener != null)
                this.rightClickListener.onComponentEvent();
        }
    }
}
