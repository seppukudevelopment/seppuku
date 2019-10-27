package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;

/**
 * created by noil on 8/15/2019 at 7:29 PM
 */
public final class EnchantCommand extends Command {

    public EnchantCommand() {
        super("Enchant", new String[]{"Ench"}, "Add enchants to your held item while in creative mode.", "Enchant <Enchantment / All> <Level / Max> ([true/false] Disable Curses)");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 3, 4)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final String enchantToApply = split[1];
        final String levelToApply = split[2];

        // ensure we got some input to parse
        if (enchantToApply != null && levelToApply != null) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (!mc.player.isCreative()) { // need to be in creative
                Seppuku.INSTANCE.errorChat("Creative mode is required to use this command.");
                return;
            }

            final ItemStack itemStack = mc.player.getHeldItemMainhand();

            if (itemStack.isEmpty()) { // needs an item of some sort to be held
                Seppuku.INSTANCE.errorChat("Please hold an item in your main hand to enchant.");
                return;
            }

            NBTTagCompound tagCompound = itemStack.getTagCompound();

            if (tagCompound == null) { // we need a tag compound to be valid to do any of this magic, so we'll check for a null compound
                tagCompound = new NBTTagCompound();
                itemStack.setTagCompound(tagCompound);
            }

            if (!tagCompound.hasKey("ench", 9)) { // check if we have enchantment tag list already or not
                tagCompound.setTag("ench", new NBTTagList()); // create new enchantment tag list
            }

            NBTTagList enchantments = itemStack.getTagCompound().getTagList("ench", 10); // this is the item's enchant compound list we are going to modify

            // loop thru all the registered enchants and find the ones we need
            for (Enchantment enchant : Enchantment.REGISTRY) {
                if (enchant == null)
                    continue;

                // disable curses? (used when doing all enchants)
                if (split.length > 3) {
                    final String disableCurses = split[3];
                    if (disableCurses.toLowerCase().equals("true") && enchant.isCurse())
                        continue;
                }

                final String enchantmentName = enchant.getTranslatedName(0).replaceAll(" ", "");

                if (enchantToApply.toLowerCase().equals("all") || enchantmentName.toLowerCase().startsWith(enchantToApply.toLowerCase())) {
                    final NBTTagCompound enchantmentCompound = new NBTTagCompound();
                    enchantmentCompound.setShort("id", (short) Enchantment.getEnchantmentID(enchant)); // set the enchant id
                    if (levelToApply.toLowerCase().startsWith("max")) {
                        enchantmentCompound.setShort("lvl", Short.MAX_VALUE); // set the level to the max short value
                    } else {
                        enchantmentCompound.setShort("lvl", Short.valueOf(levelToApply)); // set the level
                    }
                    enchantments.appendTag(enchantmentCompound); // add our new enchantment tag to the enchantment tag list
                }
            }

            // cr3at1v3 m0d3 0n1y
            mc.getConnection().sendPacket(new CPacketCreativeInventoryAction(mc.player.inventory.currentItem, itemStack));

            Seppuku.INSTANCE.logChat("Enchants have been added to your item.");
        }
    }
}
