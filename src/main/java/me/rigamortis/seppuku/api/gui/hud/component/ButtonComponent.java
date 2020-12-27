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
        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + (this.rightClickListener != null ? this.getW() - 8 : this.getW()), this.getY() + this.getH(), this.enabled ? 0x45002E00 : 0x452E0000);

        if (this.rightClickListener != null) {
            final boolean isMousingHoveringDropdown = mouseX >= this.getX() + this.getW() - 8 && mouseX <= this.getX() + this.getW() && mouseY >= this.getY() && mouseY <= this.getY() + this.getH();

            // draw bg behind triangles
            RenderUtil.drawRect(this.getX() + this.getW() - 8, this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x45202020);

            // draw right click box
            if (this.rightClickEnabled) {
                RenderUtil.drawTriangle(this.getX() + this.getW() - 4, this.getY() + 4, 3, 180, 0xFF6D55FF);
                if (isMousingHoveringDropdown)
                    RenderUtil.drawTriangle(this.getX() + this.getW() - 4, this.getY() + 4, 3, 180, 0x50FFFFFF);
            } else {
                RenderUtil.drawTriangle(this.getX() + this.getW() - 4, this.getY() + 4, 3, -90, 0x75909090);
                if (isMousingHoveringDropdown)
                    RenderUtil.drawTriangle(this.getX() + this.getW() - 4, this.getY() + 4, 3, -90, 0x50FFFFFF);
            }
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
            // handle clicking the right click button
            if (this.rightClickListener != null) {
                // is inside button
                if (mouseX >= this.getX() + this.getW() - 8 && mouseX <= this.getX() + this.getW() && mouseY >= this.getY() && mouseY <= this.getY() + this.getH()) {
                    this.rightClickListener.onComponentEvent();
                    return; // cancel normal action
                }
            }

            // enable / disable normally

            this.enabled = !this.enabled;

            if (this.mouseClickListener != null)
                this.mouseClickListener.onComponentEvent();

        } else if (button == 1) {
            if (this.rightClickListener != null)
                this.rightClickListener.onComponentEvent();
        }
    }
}
