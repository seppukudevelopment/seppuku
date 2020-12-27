package me.rigamortis.seppuku.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.gui.hud.component.ColorComponent;
import me.rigamortis.seppuku.api.gui.hud.component.ComponentListener;
import me.rigamortis.seppuku.api.gui.hud.component.HudComponent;
import me.rigamortis.seppuku.api.gui.hud.component.ResizableHudComponent;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.ColorUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.Value;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * @author noil
 */
public final class ColorsComponent extends ResizableHudComponent {

    private int scroll;

    private int totalHeight;

    private final int MAX_WIDTH = 215;
    private final int SCROLL_WIDTH = 4;
    private final int BORDER = 2;
    private final int TEXT_GAP = 1;
    private final int TITLE_BAR_HEIGHT = mc.fontRenderer.FONT_HEIGHT + 1;

    private ColorComponent currentColorComponent = null;

    public ColorsComponent() {
        super("Colors", 100, 120);
        this.setVisible(true);
        this.setSnappable(false);
        this.setW(120);
        this.setH(120);
        this.setX((mc.displayWidth / 2.0f) - (this.getW() / 2));
        this.setY((mc.displayHeight / 2.0f) - (this.getH() / 2));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (!(mc.currentScreen instanceof GuiHudEditor))
            return;

        final ScaledResolution sr = new ScaledResolution(mc);

        // Render Y pos offset (make this all modular eventually...)
        int offsetY = 0;

        // Scrolling
        this.handleScrolling(mouseX, mouseY);

        // No dragging inside box
        final boolean insideTitlebar = mouseY <= this.getY() + TITLE_BAR_HEIGHT + BORDER;
        if (!insideTitlebar) {
            this.setDragging(false);
        }

        // Is mouse inside
        final boolean mouseInside = this.isMouseInside(mouseX, mouseY);

        // clamp max width & height
        if (this.isResizeDragging()) {
            if (this.getH() > this.getTotalHeight()) {
                this.setH(this.getTotalHeight());
                this.setResizeDragging(false);
            }

            if (this.getW() > MAX_WIDTH) {
                this.setW(MAX_WIDTH);
            }
        }

        // Background & title
        RenderUtil.drawRect(this.getX() - 1, this.getY() - 1, this.getX() + this.getW() + 1, this.getY() + this.getH() + 1, 0x99101010);
        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), ColorUtil.changeAlpha(0xFF202020, mouseInside ? 0xFF : 0x99));
        mc.fontRenderer.drawStringWithShadow(this.getName(), this.getX() + BORDER, this.getY() + BORDER, ColorUtil.changeAlpha(0xFFFFFFFF, mouseInside ? 0xFF : 0x99));
        offsetY += mc.fontRenderer.FONT_HEIGHT + 1;

        // Behind hub
        RenderUtil.drawRect(this.getX() + BORDER, this.getY() + offsetY + BORDER, this.getX() + this.getW() - SCROLL_WIDTH - BORDER, this.getY() + this.getH() - BORDER, ColorUtil.changeAlpha(0xFF101010, mouseInside ? 0xFF : 0x99));

        // Scrollbar bg
        RenderUtil.drawRect(this.getX() + this.getW() - SCROLL_WIDTH, this.getY() + offsetY + BORDER, this.getX() + this.getW() - BORDER, this.getY() + this.getH() - BORDER, ColorUtil.changeAlpha(0xFF101010, mouseInside ? 0xFF : 0x99));
        // Scrollbar highlights
        if (mouseInside) {
            if (mouseX >= (this.getX() + this.getW() - SCROLL_WIDTH) && mouseX <= (this.getX() + this.getW() - BORDER)) { // mouse is inside scroll area on x-axis
                RenderUtil.drawGradientRect(this.getX() + this.getW() - SCROLL_WIDTH, this.getY() + offsetY + BORDER, this.getX() + this.getW() - BORDER, this.getY() + offsetY + 8 + BORDER, 0xFF909090, 0x00101010);
                RenderUtil.drawGradientRect(this.getX() + this.getW() - SCROLL_WIDTH, this.getY() + this.getH() - 8 - BORDER, this.getX() + this.getW() - BORDER, this.getY() + this.getH() - BORDER, 0x00101010, 0xFF909090);
                float diffY = this.getY() + TITLE_BAR_HEIGHT + ((this.getH() - TITLE_BAR_HEIGHT) / 2);
                if (mouseY > diffY) {
                    RenderUtil.drawGradientRect(this.getX() + this.getW() - SCROLL_WIDTH, this.getY() + (this.getH() / 2) + BORDER + BORDER, this.getX() + this.getW() - BORDER, this.getY() + this.getH() - BORDER, 0x00101010, 0x90909090);
                } else {
                    RenderUtil.drawGradientRect(this.getX() + this.getW() - SCROLL_WIDTH, this.getY() + offsetY + BORDER, this.getX() + this.getW() - BORDER, this.getY() + (this.getH() / 2) + BORDER + BORDER, 0x90909090, 0x00101010);
                }
            }
        }
        // Scrollbar
        RenderUtil.drawRect(this.getX() + this.getW() - SCROLL_WIDTH, MathHelper.clamp((this.getY() + offsetY + BORDER) + ((this.getH() * this.scroll) / this.totalHeight), (this.getY() + offsetY + BORDER), (this.getY() + this.getH() - BORDER)), this.getX() + this.getW() - BORDER, MathHelper.clamp((this.getY() + this.getH() - BORDER) - (this.getH() * (this.totalHeight - this.getH() - this.scroll) / this.totalHeight), (this.getY() + offsetY + BORDER), (this.getY() + this.getH() - BORDER)), ColorUtil.changeAlpha(0xFF909090, mouseInside ? 0xFF : 0x99));

        // Begin scissoring and render the component "buttons"
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        RenderUtil.glScissor(this.getX() + BORDER, this.getY() + offsetY + BORDER, this.getX() + this.getW() - BORDER - SCROLL_WIDTH, this.getY() + this.getH() - BORDER, sr);
        for (HudComponent component : Seppuku.INSTANCE.getHudManager().getComponentList()) {
            if (component != this && component.getValueList().size() > 0) {
                for (Value value : component.getValueList()) {
                    if (value.getValue().getClass() != Color.class)
                        continue;

                    RenderUtil.drawRect(this.getX() + BORDER + TEXT_GAP, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll, this.getX() + BORDER + TEXT_GAP + this.getW() - BORDER - SCROLL_WIDTH - BORDER - 2, this.getY() + offsetY + BORDER + TEXT_GAP + mc.fontRenderer.FONT_HEIGHT - this.scroll, 0x45303030);
                    final boolean insideComponent = mouseX >= (this.getX() + BORDER) && mouseX <= (this.getX() + this.getW() - BORDER - SCROLL_WIDTH) && mouseY >= (this.getY() + BORDER + mc.fontRenderer.FONT_HEIGHT + 1 + offsetY - this.scroll - mc.fontRenderer.FONT_HEIGHT + 1) && mouseY <= (this.getY() + BORDER + (mc.fontRenderer.FONT_HEIGHT) + 1 + offsetY - this.scroll);
                    if (insideComponent) {
                        RenderUtil.drawGradientRect(this.getX() + BORDER + TEXT_GAP, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll, this.getX() + BORDER + TEXT_GAP + this.getW() - BORDER - SCROLL_WIDTH - BORDER - 2, this.getY() + offsetY + BORDER + TEXT_GAP + mc.fontRenderer.FONT_HEIGHT - this.scroll, 0x30909090, 0x00101010);
                    }

                    // draw button text
                    final int valueColor = ((Color) value.getValue()).getRGB();
                    mc.fontRenderer.drawStringWithShadow(ChatFormatting.GRAY + component.getName() + ": " + ChatFormatting.RESET + value.getName(), this.getX() + BORDER + TEXT_GAP + 1, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll, valueColor);

                    offsetY += mc.fontRenderer.FONT_HEIGHT + TEXT_GAP;
                }
            }
        }
        for (Module module : Seppuku.INSTANCE.getModuleManager().getModuleList()) {
            if (module.getValueList().size() > 0) {
                for (Value value : module.getValueList()) {
                    if (value.getValue().getClass() != Color.class)
                        continue;

                    RenderUtil.drawRect(this.getX() + BORDER + TEXT_GAP, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll, this.getX() + BORDER + TEXT_GAP + this.getW() - BORDER - SCROLL_WIDTH - BORDER - 2, this.getY() + offsetY + BORDER + TEXT_GAP + mc.fontRenderer.FONT_HEIGHT - this.scroll, 0x45303030);
                    final boolean insideComponent = mouseX >= (this.getX() + BORDER) && mouseX <= (this.getX() + this.getW() - BORDER - SCROLL_WIDTH) && mouseY >= (this.getY() + BORDER + mc.fontRenderer.FONT_HEIGHT + 1 + offsetY - this.scroll - mc.fontRenderer.FONT_HEIGHT + 1) && mouseY <= (this.getY() + BORDER + (mc.fontRenderer.FONT_HEIGHT) + 1 + offsetY - this.scroll);
                    if (insideComponent) {
                        RenderUtil.drawGradientRect(this.getX() + BORDER + TEXT_GAP, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll, this.getX() + BORDER + TEXT_GAP + this.getW() - BORDER - SCROLL_WIDTH - BORDER - 2, this.getY() + offsetY + BORDER + TEXT_GAP + mc.fontRenderer.FONT_HEIGHT - this.scroll, 0x30909090, 0x00101010);
                    }

                    // draw button text
                    final int valueColor = ((Color) value.getValue()).getRGB();
                    mc.fontRenderer.drawStringWithShadow(ChatFormatting.GRAY + module.getDisplayName() + ": " + ChatFormatting.RESET + value.getName(), this.getX() + BORDER + TEXT_GAP + 1, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll, valueColor);

                    offsetY += mc.fontRenderer.FONT_HEIGHT + TEXT_GAP;
                }
            }
        }

        if (this.currentColorComponent != null) {
            // draw overlay
            RenderUtil.drawRect(this.getX() + BORDER, this.getY() + BORDER + mc.fontRenderer.FONT_HEIGHT + 1, this.getX() + this.getW() - BORDER, this.getY() + this.getH() - BORDER, 0xCC101010);

            // draw color
            RenderUtil.drawRect(this.getX() + (this.getW() / 2) - 10, this.getY() + (this.getH() / 4) - 10, this.getX() + (this.getW() / 2) + 10, this.getY() + (this.getH() / 4) + 10, this.currentColorComponent.getCurrentColor().getRGB());
            RenderUtil.drawBorderedRect(this.getX() + (this.getW() / 2) - 10, this.getY() + (this.getH() / 4) - 10, this.getX() + (this.getW() / 2) + 10, this.getY() + (this.getH() / 4) + 10, 1.0f, 0x00000000, 0xFFAAAAAA);

            if (this.getW() > 180) {
                final String hexColor = "#" + Integer.toHexString(this.currentColorComponent.getCurrentColor().getRGB()).toLowerCase().substring(2);
                final String rgbColor = String.format("r%s g%s b%s", this.currentColorComponent.getCurrentColor().getRed(), this.currentColorComponent.getCurrentColor().getGreen(), this.currentColorComponent.getCurrentColor().getBlue());
                mc.fontRenderer.drawStringWithShadow(hexColor, this.getX() + (this.getW() / 2) + 12, this.getY() + (this.getH() / 4) - 16 + mc.fontRenderer.FONT_HEIGHT, 0xFFAAAAAA);
                mc.fontRenderer.drawStringWithShadow(rgbColor, this.getX() + (this.getW() / 2) + 12, this.getY() + (this.getH() / 4) - 16 + (mc.fontRenderer.FONT_HEIGHT * 2), 0xFFAAAAAA);
            }

            // draw name
            mc.fontRenderer.drawStringWithShadow(this.currentColorComponent.getName(), this.getX() + (this.getW() / 2) - mc.fontRenderer.getStringWidth(this.currentColorComponent.getName()) / 2.0f, this.getY() + (this.getH() / 4) + 14, 0xFFFFFFFF);
        }

        // end scissoring
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // render current color component
        if (this.currentColorComponent != null) {
            this.currentColorComponent.setX(this.getX() + 20);
            this.currentColorComponent.setY(this.getY() + (this.getH() / 2));
            this.currentColorComponent.setW(this.getW() - 40);
            this.currentColorComponent.setH(10);

            // draw bg
            RenderUtil.drawRect(this.currentColorComponent.getX(), this.currentColorComponent.getY(), this.currentColorComponent.getX() + this.currentColorComponent.getW(), this.currentColorComponent.getY() + this.currentColorComponent.getH(), 0xFF101010);
            this.currentColorComponent.render(mouseX, mouseY, partialTicks);
        }

        // figures up a "total height (pixels)" of the inside of the list area (for calculating scroll height)
        this.totalHeight = BORDER + TEXT_GAP + offsetY + BORDER;
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int button) {
        if (this.currentColorComponent != null) {
            if (this.currentColorComponent.isMouseInside(mouseX, mouseY))
                this.currentColorComponent.mouseRelease(mouseX, mouseY, button);
            else if (!this.isResizeDragging()) {
                this.currentColorComponent = null;
                return;
            }
        }

        super.mouseRelease(mouseX, mouseY, button);

        final boolean inside = this.isMouseInside(mouseX, mouseY);
        final boolean insideTitlebar = mouseY <= this.getY() + BORDER + TITLE_BAR_HEIGHT;

        if (inside && button == 0 && !insideTitlebar) {
            int offsetY = BORDER;

            for (HudComponent component : Seppuku.INSTANCE.getHudManager().getComponentList()) {
                if (component != this && component.getValueList().size() > 0) {
                    for (Value value : component.getValueList()) {
                        if (value.getValue().getClass() != Color.class)
                            continue;

                        final boolean insideComponent = mouseX >= (this.getX() + BORDER) && mouseX <= (this.getX() + this.getW() - BORDER - SCROLL_WIDTH) && mouseY >= (this.getY() + BORDER + mc.fontRenderer.FONT_HEIGHT + 1 + offsetY - this.scroll) && mouseY <= (this.getY() + BORDER + (mc.fontRenderer.FONT_HEIGHT * 2) + 1 + offsetY - this.scroll);
                        if (insideComponent && this.currentColorComponent == null) {
                            ColorComponent colorComponent = new ColorComponent(component.getName() + " " + value.getName(), ((Color) value.getValue()).getRGB(), ChatFormatting.WHITE + "Click to edit...");
                            colorComponent.returnListener = new ComponentListener() {
                                @Override
                                public void onComponentEvent() {
                                    value.setValue(colorComponent.getCurrentColor());
                                }
                            };
                            this.currentColorComponent = colorComponent;
                        }

                        offsetY += mc.fontRenderer.FONT_HEIGHT + TEXT_GAP;
                    }
                }
            }

            for (Module module : Seppuku.INSTANCE.getModuleManager().getModuleList()) {
                if (module.getValueList().size() > 0) {
                    for (Value value : module.getValueList()) {
                        if (value.getValue().getClass() != Color.class)
                            continue;

                        final boolean insideComponent = mouseX >= (this.getX() + BORDER) && mouseX <= (this.getX() + this.getW() - BORDER - SCROLL_WIDTH) && mouseY >= (this.getY() + BORDER + mc.fontRenderer.FONT_HEIGHT + 1 + offsetY - this.scroll) && mouseY <= (this.getY() + BORDER + (mc.fontRenderer.FONT_HEIGHT * 2) + 1 + offsetY - this.scroll);
                        if (insideComponent && this.currentColorComponent == null) {
                            ColorComponent colorComponent = new ColorComponent(module.getDisplayName() + " " + value.getName(), ((Color) value.getValue()).getRGB(), ChatFormatting.WHITE + "Click to edit...");
                            colorComponent.returnListener = new ComponentListener() {
                                @Override
                                public void onComponentEvent() {
                                    value.setValue(colorComponent.getCurrentColor());
                                    Seppuku.INSTANCE.getConfigManager().saveAll();
                                }
                            };
                            this.currentColorComponent = colorComponent;
                        }

                        offsetY += mc.fontRenderer.FONT_HEIGHT + TEXT_GAP;
                    }
                }
            }

            if (mouseX >= (this.getX() + this.getW() - SCROLL_WIDTH) && mouseX <= (this.getX() + this.getW() - BORDER)) { // mouse is inside scroll area on x-axis
                float diffY = this.getY() + TITLE_BAR_HEIGHT + ((this.getH() - TITLE_BAR_HEIGHT) / 2);
                if (mouseY > diffY) {
                    scroll += 10;
                } else {
                    scroll -= 10;
                }
                this.clampScroll();
            }
        }
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int button) {
        super.mouseClickMove(mouseX, mouseY, button);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);

        if (this.currentColorComponent != null)
            this.currentColorComponent.keyTyped(typedChar, keyCode);
    }

    private void clampScroll() {
        if (this.scroll < 0) {
            this.scroll = 0;
        }
        if (this.scroll > this.totalHeight - this.getH()) {
            this.scroll = this.totalHeight - (int) this.getH();
        }
    }

    private void handleScrolling(int mouseX, int mouseY) {
        if (this.isMouseInside(mouseX, mouseY) && Mouse.hasWheel()) {
            this.scroll += -(Mouse.getDWheel() / 5);
            this.clampScroll();
        }
    }

    public int getScroll() {
        return scroll;
    }

    public void setScroll(int scroll) {
        this.scroll = scroll;
    }

    public int getTotalHeight() {
        return totalHeight;
    }
}
