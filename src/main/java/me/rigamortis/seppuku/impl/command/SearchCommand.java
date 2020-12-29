package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.util.StringUtil;
import me.rigamortis.seppuku.impl.config.SearchConfig;
import me.rigamortis.seppuku.impl.module.render.SearchModule;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;

/**
 * @author noil
 */
public final class SearchCommand extends Command {

    private final String[] addAlias = new String[]{"Add"};
    private final String[] removeAlias = new String[]{"Remove", "Rem", "Delete", "Del"};
    private final String[] listAlias = new String[]{"List", "Lst"};
    private final String[] clearAlias = new String[]{"Clear", "clr"};

    public SearchCommand() {
        super("Search", new String[]{"find", "locate"}, "Allows you to change what blocks are visible on search",
                "Search Add <Block_Name>\n" +
                        "Search Add <ID>\n" +
                        "Search Remove <Block_Name>\n" +
                        "Search Remove <ID>\n" +
                        "Search List\n" +
                        "Search Clear");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 3)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final SearchModule searchModule = (SearchModule) Seppuku.INSTANCE.getModuleManager().find(SearchModule.class);

        if (searchModule != null) {
            if (equals(addAlias, split[1])) {
                if (!this.clamp(input, 3, 3)) {
                    this.printUsage();
                    return;
                }

                if (StringUtil.isInt(split[2])) {
                    final int id = Integer.parseInt(split[2]);

                    if (id > 0) {
                        final Block block = Block.getBlockById(id);

                        if (searchModule.contains(Block.getIdFromBlock(block))) {
                            Seppuku.INSTANCE.logChat("Search already contains " + block.getLocalizedName());
                        } else {
                            searchModule.add(Block.getIdFromBlock(block));
                            if (searchModule.isEnabled()) {
                                searchModule.clearBlocks();
                                searchModule.updateRenders();
                            }
                            Seppuku.INSTANCE.getConfigManager().save(SearchConfig.class);
                            Seppuku.INSTANCE.logChat("Added " + block.getLocalizedName() + " to search");
                        }
                    } else {
                        Seppuku.INSTANCE.errorChat("Cannot add Air to search");
                    }
                } else {
                    final Block block = Block.getBlockFromName(split[2].toLowerCase());

                    if (block != null) {
                        if (block == Blocks.AIR) {
                            Seppuku.INSTANCE.errorChat("Cannot add Air to search");
                        } else {
                            if (searchModule.contains(Block.getIdFromBlock(block))) {
                                Seppuku.INSTANCE.logChat("Search already contains " + block.getLocalizedName());
                            } else {
                                searchModule.add(Block.getIdFromBlock(block));
                                if (searchModule.isEnabled()) {
                                    searchModule.clearBlocks();
                                    searchModule.updateRenders();
                                }
                                Seppuku.INSTANCE.getConfigManager().save(SearchConfig.class);
                                Seppuku.INSTANCE.logChat("Added " + block.getLocalizedName() + " to search");
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

                        if (searchModule.contains(Block.getIdFromBlock(block))) {
                            searchModule.remove(Block.getIdFromBlock(block));
                            if (searchModule.isEnabled()) {
                                searchModule.clearBlocks();
                                searchModule.updateRenders();
                            }
                            Seppuku.INSTANCE.getConfigManager().save(SearchConfig.class);
                            Seppuku.INSTANCE.logChat("Removed " + block.getLocalizedName() + " from search");
                        } else {
                            Seppuku.INSTANCE.logChat("Search doesn't contain " + block.getLocalizedName());
                        }
                    } else {
                        Seppuku.INSTANCE.errorChat("Cannot remove Air from search");
                    }
                } else {
                    final Block block = Block.getBlockFromName(split[2].toLowerCase());

                    if (block != null) {
                        if (block == Blocks.AIR) {
                            Seppuku.INSTANCE.errorChat("Cannot remove Air from search");
                        } else {
                            if (searchModule.contains(Block.getIdFromBlock(block))) {
                                searchModule.remove(Block.getIdFromBlock(block));
                                if (searchModule.isEnabled()) {
                                    searchModule.clearBlocks();
                                    searchModule.updateRenders();
                                }
                                Seppuku.INSTANCE.getConfigManager().save(SearchConfig.class);
                                Seppuku.INSTANCE.logChat("Removed " + block.getLocalizedName() + " from search");
                            } else {
                                Seppuku.INSTANCE.logChat("Search doesn't contain " + block.getLocalizedName());
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

                if (searchModule.getIds().size() > 0) {
                    final TextComponentString msg = new TextComponentString("\2477Search IDs: ");

                    for (int i : searchModule.getIds()) {
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
                searchModule.clear();
                if (searchModule.isEnabled()) {
                    searchModule.clearBlocks();
                    searchModule.updateRenders();
                }
                Seppuku.INSTANCE.getConfigManager().save(SearchConfig.class);
                Seppuku.INSTANCE.logChat("Cleared all blocks from search");
            } else {
                Seppuku.INSTANCE.errorChat("Unknown input " + "\247f\"" + input + "\"");
                this.printUsage();
            }
        } else {
            Seppuku.INSTANCE.errorChat("Search not present");
        }
    }
}

