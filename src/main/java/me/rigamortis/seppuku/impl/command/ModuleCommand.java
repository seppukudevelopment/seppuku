package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.impl.module.hidden.CommandsModule;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

/**
 * Author Seth
 * 5/1/2019 @ 7:59 PM.
 */
public final class ModuleCommand extends Command {

    public ModuleCommand() {
        super("Modules", new String[]{"Mods"}, "Displays all modules", "Modules");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 1, 1)) {
            this.printUsage();
            return;
        }

        final int size = Seppuku.INSTANCE.getModuleManager().getModuleList().size();

        final TextComponentString msg = new TextComponentString("\2477Modules [" + size + "]\247f ");

        final CommandsModule commandsModule = (CommandsModule) Seppuku.INSTANCE.getModuleManager().find(CommandsModule.class);

        for (int i = 0; i < size; i++) {
            final Module mod = Seppuku.INSTANCE.getModuleManager().getModuleList().get(i);
            if (mod != null) {
                msg.appendSibling(new TextComponentString((mod.isEnabled() ? "\247a" : "\247c") + mod.getDisplayName() + "\2477" + ((i == size - 1) ? "" : ", "))
                        .setStyle(new Style()
                                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("\2476" + (mod.getDesc() == null ? "There is no description for this module" : mod.getDesc()) + "\247f").appendSibling(new TextComponentString((mod.toUsageTextComponent() == null ? "" : "\n" + mod.toUsageTextComponent().getText()) + "\247f"))))
                                .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandsModule.getPrefix().getValue() + "toggle" + " " + mod.getDisplayName()))));
            }
        }

        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(msg);
    }
}
