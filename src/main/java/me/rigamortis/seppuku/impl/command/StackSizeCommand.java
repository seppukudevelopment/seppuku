package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

/**
 * Author Seth
 * 8/16/2019 @ 4:27 AM.
 */
public final class StackSizeCommand extends Command {

    public StackSizeCommand() {
        super("StackSize", new String[]{"SS"}, "Allows you to change your held item stack size while in creative mode", "StackSize <Amount>");
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

        final ItemStack itemStack = mc.player.getHeldItemMainhand();

        if (itemStack.isEmpty()) {
            Seppuku.INSTANCE.errorChat("Please hold an item in your main hand to enchant.");
            return;
        }

        final String[] split = input.split(" ");

        if (StringUtil.isInt(split[1])) {
            final int num = Integer.parseInt(split[1]);
            itemStack.setCount(num);
            itemStack.getItem().updateItemStackNBT(itemStack.getTagCompound());
            Seppuku.INSTANCE.logChat("Set your stack size to " + num);
        } else {
            Seppuku.INSTANCE.errorChat("Unknown number " + "\247f\"" + split[1] + "\"");
        }
    }
}
