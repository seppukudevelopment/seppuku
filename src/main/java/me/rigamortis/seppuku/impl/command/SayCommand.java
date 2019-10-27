package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.api.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketChatMessage;

/**
 * Author Seth
 * 5/23/2019 @ 8:14 AM.
 */
public final class SayCommand extends Command {

    public SayCommand() {
        super("Say", new String[] {"S"}, "Allows you to send a direct chat message", "Say <Message>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        Minecraft.getMinecraft().player.connection.sendPacket(new CPacketChatMessage(input.substring(split[0].length() + 1)));
    }
}
