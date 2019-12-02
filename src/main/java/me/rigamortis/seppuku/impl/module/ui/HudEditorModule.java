package me.rigamortis.seppuku.impl.module.ui;

import me.rigamortis.seppuku.api.event.minecraft.EventDisplayGui;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 7/25/2019 @ 4:16 AM.
 */
public final class HudEditorModule extends Module {

    public final Value<Boolean> blur = new Value("Blur", new String[]{"b"}, "Apply a blur effect to the Hud Editor's background.", true);
    public final Value<Boolean> tooltips = new Value("ToolTips", new String[]{"TT", "Tool"}, "Displays tooltips for modules.", true);

    private boolean open;

    public HudEditorModule() {
        super("HudEditor", new String[]{"HudEdit", "HEdit"}, "Displays a menu to modify the hud", "GRAVE", -1, ModuleType.UI);
        this.setHidden(true);
    }

    @Override
    public void onToggle() {
        super.onToggle();
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.world != null) {
            mc.displayGuiScreen(new GuiHudEditor());

            if(this.blur.getValue()) {
                if (OpenGlHelper.shadersSupported) {
                    mc.entityRenderer.loadShader(new ResourceLocation("minecraft", "shaders/post/blur.json"));
                }
            }
            this.open = true;
        }
    }

    @Listener
    public void displayGui(EventDisplayGui event) {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiHudEditor) {
            if (event.getScreen() == null && this.open) {
                event.setCanceled(true);
            }
        }
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}
