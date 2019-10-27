package me.rigamortis.seppuku.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Author Seth
 * 7/25/2019 @ 7:24 AM.
 */
public final class ArrayListComponent extends DraggableHudComponent {

    public ArrayListComponent() {
        super("ArrayList");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final List<Module> mods = new ArrayList<>();

        for (Module mod : Seppuku.INSTANCE.getModuleManager().getModuleList()) {
            if (mod != null && mod.getType() != Module.ModuleType.HIDDEN && mod.isEnabled() && !mod.isHidden()) {
                mods.add(mod);
            }
        }

        final Comparator<Module> comparator = (first, second) -> {
            final String firstName = first.getDisplayName() + (first.getMetaData() != null ? " " + ChatFormatting.GRAY + first.getMetaData() : "");
            final String secondName = second.getDisplayName() + (second.getMetaData() != null ? " " + ChatFormatting.GRAY + second.getMetaData() : "");
            final float dif = Minecraft.getMinecraft().fontRenderer.getStringWidth(secondName) - Minecraft.getMinecraft().fontRenderer.getStringWidth(firstName);
            return dif != 0 ? (int) dif : secondName.compareTo(firstName);
        };

        mods.sort(comparator);

        float xOffset = 0;
        float yOffset = 0;
        float maxWidth = 0;

        for (Module mod : mods) {
            if (mod != null && mod.getType() != Module.ModuleType.HIDDEN && mod.isEnabled() && !mod.isHidden()) {
                final String name = mod.getDisplayName() + (mod.getMetaData() != null ? " " + ChatFormatting.GRAY + mod.getMetaData() : "");

                final float width = Minecraft.getMinecraft().fontRenderer.getStringWidth(name);

                if (width >= maxWidth) {
                    maxWidth = width;
                }

                if (this.getAnchorPoint() != null) {
                    switch (this.getAnchorPoint().getPoint()) {
                        case TOP_CENTER:
                            xOffset = (this.getW() - Minecraft.getMinecraft().fontRenderer.getStringWidth(name)) / 2;
                            break;
                        case TOP_LEFT:
                        case BOTTOM_LEFT:
                            xOffset = 0;
                            break;
                        case TOP_RIGHT:
                        case BOTTOM_RIGHT:
                            xOffset = this.getW() - Minecraft.getMinecraft().fontRenderer.getStringWidth(name);
                            break;
                    }
                }

                if (this.getAnchorPoint() != null) {
                    switch (this.getAnchorPoint().getPoint()) {
                        case TOP_CENTER:
                        case TOP_LEFT:
                        case TOP_RIGHT:
                            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(name, this.getX() + xOffset, this.getY() + yOffset, mod.getColor());
                            yOffset += (Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT + 1);
                            break;
                        case BOTTOM_LEFT:
                        case BOTTOM_RIGHT:
                            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(name, this.getX() + xOffset, this.getY() + (this.getH() - Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT) + yOffset, mod.getColor());
                            yOffset -= (Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT + 1);
                            break;
                    }
                } else {
                    Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(name, this.getX() + xOffset, this.getY() + yOffset, mod.getColor());
                    yOffset += (Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT + 1);
                }
            }
        }

        this.setW(maxWidth);
        this.setH(Math.abs(yOffset));
    }

}
