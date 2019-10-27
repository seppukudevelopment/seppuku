package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.module.Module;

/**
 * Author Seth
 * 4/16/2019 @ 10:01 PM.
 */
public final class HideCommand extends Command {

    public HideCommand() {
        super("Hide", new String[] {"Hid"}, "Allows you to hide modules from the arraylist", "Hide <Module>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final Module mod = Seppuku.INSTANCE.getModuleManager().find(split[1]);

        if(mod != null) {
            if(mod.getType() == Module.ModuleType.HIDDEN) {
                Seppuku.INSTANCE.errorChat("Cannot hide " + "\247f\"" + mod.getDisplayName() + "\"");
            }else{
                mod.setHidden(!mod.isHidden());
                Seppuku.INSTANCE.getConfigManager().saveAll();

                if(mod.isHidden()) {
                    Seppuku.INSTANCE.logChat("\247c" + mod.getDisplayName() + "\247f is now hidden");
                }else{
                    Seppuku.INSTANCE.logChat("\247c" + mod.getDisplayName() + "\247f is no longer hidden");
                }
            }
            //TODO config
        }else{
            Seppuku.INSTANCE.errorChat("Unknown module " + "\247f\"" + split[1] + "\"");
            final Module similar = Seppuku.INSTANCE.getModuleManager().findSimilar(split[1]);

            if(similar != null) {
                Seppuku.INSTANCE.logChat("Did you mean " + "\247c" + similar.getDisplayName() + "\247f?");
            }
        }
    }
}
