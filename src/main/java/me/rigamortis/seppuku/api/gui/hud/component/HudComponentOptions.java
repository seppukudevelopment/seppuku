package me.rigamortis.seppuku.api.gui.hud.component;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.util.RenderUtil;
import net.minecraft.client.Minecraft;

/**
 * created by noil on 8/9/2019 at 9:28 AM
 */
public class HudComponentOptions extends HudComponent {

    private HudComponent parent;

    public HudComponentOptions(HudComponent parent) {
        this.parent = parent;
        this.setVisible(false);
    }

    @Override
    public void mouseClick(int mouseX, int mouseY, int button) {
        final boolean inside = mouseX >= this.getX() && mouseX <= this.getX() + this.getW() && mouseY >= this.getY() && mouseY <= this.getY() + this.getH();
        if (inside && button == 0) {
            Seppuku.INSTANCE.getHudManager().moveToTop(this);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (parent == null)
            return;

        this.setX(parent.getX() + parent.getW());
        this.setY(parent.getY());

        final int parentNameWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(parent.getName());
        final int visibleStringWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth("Visible");
        int yOffset = 0;

        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + parentNameWidth, this.getY() + Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT, 0x75505050);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(parent.getName(), this.getX(), this.getY(), 0xFFFFFFFF);

        yOffset += Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;

        RenderUtil.drawRect(this.getX(), this.getY() + yOffset, this.getX() + visibleStringWidth, this.getY() + yOffset + Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT, parent.isVisible() ? 0x7550FF50 : 0x75FF5050);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("Visible", this.getX(), this.getY() + yOffset, 0xFFFFFFFF);

        yOffset += Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
        this.setW(Math.max(parentNameWidth, visibleStringWidth));
        this.setH(yOffset);
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int button) {
        if (button == 0) {
            if ((mouseX > this.getX()) && (mouseX < this.getX() + Minecraft.getMinecraft().fontRenderer.getStringWidth("Visible"))) {
                if (mouseY > (this.getY() + Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT)) {
                    if (mouseY < (this.getY() + this.getH())) {
                        parent.setVisible(!parent.isVisible());
                    }
                }
            }
        }

    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int button) {
        super.mouseClickMove(mouseX, mouseY, button);
    }

    public HudComponent getParent() {
        return parent;
    }

    public void setParent(HudComponent parent) {
        this.parent = parent;
    }
}
