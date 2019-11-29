package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.old.OptionalValue;

/**
 * Author Seth
 * 4/16/2019 @ 9:01 PM.
 */
public final class ToggleCommand extends Command {

    public ToggleCommand() {
        super("Toggle", new String[] {"T", "Tog"}, "Allows you to toggle modules or between two mode options", "Toggle <Module>\nToggle <Module> <Mode> <Option> <Option>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 5)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final Module mod = Seppuku.INSTANCE.getModuleManager().find(split[1]);

        if(mod != null) {
            if(mod.getType() == Module.ModuleType.HIDDEN) {
                Seppuku.INSTANCE.errorChat("Cannot toggle " + "\247f\"" + mod.getDisplayName() + "\"");
            }else{

                if(split.length > 2) {
                    if (!this.clamp(input, 5, 5)) {
                        this.printUsage();
                        return;
                    }

                    final OptionalValue val = (OptionalValue)mod.find(split[2]);
                    if(val != null) {
                        //TODO support numbers too
                        final int firstOption = val.getOption(split[3]);

                        if(firstOption != -1) {
                            final int secondOption = val.getOption(split[4]);
                            if(secondOption != -1) {
                                if(firstOption != secondOption) {
                                    if(val.getInt() == firstOption) {
                                        val.setInt(secondOption);
                                        Seppuku.INSTANCE.logChat(mod.getDisplayName() + " \247c" + val.getDisplayName() + "\247f set to " + val.getOptions()[secondOption]);
                                        Seppuku.INSTANCE.getConfigManager().saveAll();
                                    }else{
                                        val.setInt(firstOption);
                                        Seppuku.INSTANCE.logChat(mod.getDisplayName() + " \247c" + val.getDisplayName() + "\247f set to " + val.getOptions()[firstOption]);
                                        Seppuku.INSTANCE.getConfigManager().saveAll();
                                    }
                                }else{
                                    Seppuku.INSTANCE.errorChat("Both options are the same");
                                }
                            }else{
                                Seppuku.INSTANCE.errorChat("Invalid input " + "\"" + split[4] + "\" expected a name");
                            }
                        }else{
                            Seppuku.INSTANCE.errorChat("Invalid input " + "\"" + split[3] + "\" expected a name");
                        }
                    }else{
                        Seppuku.INSTANCE.errorChat("Unknown Mode \"" + split[2] + "\"");
                        //TODO similar values?
                    }
                }else{
                    mod.toggle();
                    Seppuku.INSTANCE.logChat("Toggled " + (mod.isEnabled() ? "\247a" : "\247c") + mod.getDisplayName());
                }
            }
            Seppuku.INSTANCE.getConfigManager().saveAll();
        }else{
            Seppuku.INSTANCE.errorChat("Unknown module " + "\247f\"" + split[1] + "\"");
            final Module similar = Seppuku.INSTANCE.getModuleManager().findSimilar(split[1]);

            if(similar != null) {
                Seppuku.INSTANCE.logChat("Did you mean " + "\247c" + similar.getDisplayName() + "\247f?");
            }
        }
    }

}
