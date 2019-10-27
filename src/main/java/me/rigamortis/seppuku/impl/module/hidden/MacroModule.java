package me.rigamortis.seppuku.impl.module.hidden;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.minecraft.EventKeyPress;
import me.rigamortis.seppuku.api.macro.Macro;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 5/7/2019 @ 9:49 PM.
 */
public final class MacroModule extends Module {

    public MacroModule() {
        super("Macros", new String[] {"mac"}, "Allows you to bind macros to keys", "NONE", -1, ModuleType.HIDDEN);
        this.setHidden(true);
        this.toggle();
    }

    @Listener
    public void keyPress(EventKeyPress event) {
        for(Macro macro : Seppuku.INSTANCE.getMacroManager().getMacroList()) {
            if(event.getKey() == Keyboard.getKeyIndex(macro.getKey()) && Keyboard.getKeyIndex(macro.getKey()) != Keyboard.KEY_NONE) {
                final String[] split = macro.getMacro().split(";");

                for(String s : split) {
                    Minecraft.getMinecraft().player.sendChatMessage(s);
                }
            }
        }
    }

}
