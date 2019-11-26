package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.gui.EventRenderPotions;
import me.rigamortis.seppuku.api.event.render.EventRender2D;
import me.rigamortis.seppuku.api.gui.hud.component.HudComponent;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.BooleanValue;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/7/2019 @ 10:17 PM.
 */
public final class HudModule extends Module {

    public final BooleanValue hidePotions = new BooleanValue("HidePotions", new String[]{"HidePotions", "HidePots", "Hide_Potions"}, true);

    public HudModule() {
        super("Hud", new String[]{"Overlay"}, "Shows lots of useful info", "NONE", -1, ModuleType.RENDER);
        this.setHidden(true);
        this.setEnabled(true);
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
                component.render(0, 0, mc.getRenderPartialTicks());
            }
        }
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Listener
    public void renderPotions(EventRenderPotions event) {
        if (this.hidePotions.getBoolean()) {
            event.setCanceled(true);
        }
    }
}
