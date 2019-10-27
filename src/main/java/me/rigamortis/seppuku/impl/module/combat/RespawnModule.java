package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.api.event.minecraft.EventDisplayGui;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/20/2019 @ 9:09 AM.
 */
public final class RespawnModule extends Module {

    public RespawnModule() {
        super("Respawn", new String[] {"AutoRespawn", "Resp"}, "Automatically respawn after death", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void displayGuiScreen(EventDisplayGui event) {
        if(event.getScreen() != null && event.getScreen() instanceof GuiGameOver) {
            Minecraft.getMinecraft().player.respawnPlayer();
        }
    }

}
