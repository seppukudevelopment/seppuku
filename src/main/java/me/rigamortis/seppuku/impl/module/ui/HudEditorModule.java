package me.rigamortis.seppuku.impl.module.ui;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.minecraft.EventDisplayGui;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
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

    public HudEditorModule() {
        super("HudEditor", new String[]{"HudEdit", "HEdit"}, "Displays a menu to modify the hud", "GRAVE", -1, ModuleType.UI);
        this.setHidden(true);
    }
    
    @Override
    public void onEnable() {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.world != null) {
            mc.displayGuiScreen(new GuiHudEditor());
            if (this.blur.getValue() && OpenGlHelper.shadersSupported) {
                mc.entityRenderer.loadShader(new ResourceLocation("minecraft", "shaders/post/blur.json"));
            }
        }
    }
    
    @Override
    public void onDisable() {}
}
