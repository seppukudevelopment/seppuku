package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.input.Mouse;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public class MiddleClickPearlModule extends Module {
    private boolean clicked;
    private final Minecraft mc = Minecraft.getMinecraft();

    public MiddleClickPearlModule() {
        super("MiddleClickPearl", new String[]{"mcp", "autopearl"}, "Throws a pearl if you middle-click pointing in mid-air", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (mc.currentScreen == null) {
                if (Mouse.isButtonDown(2)) {
                    if (!this.clicked) {
                        final RayTraceResult result = mc.objectMouseOver;
                        if (result != null && result.typeOfHit == RayTraceResult.Type.MISS) {
                            final int pearlSlot = findPearlInHotbar();
                            if (pearlSlot != -1) {
                                final int oldSlot = mc.player.inventory.currentItem;
                                mc.player.inventory.currentItem = pearlSlot;
                                mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                                mc.player.inventory.currentItem = oldSlot;
                            }
                        }
                    }
                    this.clicked = true;
                } else {
                    this.clicked = false;
                }
            }
        }
    }

    private boolean isItemStackPearl(final ItemStack itemStack) {
        return itemStack.getItem() instanceof ItemEnderPearl;
    }

    private int findPearlInHotbar() {
        for (int index = 0; InventoryPlayer.isHotbar(index); index++) {
            if (isItemStackPearl(mc.player.inventory.getStackInSlot(index))) return index;
        }
        return -1;
    }
}