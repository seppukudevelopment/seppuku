package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.GuiConnecting;

/**
 * Author Seth
 * 5/24/2019 @ 2:47 AM.
 */
public final class ConnectCommand extends Command {

    public ConnectCommand() {
        super("Connect", new String[]{"Con"}, "Connects to a server", "Connect <Host>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final String[] host = split[1].split(":");

        int port = 25565;

        if (host.length > 1) {
            if (StringUtil.isInt(host[1])) {
                port = Integer.parseInt(host[1]);
            }else{
                Seppuku.INSTANCE.errorChat("Invalid port \"" + host[1] + "\"");
            }
        }

        if(Minecraft.getMinecraft().player.connection.getNetworkManager().channel().isOpen()) {
            Minecraft.getMinecraft().player.connection.getNetworkManager().closeChannel(null);
        }

        Minecraft.getMinecraft().displayGuiScreen(new GuiConnecting(null, Minecraft.getMinecraft(), host[0], port));
    }

}
