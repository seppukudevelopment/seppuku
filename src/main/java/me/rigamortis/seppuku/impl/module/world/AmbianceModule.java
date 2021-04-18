package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.api.event.render.EventRenderSky;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.texture.Texture;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.awt.*;

public class AmbianceModule extends Module {

    public enum SkyMode {
        NORMAL, COLOR, SEPPUKU, RAINBOW, END, NONE
    }

    public final Value<SkyMode> skyMode = new Value<SkyMode>("SkyMode", new String[]{"Sky", "Sm", "SkieMode", "Skie", "Skies"}, "Edit the skybox.", SkyMode.SEPPUKU);
    public final Value<Color> skyColor = new Value<Color>("SkyColor", new String[]{"SkyCol", "Sc", "SkieColor", "SkieCol", "Color", "C"}, "Edit the skybox color (COLOR mode only).",  new Color(0, 127, 255));
    public final Value<Integer> skyGamma = new Value<Integer>("SkyGamma", new String[]{"SkyGam", "SkyG", "Sg", "Gamma", "G"}, "Edit the skybox gamma.", 128, 1, 255, 1);
    public final Value<Integer> skyGammaEnd = new Value<Integer>("SkyGammaEnd", new String[]{"SkyGamEnd", "SkyGe", "Sge", "GammaEnd", "GamEnd", "Ge"}, "Edit the skybox gamma (END mode only).", 40, 1, 255, 1);

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Texture seppukuSkyTexture;
    private final Texture rainbowSkyTexture;
    private static final ResourceLocation END_SKY_TEXTURES = new ResourceLocation("textures/environment/end_sky.png");

    public AmbianceModule() {
        super("Ambiance", new String[]{"Ambience", "CustomSky", "CustomSound", "CustomSounds"}, "Edit ambient parts of the game. (Sky, sounds, etc...)", "NONE", -1, ModuleType.WORLD);
        this.seppukuSkyTexture = new Texture("seppuku_sky.jpg");
        this.rainbowSkyTexture = new Texture("spectrum.jpg");
    }

    @Listener
    public void onRenderSky(EventRenderSky event) {
        if (this.skyMode.getValue() != SkyMode.NORMAL) {
            event.setCanceled(true);
            this.renderSky();
        }
    }

    private void renderSky() {
        GlStateManager.disableFog();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.depthMask(false);
        switch (this.skyMode.getValue()) {
            case SEPPUKU:
                this.seppukuSkyTexture.bind();
                break;
            case RAINBOW:
                this.rainbowSkyTexture.bind();
                break;
            case END:
                this.mc.getRenderManager().renderEngine.bindTexture(END_SKY_TEXTURES);
                break;
        }
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        for (int k1 = 0; k1 < 6; ++k1) {
            GlStateManager.pushMatrix();
            if (k1 == 1) {
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            }

            if (k1 == 2) {
                GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            }

            if (k1 == 3) {
                GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
            }

            if (k1 == 4) {
                GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
            }

            if (k1 == 5) {
                GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
            }

            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            switch (this.skyMode.getValue()) {
                case SEPPUKU:
                case RAINBOW:
                    bufferbuilder.pos(-100.0D, -100.0D, -100.0D).tex(0.0D, 0.0D).color(this.skyGamma.getValue(), this.skyGamma.getValue(), this.skyGamma.getValue(), 255).endVertex();
                    bufferbuilder.pos(-100.0D, -100.0D, 100.0D).tex(0.0D, 2.0D).color(this.skyGamma.getValue(), this.skyGamma.getValue(), this.skyGamma.getValue(), 255).endVertex();
                    bufferbuilder.pos(100.0D, -100.0D, 100.0D).tex(2.0D, 2.0D).color(this.skyGamma.getValue(), this.skyGamma.getValue(), this.skyGamma.getValue(), 255).endVertex();
                    bufferbuilder.pos(100.0D, -100.0D, -100.0D).tex(2.0D, 0.0D).color(this.skyGamma.getValue(), this.skyGamma.getValue(), this.skyGamma.getValue(), 255).endVertex();
                    break;
                case COLOR:
                    bufferbuilder.pos(-100.0D, -100.0D, -100.0D).tex(0.0D, 0.0D).color(this.skyColor.getValue().getRed(), this.skyColor.getValue().getGreen(), this.skyColor.getValue().getBlue(), 255).endVertex();
                    bufferbuilder.pos(-100.0D, -100.0D, 100.0D).tex(0.0D, 16.0D).color(this.skyColor.getValue().getRed(), this.skyColor.getValue().getGreen(), this.skyColor.getValue().getBlue(), 255).endVertex();
                    bufferbuilder.pos(100.0D, -100.0D, 100.0D).tex(16.0D, 16.0D).color(this.skyColor.getValue().getRed(), this.skyColor.getValue().getGreen(), this.skyColor.getValue().getBlue(), 255).endVertex();
                    bufferbuilder.pos(100.0D, -100.0D, -100.0D).tex(16.0D, 0.0D).color(this.skyColor.getValue().getRed(), this.skyColor.getValue().getGreen(), this.skyColor.getValue().getBlue(), 255).endVertex();
                    break;
                case END:
                    bufferbuilder.pos(-100.0D, -100.0D, -100.0D).tex(0.0D, 0.0D).color(this.skyGammaEnd.getValue(), this.skyGammaEnd.getValue(), this.skyGammaEnd.getValue(), 255).endVertex();
                    bufferbuilder.pos(-100.0D, -100.0D, 100.0D).tex(0.0D, 16.0D).color(this.skyGammaEnd.getValue(), this.skyGammaEnd.getValue(), this.skyGammaEnd.getValue(), 255).endVertex();
                    bufferbuilder.pos(100.0D, -100.0D, 100.0D).tex(16.0D, 16.0D).color(this.skyGammaEnd.getValue(), this.skyGammaEnd.getValue(), this.skyGammaEnd.getValue(), 255).endVertex();
                    bufferbuilder.pos(100.0D, -100.0D, -100.0D).tex(16.0D, 0.0D).color(this.skyGammaEnd.getValue(), this.skyGammaEnd.getValue(), this.skyGammaEnd.getValue(), 255).endVertex();
                    break;
                case NONE:
                    bufferbuilder.pos(-100.0D, -100.0D, -100.0D).tex(0.0D, 0.0D).color(10, 10, 10, 255).endVertex();
                    bufferbuilder.pos(-100.0D, -100.0D, 100.0D).tex(0.0D, 16.0D).color(10, 10, 10, 255).endVertex();
                    bufferbuilder.pos(100.0D, -100.0D, 100.0D).tex(16.0D, 16.0D).color(10, 10, 10, 255).endVertex();
                    bufferbuilder.pos(100.0D, -100.0D, -100.0D).tex(16.0D, 0.0D).color(10, 10, 10, 255).endVertex();
                    break;
            }
            tessellator.draw();
            GlStateManager.popMatrix();
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
    }
}
