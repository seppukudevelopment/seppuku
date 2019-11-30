package me.rigamortis.seppuku.impl.module.hidden;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.event.minecraft.EventKeyPress;
import me.rigamortis.seppuku.api.event.player.EventSendChatMessage;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import org.lwjgl.input.Keyboard;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/16/2019 @ 8:44 AM.
 */
public final class CommandsModule extends Module {

    public final Value<String> prefix = new Value("Prefix", new String[]{"prefx", "pfx"}, "The command prefix.", ".");

    public CommandsModule() {
        super("Commands", new String[]{"cmds", "cmd"}, "Allows you to execute client commands", "NONE", -1, ModuleType.HIDDEN);
        this.setHidden(true);
        this.toggle();
    }

    @Listener
    public void keyPress(EventKeyPress event) {
        if(this.prefix.getValue().length() == 1) {
            final char key = Keyboard.getEventCharacter();
            if(this.prefix.getValue().charAt(0) == key) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiChat());
            }
        }
    }

    @Listener
    public void sendChatMessage(EventSendChatMessage event) {
        if (event.getMessage().startsWith(this.prefix.getValue())) {
            final String input = event.getMessage().substring(this.prefix.getValue().length());
            final String[] split = input.split(" ");

            final Command command = Seppuku.INSTANCE.getCommandManager().find(split[0]);

            if (command != null) {
                try {
                    command.exec(input);
                } catch (Exception e) {
                    e.printStackTrace();
                    Seppuku.INSTANCE.errorChat("Error while running command");
                }
            } else {
                Seppuku.INSTANCE.errorChat("Unknown command " + "\247f\"" + event.getMessage() + "\"");
                final Command similar = Seppuku.INSTANCE.getCommandManager().findSimilar(split[0]);

                if (similar != null) {
                    Seppuku.INSTANCE.logChat("Did you mean " + "\247c" + similar.getDisplayName() + "\247f?");
                }
            }

            event.setCanceled(true);
        }
    }

    public Value<String> getPrefix() {
        return prefix;
    }
}
