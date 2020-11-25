package me.rigamortis.seppuku.api.gui.hud.component;

import me.rigamortis.seppuku.api.util.RenderUtil;
import net.minecraft.client.Minecraft;

public class ColorComponent extends TextComponent {

    int currentColor;

    private static final int BORDER = 1;
    private static final int TEXT_BLOCK_PADDING = 1;
    private static final int TEXT_BLOCK_WIDTH = 2;
    private static final int TEXT_BLOCK_HEIGHT = 7;
    private static final int COLOR_SIZE = 9;

    public ColorComponent(String name, int defaultColor) {
        super(name, String.valueOf(defaultColor), false);
        this.currentColor = defaultColor;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x45303030);
        RenderUtil.drawRect(this.getX() + BORDER, this.getY() + BORDER, this.getX() + BORDER + COLOR_SIZE, this.getY() + BORDER + COLOR_SIZE, this.currentColor);

        Minecraft.getMinecraft().fontRenderer.drawString(this.getName() + ": " + this.displayValue, (int) this.getX() + BORDER + COLOR_SIZE + BORDER, (int) this.getY() + BORDER, this.focused ? 0xFFFFFFFF : 0xFFAAAAAA);

        if (this.focused) {
            float blockX = this.getX() + Minecraft.getMinecraft().fontRenderer.getStringWidth(this.getName() + ": " + this.displayValue) + TEXT_BLOCK_PADDING;
            float blockY = this.getY() + TEXT_BLOCK_PADDING;
            int blockHeight = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT - TEXT_BLOCK_HEIGHT;
            RenderUtil.drawRect(blockX, blockY, blockX + TEXT_BLOCK_WIDTH, blockY + blockHeight, 0xFFFFFFFF);
        }
    }

    @Override
    protected void enterPressed() {
        super.enterPressed();
        this.currentColor = Integer.parseInt(this.displayValue);
    }
}
