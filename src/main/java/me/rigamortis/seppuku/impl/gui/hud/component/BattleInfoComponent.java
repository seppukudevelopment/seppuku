package me.rigamortis.seppuku.impl.gui.hud.component;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.util.ColorUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import me.rigamortis.seppuku.impl.module.combat.CrystalAuraModule;
import me.rigamortis.seppuku.impl.module.combat.KillAuraModule;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public final class BattleInfoComponent extends DraggableHudComponent {

    private CrystalAuraModule crystalAuraModule;
    private KillAuraModule killAuraModule;

    private AbstractClientPlayer currentOpponent;

    public BattleInfoComponent() {
        super("BattleInfo");
        this.setW(117);
        this.setH(48);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.world != null && mc.player != null) {
            if (this.crystalAuraModule == null)
                this.crystalAuraModule = (CrystalAuraModule) Seppuku.INSTANCE.getModuleManager().find(CrystalAuraModule.class);

            if (this.killAuraModule == null)
                this.killAuraModule = (KillAuraModule) Seppuku.INSTANCE.getModuleManager().find(KillAuraModule.class);

            // local vars
            int itemSpacingOffset = 2;
            String targetType = "target";

            // grid
            if (mc.currentScreen instanceof GuiHudEditor) {
                for (float j = this.getX() + this.getW(); j > this.getX(); j -= 10) {
                    if (j <= this.getX())
                        continue;

                    if (j >= this.getX() + this.getW())
                        continue;

                    RenderUtil.drawLine(j, this.getY(), j, this.getY() + this.getH(), 2.0f, 0x75101010);
                }
            } else {
                // background
                RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x75101010);
            }

            if (this.crystalAuraModule.getCurrentAttackEntity() != null) {
                this.currentOpponent = (AbstractClientPlayer) this.crystalAuraModule.getCurrentAttackPlayer();
                targetType = "crystal aura";
            } else if (this.killAuraModule.getCurrentTarget() != null) {
                if (this.killAuraModule.getCurrentTarget() instanceof AbstractClientPlayer) {
                    this.currentOpponent = (AbstractClientPlayer) this.killAuraModule.getCurrentTarget();
                    targetType = "kill aura";
                }
            } else if (mc.objectMouseOver != null) {
                if (mc.objectMouseOver.entityHit != null) {
                    if (mc.objectMouseOver.entityHit instanceof AbstractClientPlayer && mc.objectMouseOver.entityHit.isEntityAlive()) {
                        this.currentOpponent = (AbstractClientPlayer) mc.objectMouseOver.entityHit;
                        targetType = "mouse over";
                    }
                }
            } else {
                this.currentOpponent = null;
            }

            if (this.currentOpponent != null) {
                if (this.currentOpponent.isEntityAlive()) {
                    // head bg
                    RenderUtil.drawRect(this.getX() + 1, this.getY() + 1, this.getX() + 27, this.getY() + 27, 0x75101010);

                    // head
                    mc.getTextureManager().bindTexture(this.currentOpponent.getLocationSkin());
                    RenderUtil.drawTexture(this.getX() + 2, this.getY() + 2, 24, 24, 8F / 64f, 8f / 64f, 16f / 64f, 16f / 64f);

                    // armor and items
                    GlStateManager.pushMatrix();
                    RenderHelper.enableGUIStandardItemLighting();
                    for (int i = 3; i >= 0; i--) {
                        final ItemStack stack = this.currentOpponent.inventoryContainer.getSlot(8 - i).getStack();
                        RenderUtil.drawRect((int) this.getX() + itemSpacingOffset, (int) this.getY() + 29, (int) this.getX() + itemSpacingOffset + 18, (int) this.getY() + 29 + 17, 0x75101010);
                        if (!stack.isEmpty()) {
                            mc.getRenderItem().renderItemAndEffectIntoGUI(stack, (int) this.getX() + itemSpacingOffset + 1, (int) this.getY() + 29);
                            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, (int) this.getX() + itemSpacingOffset + 1, (int) this.getY() + 29);
                        }
                        itemSpacingOffset += 19;
                    }

                    final ItemStack mainHandStack = this.currentOpponent.getHeldItem(EnumHand.MAIN_HAND);
                    RenderUtil.drawRect((int) this.getX() + itemSpacingOffset, (int) this.getY() + 29, (int) this.getX() + itemSpacingOffset + 18, (int) this.getY() + 29 + 17, 0x75101010);
                    if (!mainHandStack.isEmpty()) {
                        mc.getRenderItem().renderItemAndEffectIntoGUI(mainHandStack, (int) this.getX() + itemSpacingOffset + 1, (int) this.getY() + 29);
                        mc.getRenderItem().renderItemOverlays(mc.fontRenderer, mainHandStack, (int) this.getX() + itemSpacingOffset + 1, (int) this.getY() + 29);
                    }

                    itemSpacingOffset += 19;

                    final ItemStack offHandStack = this.currentOpponent.getHeldItem(EnumHand.OFF_HAND);
                    RenderUtil.drawRect((int) this.getX() + itemSpacingOffset, (int) this.getY() + 29, (int) this.getX() + itemSpacingOffset + 18, (int) this.getY() + 29 + 17, 0x75101010);
                    if (!offHandStack.isEmpty()) {
                        mc.getRenderItem().renderItemAndEffectIntoGUI(offHandStack, (int) this.getX() + itemSpacingOffset + 1, (int) this.getY() + 29);
                        mc.getRenderItem().renderItemOverlays(mc.fontRenderer, offHandStack, (int) this.getX() + itemSpacingOffset + 1, (int) this.getY() + 29);
                    }

                    RenderHelper.disableStandardItemLighting();
                    GlStateManager.popMatrix();

                    // hp bg
                    RenderUtil.drawRect(this.getX() + 28, this.getY() + 2 + mc.fontRenderer.FONT_HEIGHT * 2, this.getX() + this.getW() - 2, this.getY() + 2 + mc.fontRenderer.FONT_HEIGHT * 2 + 8, 0x75101010);
                    // hp
                    final float hpWidth = ((this.currentOpponent.getHealth() * ((this.getX() + this.getW() - 2 - 1) - (this.getX() + 29))) / this.currentOpponent.getMaxHealth());
                    RenderUtil.drawSideGradientRect(this.getX() + 29, this.getY() + 2 + mc.fontRenderer.FONT_HEIGHT * 2 + 1, this.getX() + this.getW() - 2 - 1, this.getY() + 2 + mc.fontRenderer.FONT_HEIGHT * 2 + 7, 0x3000FF00, 0x300000FF);
                    RenderUtil.drawRect(this.getX() + 29, this.getY() + 2 + mc.fontRenderer.FONT_HEIGHT * 2 + 1, ((this.getX() + 29) - (this.getX() + this.getW() - 2 - 1)) + (this.getX() + this.getW() - 2 - 1) + hpWidth, this.getY() + 2 + mc.fontRenderer.FONT_HEIGHT * 2 + 7, ColorUtil.getHealthColor(this.currentOpponent));
                    RenderUtil.drawGradientRect(this.getX() + 29, this.getY() + 2 + mc.fontRenderer.FONT_HEIGHT * 2 + 1, this.getX() + this.getW() - 2 - 1, this.getY() + 2 + mc.fontRenderer.FONT_HEIGHT * 2 + 7, 0x00000000, 0x50000000);

                    //if (this.currentOpponent instanceof AbstractClientPlayer) {
                    //final AbstractClientPlayer abstractClientPlayer = (AbstractClientPlayer) this.currentOpponent;
                    //Minecraft.getMinecraft().getTextureManager().bindTexture(abstractClientPlayer.getLocationSkin());
                    //RenderUtil.drawTexture(this.getX(), this.getY(), 20, 20, 1, 0, 1, 1);
                    // }

                    // entity name
                    mc.fontRenderer.drawStringWithShadow(this.currentOpponent.getName(), this.getX() + 28, this.getY() + 2, 0xFFEEEEEE);
                    mc.fontRenderer.drawStringWithShadow("[" + targetType + "]", this.getX() + 28, this.getY() + 2 + mc.fontRenderer.FONT_HEIGHT, 0xFF999999);
                } else {
                    this.currentOpponent = null;
                }
            } else {
                mc.fontRenderer.drawStringWithShadow("Waiting for target...", this.getX() + 2, this.getY() + 2, 0xFFAAAAAA);
            }

            //if (this.isMouseInside(mouseX, mouseY)) { // mouse is inside
            // draw extra info
            //}
        } else {
            mc.fontRenderer.drawStringWithShadow("(battle info)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }
}
