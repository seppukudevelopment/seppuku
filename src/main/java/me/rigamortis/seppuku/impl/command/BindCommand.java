package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.module.Module;
import org.lwjgl.input.Keyboard;

/**
 * Author Seth
 * 4/16/2019 @ 10:22 PM.
 */
public final class BindCommand extends Command {

    private String[] clearAlias = new String[]{"Clear", "C"};

    public BindCommand() {
        super("Bind", new String[] {"B"}, "Allows you to change keybinds for modules", "Bind <Module> <Key>\nBind Clear");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 3)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        if (equals(clearAlias, split[1])) {
            if (!this.clamp(input, 2, 2)) {
                this.printUsage();
                return;
            }

            int count = 0;

            for(Module mod : Seppuku.INSTANCE.getModuleManager().getModuleList()) {
                if(mod.getType() != Module.ModuleType.HIDDEN && mod.getKey() != null && !mod.getKey().equals("NONE")) {
                    count++;
                    mod.setKey("NONE");
                }
            }

            if(count > 0) {
                Seppuku.INSTANCE.logChat("Removed " + count + " Bind" + (count > 1 ? "s" : ""));
            }else{
                Seppuku.INSTANCE.logChat("You have no binds");
            }
        }else{
            if (!this.clamp(input, 3, 3)) {
                this.printUsage();
                return;
            }

            final Module mod = Seppuku.INSTANCE.getModuleManager().find(split[1]);

            if(mod != null) {
                if (mod.getType() == Module.ModuleType.HIDDEN) {
                    Seppuku.INSTANCE.errorChat("Cannot change bind of " + "\247f\"" + mod.getDisplayName() + "\"");
                }else{
                    if(split[2].equalsIgnoreCase(mod.getKey())) {
                        Seppuku.INSTANCE.logChat("\247c" + mod.getDisplayName() + "'s\247f key is already " + split[2].toUpperCase());
                    }else{
                        if(split[2].equalsIgnoreCase("NONE")) {
                            Seppuku.INSTANCE.logChat("Bound \247c" + mod.getDisplayName() + "\247f to " + split[2].toUpperCase());
                            mod.setKey(split[2].toUpperCase());
                            Seppuku.INSTANCE.getConfigManager().saveAll();
                        }else if(Keyboard.getKeyIndex(split[2].toUpperCase()) != Keyboard.KEY_NONE) {
                            Seppuku.INSTANCE.logChat("Bound \247c" + mod.getDisplayName() + "\247f to " + split[2].toUpperCase());
                            mod.setKey(split[2].toUpperCase());
                            Seppuku.INSTANCE.getConfigManager().saveAll();
                        }else{
                            Seppuku.INSTANCE.logChat("\247c" + split[2] + "\247f is not a valid key");
                        }
                    }
                }
            }else{
                Seppuku.INSTANCE.errorChat("Unknown module " + "\247f\"" + split[1] + "\"");
                final Module similar = Seppuku.INSTANCE.getModuleManager().findSimilar(split[1]);
                if(similar != null) {
                    Seppuku.INSTANCE.logChat("Did you mean " + "\247c" + similar.getDisplayName() + "\247f?");
                }
            }
        }
    }
}
