package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.module.Module;

/**
 * Author Seth
 * 4/16/2019 @ 9:01 PM.
 */
public final class ToggleCommand extends Command {

    public ToggleCommand() {
        super("Toggle", new String[]{"T", "Tog"}, "Allows you to toggle modules or between two mode options", "Toggle <Module>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 5)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final Module mod = Seppuku.INSTANCE.getModuleManager().find(split[1]);

        if (mod != null) {
            if (mod.getType() == Module.ModuleType.HIDDEN) {
                Seppuku.INSTANCE.errorChat("Cannot toggle " + "\247f\"" + mod.getDisplayName() + "\"");
            } else {
                mod.toggle();
                Seppuku.INSTANCE.logChat("Toggled " + (mod.isEnabled() ? "\247a" : "\247c") + mod.getDisplayName());
            }
            Seppuku.INSTANCE.getConfigManager().saveAll();
        } else {
            Seppuku.INSTANCE.errorChat("Unknown module " + "\247f\"" + split[1] + "\"");
            final Module similar = Seppuku.INSTANCE.getModuleManager().findSimilar(split[1]);

            if (similar != null) {
                Seppuku.INSTANCE.logChat("Did you mean " + "\247c" + similar.getDisplayName() + "\247f?");
            }
        }
    }

}
