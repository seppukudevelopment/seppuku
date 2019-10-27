package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.macro.Macro;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import org.lwjgl.input.Keyboard;

/**
 * Author Seth
 * 5/7/2019 @ 4:20 AM.
 */
public final class MacroCommand extends Command {

    private String[] addAlias = new String[]{"Add", "A"};
    private String[] removeAlias = new String[]{"Remove", "R", "Rem", "Delete", "Del"};
    private String[] listAlias = new String[]{"List", "L"};
    private String[] clearAlias = new String[]{"Clear", "C"};

    public MacroCommand() {
        super("Macro", new String[]{"Mac"}, "Allows you to create chat macros", "Macro Add <Name> <Key> <Macro>\n" +
                "Macro Remove <Name>\n" +
                "Macro List\n" +
                "Macro Clear");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        if (equals(addAlias, split[1])) {
            if (!this.clamp(input, 5)) {
                this.printUsage();
                return;
            }

            final String name = split[2];
            final String key = split[3];

            final Macro macro = Seppuku.INSTANCE.getMacroManager().find(name);

            if(macro != null) {
                Seppuku.INSTANCE.logChat("\247c\"" + name + "\"\247f is already a macro");
            }else{
                if(Keyboard.getKeyIndex(key.toUpperCase()) != Keyboard.KEY_NONE) {
                    final StringBuilder sb = new StringBuilder();

                    final int size = split.length;

                    for (int i = 4; i < size; i++) {
                        final String arg = split[i];
                        sb.append(arg + ((i == size - 1) ? "" : " "));
                    }

                    Seppuku.INSTANCE.logChat("Added macro \247c" + name + "\247f bound to " + key.toUpperCase());
                    Seppuku.INSTANCE.getMacroManager().getMacroList().add(new Macro(name, key.toUpperCase(), sb.toString()));
                    Seppuku.INSTANCE.getConfigManager().saveAll();
                }else{
                    Seppuku.INSTANCE.logChat("\247c" + key + "\247f is not a valid key");
                }
            }
        } else if (equals(removeAlias, split[1])) {
            if (!this.clamp(input, 3, 3)) {
                this.printUsage();
                return;
            }

            final String name = split[2];

            final Macro macro = Seppuku.INSTANCE.getMacroManager().find(name);

            if(macro != null) {
                Seppuku.INSTANCE.logChat("Removed macro \247c" + macro.getName() + " \247f");
                Seppuku.INSTANCE.getMacroManager().getMacroList().remove(macro);
                Seppuku.INSTANCE.getConfigManager().saveAll();
            }else{
                //TODO similar
                Seppuku.INSTANCE.errorChat("Unknown macro " + "\247f\"" + name + "\"");
            }
        } else if (equals(listAlias, split[1])) {
            if (!this.clamp(input, 2, 2)) {
                this.printUsage();
                return;
            }

            final int size = Seppuku.INSTANCE.getMacroManager().getMacroList().size();

            if(size > 0) {
                final TextComponentString msg = new TextComponentString("\2477Macros [" + size + "]\247f ");

                for (int i = 0; i < size; i++) {
                    final Macro macro = Seppuku.INSTANCE.getMacroManager().getMacroList().get(i);
                    if (macro != null) {
                        msg.appendSibling(new TextComponentString("\247a" + macro.getName() + "\2477" + ((i == size - 1) ? "" : ", "))
                                .setStyle(new Style()
                                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Key: " + macro.getKey().toUpperCase() + "\n" + "Macro: " + macro.getMacro())))));
                    }
                }

                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(msg);
            }else{
                Seppuku.INSTANCE.logChat("You don't have any macros");
            }
        } else if (equals(clearAlias, split[1])) {
            if (!this.clamp(input, 2, 2)) {
                this.printUsage();
                return;
            }

            final int macros = Seppuku.INSTANCE.getMacroManager().getMacroList().size();

            if(macros > 0) {
                Seppuku.INSTANCE.logChat("Removed \247c" + macros + "\247f macro" + (macros > 1 ? "s" : ""));
                Seppuku.INSTANCE.getMacroManager().getMacroList().clear();
                Seppuku.INSTANCE.getConfigManager().saveAll();
            }else{
                Seppuku.INSTANCE.logChat("You don't have any macros");
            }
        } else {
            Seppuku.INSTANCE.errorChat("Unknown input " + "\247f\"" + input + "\"");
            this.printUsage();
        }

    }

}
