package me.rigamortis.seppuku.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import net.minecraft.client.Minecraft;

/**
 * created by noil on 10/22/2019 at 10:06 AM
 */
public final class TutorialComponent extends DraggableHudComponent {

    /**
     * I want to make this more object oriented and have pages and such, but we need this
     * release out soon, and these are the core basics people need to know
     * ~noil
     */
    private static final int CLOSE_BUTTON_SIZE = 12;
    private static final int CLOSE_X_PADDING = 2;

    public TutorialComponent() {
        super("Tutorial");
        this.setW(200);
        this.setH(173);
        this.setX((Minecraft.getMinecraft().displayWidth / 2) - (this.getW() / 2));
        this.setY((Minecraft.getMinecraft().displayHeight / 2) - (this.getH() / 2));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final Minecraft mc = Minecraft.getMinecraft();

        if (!(mc.currentScreen instanceof GuiHudEditor)) // ensure we are in the hud editor screen only
            return;

        // background
        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0xFF202020);

        final String tutorialData = "Hud Editor Tutorial\n\n" +
                ChatFormatting.BOLD + "Anchor Points\n" + ChatFormatting.RESET +
                "- Move hud components by clicking & dragging them.\n" +
                "- Anchor points are located at each corner of the screen, and also one centered at the top of the screen.\n" +
                "- Drag a component near an anchor point and release the mouse to lock it in place.\n\n" +
                ChatFormatting.BOLD + "Combine\n" + ChatFormatting.RESET +
                "- Combine components together by dragging one into another one, releasing the mouse will combine them together.\n" +
                "- Both top and bottom parts of a component are able to be glued to.";

        mc.fontRenderer.drawSplitString(tutorialData, (int) this.getX() + 2, (int) this.getY() + 2, 200, 0xFFFFFFFF);

        // close button
        RenderUtil.drawRect(this.getX() + this.getW() - CLOSE_BUTTON_SIZE, this.getY(), this.getX() + this.getW(), this.getY() + CLOSE_BUTTON_SIZE, 0x75101010);
        RenderUtil.drawLine(this.getX() + this.getW() - CLOSE_BUTTON_SIZE + CLOSE_X_PADDING, this.getY() + CLOSE_X_PADDING, this.getX() + this.getW() - CLOSE_X_PADDING, this.getY() + CLOSE_BUTTON_SIZE - CLOSE_X_PADDING, 1, 0xFFFFFFFF);
        RenderUtil.drawLine(this.getX() + this.getW() - CLOSE_BUTTON_SIZE + CLOSE_X_PADDING, this.getY() + CLOSE_BUTTON_SIZE - CLOSE_X_PADDING, this.getX() + this.getW() - CLOSE_X_PADDING, this.getY() + CLOSE_X_PADDING, 1, 0xFFFFFFFF);

        // drag me!
        mc.fontRenderer.drawStringWithShadow("(drag me!)", this.getX() + this.getW() - 80, this.getY() + 10, 0xFFAAAAAA);
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int button) {
        super.mouseRelease(mouseX, mouseY, button);

        final boolean insideCloseButton = mouseX >= this.getX() + (this.getW() - CLOSE_BUTTON_SIZE) &&
                mouseX <= this.getX() + this.getW() &&
                mouseY >= this.getY() &&
                mouseY <= this.getY() + CLOSE_BUTTON_SIZE;

        if (insideCloseButton && button == 0) {
            this.setVisible(false);
        }
    }
}
