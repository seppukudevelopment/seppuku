package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.event.render.EventRender2D;
import me.rigamortis.seppuku.impl.module.world.InfEnderChestModule;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author Seth (riga)
 */
public final class LastInvCommand extends Command {

    public LastInvCommand() {
        super("LastInv", new String[]{"EnderChest", "Echest", "Portable"}, "Opens your previous inventory if \"MoreInv\" is enabled", "LastInv");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 1, 1)) {
            this.printUsage();
            return;
        }

        Seppuku.INSTANCE.getEventManager().addEventListener(this); // subscribe to the event listener
    }

    @Listener
    public void render(EventRender2D event) {
        final InfEnderChestModule mod = (InfEnderChestModule) Seppuku.INSTANCE.getModuleManager().find(InfEnderChestModule.class);
        if (mod != null) {
            if (mod.getScreen() != null) {
                Minecraft.getMinecraft().displayGuiScreen(mod.getScreen());
                Seppuku.INSTANCE.logChat("Opening the last inventory.");
            } else {
                Seppuku.INSTANCE.logChat("Inventory already closed.");
            }
        }
        Seppuku.INSTANCE.getEventManager().removeEventListener(this);
    }

}
