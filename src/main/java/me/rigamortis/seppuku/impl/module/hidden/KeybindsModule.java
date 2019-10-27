package me.rigamortis.seppuku.impl.module.hidden;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.minecraft.EventKeyPress;
import me.rigamortis.seppuku.api.module.Module;
import org.lwjgl.input.Keyboard;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/7/2019 @ 10:25 PM.
 */
public final class KeybindsModule extends Module {

    public KeybindsModule() {
        super("Keybinds", new String[] {"Binds"}, "Allows you to bind modules to keys", "NONE", -1, ModuleType.HIDDEN);
        this.setHidden(true);
        this.toggle();
    }

    @Listener
    public void keyPress(EventKeyPress event) {
        for(Module mod : Seppuku.INSTANCE.getModuleManager().getModuleList()) {
            if(mod != null) {
                if(mod.getType() != ModuleType.HIDDEN && event.getKey() == Keyboard.getKeyIndex(mod.getKey()) && Keyboard.getKeyIndex(mod.getKey()) != Keyboard.KEY_NONE) {
                    mod.toggle();
                    Seppuku.INSTANCE.getConfigManager().saveAll();
                }
            }
        }
    }

}
