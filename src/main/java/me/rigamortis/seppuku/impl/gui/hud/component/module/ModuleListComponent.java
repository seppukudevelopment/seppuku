package me.rigamortis.seppuku.impl.gui.hud.component.module;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.gui.hud.component.ResizableHudComponent;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import me.rigamortis.seppuku.impl.module.ui.HudEditorModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/**
 * created by noil on 11/4/19 at 12:02 PM
 */
public final class ModuleListComponent extends ResizableHudComponent {

    private Module.ModuleType type;

    private int scroll;

    private int totalHeight;

    private final int SCROLL_WIDTH = 4;
    private final int BORDER = 2;
    private final int TEXT_GAP = 1;
    private final int TITLE_BAR_HEIGHT = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT + 1;

    private final HudEditorModule hudEditorModule = (HudEditorModule) Seppuku.INSTANCE.getModuleManager().find(HudEditorModule.class);

    public ModuleListComponent(Module.ModuleType type) {
        super(StringUtils.capitalize(type.name().toLowerCase()), 100, 100);
        this.type = type;
        this.setSnappable(false);
        this.setW(100);
        this.setH(100);
        this.setX((Minecraft.getMinecraft().displayWidth / 2) - (this.getW() / 2));
        this.setY((Minecraft.getMinecraft().displayHeight / 2) - (this.getH() / 2));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final Minecraft mc = Minecraft.getMinecraft();

        if (!(mc.currentScreen instanceof GuiHudEditor))
            return;

        final ScaledResolution sr = new ScaledResolution(mc);

        // Render Y pos offset (make this all modular evnentually...)
        int offsetY = 0;

        // Scrolling
        this.handleScrolling(mouseX, mouseY);

        // No dragging inside box
        final boolean insideTitlebar = mouseY <= this.getY() + TITLE_BAR_HEIGHT + BORDER;
        if (!insideTitlebar) {
            this.setDragging(false);
        }

        // Background & title
        RenderUtil.drawRect(this.getX() - 1, this.getY() - 1, this.getX() + this.getW() + 1, this.getY() + this.getH() + 1, 0x99101010);
        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0xFF202020);
        mc.fontRenderer.drawStringWithShadow(this.getName(), this.getX() + 2, this.getY() + 2, 0xFFFFFFFF);
        offsetY += mc.fontRenderer.FONT_HEIGHT + 1;

        // Behind hub
        RenderUtil.drawRect(this.getX() + BORDER, this.getY() + offsetY + BORDER, this.getX() + this.getW() - SCROLL_WIDTH - BORDER, this.getY() + this.getH() - BORDER, 0xFF101010);

        // Scrollbar bg
        RenderUtil.drawRect(this.getX() + this.getW() - SCROLL_WIDTH, this.getY() + offsetY + BORDER, this.getX() + this.getW() - BORDER, this.getY() + this.getH() - BORDER, 0xFF101010);
        // Scrollbar highlights
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
        // Scrollbar
        RenderUtil.drawRect(this.getX() + this.getW() - SCROLL_WIDTH, MathHelper.clamp((this.getY() + offsetY + BORDER) + ((this.getH() * this.scroll) / this.totalHeight), (this.getY() + offsetY + BORDER), (this.getY() + this.getH() - BORDER)), this.getX() + this.getW() - BORDER, MathHelper.clamp((this.getY() + this.getH() - BORDER) - (this.getH() * (this.totalHeight - this.getH() - this.scroll) / this.totalHeight), (this.getY() + offsetY + BORDER), (this.getY() + this.getH() - BORDER)), 0xFF909090);

