package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;

import java.util.Iterator;

/**
 * Author Seth
 * 8/16/2019 @ 3:30 AM.
 */
public final class SpawnEggCommand extends Command {

    private String[] listAlias = new String[]{"List", "L"};
    private String[] giveAlias = new String[]{"Give", "G"};

    public SpawnEggCommand() {
        super("SpawnEgg", new String[]{"SEgg"}, "Allows you to spawn in any spawn egg while in creative mode", "SpawnEgg Give <Entity>\n" +
                "SpawnEgg List");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 3)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final Minecraft mc = Minecraft.getMinecraft();

        if (!mc.player.isCreative()) {
            Seppuku.INSTANCE.errorChat("Creative mode is required to use this command.");
            return;
        }

        if (equals(listAlias, split[1])) {
            if (!this.clamp(input, 2, 2)) {
                this.printUsage();
                return;
            }

            final int size = EntityList.getEntityNameList().size();

            if (size > 0) {
                final TextComponentString msg = new TextComponentString("\2477Entities [" + size + "]\247f ");

                final Iterator it = EntityList.getEntityNameList().iterator();

                int index = 0;

                while (it.hasNext()) {
                    final ResourceLocation res = (ResourceLocation) it.next();
                    if (res != null) {
                        msg.appendSibling(new TextComponentString("\247a" + res.getPath() + ":" + res.getPath() + "\2477" + ((index == size - 1) ? "" : ", ")));
                        index++;
                    }
                }

                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(msg);
            }
        } else if (equals(giveAlias, split[1])) {
            if (!this.clamp(input, 3, 3)) {
                this.printUsage();
                return;
            }

            final ResourceLocation res = this.find(split[2]);

            if (res == null) {
                final ResourceLocation similar = this.findSimilar(split[2]);
                if (similar != null) {
                    Seppuku.INSTANCE.errorChat("Unknown entity " + "\247f\"" + split[2] + "\"");
                    Seppuku.INSTANCE.logChat("Did you mean " + "\247c" + similar.getPath() + "\247f?");
                }
            } else {
                final ItemStack itemStack = new ItemStack(Item.getItemById(383));
                final NBTTagCompound tagCompound = (itemStack.hasTagCompound()) ? itemStack.getTagCompound() : new NBTTagCompound();
                final NBTTagCompound entityTag = new NBTTagCompound();

                entityTag.setString("id", res.getNamespace() + ":" + res.getPath());
                tagCompound.setTag("EntityTag", entityTag);
                itemStack.setTagCompound(tagCompound);

                final int slot = this.findEmptyhotbar();

                mc.player.connection.sendPacket(new CPacketCreativeInventoryAction(36 + (slot != -1 ? slot : mc.player.inventory.currentItem), itemStack));
                Seppuku.INSTANCE.logChat("Gave you a spawn egg with entity type: " + res.getNamespace() + ":" + res.getPath());
            }
        } else {
            Seppuku.INSTANCE.errorChat("Unknown input " + "\247f\"" + input + "\"");
            this.printUsage();
        }
    }

    private ResourceLocation findSimilar(String name) {
        ResourceLocation ret = null;
        double similarity = 0.0f;

        for (ResourceLocation res : EntityList.getEntityNameList()) {
            final double currentSimilarity = StringUtil.levenshteinDistance(name, res.getPath());

            if (currentSimilarity >= similarity) {
                similarity = currentSimilarity;
                ret = res;
            }
        }

        return ret;
    }

    private ResourceLocation find(String name) {
        for (ResourceLocation res : EntityList.getEntityNameList()) {
            if (res.getPath().equalsIgnoreCase(name)) {
                return res;
            }
        }
        return null;
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
