package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class NoToast extends Module {

    public NoToast() {
        super("NoToast", new String[]{"Toast"}, "Prevents Mojang Harrasment", "NONE", -1, ModuleType.WORLD);
    }

    final Minecraft mc = Minecraft.getMinecraft();

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        mc.getToastGui().clear();
    }
}
