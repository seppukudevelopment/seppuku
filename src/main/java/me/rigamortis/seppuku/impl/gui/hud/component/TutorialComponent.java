package me.rigamortis.seppuku.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;

/**
 * created by noil on 10/22/2019 at 10:06 AM
 */
public final class TutorialComponent extends PopupComponent {

    public TutorialComponent() {
        super("Tutorial", "");

        final String tutorialData = "Hud Editor Tutorial\n\n" +
                ChatFormatting.BOLD + "Anchor Points\n" + ChatFormatting.RESET +
                "- Move hud components by clicking & dragging them.\n" +
                "- Anchor points are located at each corner of the screen (also at the center top & bottom).\n" +
                "- Drag a component's corner near an anchor point and release the mouse to lock it in place.\n\n" +
                ChatFormatting.BOLD + "Combine\n" + ChatFormatting.RESET +
                "- Combine components together by dragging one into another one, releasing the mouse will combine them together.\n" +
                "- Both top and bottom parts of a component are able to be glued to.";

        this.setTextData(tutorialData);

        this.setVisible(true);
        this.setSnappable(false);
        this.setW(200);
        this.setH(173);
        this.setX((mc.displayWidth / 2.0f) - (this.getW() / 2.0f));
        this.setY((mc.displayHeight / 2.0f) - (this.getH() / 2.0f));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!(mc.currentScreen instanceof GuiHudEditor)) // ensure we are in the hud editor screen only
            return;

        super.render(mouseX, mouseY, partialTicks);

        // drag me!
        mc.fontRenderer.drawStringWithShadow("(drag me!)", this.getX() + this.getW() - 80, this.getY() + 10, 0xFFAAAAAA);
    }
}
