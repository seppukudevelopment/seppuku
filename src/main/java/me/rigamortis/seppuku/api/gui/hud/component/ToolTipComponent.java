package me.rigamortis.seppuku.api.gui.hud.component;

import me.rigamortis.seppuku.api.util.ColorUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

/**
 * @author noil
 */
public class ToolTipComponent extends HudComponent {

    public String text;
    public int alpha;

    public ToolTipComponent(String text) {
        this.text = text;
        this.alpha = 0;

        final int tooltipWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
        final int tooltipHeight = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
        this.setW(tooltipWidth);
        this.setH(tooltipHeight);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (this.alpha < 0xFF/*max alpha*/) {
            this.alpha += 2/* arbitrary value, the speed at which it fades in essentially */;
        }

        if (this.alpha > 0x99/* another arbitrary value, the alpha hex value at which it begins to show on screen*/) {
            this.setX(mouseX - (this.getW() / 2.0f));
            this.setY(mouseY - 12);

            // background
            RenderUtil.drawRect(this.getX() - 2, this.getY() - 2, this.getX() + this.getW() + 2, this.getY() + this.getH() + 2, ColorUtil.changeAlpha(0x80101010, this.alpha / 2));
            RenderUtil.drawRect(this.getX() - 1, this.getY() - 1, this.getX() + this.getW() + 1, this.getY() + this.getH() + 1, ColorUtil.changeAlpha(0xAD101010, this.alpha / 2));

            // text
            GlStateManager.enableBlend();
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(this.text, this.getX() + 1, this.getY() + 1, ColorUtil.changeAlpha(0xADC255FF, this.alpha));
            GlStateManager.disableBlend();
        }
    }
}
