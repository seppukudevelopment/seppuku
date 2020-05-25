package me.rigamortis.seppuku.impl.gui.hud.component;

import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import net.minecraft.client.Minecraft;

/**
 * created by noil on 11/4/19 at 7:54 AM
 */
public class PopupComponent extends DraggableHudComponent {

    private static final int CLOSE_BUTTON_SIZE = 12;
    private static final int CLOSE_X_PADDING = 2;

    private String textData;

    public PopupComponent(String name, String textData) {
        super(name);
        this.textData = textData;
        this.setW(100);
        this.setH(50);
        this.setX((Minecraft.getMinecraft().displayWidth / 2) - (this.getW() / 2));
        this.setY((Minecraft.getMinecraft().displayHeight / 2) - (this.getH() / 2));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final Minecraft mc = Minecraft.getMinecraft();

        // background
        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0xFF202020);

        mc.fontRenderer.drawSplitString(this.textData, (int) this.getX() + 2, (int) this.getY() + 2, 200, 0xFFFFFFFF);

        // close button
        RenderUtil.drawRect(this.getX() + this.getW() - CLOSE_BUTTON_SIZE, this.getY(), this.getX() + this.getW(), this.getY() + CLOSE_BUTTON_SIZE, 0x75101010);
        RenderUtil.drawLine(this.getX() + this.getW() - CLOSE_BUTTON_SIZE + CLOSE_X_PADDING, this.getY() + CLOSE_X_PADDING, this.getX() + this.getW() - CLOSE_X_PADDING, this.getY() + CLOSE_BUTTON_SIZE - CLOSE_X_PADDING, 1, 0xFFFFFFFF);
        RenderUtil.drawLine(this.getX() + this.getW() - CLOSE_BUTTON_SIZE + CLOSE_X_PADDING, this.getY() + CLOSE_BUTTON_SIZE - CLOSE_X_PADDING, this.getX() + this.getW() - CLOSE_X_PADDING, this.getY() + CLOSE_X_PADDING, 1, 0xFFFFFFFF);
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int button) {
        super.mouseRelease(mouseX, mouseY, button);

        final boolean insideCloseButton = mouseX >= this.getX() + (this.getW() - CLOSE_BUTTON_SIZE) &&
                mouseX <= this.getX() + this.getW() &&
                mouseY >= this.getY() &&
                mouseY <= this.getY() + CLOSE_BUTTON_SIZE;

        if (insideCloseButton && button == 0) {
            this.onCloseButton();
        }
    }

    public String getTextData() {
        return textData;
    }

    public void setTextData(String textData) {
        this.textData = textData;
    }

    public void onCloseButton() {
        this.setVisible(false);
    }
}
