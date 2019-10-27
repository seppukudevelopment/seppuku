package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;

/**
 * Author Seth
 * 5/12/2019 @ 11:15 AM.
 */
public final class UnloadCommand extends Command {

    public UnloadCommand() {
        super("Unload", new String[] {"ULoad"}, "Unloads the client", "Unload");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 1, 1)) {
            this.printUsage();
            return;
        }

        Seppuku.INSTANCE.unload();
    }
}
