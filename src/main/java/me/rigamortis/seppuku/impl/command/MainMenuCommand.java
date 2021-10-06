package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;

/**
 * @author noil
 */
public final class MainMenuCommand extends Command {

    public MainMenuCommand() {
        super("MainMenu", new String[]{"ToggleMainMenu", "ToggleMM", "CustomMainMenu", "CustomMM"}, "Enables or disables the Seppuku main menu.", "MainMenu");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 1, 1)) {
            this.printUsage();
            return;
        }

        Seppuku.INSTANCE.getConfigManager().setCustomMainMenuHidden(!Seppuku.INSTANCE.getConfigManager().isCustomMainMenuHidden());
        Seppuku.INSTANCE.logChat("Custom main menu " + (Seppuku.INSTANCE.getConfigManager().isCustomMainMenuHidden() ? "hidden." : "restored."));
    }
}
