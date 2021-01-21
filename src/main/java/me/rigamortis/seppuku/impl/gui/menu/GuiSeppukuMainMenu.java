package me.rigamortis.seppuku.impl.gui.menu;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.minecraft.EventDisplayGui;
import me.rigamortis.seppuku.api.gui.hud.particle.ParticleSystem;
import me.rigamortis.seppuku.api.gui.menu.MainMenuButton;
import me.rigamortis.seppuku.api.texture.Texture;
import me.rigamortis.seppuku.impl.fml.SeppukuMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.GuiModList;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.awt.*;
import java.net.URL;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

/**
 * Author Seth
 * 9/4/2019 @ 7:18 PM.
 */
public final class GuiSeppukuMainMenu extends GuiScreen {

    private MainMenuButton singlePlayer;
    private MainMenuButton multiPlayer;
    private MainMenuButton options;
    private MainMenuButton donate;
    private MainMenuButton hudEditor;
    private MainMenuButton alts;
    private MainMenuButton mods;
    private MainMenuButton quit;
    private MainMenuButton disable;
    private MainMenuButton language;

    private Texture seppukuLogo;

    private ScaledResolution scaledResolution;

    private ParticleSystem particleSystem;

    private boolean inactive = false;

    public GuiSeppukuMainMenu() {
        Seppuku.INSTANCE.getEventManager().addEventListener(this);
    }

