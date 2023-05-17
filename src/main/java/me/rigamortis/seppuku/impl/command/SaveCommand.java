package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;

/**
 * @author noil
 */
public final class SaveCommand extends Command {

    public SaveCommand() {
        super("Save", new String[]{"SaveAll"}, "Saves all client settings to disk.", "Save");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 1, 1)) {
            this.printUsage();
            return;
        }

        Seppuku.INSTANCE.getConfigManager().saveAll();
        Seppuku.INSTANCE.logChat("Saved current config.");
    }
}

