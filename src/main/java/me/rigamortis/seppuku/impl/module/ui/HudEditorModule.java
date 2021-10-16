package me.rigamortis.seppuku.impl.module.ui;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

/**
 * Author Seth
 * 7/25/2019 @ 4:16 AM.
 */
public final class HudEditorModule extends Module {

    public final Value<Boolean> blur = new Value<Boolean>("Blur", new String[]{"b"}, "Apply a blur effect to the Hud Editor's background", false);
    public final Value<Boolean> tooltips = new Value<Boolean>("ToolTips", new String[]{"TT", "Tool"}, "Displays tooltips for modules", true);

    private boolean open;

    public HudEditorModule() {
        super("HudEditor", new String[]{"HudEdit", "HEdit", "GUI", "ClickGUI"}, "Displays a menu to modify the hud", "GRAVE", -1, ModuleType.UI);
        this.setHidden(true);
    }

    @Override
    public void onToggle() {
        super.onToggle();
        this.displayHudEditor();
    }

    public void displayHudEditor() {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.world != null) {
            mc.displayGuiScreen(Seppuku.INSTANCE.getHudEditor());

            if (this.blur.getValue()) {
                if (OpenGlHelper.shadersSupported) {
                    mc.entityRenderer.loadShader(new ResourceLocation("minecraft", "shaders/post/blur.json"));
                }
            }

            this.open = true;

            this.setEnabled(false);
        }
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}
