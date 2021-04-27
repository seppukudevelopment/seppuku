package me.rigamortis.seppuku.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.client.EventSaveConfig;
import me.rigamortis.seppuku.api.event.world.EventLoadWorld;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.ColorUtil;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import me.rigamortis.seppuku.impl.gui.hud.anchor.AnchorPoint;
import me.rigamortis.seppuku.impl.module.hidden.ArrayListModule;
import me.rigamortis.seppuku.impl.module.render.HudModule;
import net.minecraft.client.gui.ScaledResolution;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static me.rigamortis.seppuku.impl.module.hidden.ArrayListModule.Mode.ALPHABET;
import static me.rigamortis.seppuku.impl.module.hidden.ArrayListModule.Mode.LENGTH;

/**
 * @author seth  * 7/25/2019 @ 7:24 AM
 * @author noil
 */
public final class EnabledModsComponent extends DraggableHudComponent {

    private ArrayListModule.Mode SORTING_MODE = LENGTH;
    private boolean SHOW_METADATA = true;
    private boolean LOWERCASE = false;

    private boolean RAINBOW = false;
    private float RAINBOW_HUE_DIFFERENCE = 2.5f;
    private float RAINBOW_HUE_SPEED = 50.f;
    private float RAINBOW_SATURATION = 50.f;
    private float RAINBOW_BRIGHTNESS = 50.f;

