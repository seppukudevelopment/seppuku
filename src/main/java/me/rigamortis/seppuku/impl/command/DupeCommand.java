package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.api.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.play.client.CPacketUseEntity;

/**
 * Author Seth
 * 5/12/2019 @ 8:05 PM.
 */
public final class DupeCommand extends Command {

    public DupeCommand() {
        super("Dupe", new String[] {"Dup", "Doop"}, "Allows you to dupe your inventory", "Dupe");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 1, 1)) {
            this.printUsage();
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();

        if(mc.player != null) {
            for (int i = 0; i <= 45; i++) {
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, i, -1, ClickType.THROW, mc.player);
            }

            mc.player.connection.sendPacket(new CPacketUseEntity(mc.player));
        }
    }
}
