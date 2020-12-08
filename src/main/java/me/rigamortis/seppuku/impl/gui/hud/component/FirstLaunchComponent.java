package me.rigamortis.seppuku.impl.gui.hud.component;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.impl.module.render.HudModule;
import net.minecraft.util.text.TextFormatting;

/**
 * created by noil on 5/7/2020
 */
public final class FirstLaunchComponent extends DraggableHudComponent {

    private final Module hudModule;

    private String textData;

    public FirstLaunchComponent() {
        super("FirstLaunch");

        final String textData = TextFormatting.WHITE + "Welcome to the " + TextFormatting.RESET + "Seppuku Client\n\n" +
                TextFormatting.WHITE + "Press ~ (GRAVE) or RSHIFT to open the GUI.";
        this.setTextData(textData);

        this.setVisible(true);
        this.setSnappable(false);
        this.setW(188);
        this.setH(38);
        this.setX(2);
        this.setY(2);

        this.hudModule = Seppuku.INSTANCE.getModuleManager().find(HudModule.class);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        // Background
        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x99202020);

        // Render text data
        mc.fontRenderer.drawSplitString(this.textData, (int) this.getX() + 2, (int) this.getY() + 2, 200, 0xFF9900EE);
    }

    public void onClose() {
        if (this.hudModule != null) {
            if (this.hudModule.isEnabled()) {
                this.hudModule.onEnable();
            } else {
                this.hudModule.toggle();
            }
            this.hudModule.setEnabled(true);
        }

        this.setVisible(false);
    }

    public String getTextData() {
        return textData;
    }

    public void setTextData(String textData) {
        this.textData = textData;
    }
}