    public EnabledModsComponent(AnchorPoint anchorPoint) {
        super("EnabledMods");
        this.setAnchorPoint(anchorPoint); // by default anchors in the top right corner of the hud
        this.setVisible(true);

        Seppuku.INSTANCE.getEventManager().addEventListener(this); // subscribe to the event manager
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final List<Module> mods = new ArrayList<>();
        final ScaledResolution res = new ScaledResolution(mc);
        boolean isInHudEditor = mc.currentScreen instanceof GuiHudEditor;

        float xOffset = 0;
        float yOffset = 0;
        float maxWidth = 0;
        int hueDifference = 0;

        for (Module mod : Seppuku.INSTANCE.getModuleManager().getModuleList()) {
            if (mod != null && mod.getType() != Module.ModuleType.HIDDEN && mod.isEnabled() && !mod.isHidden()) {
                mods.add(mod);
            }
        }

        if (mods.size() > 0) {
            if (SORTING_MODE.equals(LENGTH)) {
                final Comparator<Module> lengthComparator = (first, second) -> {
                    String firstName = first.getDisplayName() + (SHOW_METADATA ? (first.getMetaData() != null ? " " + ChatFormatting.GRAY + "[" + ChatFormatting.WHITE + first.getMetaData().toLowerCase() + ChatFormatting.GRAY + "]" : "") : "");
                    String secondName = second.getDisplayName() + (SHOW_METADATA ? (second.getMetaData() != null ? " " + ChatFormatting.GRAY + "[" + ChatFormatting.WHITE + second.getMetaData().toLowerCase() + ChatFormatting.GRAY + "]" : "") : "");
                    if (LOWERCASE) {
                        firstName = firstName.toLowerCase();
                        secondName = secondName.toLowerCase();
                    }
                    final float dif = mc.fontRenderer.getStringWidth(secondName) - mc.fontRenderer.getStringWidth(firstName);
                    return dif != 0 ? (int) dif : secondName.compareTo(firstName);
                };
                mods.sort(lengthComparator);
            } else if (SORTING_MODE.equals(ALPHABET)) {
                final Comparator<Module> alphabeticalComparator = (first, second) -> {
                    String firstName = first.getDisplayName() + (SHOW_METADATA ? (first.getMetaData() != null ? " " + ChatFormatting.GRAY + "[" + ChatFormatting.WHITE + first.getMetaData().toLowerCase() + ChatFormatting.GRAY + "]" : "") : "");
                    String secondName = second.getDisplayName() + (SHOW_METADATA ? (second.getMetaData() != null ? " " + ChatFormatting.GRAY + "[" + ChatFormatting.WHITE + second.getMetaData().toLowerCase() + ChatFormatting.GRAY + "]" : "") : "");
                    if (LOWERCASE) {
                        firstName = firstName.toLowerCase();
                        secondName = secondName.toLowerCase();
                    }
                    return firstName.compareToIgnoreCase(secondName);
                };
                mods.sort(alphabeticalComparator);
            }

            for (Module mod : mods) {
                if (mod != null && mod.getType() != Module.ModuleType.HIDDEN && mod.isEnabled() && !mod.isHidden()) {
                    String name = mod.getDisplayName() + (SHOW_METADATA ? (mod.getMetaData() != null ? " " + ChatFormatting.GRAY + "[" + ChatFormatting.WHITE + mod.getMetaData().toLowerCase() + ChatFormatting.GRAY + "]" : "") : "");
                    if (LOWERCASE)
                        name = name.toLowerCase();

                    final float width = mc.fontRenderer.getStringWidth(name);

                    int color;
                    if (RAINBOW && mc.player != null) {
                        Color rainbow = new Color(Color.HSBtoRGB((float) (mc.player.ticksExisted / (100.0D - RAINBOW_HUE_SPEED) + Math.sin(hueDifference / (100.0D - RAINBOW_HUE_SPEED * Math.PI / 2.0D))) % 1.0F, RAINBOW_SATURATION, RAINBOW_BRIGHTNESS));
                        color = ColorUtil.changeAlpha((new Color(rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue())).getRGB(), 0xFF);
                    } else {
                        color = ColorUtil.changeAlpha(mod.getColor(), 0xFF);
                    }

                    if (width >= maxWidth) {
                        maxWidth = width;
                    }

                    if (this.getAnchorPoint() != null) {
                        if (this.getAnchorPoint().getPoint() != null) {
                            switch (this.getAnchorPoint().getPoint()) {
                                case TOP_CENTER:
                                case BOTTOM_CENTER:
                                    xOffset = (this.getW() - mc.fontRenderer.getStringWidth(name)) / 2;
                                    break;
                                case TOP_LEFT:
                                case BOTTOM_LEFT:
                                    xOffset = 0;
                                    break;
                                case TOP_RIGHT:
                                case BOTTOM_RIGHT:
                                    xOffset = this.getW() - mc.fontRenderer.getStringWidth(name);
                                    break;
                            }

                            switch (this.getAnchorPoint().getPoint()) {
                                case TOP_CENTER:
                                case TOP_LEFT:
                                case TOP_RIGHT:
                                    mc.fontRenderer.drawStringWithShadow(name, this.getX() + xOffset, this.getY() + yOffset, color);
                                    yOffset += (mc.fontRenderer.FONT_HEIGHT + 1);
                                    break;
                                case BOTTOM_CENTER:
                                case BOTTOM_LEFT:
                                case BOTTOM_RIGHT:
                                    mc.fontRenderer.drawStringWithShadow(name, this.getX() + xOffset, this.getY() + (this.getH() - mc.fontRenderer.FONT_HEIGHT) + yOffset, color);
                                    yOffset -= (mc.fontRenderer.FONT_HEIGHT + 1);
                                    break;
                            }
                        }
                    } else {
                        mc.fontRenderer.drawStringWithShadow(name, this.getX() + xOffset, this.getY() + yOffset, color);
                        yOffset += (mc.fontRenderer.FONT_HEIGHT + 1);
                    }

                    hueDifference = (int) (hueDifference + RAINBOW_HUE_DIFFERENCE);
                }
            }
        }

        if (isInHudEditor) {
            if (maxWidth == 0) { // no mods
                final String arraylist = "(enabled mods)";
                mc.fontRenderer.drawStringWithShadow(arraylist, this.getX(), this.getY(), 0xFFAAAAAA);
                maxWidth = mc.fontRenderer.getStringWidth(arraylist) + 1 /* right side gap */;
                yOffset = mc.fontRenderer.FONT_HEIGHT + 1 /* right side gap */;
            }
        }

        this.setW(maxWidth);
        this.setH(Math.abs(yOffset));

        if (this.getH() > res.getScaledHeight()) {
            this.setH(res.getScaledHeight() - 4);
        }
    }

    @Listener
    public void onLoadWorld(EventLoadWorld eventLoadWorld) {
        this.updateValues();
    }

    @Listener
    public void onConfigSave(EventSaveConfig eventSaveConfig) {
        this.updateValues();
    }

    private void updateValues() {
        final HudModule hudModule = (HudModule) Seppuku.INSTANCE.getModuleManager().find(HudModule.class);
        if (hudModule != null && hudModule.isEnabled()) {
            this.RAINBOW = hudModule.rainbow.getValue();
            this.RAINBOW_HUE_DIFFERENCE = hudModule.rainbowHueDifference.getValue();
            this.RAINBOW_HUE_SPEED = hudModule.rainbowHueSpeed.getValue();
            this.RAINBOW_SATURATION = hudModule.rainbowSaturation.getValue();
            this.RAINBOW_BRIGHTNESS = hudModule.rainbowBrightness.getValue();
        }

        final ArrayListModule arrayListModule = (ArrayListModule) Seppuku.INSTANCE.getModuleManager().find(ArrayListModule.class);
        if (arrayListModule != null) {
            this.SORTING_MODE = arrayListModule.mode.getValue();
            this.LOWERCASE = arrayListModule.lowercase.getValue();
            this.SHOW_METADATA = arrayListModule.showMetadata.getValue();
        }
    }
}
