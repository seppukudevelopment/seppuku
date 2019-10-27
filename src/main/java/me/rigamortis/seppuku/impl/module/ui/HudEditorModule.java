package me.rigamortis.seppuku.impl.module.ui;

import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import net.minecraft.client.Minecraft;

/**
 * Author Seth
 * 7/25/2019 @ 4:16 AM.
 */
public final class HudEditorModule extends Module {

    private boolean open;

    public HudEditorModule() {
        super("HudEditor", new String[] {"HudEdit", "HEdit"}, "Displays a menu to modify the hud", "NONE", -1, ModuleType.UI);
        this.setHidden(true);
    }

    @Override
    public void onToggle() {
        super.onToggle();
        Minecraft.getMinecraft().displayGuiScreen(new GuiHudEditor());
        this.open = true;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}
