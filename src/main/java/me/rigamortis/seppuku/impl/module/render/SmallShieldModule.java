package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author fsck
 * 2019-10-20.
 */
public final class SmallShieldModule extends Module {

    final Minecraft mc = Minecraft.getMinecraft();
    ItemRenderer itemRenderer = Minecraft.getMinecraft().entityRenderer.itemRenderer;

    public SmallShieldModule() {
        super("SmallShield", new String[]{"SmallShield", "SS"}, "Lowers your offhand", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void changeOffhandProgress(EventPlayerUpdate event) {
        itemRenderer.equippedProgressOffHand = 0.5F;
    }
}
