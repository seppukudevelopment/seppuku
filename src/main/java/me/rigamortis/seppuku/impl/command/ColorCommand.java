package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.StringUtil;

/**
 * Author Seth
 * 4/16/2019 @ 10:17 PM.
 */
public final class ColorCommand extends Command {

    public ColorCommand() {
        super("Color", new String[]{"Col", "Colour"}, "Allows you to change arraylist colors", "Color <Module> <Hex>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 3, 3)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final Module mod = Seppuku.INSTANCE.getModuleManager().find(split[1]);

        if (mod != null) {
            if (mod.getType() == Module.ModuleType.HIDDEN) {
                Seppuku.INSTANCE.errorChat("Cannot change color of " + "\247f\"" + mod.getDisplayName() + "\"");
            } else {
                if (StringUtil.isLong(split[2], 16)) {
                    Seppuku.INSTANCE.logChat("\247c" + mod.getDisplayName() + "\247f color has been set to " + split[2].toUpperCase());
                    mod.setColor((int) Long.parseLong(split[2], 16));
                    Seppuku.INSTANCE.getConfigManager().saveAll();
                } else {
                    Seppuku.INSTANCE.errorChat("Invalid input " + "\"" + split[2] + "\" expected a hex value");
                }
            }
        } else {
            Seppuku.INSTANCE.errorChat("Unknown module " + "\247f\"" + split[1] + "\"");
            final Module similar = Seppuku.INSTANCE.getModuleManager().findSimilar(split[1]);
            if (similar != null) {
                Seppuku.INSTANCE.logChat("Did you mean " + "\247c" + similar.getDisplayName() + "\247f?");
            }
        }
    }
}
