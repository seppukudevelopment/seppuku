package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.api.value.old.BooleanValue;
import me.rigamortis.seppuku.api.value.old.NumberValue;
import me.rigamortis.seppuku.api.value.old.StringValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.network.play.client.CPacketPlaceRecipe;
import net.minecraft.util.ResourceLocation;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 7/19/2019 @ 8:46 PM.
 */
public final class AutoCraftModule extends Module {

    public final BooleanValue drop = new BooleanValue("Drop", new String[]{"d"}, false);
    public final StringValue recipe = new StringValue("Recipe", new String[]{"Recipes", "Rec", "Rec"}, "");
    public final NumberValue delay = new NumberValue("Delay", new String[]{"Del"}, 50.0f, Float.class, 0.0f, 1000.0f, 1.0f);

    private Timer timer = new Timer();

    public AutoCraftModule() {
        super("AutoCraft", new String[]{"AutomaticCraft", "ACraft"}, "Automatically crafts recipes", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (this.recipe.getString().length() > 0 && this.timer.passed(this.delay.getFloat())) {
                if (mc.currentScreen == null || mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiCrafting) {
                    mc.player.connection.sendPacket(new CPacketPlaceRecipe(mc.player.openContainer.windowId, CraftingManager.getRecipe(new ResourceLocation(this.recipe.getString().toLowerCase())), true));

                    mc.playerController.windowClick(mc.player.openContainer.windowId, 0, 0, this.drop.getBoolean() ? ClickType.THROW : ClickType.QUICK_MOVE, mc.player);
                    mc.playerController.updateController();
                }

                this.timer.reset();
            }
        }
    }

}
