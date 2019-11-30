package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.api.event.gui.EventRenderHelmet;
import me.rigamortis.seppuku.api.event.gui.EventRenderPortal;
import me.rigamortis.seppuku.api.event.render.EventRenderOverlay;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/9/2019 @ 12:45 AM.
 */
public final class NoOverlayModule extends Module {

    public final Value<Boolean> portal = new Value<Boolean>("Portal", new String[]{}, "Disables the portal screen overlay when using a portal.", true);
    public final Value<Boolean> helmet = new Value<Boolean>("Helmet", new String[]{}, "Disables the helmet/pumpkin screen overlay.", true);
    public final Value<Boolean> block = new Value<Boolean>("Block", new String[]{}, "Disables the block-side screen overlay when inside of a block.", true);
    public final Value<Boolean> water = new Value<Boolean>("Water", new String[]{}, "Disables the water screen overlay when under water.", true);
    public final Value<Boolean> fire = new Value<Boolean>("Fire", new String[]{}, "Disables the fire screen overlay when on fire.", true);

    public NoOverlayModule() {
        super("NoOverlay", new String[]{"AntiOverlay"}, "Removes screen overlay effects", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void renderOverlay(EventRenderOverlay event) {
        if (this.block.getValue() && event.getType() == EventRenderOverlay.OverlayType.BLOCK) {
            event.setCanceled(true);
        }
        if (this.water.getValue() && event.getType() == EventRenderOverlay.OverlayType.LIQUID) {
            event.setCanceled(true);
        }
        if (this.fire.getValue() && event.getType() == EventRenderOverlay.OverlayType.FIRE) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void renderHelmet(EventRenderHelmet event) {
        if (this.helmet.getValue()) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void renderPortal(EventRenderPortal event) {
        if (this.portal.getValue()) {
            event.setCanceled(true);
        }
    }

}
