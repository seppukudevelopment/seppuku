package me.rigamortis.seppuku.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.util.PotionUtil;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Author Seth
 * 7/25/2019 @ 7:54 AM.
 */
public final class PotionEffectsComponent extends DraggableHudComponent {

    public PotionEffectsComponent() {
        super("PotionEffects");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final Minecraft mc = Minecraft.getMinecraft();

        final List<PotionEffect> effects =
                new ArrayList<>(mc.player.getActivePotionEffects());

        final Comparator<PotionEffect> comparator = (first, second) -> {
            final String firstEffect = PotionUtil.getFriendlyPotionName(first) + " " + ChatFormatting.GRAY + Potion.getPotionDurationString(first, 1.0F);
            final String secondEffect = PotionUtil.getFriendlyPotionName(second) + " " + ChatFormatting.GRAY + Potion.getPotionDurationString(second, 1.0F);
            final float dif = mc.fontRenderer.getStringWidth(secondEffect) - mc.fontRenderer.getStringWidth(firstEffect);
            return dif != 0 ? (int) dif : secondEffect.compareTo(firstEffect);
        };

        effects.sort(comparator);

        float xOffset = 0;
        float yOffset = 0;
        float maxWidth = 0;

        for (PotionEffect potionEffect : effects) {
            if (potionEffect != null) {
                final String effect = PotionUtil.getFriendlyPotionName(potionEffect) + " " + ChatFormatting.GRAY + Potion.getPotionDurationString(potionEffect, 1.0F);

                final float width = mc.fontRenderer.getStringWidth(effect);

                if (width >= maxWidth) {
                    maxWidth = width;
                }

                if (this.getAnchorPoint() != null) {
                    switch (this.getAnchorPoint().getPoint()) {
                        case TOP_CENTER:
                            xOffset = (this.getW() - mc.fontRenderer.getStringWidth(effect)) / 2;
                            break;
                        case TOP_LEFT:
                        case BOTTOM_LEFT:
                            xOffset = 0;
                            break;
                        case TOP_RIGHT:
                        case BOTTOM_RIGHT:
                            xOffset = this.getW() - mc.fontRenderer.getStringWidth(effect);
                            break;
                    }
                }

                if (this.getAnchorPoint() != null) {
                    switch (this.getAnchorPoint().getPoint()) {
                        case TOP_CENTER:
                        case TOP_LEFT:
                        case TOP_RIGHT:
                            mc.fontRenderer.drawStringWithShadow(effect, this.getX() + xOffset, this.getY() + yOffset, potionEffect.getPotion().getLiquidColor());
                            yOffset += (mc.fontRenderer.FONT_HEIGHT + 1);
                            break;
                        case BOTTOM_LEFT:
                        case BOTTOM_RIGHT:
                            mc.fontRenderer.drawStringWithShadow(effect, this.getX() + xOffset, this.getY() + (this.getH() - mc.fontRenderer.FONT_HEIGHT) + yOffset, potionEffect.getPotion().getLiquidColor());
                            yOffset -= (mc.fontRenderer.FONT_HEIGHT + 1);
                            break;
                    }
                } else {
                    mc.fontRenderer.drawStringWithShadow(effect, this.getX() + xOffset, this.getY() + yOffset, potionEffect.getPotion().getLiquidColor());
                    yOffset += (mc.fontRenderer.FONT_HEIGHT + 1);
                }
            }
        }

        if (mc.currentScreen instanceof GuiHudEditor) {
            if (effects.size() <= 0) {
                final String placeholder = "(my potion effects)";
                this.setW(mc.fontRenderer.getStringWidth(placeholder));
                this.setH(mc.fontRenderer.FONT_HEIGHT);
                mc.fontRenderer.drawStringWithShadow(placeholder, this.getX(), this.getY(), 0xFFFFFFFF);
                return;
            }
        }

        this.setW(maxWidth);
        this.setH(Math.abs(yOffset));
    }

}