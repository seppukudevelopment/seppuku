package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.util.ResourceLocation;

/**
 * Author Seth
 * 8/18/2019 @ 10:37 PM.
 */
public final class GiveCommand extends Command {

    public GiveCommand() {
        super("Give", new String[] {"Giv"}, "Allows you to give yourself any item while in creative", "Give <Item> <Data>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2)) {
            this.printUsage();
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();

        if (!mc.player.isCreative()) {
            Seppuku.INSTANCE.errorChat("Creative mode is required to use this command.");
            return;
        }

        final String[] split = input.split(" ");

        final Item item = this.findItem(split[1]);

        if(item != null) {
            int amount = 1;
            int meta = 0;

            if(split.length >= 3) {
                if(StringUtil.isInt(split[2])) {
                    amount = Integer.parseInt(split[2]);
                }else{
                    Seppuku.INSTANCE.errorChat("Unknown number " + "\247f\"" + split[2] + "\"");
                }
            }

            if(split.length >= 4) {
                if(StringUtil.isInt(split[3])) {
                    meta = Integer.parseInt(split[3]);
                }else{
                    Seppuku.INSTANCE.errorChat("Unknown number " + "\247f\"" + split[3] + "\"");
                }
            }

            final ItemStack itemStack = new ItemStack(item, amount, meta);

            if(split.length >= 5) {
                final String s = this.buildString(split, 4);

                try {
                    itemStack.setTagCompound(JsonToNBT.getTagFromJson(s));
                } catch (NBTException e) {
                    e.printStackTrace();
                }
            }

            final int slot = this.findEmptyhotbar();
            mc.player.connection.sendPacket(new CPacketCreativeInventoryAction(36 + (slot != -1 ? slot : mc.player.inventory.currentItem), itemStack));
            Seppuku.INSTANCE.logChat("Gave you " + amount + " " + itemStack.getDisplayName());
        }else{
            final ResourceLocation similar = this.findSimilarItem(split[1]);

            if(similar != null) {
                Seppuku.INSTANCE.errorChat("Unknown item " + "\247f\"" + split[1] + "\"");
                Seppuku.INSTANCE.logChat("Did you mean " + "\247c" + similar.getPath() + "\247f?");
            }
        }
    }

    private String buildString(String[] args, int startPos) {
        final StringBuilder sb = new StringBuilder();

        for (int i = startPos; i < args.length; ++i) {
            if (i > startPos) {
                sb.append(" ");
            }

            final String s = args[i];
            sb.append(s);
        }

        return sb.toString();
    }

    private ResourceLocation findSimilarItem(String name) {
        ResourceLocation ret = null;
        double similarity = 0.0f;

        for (ResourceLocation res : Item.REGISTRY.getKeys()) {
            final double currentSimilarity = StringUtil.levenshteinDistance(name, res.getPath());

            if (currentSimilarity >= similarity) {
                similarity = currentSimilarity;
                ret = res;
            }
        }

        return ret;
    }

    private Item findItem(String name) {
        final ResourceLocation res = new ResourceLocation(name);
        return Item.REGISTRY.getObject(res);
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
