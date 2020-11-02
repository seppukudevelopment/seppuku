package me.rigamortis.seppuku.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.util.PotionUtil;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

/**
 * created by noil on 9/4/2019 at 10:38 PM
 */
public final class EnemyPotionsComponent extends DraggableHudComponent {

    public EnemyPotionsComponent() {
        super("EnemyPotions");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final Minecraft mc = Minecraft.getMinecraft();

        int effectCount = 0;
        float xOffset = 0;
        float yOffset = 0;
        float maxWidth = 0;

        if (mc.player != null && mc.world != null) {
            for (EntityPlayer player : mc.world.playerEntities) {

                // Check for self & friendds
                if ((player == mc.player) || (Seppuku.INSTANCE.getFriendManager().isFriend(player) != null))
                    continue;

                // Loop thru active potion effects on the player
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    if (effect.getDuration() <= 0)
                        continue;

                    final String effectString = PotionUtil.getFriendlyPotionName(effect);
                    if (effectString != null) { // will return null if it doesn't exist as a valid formatted name
                        final String displayLine = String.format("%s (%s %s)", ChatFormatting.WHITE + player.getName(), ChatFormatting.RESET + effectString, ChatFormatting.GRAY + Potion.getPotionDurationString(effect, 1.0F) + ChatFormatting.WHITE);

                        final float width = mc.fontRenderer.getStringWidth(displayLine);
                        if (width >= maxWidth) {
                            maxWidth = width;
                        }

                        if (this.getAnchorPoint() != null) {
                            switch (this.getAnchorPoint().getPoint()) {
                                case TOP_CENTER:
                                    xOffset = (this.getW() - width) / 2;
                                    break;
                                case TOP_LEFT:
                                case BOTTOM_LEFT:
                                    xOffset = 0;
                                    break;
                                case TOP_RIGHT:
                                case BOTTOM_RIGHT:
                                    xOffset = this.getW() - width;
                                    break;
                            }
                        }

                        if (this.getAnchorPoint() != null) {
                            switch (this.getAnchorPoint().getPoint()) {
                                case TOP_CENTER:
                                case TOP_LEFT:
                                case TOP_RIGHT:
                                    mc.fontRenderer.drawStringWithShadow(displayLine, this.getX() + xOffset, this.getY() + yOffset, effect.getPotion().getLiquidColor());
                                    yOffset += (mc.fontRenderer.FONT_HEIGHT + 1);
                                    break;
                                case BOTTOM_LEFT:
                                case BOTTOM_RIGHT:
                                    mc.fontRenderer.drawStringWithShadow(displayLine, this.getX() + xOffset, this.getY() + (this.getH() - mc.fontRenderer.FONT_HEIGHT) + yOffset, effect.getPotion().getLiquidColor());
                                    yOffset -= (mc.fontRenderer.FONT_HEIGHT + 1);
                                    break;
                            }
                        } else {
                            mc.fontRenderer.drawStringWithShadow(displayLine, this.getX() + xOffset, this.getY() + yOffset, effect.getPotion().getLiquidColor());
                            yOffset += (mc.fontRenderer.FONT_HEIGHT + 1);
                        }

                        effectCount += 1;
                    }
                }
            }
        }

        if (Minecraft.getMinecraft().currentScreen instanceof GuiHudEditor) {
            if (effectCount == 0) {
                final String placeholder = "(enemy potion effects)";
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