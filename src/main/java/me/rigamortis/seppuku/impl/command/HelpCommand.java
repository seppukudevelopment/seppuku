package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;

/**
 * Author Seth
 * 4/16/2019 @ 8:39 AM.
 */
public final class HelpCommand extends Command {

    public HelpCommand() {
        super("Help", new String[]{"H", "?"}, "Displays all commands", "Help");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 1, 1)) {
            this.printUsage();
            return;
        }

        final int size = Seppuku.INSTANCE.getCommandManager().getCommandList().size();

        final TextComponentString msg = new TextComponentString("\2477Commands [" + size + "]\247f ");

        for (int i = 0; i < size; i++) {
            final Command cmd = Seppuku.INSTANCE.getCommandManager().getCommandList().get(i);

            msg.appendSibling(new TextComponentString("\247a" + cmd.getDisplayName() + "\2477" + ((i == size - 1) ? "" : ", "))
                    .setStyle(new Style()
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("\2476" + cmd.getDesc() + "\247f")
                                    .appendSibling(new TextComponentString("\n" + cmd.getUsage()))))));
        }

        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(msg);
    }
}
