package me.rigamortis.seppuku.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import me.rigamortis.seppuku.impl.gui.hud.anchor.AnchorPoint;
import me.rigamortis.seppuku.impl.module.hidden.ArrayListModule;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static me.rigamortis.seppuku.impl.module.hidden.ArrayListModule.Mode.*;

/**
 * @author seth  * 7/25/2019 @ 7:24 AM
 * @author noil
 */
public final class EnabledModsComponent extends DraggableHudComponent {

    public EnabledModsComponent(AnchorPoint anchorPoint) {
        super("EnabledMods");
        this.setAnchorPoint(anchorPoint); // by default anchors in the top right corner of the hud
        this.setVisible(true);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final List<Module> mods = new ArrayList<>();
        final Minecraft mc = Minecraft.getMinecraft();
        boolean isInHudEditor = mc.currentScreen instanceof GuiHudEditor;

        for (Module mod : Seppuku.INSTANCE.getModuleManager().getModuleList()) {
            if (mod != null && mod.getType() != Module.ModuleType.HIDDEN && mod.isEnabled() && !mod.isHidden()) {
                mods.add(mod);
            }
        }

        Object sorting_mode = Seppuku.INSTANCE.getModuleManager().find(ArrayListModule.class).find("Sorting").getValue();
        if (sorting_mode.equals(LENGTH)) {
            final Comparator<Module> lengthComparator = (first, second) -> {
                final String firstName = first.getDisplayName() + (first.getMetaData() != null ? " " + ChatFormatting.GRAY + "[" + ChatFormatting.WHITE + first.getMetaData().toLowerCase() + ChatFormatting.GRAY + "]" : "");
                final String secondName = second.getDisplayName() + (second.getMetaData() != null ? " " + ChatFormatting.GRAY + "[" + ChatFormatting.WHITE + second.getMetaData().toLowerCase() + ChatFormatting.GRAY + "]" : "");
                final float dif = mc.fontRenderer.getStringWidth(secondName) - mc.fontRenderer.getStringWidth(firstName);
                return dif != 0 ? (int) dif : secondName.compareTo(firstName);
            };
            mods.sort(lengthComparator);
        } else if (sorting_mode.equals(ALPHABET)) {
            final Comparator<Module> alphabeticalComparator = (first, second) -> {
                final String firstName = first.getDisplayName() + (first.getMetaData() != null ? " " + ChatFormatting.GRAY + "[" + ChatFormatting.WHITE + first.getMetaData().toLowerCase() + ChatFormatting.GRAY + "]" : "");
                final String secondName = second.getDisplayName() + (second.getMetaData() != null ? " " + ChatFormatting.GRAY + "[" + ChatFormatting.WHITE + second.getMetaData().toLowerCase() + ChatFormatting.GRAY + "]" : "");
                return firstName.compareToIgnoreCase(secondName);
            };
            mods.sort(alphabeticalComparator);
        } else if (sorting_mode.equals(UNSORTED)) {

        }

        float xOffset = 0;
        float yOffset = 0;
        float maxWidth = 0;

        for (Module mod : mods) {
            if (mod != null && mod.getType() != Module.ModuleType.HIDDEN && mod.isEnabled() && !mod.isHidden()) {

                final String name = mod.getDisplayName() + (mod.getMetaData() != null ? " " + ChatFormatting.GRAY + "[" + ChatFormatting.WHITE + mod.getMetaData().toLowerCase() + ChatFormatting.GRAY + "]" : "");

                final float width = mc.fontRenderer.getStringWidth(name);

                if (width >= maxWidth) {
                    maxWidth = width;
                }

                if (this.getAnchorPoint() != null) {
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
                }

                if (this.getAnchorPoint() != null) {
                    switch (this.getAnchorPoint().getPoint()) {
                        case TOP_CENTER:
                        case TOP_LEFT:
                        case TOP_RIGHT:
                            mc.fontRenderer.drawStringWithShadow(name, this.getX() + xOffset, this.getY() + yOffset, mod.getColor());
                            yOffset += (mc.fontRenderer.FONT_HEIGHT + 1);
                            break;
                        case BOTTOM_CENTER:
                        case BOTTOM_LEFT:
                        case BOTTOM_RIGHT:
                            mc.fontRenderer.drawStringWithShadow(name, this.getX() + xOffset, this.getY() + (this.getH() - mc.fontRenderer.FONT_HEIGHT) + yOffset, mod.getColor());
                            yOffset -= (mc.fontRenderer.FONT_HEIGHT + 1);
                            break;
                    }
                } else {
                    mc.fontRenderer.drawStringWithShadow(name, this.getX() + xOffset, this.getY() + yOffset, mod.getColor());
                    yOffset += (mc.fontRenderer.FONT_HEIGHT + 1);
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
    }

}
