package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.impl.module.misc.AutoIgnoreModule;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

/**
 * Author Seth
 * 7/1/2019 @ 11:37 PM.
 */
public final class AutoIgnoreCommand extends Command {

    private String[] addAlias = new String[]{"Add", "A"};
    private String[] removeAlias = new String[]{"Remove", "R", "Rem", "Delete", "Del"};
    private String[] listAlias = new String[]{"List", "L"};
    private String[] clearAlias = new String[]{"Clear", "C"};

    public AutoIgnoreCommand() {
        super("AutoIgnore", new String[]{"AutomaticIgnore", "AIG", "AIgnore"}, "Allows you to add or remove phrases from AutoIgnore", "AutoIgnore Add <Phrase>\n" +
                "AutoIgnore Remove <Phrase>\n" +
                "AutoIgnore List\n" +
                "AutoIgnore Clear");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final AutoIgnoreModule autoIgnoreModule = (AutoIgnoreModule) Seppuku.INSTANCE.getModuleManager().find(AutoIgnoreModule.class);

        if (autoIgnoreModule == null) {
            Seppuku.INSTANCE.errorChat("AutoIgnore is missing");
            return;
        }

        if (equals(addAlias, split[1])) {
            if (!this.clamp(input, 3)) {
                this.printUsage();
                return;
            }
            final StringBuilder sb = new StringBuilder();

            for (int i = 2; i < split.length; i++) {
                final String s = split[i];
                sb.append(s + (i == split.length - 1 ? "" : " "));
            }

            final String phrase = sb.toString();

            if (autoIgnoreModule.blacklistContains(phrase.toLowerCase())) {
                Seppuku.INSTANCE.logChat("AutoIgnore already contains that phrase");
            } else {
                Seppuku.INSTANCE.logChat("Added phrase \"" + phrase + "\"");
                autoIgnoreModule.getBlacklist().add(phrase);
                Seppuku.INSTANCE.getConfigManager().saveAll();
            }
        } else if (equals(removeAlias, split[1])) {
            if (!this.clamp(input, 3)) {
                this.printUsage();
                return;
            }
            final StringBuilder sb = new StringBuilder();

            for (int i = 2; i < split.length; i++) {
                final String s = split[i];
                sb.append(s + (i == split.length - 1 ? "" : " "));
            }

            final String phrase = sb.toString();

            if (autoIgnoreModule.blacklistContains(phrase.toLowerCase())) {
                Seppuku.INSTANCE.logChat("Removed phrase \"" + phrase + "\"");
                autoIgnoreModule.getBlacklist().remove(phrase);
                Seppuku.INSTANCE.getConfigManager().saveAll();
            } else {
                Seppuku.INSTANCE.logChat("AutoIgnore does not contain that phrase");
            }
        } else if (equals(listAlias, split[1])) {
            if (!this.clamp(input, 2, 2)) {
                this.printUsage();
                return;
            }

            final int size = autoIgnoreModule.getBlacklist().size();

            if (size > 0) {
                final TextComponentString msg = new TextComponentString("\2477Phrases [" + size + "]\247f ");

                for (int i = 0; i < size; i++) {
                    final String phrase = autoIgnoreModule.getBlacklist().get(i);
                    if (phrase != null) {
                        msg.appendSibling(new TextComponentString("\247a" + phrase + "\2477" + ((i == size - 1) ? "" : ", ")));
                    }
                }

                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(msg);
            } else {
                Seppuku.INSTANCE.logChat("You don't have any phrases");
            }
        } else if (equals(clearAlias, split[1])) {
            if (!this.clamp(input, 2, 2)) {
                this.printUsage();
                return;
            }

            final int size = autoIgnoreModule.getBlacklist().size();

            if (size > 0) {
                Seppuku.INSTANCE.logChat("Removed \247c" + size + "\247f phrase" + (size > 1 ? "s" : ""));
                autoIgnoreModule.getBlacklist().clear();
                Seppuku.INSTANCE.getConfigManager().saveAll();
            } else {
                Seppuku.INSTANCE.logChat("You don't have any phrases");
            }

        } else {
            Seppuku.INSTANCE.errorChat("Unknown input " + "\247f\"" + input + "\"");
            this.printUsage();
        }
    }
}
