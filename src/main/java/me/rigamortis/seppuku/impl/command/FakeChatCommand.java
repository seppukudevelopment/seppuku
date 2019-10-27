package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.api.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

/**
 * Author Seth
 * 8/1/2019 @ 7:22 PM.
 */
public final class FakeChatCommand extends Command {

    public FakeChatCommand() {
        super("FakeChat", new String[] {"FChat", "TellRaw"}, "Allows you to add a fake chat message", "FakeChat <Message>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final StringBuilder sb = new StringBuilder();

        for (int i = 1; i < split.length; i++) {
            final String s = split[i];
            sb.append(s + (i == split.length - 1 ? "" : " "));
        }

        final String message = sb.toString();
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message.replace("&", "\247")));
    }
}
