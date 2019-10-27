package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;

/**
 * Author Seth
 * 8/18/2019 @ 10:29 PM.
 */
public final class SkullCommand extends Command {

    public SkullCommand() {
        super("Skull", new String[] {"Skll"}, "Allows you to give yourself any player head while in creative", "Skull <Username>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();

        if (!mc.player.isCreative()) {
            Seppuku.INSTANCE.errorChat("Creative mode is required to use this command.");
            return;
        }

        final String[] split = input.split(" ");

        final ItemStack itemStack = new ItemStack(Items.SKULL);
        itemStack.setItemDamage(3);
        itemStack.setTagCompound(new NBTTagCompound());
        itemStack.getTagCompound().setString("SkullOwner", split[1]);

        final int slot = this.findEmptyhotbar();

        mc.player.connection.sendPacket(new CPacketCreativeInventoryAction(36 + (slot != -1 ? slot : mc.player.inventory.currentItem), itemStack));
        Seppuku.INSTANCE.logChat("Gave you skull with username " + split[1]);
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
