package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;

public final class ConfigCommand extends Command {
    public ConfigCommand() {
        super("Config", new String[]{"Conf"}, "Change the active config", "Config <config>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");
        final String config = split[1];
        Seppuku.INSTANCE.getConfigManager().switchToConfig(config);
        Seppuku.INSTANCE.logChat("\247c" + "Switched to config " + config);
    }
}
