package me.rigamortis.seppuku.impl.gui.menu;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.minecraft.EventDisplayGui;
import me.rigamortis.seppuku.api.texture.Texture;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.impl.fml.SeppukuMod;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
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

    private Texture seppukuLogo;

    public GuiSeppukuMainMenu() {
        Seppuku.INSTANCE.getEventManager().addEventListener(this);
    }

    @Override
    public void initGui() {
        super.initGui();

        if (this.seppukuLogo == null)
            this.seppukuLogo = new Texture("seppuku-logo.png");

        final GuiSeppukuMainMenu menu = this;

        final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

        float height = (res.getScaledHeight() / 4.0f) + mc.fontRenderer.FONT_HEIGHT / 2.0f + 18;

        this.singlePlayer = new MainMenuButton(res.getScaledWidth() / 2.0f - 70, height, "Singleplayer") {
            @Override
            public void action() {
                mc.displayGuiScreen(new GuiWorldSelection(menu));
            }
        };

        height += 20;

        this.multiPlayer = new MainMenuButton(res.getScaledWidth() / 2.0f - 70, height, "Multiplayer") {
            @Override
            public void action() {
                mc.displayGuiScreen(new GuiMultiplayer(menu));
            }
        };

        height += 20;

        this.options = new MainMenuButton(res.getScaledWidth() / 2.0f - 70, height, "Options") {
            @Override
            public void action() {
                mc.displayGuiScreen(new GuiOptions(menu, mc.gameSettings));
            }
        };

        height += 20;

        this.mods = new MainMenuButton(res.getScaledWidth() / 2.0f - 70, height, "Mods") {
            @Override
            public void action() {
                mc.displayGuiScreen(new GuiModList(menu));
            }
        };

        height += 20;

        this.donate = new MainMenuButton(res.getScaledWidth() / 2.0f - 70, height, 69, 18, ChatFormatting.GRAY + "Donate!") {
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

        this.hudEditor = new MainMenuButton(res.getScaledWidth() / 2.0f + 1, height, 69, 18, ChatFormatting.GRAY + "Hud Editor") {
            @Override
            public void action() {
                mc.displayGuiScreen(new GuiHudEditor());
            }
        };

        height += 20;

        /*this.alts = new MainMenuButton(res.getScaledWidth() / 2.0f - 70, height, "Alts") {
            @Override
            public void action() {
                //TODO
            }
        };

        height += 20;*/

        this.quit = new MainMenuButton(res.getScaledWidth() / 2.0f - 70, height, "Quit") {
            @Override
            public void action() {
                mc.shutdown();
            }
        };

    }

    @Listener
    public void displayScreen(EventDisplayGui event) {
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

    public void unload() {
        Seppuku.INSTANCE.getEventManager().removeEventListener(this);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawDefaultBackground();
        final ScaledResolution res = new ScaledResolution(mc);

        // begin gl states
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        // draw logo
        this.seppukuLogo.bind();
        this.seppukuLogo.render((res.getScaledWidth() / 2.0f) - 120, (res.getScaledHeight() / 8.0f), 240, 38);
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
        //this.alts.render(mouseX, mouseY, partialTicks);
        this.quit.render(mouseX, mouseY, partialTicks);

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
            //this.alts.mouseClicked(mouseX, mouseY, mouseButton);
            this.quit.mouseClicked(mouseX, mouseY, mouseButton);
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
        //this.alts.mouseRelease(mouseX, mouseY, state);
        this.quit.mouseRelease(mouseX, mouseY, state);
    }

    private void drawSplashText() {
        final Minecraft mc = Minecraft.getMinecraft();
        final ScaledResolution res = new ScaledResolution(mc);

        final String spash = "Welcome, " + mc.getSession().getUsername();
        this.drawString(this.fontRenderer, spash, 2, res.getScaledHeight() - mc.fontRenderer.FONT_HEIGHT, -1);
    }

}
