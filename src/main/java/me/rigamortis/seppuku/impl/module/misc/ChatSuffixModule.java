package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.player.EventSendChatMessage;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.impl.module.hidden.CommandsModule;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketChatMessage;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * created by noil on 11/8/19 at 7:48 PM
 */
public final class ChatSuffixModule extends Module {

    private final String prefix = "\u23D0 \uA731\u1D07\u1D18\u1D18\u1D1C\u1D0B\u1D1C";

    public ChatSuffixModule() {
        super("ChatSuffix", new String[]{"Suffix", "Chat_Suffix", "CustomChat", "Custom_Chat"}, "Add a custom suffix to your chat messages.", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onSendChatMessage(EventSendChatMessage event) {
        final CommandsModule cmds = (CommandsModule) Seppuku.INSTANCE.getModuleManager().find(CommandsModule.class);
        if (cmds == null)
            return;

        if (event.getMessage().startsWith("/") || event.getMessage().startsWith(cmds.prefix.getString()))
            return;

        event.setCanceled(true);
        Minecraft.getMinecraft().getConnection().sendPacket(new CPacketChatMessage(event.getMessage() + " " + prefix));
    }
}
