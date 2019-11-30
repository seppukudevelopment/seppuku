package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.impl.module.hidden.CommandsModule;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketChatMessage;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author: Francesco
 * 18/10/2019 at 21:03.
 * Might contain literal spaghetti code.
 */

public class DiscordBypassModule extends Module {

    public DiscordBypassModule() {
        super("DiscordBypass", new String[]{"DiscBypass", "NoDiscord", "NoBan"}, "Bypasses jj's stupid plugin that tempbans you when you say \"dicord\" nearby spawn.", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof CPacketChatMessage) {
                final CPacketChatMessage packet = (CPacketChatMessage) event.getPacket();
                final CommandsModule commands = (CommandsModule) Seppuku.INSTANCE.getModuleManager().find(CommandsModule.class);
                if (commands != null) {
                    if (packet.getMessage().startsWith(commands.prefix.getValue()) || packet.getMessage().startsWith("/"))
                        return;
                    //Technically the "spawn area" is usually around 23x23 chunks, but jj might've changed it... or perhaps he might not be using the spawn chunks at all.
                    //Just in case he's going to the "anarchy definition" of spawn, I set it to 1k. Better safe than sorry, nobody except jj will notice the extra #.
                    if (Minecraft.getMinecraft().player.posX <= 1000 || Minecraft.getMinecraft().player.posZ <= 1000) {
                        //This might seem like a stupid way to do it, but it's the best one I know to get this done without making the whole message lowercase.
                        //If you want the straightforward method, you can use the part that is commented out down here.
                        /*
                        if (packet.message.toLowerCase().contains("discord"))
                            packet.message = packet.message.toLowerCase().replace("discord", "disc#ord");
                         */
                        if (packet.message.toLowerCase().contains("discord")) {
                            for (int i = 0; i < packet.message.length(); i++) {
                                if (packet.message.toLowerCase().charAt(i) == 'd') {
                                    if (packet.message.toLowerCase().toLowerCase().charAt(i + 1) == 'i' &&
                                            packet.message.toLowerCase().toLowerCase().charAt(i + 2) == 's' &&
                                            packet.message.toLowerCase().toLowerCase().charAt(i + 3) == 'c' &&
                                            packet.message.toLowerCase().toLowerCase().charAt(i + 4) == 'o' &&
                                            packet.message.toLowerCase().toLowerCase().charAt(i + 5) == 'r' &&
                                            packet.message.toLowerCase().toLowerCase().charAt(i + 6) == 'd')
                                        packet.message = new StringBuilder(packet.message).insert(i + 3, "#").toString();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
