package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.gui.EventRenderPotions;
import me.rigamortis.seppuku.api.event.render.EventRender2D;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.gui.hud.component.HudComponent;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import me.rigamortis.seppuku.impl.gui.hud.anchor.AnchorPoint;
import me.rigamortis.seppuku.impl.module.movement.FlightModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/7/2019 @ 10:17 PM.
 */
public final class HudModule extends Module {

    public final Value<HudModule.Mode> mode = new Value<HudModule.Mode>("Sorting Mode", new String[]{"Sorting", "sort"}, "Changes arraylist sorting.", HudModule.Mode.LENGTH);
    public final Value<Boolean> hidePotions = new Value<Boolean>("HidePotions", new String[]{"HidePotions", "HidePots", "Hide_Potions"}, "Hides the Vanilla potion hud (at the top right of the screen).", true);

    public HudModule() {
        super("Hud", new String[]{"Overlay"}, "Shows lots of useful info", "NONE", -1, ModuleType.RENDER);
        this.setHidden(true);
    }

    @Listener
    public void render(EventRender2D event) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.gameSettings.showDebugInfo) {
            return;
        }

        if (mc.currentScreen instanceof GuiHudEditor) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        for (HudComponent component : Seppuku.INSTANCE.getHudManager().getComponentList()) {
            if (component.isVisible()) {

                //dont render components with the TOP_CENTER anchor if we are looking at the tab list
                if (component instanceof DraggableHudComponent) {
                    final DraggableHudComponent draggableComponent = (DraggableHudComponent) component;
                    if (draggableComponent.getAnchorPoint() != null && draggableComponent.getAnchorPoint().getPoint() == AnchorPoint.Point.TOP_CENTER) {
                        if (!mc.gameSettings.keyBindPlayerList.isKeyDown()) {
                            draggableComponent.render(0, 0, mc.getRenderPartialTicks());
                        }
                    } else {
                        draggableComponent.render(0, 0, mc.getRenderPartialTicks());
                    }
                } else {
                    component.render(0, 0, mc.getRenderPartialTicks());
                }
            }
        }
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Listener
    public void renderPotions(EventRenderPotions event) {
        if (this.hidePotions.getValue()) {
            event.setCanceled(true);
        }
    }

    public enum Mode {
        LENGTH, ALPHABET, UNSORTED;
    }
}