    @Override
    public void initGui() {
        super.initGui();

        final GuiSeppukuMainMenu menu = this;
        final Minecraft mc = Minecraft.getMinecraft();
        this.scaledResolution = new ScaledResolution(mc);

        if (this.seppukuLogo == null)
            this.seppukuLogo = new Texture("seppuku-logo.png");

        if (this.particleSystem == null)
            this.particleSystem = new ParticleSystem(this.scaledResolution);

        // resize the seppuku hud editor with the size of the main menu
        Seppuku.INSTANCE.getHudEditor().onResize(mc, this.scaledResolution.getScaledWidth(), this.scaledResolution.getScaledHeight());

        float height = (this.scaledResolution.getScaledHeight() / 4.0f) + mc.fontRenderer.FONT_HEIGHT / 2.0f + 18;

        this.singlePlayer = new MainMenuButton(this.scaledResolution.getScaledWidth() / 2.0f - 70, height, "Singleplayer") {
            @Override
            public void action() {
                mc.displayGuiScreen(new GuiWorldSelection(menu));
            }
        };

        height += 20;

        this.multiPlayer = new MainMenuButton(this.scaledResolution.getScaledWidth() / 2.0f - 70, height, "Multiplayer") {
            @Override
            public void action() {
                mc.displayGuiScreen(new GuiMultiplayer(menu));
            }
        };

        height += 20;

        this.options = new MainMenuButton(this.scaledResolution.getScaledWidth() / 2.0f - 70, height, "Options") {
            @Override
            public void action() {
                mc.displayGuiScreen(new GuiOptions(menu, mc.gameSettings));
            }
        };

        height += 20;

        this.mods = new MainMenuButton(this.scaledResolution.getScaledWidth() / 2.0f - 70, height, "Mods") {
            @Override
            public void action() {
                mc.displayGuiScreen(new GuiModList(menu));
            }
        };

        height += 20;

        this.alts = new MainMenuButton(this.scaledResolution.getScaledWidth() / 2.0f - 70, height, "Alts") {
            @Override
            public void action() {
                mc.displayGuiScreen(new GuiAltManager(menu));
            }
        };

        height += 20;

        this.donate = new MainMenuButton(this.scaledResolution.getScaledWidth() / 2.0f - 70, height, 69, 18, ChatFormatting.YELLOW + "Donate") {
            @Override
            public void action() {
                try {
                    final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;

                    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                        try {
                            desktop.browse(new URL("https://seppuku.pw/donate.html").toURI());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        this.hudEditor = new MainMenuButton(this.scaledResolution.getScaledWidth() / 2.0f + 1, height, 69, 18, ChatFormatting.GRAY + "Hud Editor") {
            @Override
            public void action() {
                mc.displayGuiScreen(Seppuku.INSTANCE.getHudEditor());
            }
        };

        height += 20;

        this.quit = new MainMenuButton(this.scaledResolution.getScaledWidth() / 2.0f - 70, height, "Quit") {
            @Override
            public void action() {
                mc.shutdown();
            }
        };

        this.disable = new MainMenuButton(2, 2, 14, 14, "X") {
            @Override
            public void action() {
                inactive = true;
                mc.displayGuiScreen(new GuiMainMenu());
            }
        };

        this.language = new MainMenuButton(2, 18, 14, 14, "L") {
            @Override
            public void action() {
                mc.displayGuiScreen(new GuiLanguage(new GuiSeppukuMainMenu(), mc.gameSettings, mc.getLanguageManager()));
            }
        };
    }

    @Listener
    public void displayScreen(EventDisplayGui event) {
        if (this.inactive)
            return;

        if (event.getScreen() == null && mc.world == null) {
            event.setCanceled(true);
            Minecraft.getMinecraft().displayGuiScreen(this);
        }

        if (Minecraft.getMinecraft().currentScreen instanceof GuiSeppukuMainMenu && event.getScreen() == null) {
            event.setCanceled(true);
        }

        if (event.getScreen() != null) {
            if (event.getScreen() instanceof GuiMainMenu) {
                event.setCanceled(true);
                Minecraft.getMinecraft().displayGuiScreen(this);
            }
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (this.particleSystem != null) {
            this.particleSystem.setScaledResolution(this.scaledResolution);
            this.particleSystem.update();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawDefaultBackground();

        // draw particle system
        if (this.particleSystem != null)
            this.particleSystem.render(mouseX, mouseY);

        // begin gl states
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        // draw logo
        this.seppukuLogo.bind();
        this.seppukuLogo.render((this.scaledResolution.getScaledWidth() / 2.0f) - 120, (this.scaledResolution.getScaledHeight() / 8.0f), 240, 38);
        //RenderUtil.drawLine(res.getScaledWidth() / 2, 0, res.getScaledWidth() / 2, res.getScaledHeight(), 1, 0x75909090);
        //RenderUtil.drawLine(0, res.getScaledHeight() / 2, res.getScaledWidth(), res.getScaledHeight() / 2, 1, 0x75909090);

        /*
        GlStateManager.pushMatrix();
        GlStateManager.translate(res.getScaledWidth() / 2.0f - (mc.fontRenderer.getStringWidth("Seppuku \2477" + SeppukuMod.VERSION) / 2.0f * 4), (res.getScaledHeight() / 4.0f) - (mc.fontRenderer.FONT_HEIGHT * 4), 0);
        GlStateManager.scale(4.0f, 4.0f, 4.0f);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("Seppuku \2477" + SeppukuMod.VERSION, 0, 0, -1);
        //GlStateManager.scale(1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
        */

        // draw text
        this.drawSplashText();

        // draw buttons
        this.singlePlayer.render(mouseX, mouseY, partialTicks);
        this.multiPlayer.render(mouseX, mouseY, partialTicks);
        this.options.render(mouseX, mouseY, partialTicks);
        this.mods.render(mouseX, mouseY, partialTicks);
        this.donate.render(mouseX, mouseY, partialTicks);
        this.hudEditor.render(mouseX, mouseY, partialTicks);
        this.alts.render(mouseX, mouseY, partialTicks);
        this.quit.render(mouseX, mouseY, partialTicks);
        this.disable.render(mouseX, mouseY, partialTicks);
        this.language.render(mouseX, mouseY, partialTicks);

        // end gl states
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        try {
            this.singlePlayer.mouseClicked(mouseX, mouseY, mouseButton);
            this.multiPlayer.mouseClicked(mouseX, mouseY, mouseButton);
            this.options.mouseClicked(mouseX, mouseY, mouseButton);
            this.mods.mouseClicked(mouseX, mouseY, mouseButton);
            this.donate.mouseClicked(mouseX, mouseY, mouseButton);
            this.hudEditor.mouseClicked(mouseX, mouseY, mouseButton);
            this.alts.mouseClicked(mouseX, mouseY, mouseButton);
            this.quit.mouseClicked(mouseX, mouseY, mouseButton);
            this.disable.mouseClicked(mouseX, mouseY, mouseButton);
            this.language.mouseClicked(mouseX, mouseY, mouseButton);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        this.singlePlayer.mouseRelease(mouseX, mouseY, state);
        this.multiPlayer.mouseRelease(mouseX, mouseY, state);
        this.options.mouseRelease(mouseX, mouseY, state);
        this.mods.mouseRelease(mouseX, mouseY, state);
        this.donate.mouseRelease(mouseX, mouseY, state);
        this.hudEditor.mouseRelease(mouseX, mouseY, state);
        this.alts.mouseRelease(mouseX, mouseY, state);
        this.quit.mouseRelease(mouseX, mouseY, state);
        this.disable.mouseRelease(mouseX, mouseY, state);
        this.language.mouseRelease(mouseX, mouseY, state);
    }

    @Override
    public void onResize(Minecraft mcIn, int w, int h) {
        super.onResize(mcIn, w, h);

        this.particleSystem = new ParticleSystem(new ScaledResolution(mcIn));

        // resize the seppuku hud editor with the size of the main menu
        Seppuku.INSTANCE.getHudEditor().onResize(mcIn, w, h);
    }

    private void drawSplashText() {
        final Minecraft mc = Minecraft.getMinecraft();
        final ScaledResolution res = new ScaledResolution(mc);

        final String spash = "Welcome, " + mc.getSession().getUsername();
        this.drawString(this.fontRenderer, spash, 1, res.getScaledHeight() - mc.fontRenderer.FONT_HEIGHT, -1);

        final String version = ChatFormatting.GRAY + "Version " + SeppukuMod.VERSION + " for " + Minecraft.getMinecraft().getVersion();
        this.drawString(this.fontRenderer, version, res.getScaledWidth() - mc.fontRenderer.getStringWidth(version) - 1, res.getScaledHeight() - mc.fontRenderer.FONT_HEIGHT, -1);
    }

    public void unload() {
        Seppuku.INSTANCE.getEventManager().removeEventListener(this);
    }
}