        // Begin scissoring and render the module "buttons"
        RenderUtil.glScissor(this.getX() + BORDER, this.getY() + offsetY + BORDER, this.getX() + this.getW() - BORDER - SCROLL_WIDTH, this.getY() + this.getH() - BORDER, sr);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        for (Module module : Seppuku.INSTANCE.getModuleManager().getModuleList(this.type)) {
            RenderUtil.drawRect(this.getX() + BORDER + TEXT_GAP, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll, this.getX() + BORDER + TEXT_GAP + this.getW() - BORDER - SCROLL_WIDTH - BORDER - 2, this.getY() + offsetY + BORDER + TEXT_GAP + mc.fontRenderer.FONT_HEIGHT - this.scroll, module.isEnabled() ? 0x451b002a : 0x451F1C22);
            final boolean insideModule = mouseX >= (this.getX() + BORDER) && mouseX <= (this.getX() + this.getW() - BORDER - SCROLL_WIDTH) && mouseY >= (this.getY() + BORDER + Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT + 1 + offsetY - this.scroll - mc.fontRenderer.FONT_HEIGHT + 1) && mouseY <= (this.getY() + BORDER + (Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT) + 1 + offsetY - this.scroll);
            if (insideModule) {
                RenderUtil.drawGradientRect(this.getX() + BORDER + TEXT_GAP, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll, this.getX() + BORDER + TEXT_GAP + this.getW() - BORDER - SCROLL_WIDTH - BORDER - 2, this.getY() + offsetY + BORDER + TEXT_GAP + mc.fontRenderer.FONT_HEIGHT - this.scroll, 0x30909090, 0x00101010);
            }
            mc.fontRenderer.drawStringWithShadow(module.getDisplayName(), this.getX() + BORDER + TEXT_GAP + 1, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll, module.isEnabled() ? 0xFFC255FF : 0xFF7A6E80);
            offsetY += mc.fontRenderer.FONT_HEIGHT + TEXT_GAP;
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Handle tooltips
        if (this.hudEditorModule != null && this.hudEditorModule.tooltips.getValue()) {
            final boolean inside = mouseX >= this.getX() && mouseX <= this.getX() + this.getW() && mouseY >= this.getY() && mouseY <= this.getY() + this.getH();

            if (inside) {
                int height = BORDER;
                for (Module module : Seppuku.INSTANCE.getModuleManager().getModuleList(this.type)) {
                    final boolean insideComponent = mouseX >= (this.getX() + BORDER) && mouseX <= (this.getX() + this.getW() - BORDER - SCROLL_WIDTH) && mouseY >= (this.getY() + BORDER + Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT + 1 + height - this.scroll) && mouseY <= (this.getY() + BORDER + (Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT * 2) + 1 + height - this.scroll);
                    if (!insideTitlebar && insideComponent) {
                        final int tooltipWidth = mc.fontRenderer.getStringWidth(module.getDesc());
                        final int tooltipHeight = mc.fontRenderer.FONT_HEIGHT;

                        GlStateManager.translate(mouseX - tooltipWidth / 2, mouseY - tooltipHeight, 0);
                        // Tooltip background
                        RenderUtil.drawRect(-2, -6, tooltipWidth + 2, 5, 0x80101010);
                        RenderUtil.drawRect(-1, -5, tooltipWidth + 1, 4, 0xAD101010);

                        // Tooltip
                        mc.fontRenderer.drawStringWithShadow(module.getDesc(), 0, -5, 0xADC255FF);
                        GlStateManager.translate(-(mouseX - tooltipWidth / 2), -(mouseY - tooltipHeight), 0);
                    }
                    height += Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT + TEXT_GAP;
                }
            }
        }

        // figures up a "total height (pixels)" of the inside of the list area (for calculating scroll height)
        this.totalHeight = BORDER + TEXT_GAP + offsetY + BORDER;
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int button) {
        super.mouseRelease(mouseX, mouseY, button);

        final boolean inside = mouseX >= this.getX() && mouseX <= this.getX() + this.getW() && mouseY >= this.getY() && mouseY <= this.getY() + this.getH();
        final int titleBarHeight = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT + 1;
        final boolean insideTitlebar = mouseY <= this.getY() + BORDER + titleBarHeight;

        if (inside && button == 0 && !insideTitlebar) {
            int offsetY = BORDER;

            for (Module module : Seppuku.INSTANCE.getModuleManager().getModuleList(this.type)) {
                final boolean insideComponent = mouseX >= (this.getX() + BORDER) && mouseX <= (this.getX() + this.getW() - BORDER - SCROLL_WIDTH) && mouseY >= (this.getY() + BORDER + Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT + 1 + offsetY - this.scroll) && mouseY <= (this.getY() + BORDER + (Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT * 2) + 1 + offsetY - this.scroll);
                if (insideComponent) {
                    module.toggle();
                    this.setDragging(false);
                }
                offsetY += Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT + TEXT_GAP;
            }

            if (mouseX >= (this.getX() + this.getW() - SCROLL_WIDTH) && mouseX <= (this.getX() + this.getW() - BORDER)) { // mouse is inside scroll area on x-axis
                float diffY = this.getY() + TITLE_BAR_HEIGHT + ((this.getH() - TITLE_BAR_HEIGHT) / 2);
                if (mouseY > diffY) {
                    scroll += 10;
                } else {
                    scroll -= 10;
                }
            }
        }
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int button) {
        super.mouseClickMove(mouseX, mouseY, button);
    }

    private void handleScrolling(int mouseX, int mouseY) {
        final boolean inside = mouseX >= this.getX() && mouseX <= this.getX() + this.getW() && mouseY >= this.getY() && mouseY <= this.getY() + this.getH();
        if (inside && Mouse.hasWheel()) {
            this.scroll += -(Mouse.getDWheel() / 10);

            if (this.scroll < 0) {
                this.scroll = 0;
            }
            if (this.scroll > this.totalHeight - this.getH()) {
                this.scroll = this.totalHeight - (int) this.getH();
            }
        }
    }
}
