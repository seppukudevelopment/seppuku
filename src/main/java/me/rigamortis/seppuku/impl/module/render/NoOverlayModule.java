package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.api.event.gui.EventRenderHelmet;
import me.rigamortis.seppuku.api.event.gui.EventRenderPortal;
import me.rigamortis.seppuku.api.event.render.EventRenderOverlay;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.BooleanValue;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/9/2019 @ 12:45 AM.
 */
public final class NoOverlayModule extends Module {

    public final BooleanValue portal = new BooleanValue("Portal", new String[]{}, true);
    public final BooleanValue helmet = new BooleanValue("Helmet", new String[]{}, true);
    public final BooleanValue block = new BooleanValue("Block", new String[]{}, true);
    public final BooleanValue water = new BooleanValue("Water", new String[]{}, true);
    public final BooleanValue fire = new BooleanValue("Fire", new String[]{}, true);

    public NoOverlayModule() {
        super("NoOverlay", new String[]{"AntiOverlay"}, "Removes screen overlay effects", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void renderOverlay(EventRenderOverlay event) {
        if (this.block.getBoolean() && event.getType() == EventRenderOverlay.OverlayType.BLOCK) {
            event.setCanceled(true);
        }
        if (this.water.getBoolean() && event.getType() == EventRenderOverlay.OverlayType.LIQUID) {
            event.setCanceled(true);
        }
        if (this.fire.getBoolean() && event.getType() == EventRenderOverlay.OverlayType.FIRE) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void renderHelmet(EventRenderHelmet event) {
        if (this.helmet.getBoolean()) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void renderPortal(EventRenderPortal event) {
        if (this.portal.getBoolean()) {
            event.setCanceled(true);
        }
    }

}
