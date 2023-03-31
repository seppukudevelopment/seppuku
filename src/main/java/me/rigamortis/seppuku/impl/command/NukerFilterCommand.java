package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.util.StringUtil;
import me.rigamortis.seppuku.impl.config.NukerFilterConfig;
import me.rigamortis.seppuku.impl.module.world.NukerModule;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;

/**
 * @author Old Chum
 * @since 3/30/2023
 */
public class NukerFilterCommand extends Command {

    private final String[] addAlias = new String[]{"Add", "A"};
    private final String[] removeAlias = new String[]{"Remove", "Rem", "R", "Delete", "Del", "D"};
    private final String[] listAlias = new String[]{"List", "Lst"};
    private final String[] clearAlias = new String[]{"Clear", "C"};

    public NukerFilterCommand() {
        super("NukerFilter", new String[]{"NukerF", "FilterN"}, "Allows you to change what blocks nuker mines",
                "NukerFilter Add <Block_Name>\n" +
                        "NukerFilter Add <ID>\n" +
                        "NukerFilter Remove <Block_Name>\n" +
                        "NukerFilter Remove <ID>\n" +
                        "NukerFilter List\n" +
                        "NukerFilter Clear");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 3)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final NukerModule nuker = (NukerModule) Seppuku.INSTANCE.getModuleManager().find(NukerModule.class);

        if (nuker != null) {
            if (equals(addAlias, split[1])) {
                if (!this.clamp(input, 3, 3)) {
                    this.printUsage();
                    return;
                }

                if (StringUtil.isInt(split[2])) {
                    final int id = Integer.parseInt(split[2]);

                    if (id > 0) {
                        final Block block = Block.getBlockById(id);

                        if (nuker.contains(block)) {
                            Seppuku.INSTANCE.logChat("Nuker already contains " + block.getLocalizedName());
                        } else {
                            nuker.add(Block.getIdFromBlock(block));

                            Seppuku.INSTANCE.getConfigManager().save(NukerFilterConfig.class);
                            Seppuku.INSTANCE.logChat("Added " + block.getLocalizedName() + " to nuker");
                        }
                    } else {
                        Seppuku.INSTANCE.errorChat("Cannot add Air to nuker");
                    }
                } else {
                    final Block block = Block.getBlockFromName(split[2].toLowerCase());

                    if (block != null) {
                        if (block == Blocks.AIR) {
                            Seppuku.INSTANCE.errorChat("Cannot add Air to nuker");
                        } else {
                            if (nuker.contains(block)) {
                                Seppuku.INSTANCE.logChat("Nuker already contains " + block.getLocalizedName());
                            } else {
                                nuker.add(Block.getIdFromBlock(block));

                                Seppuku.INSTANCE.getConfigManager().save(NukerFilterConfig.class);
                                Seppuku.INSTANCE.logChat("Added " + block.getLocalizedName() + " to nuker");
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

                        if (nuker.contains(block)) {
                            nuker.remove(Block.getIdFromBlock(block));

                            Seppuku.INSTANCE.getConfigManager().save(NukerFilterConfig.class);
                            Seppuku.INSTANCE.logChat("Removed " + block.getLocalizedName() + " from nuker");
                        } else {
                            Seppuku.INSTANCE.logChat("Nuker doesn't contain " + block.getLocalizedName());
                        }
                    } else {
                        Seppuku.INSTANCE.errorChat("Cannot remove Air from nuker");
                    }
                } else {
                    final Block block = Block.getBlockFromName(split[2].toLowerCase());

                    if (block != null) {
                        if (block == Blocks.AIR) {
                            Seppuku.INSTANCE.errorChat("Cannot remove Air from nuker");
                        } else {
                            if (nuker.contains(block)) {
                                nuker.remove(Block.getIdFromBlock(block));

                                Seppuku.INSTANCE.getConfigManager().save(NukerFilterConfig.class);
                                Seppuku.INSTANCE.logChat("Removed " + block.getLocalizedName() + " from nuker");
                            } else {
                                Seppuku.INSTANCE.logChat("Nuker doesn't contain " + block.getLocalizedName());
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

                if (nuker.getFilter().getValue().size() > 0) {
                    final TextComponentString msg = new TextComponentString("\2477Nuker IDs: ");

                    for (Block block : nuker.getFilter().getValue()) {
                        msg.appendSibling(new TextComponentString("\2477[\247a" + Block.getIdFromBlock(block) + "\2477] ")
                                .setStyle(new Style()
                                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(block.getLocalizedName())))));
                    }

                    Seppuku.INSTANCE.logcChat(msg);
                } else {
                    Seppuku.INSTANCE.logChat("You don't have any nuker ids");
                }
            } else if (equals(clearAlias, split[1])) {
                if (!this.clamp(input, 2, 2)) {
                    this.printUsage();
                    return;
                }
                nuker.clear();

                Seppuku.INSTANCE.getConfigManager().save(NukerFilterConfig.class);
                Seppuku.INSTANCE.logChat("Cleared all blocks from nuker");
            } else {
                Seppuku.INSTANCE.errorChat("Unknown input " + "\247f\"" + input + "\"");
                this.printUsage();
            }
        } else {
            Seppuku.INSTANCE.errorChat("Nuker not present");
        }
    }
}
