package me.rigamortis.seppuku.impl.gui.hud;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.gui.hud.component.HudComponent;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.impl.gui.hud.anchor.AnchorPoint;
import me.rigamortis.seppuku.impl.module.ui.HudEditorModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

/**
 * Author Seth
 * 7/25/2019 @ 4:15 AM.
 */
public final class GuiHudEditor extends GuiScreen {

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        final HudEditorModule mod = (HudEditorModule) Seppuku.INSTANCE.getModuleManager().find(HudEditorModule.class);

        if (mod != null) {
            if (keyCode == Keyboard.getKeyIndex(mod.getKey())) {
                if (mod.isOpen()) {
                    mod.setOpen(false);
                } else {
                    Minecraft.getMinecraft().displayGuiScreen(null);
                }
            }
        }

        for (HudComponent component : Seppuku.INSTANCE.getHudManager().getComponentList()) {
            if (component.isVisible()) {
                component.keyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    public void onResize(Minecraft mcIn, int w, int h) {
        super.onResize(mcIn, w, h);

        final ScaledResolution sr = new ScaledResolution(mcIn);
        for (AnchorPoint anchorPoint : Seppuku.INSTANCE.getHudManager().getAnchorPoints()) {
            anchorPoint.updatePosition(sr);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawDefaultBackground();

        final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

        final float halfWidth = res.getScaledWidth() / 2.0f;
        final float halfHeight = res.getScaledHeight() / 2.0f;
        RenderUtil.drawLine(halfWidth, 0, halfWidth, res.getScaledHeight(), 1, 0x75909090);
        RenderUtil.drawLine(0, halfHeight, res.getScaledWidth(), halfHeight, 1, 0x75909090);

        for (AnchorPoint point : Seppuku.INSTANCE.getHudManager().getAnchorPoints()) {
            RenderUtil.drawRect(point.getX() - 1, point.getY() - 1, point.getX() + 1, point.getY() + 1, 0x75909090);
        }

        for (HudComponent component : Seppuku.INSTANCE.getHudManager().getComponentList()) {
            if (component.isVisible()) {
                component.render(mouseX, mouseY, partialTicks);

                if (component instanceof DraggableHudComponent) {
                    DraggableHudComponent draggable = (DraggableHudComponent) component;
                    if (draggable.isDragging()) {
                        int SIZE = 2;
                        if (draggable.getW() < 12 || draggable.getH() < 12)
                            SIZE = 1;
                        else if (draggable.getW() <= 0 || draggable.getH() <= 0)
                            SIZE = 0;

                        boolean colliding = false;

                        for (HudComponent other : Seppuku.INSTANCE.getHudManager().getComponentList()) {
                            if (other instanceof DraggableHudComponent) {
                                DraggableHudComponent otherDraggable = (DraggableHudComponent) other;
                                if (other != draggable && draggable.collidesWith(otherDraggable) && otherDraggable.isVisible() && draggable.isSnappable() && otherDraggable.isSnappable()) {
                                    colliding = true;
                                    RenderUtil.drawBorderedRect(draggable.getX() - 1, draggable.getY() - 1, draggable.getX() + draggable.getW() + 1, draggable.getY() + draggable.getH() + 1, 1, 0x00000000, 0x3500FF00);
                                    RenderUtil.drawRect(draggable.getX(), draggable.getY(), draggable.getX() + draggable.getW(), draggable.getY() + draggable.getH(), 0x3500FF00);
                                    RenderUtil.drawBorderedRect(other.getX() - 1, other.getY() - 1, other.getX() + other.getW() + 1, other.getY() + other.getH() + 1, 1, 0x00000000, 0x3500FF00);
                                    RenderUtil.drawRect(other.getX(), other.getY(), other.getX() + other.getW(), other.getY() + other.getH(), 0x3500FF00);
                                }
                            }
                        }

                        // render white borders if snapable and is not colliding with any components
                        if (draggable.isSnappable() && !colliding) {
                            int snappableBackgroundColor = 0x00000000;
                            if (draggable.findClosest(mouseX, mouseY) != null) { // has an anchor point nearby
                                snappableBackgroundColor = 0x35FFFFFF;
                            }
                            RenderUtil.drawBorderedRect(draggable.getX() - 1, draggable.getY() - 1, draggable.getX() + draggable.getW() + 1, draggable.getY() + draggable.getH() + 1, 1, snappableBackgroundColor, 0x90FFFFFF);
                            RenderUtil.drawRect(draggable.getX(), draggable.getY(), draggable.getX() + SIZE, draggable.getY() + SIZE, 0x90FFFFFF);
                            RenderUtil.drawRect(draggable.getX() + draggable.getW() - SIZE, draggable.getY(), draggable.getX() + draggable.getW(), draggable.getY() + SIZE, 0x90FFFFFF);
                            RenderUtil.drawRect(draggable.getX(), (draggable.getY() + draggable.getH()) - SIZE, draggable.getX() + SIZE, draggable.getY() + draggable.getH(), 0x90FFFFFF);
                            RenderUtil.drawRect(draggable.getX() + draggable.getW() - SIZE, (draggable.getY() + draggable.getH()) - SIZE, draggable.getX() + draggable.getW(), draggable.getY() + draggable.getH(), 0x90FFFFFF);
                        }

                        // dragging highlight
                        //RenderUtil.drawRect(draggable.getX(), draggable.getY(), draggable.getX() + draggable.getW(), draggable.getY() + draggable.getH(), 0x35FFFFFF);
                    }
                }
            }
        }
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        for (HudComponent component : Seppuku.INSTANCE.getHudManager().getComponentList()) {
            if (component.isVisible()) {
                component.mouseClickMove(mouseX, mouseY, clickedMouseButton);
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);

            for (HudComponent component : Seppuku.INSTANCE.getHudManager().getComponentList()) {
                if (component.isVisible()) {
                    component.mouseClick(mouseX, mouseY, mouseButton);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        for (HudComponent component : Seppuku.INSTANCE.getHudManager().getComponentList()) {
            if (component.isVisible()) {
                component.mouseRelease(mouseX, mouseY, state);
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        //Seppuku.INSTANCE.getConfigManager().saveAll();

        final HudEditorModule hudEditorModule = (HudEditorModule) Seppuku.INSTANCE.getModuleManager().find(HudEditorModule.class);
        if (hudEditorModule != null) {
            if (hudEditorModule.blur.getValue()) {
                if (OpenGlHelper.shadersSupported) {
                    mc.entityRenderer.stopUseShader();
                }
            }
        }

        for (HudComponent component : Seppuku.INSTANCE.getHudManager().getComponentList()) {
            if (component instanceof DraggableHudComponent) {
                if (component.isVisible()) {
                    final DraggableHudComponent draggable = (DraggableHudComponent) component;
                    if (draggable.isDragging())
                        draggable.setDragging(false);
                }
            }
        }

        // go back to previous screen
        super.onGuiClosed();
    }

    public void unload() {
        // empty
    }
}
