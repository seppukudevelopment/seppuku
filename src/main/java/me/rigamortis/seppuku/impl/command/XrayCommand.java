package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.util.StringUtil;
import me.rigamortis.seppuku.impl.config.XrayConfig;
import me.rigamortis.seppuku.impl.module.render.XrayModule;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;

/**
 * Author Seth
 * 4/16/2019 @ 10:42 PM.
 */
public final class XrayCommand extends Command {

    private final String[] addAlias = new String[]{"Add", "A"};
    private final String[] removeAlias = new String[]{"Remove", "Rem", "R", "Delete", "Del", "D"};
    private final String[] listAlias = new String[]{"List", "Lst"};
    private final String[] clearAlias = new String[]{"Clear", "C"};

    public XrayCommand() {
        super("Xray", new String[]{"JadeVision", "Jade"}, "Allows you to change what blocks are visible on xray",
                "Xray Add <Block_Name>\n" +
                        "Xray Add <ID>\n" +
                        "Xray Remove <Block_Name>\n" +
                        "Xray Remove <ID>\n" +
                        "Xray List\n" +
                        "Xray Clear");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 3)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final XrayModule xray = (XrayModule) Seppuku.INSTANCE.getModuleManager().find(XrayModule.class);

        if (xray != null) {
            if (equals(addAlias, split[1])) {
                if (!this.clamp(input, 3, 3)) {
                    this.printUsage();
                    return;
                }

                if (StringUtil.isInt(split[2])) {
                    final int id = Integer.parseInt(split[2]);

                    if (id > 0) {
                        final Block block = Block.getBlockById(id);

                        if (block != null) {
                            if (xray.contains(Block.getIdFromBlock(block))) {
                                Seppuku.INSTANCE.logChat("Xray already contains " + block.getLocalizedName());
                            } else {
                                xray.add(Block.getIdFromBlock(block));
                                if (xray.isEnabled()) {
                                    xray.updateRenders();
                                }
                                Seppuku.INSTANCE.getConfigManager().save(XrayConfig.class);
                                Seppuku.INSTANCE.logChat("Added " + block.getLocalizedName() + " to xray");
                            }
                        } else {
                            Seppuku.INSTANCE.logChat("\247c" + split[2] + "\247f is not a valid block");
                        }
                    } else {
                        Seppuku.INSTANCE.errorChat("Cannot add Air to xray");
                    }
                } else {
                    final Block block = Block.getBlockFromName(split[2].toLowerCase());

                    if (block != null) {
                        if (block == Blocks.AIR) {
                            Seppuku.INSTANCE.errorChat("Cannot add Air to xray");
                        } else {
                            if (xray.contains(Block.getIdFromBlock(block))) {
                                Seppuku.INSTANCE.logChat("Xray already contains " + block.getLocalizedName());
                            } else {
                                xray.add(Block.getIdFromBlock(block));
                                if (xray.isEnabled()) {
                                    xray.updateRenders();
                                }
                                Seppuku.INSTANCE.getConfigManager().save(XrayConfig.class);
                                Seppuku.INSTANCE.logChat("Added " + block.getLocalizedName() + " to xray");
                            }
                        }
                    } else {
                        Seppuku.INSTANCE.logChat("\247c" + split[2] + "\247f is not a valid block");
                    }
                }
            } else if (equals(removeAlias, split[1])) {
                if (!this.clamp(input, 3, 3)) {
                    this.printUsage();
                    return;
                }

                if (StringUtil.isInt(split[2])) {
                    final int id = Integer.parseInt(split[2]);

                    if (id > 0) {
                        final Block block = Block.getBlockById(id);

                        if (block != null) {
                            if (xray.contains(Block.getIdFromBlock(block))) {
                                xray.remove(Block.getIdFromBlock(block));
                                if (xray.isEnabled()) {
                                    xray.updateRenders();
                                }
                                Seppuku.INSTANCE.getConfigManager().save(XrayConfig.class);
                                Seppuku.INSTANCE.logChat("Removed " + block.getLocalizedName() + " from xray");
                            } else {
                                Seppuku.INSTANCE.logChat("Xray doesn't contain " + block.getLocalizedName());
                            }
                        } else {
                            Seppuku.INSTANCE.logChat("\247c" + split[2] + "\247f is not a valid block");
                        }
                    } else {
                        Seppuku.INSTANCE.errorChat("Cannot remove Air from xray");
                    }
                } else {
                    final Block block = Block.getBlockFromName(split[2].toLowerCase());

                    if (block != null) {
                        if (block == Blocks.AIR) {
                            Seppuku.INSTANCE.errorChat("Cannot remove Air from xray");
                        } else {
                            if (xray.contains(Block.getIdFromBlock(block))) {
                                xray.remove(Block.getIdFromBlock(block));
                                if (xray.isEnabled()) {
                                    xray.updateRenders();
                                }
                                Seppuku.INSTANCE.getConfigManager().save(XrayConfig.class);
                                Seppuku.INSTANCE.logChat("Removed " + block.getLocalizedName() + " from xray");
                            } else {
                                Seppuku.INSTANCE.logChat("Xray doesn't contain " + block.getLocalizedName());
                            }
                        }
                    } else {
                        Seppuku.INSTANCE.logChat("\247c" + split[2] + "\247f is not a valid block");
                    }
                }
            } else if (equals(listAlias, split[1])) {
                if (!this.clamp(input, 2, 2)) {
                    this.printUsage();
                    return;
                }

                if (xray.getIds().size() > 0) {
                    final TextComponentString msg = new TextComponentString("\247Xray IDs: ");

                    for (int i : xray.getIds()) {
                        msg.appendSibling(new TextComponentString("\2477[\247a" + i + "\2477] ")
                                .setStyle(new Style()
                                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(Block.getBlockById(i).getLocalizedName())))));
                    }

                    Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(msg);
                } else {
                    Seppuku.INSTANCE.logChat("You don't have any search ids");
                }
            } else if (equals(clearAlias, split[1])) {
                if (!this.clamp(input, 2, 2)) {
                    this.printUsage();
                    return;
                }
                xray.clear();
                if (xray.isEnabled()) {
                    xray.updateRenders();
                }
                Seppuku.INSTANCE.getConfigManager().save(XrayConfig.class);
                Seppuku.INSTANCE.logChat("Cleared all blocks from xray");
            } else {
                Seppuku.INSTANCE.errorChat("Unknown input " + "\247f\"" + input + "\"");
                this.printUsage();
            }
        } else {
            Seppuku.INSTANCE.errorChat("Xray not present");
        }
    }
}
