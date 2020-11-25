package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.module.Module;

/**
 * Author Ice
 * 5/06/2020 @ 18:14 PM.
 */
public final class RenameModuleCommand extends Command {

    public RenameModuleCommand() {
        super("RenameModule", new String[]{"rm", "renamemod", "renamemodule"}, "Rename modules.", "renamemodule <module> <name>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 3, 3)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final String originalModuleName = split[1];
        final String newModuleName = split[2];

        if (Seppuku.INSTANCE.getModuleManager().find(originalModuleName) != null) {
            Module mod = Seppuku.INSTANCE.getModuleManager().find(originalModuleName);

            mod.setDisplayName(newModuleName);

            Seppuku.INSTANCE.getConfigManager().saveAll();
            Seppuku.INSTANCE.logChat("Set " + originalModuleName + " custom alias to " + newModuleName);
        } else {
            Seppuku.INSTANCE.logChat(originalModuleName + " does not exist!");
        }


    }
}

