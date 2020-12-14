package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.api.event.player.EventGetMouseOver;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemTool;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author noil
 */
public final class NoEntityTraceModule extends Module {

    public Value<Boolean> toolsOnly = new Value<Boolean>("Tools", new String[]{"OnlyTools", "Tool", "Pickaxe", "Axe", "Shovel"}, "When enabled, you will only trace through entities when holding tools.", true);

    public NoEntityTraceModule() {
        super("NoEntityTrace", new String[]{"NoMiningTrace", "EntityTrace", "MiningTrace", "NoBB"}, "Mine through entities by overriding the moused over entity-list.", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void onGetMouseOver(EventGetMouseOver event) {
        if (this.toolsOnly.getValue()) {
            final Minecraft mc = Minecraft.getMinecraft();
            if (mc.player != null) {
                if (mc.player.getHeldItemMainhand().getItem() instanceof ItemTool ||
                        mc.player.getHeldItemOffhand().getItem() instanceof ItemTool) {
                    event.setCanceled(true);
                }
            }
            return; // return so we don't cancel swords, swinging hand, etc
        }

        event.setCanceled(true);
    }
}
