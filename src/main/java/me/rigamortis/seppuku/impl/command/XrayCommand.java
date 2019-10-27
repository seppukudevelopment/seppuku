package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.util.StringUtil;
import me.rigamortis.seppuku.impl.module.render.XrayModule;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

/**
 * Author Seth
 * 4/16/2019 @ 10:42 PM.
 */
public final class XrayCommand extends Command {

    private String[] addAlias = new String[]{"Add", "A"};
    private String[] removeAlias = new String[]{"Remove", "Rem", "R", "Delete", "Del", "D"};
    private String[] clearAlias = new String[]{"Clear", "C"};

    public XrayCommand() {
        super("Xray", new String[] {"JadeVision", "Jade"}, "Allows you to change what blocks are visible on xray",
                "Xray Add <Block_Name>\n" +
                        "Xray Add <ID>\n" +
                        "Xray Remove <Block_Name>\n" +
                        "Xray Remove <ID>\n" +
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

        if(xray != null) {
            if (equals(addAlias, split[1])) {
                if (!this.clamp(input, 3, 3)) {
                    this.printUsage();
                    return;
                }

                if(StringUtil.isInt(split[2])) {
                    final int id = Integer.parseInt(split[2]);

                    if(id > 0) {
                        final Block block = Block.getBlockById(id);

                        if(block != null) {
                            if(xray.contains(Block.getIdFromBlock(block))) {
                                Seppuku.INSTANCE.logChat("Xray already contains " + block.getLocalizedName());
                            }else{
                                xray.add(Block.getIdFromBlock(block));
                                if(xray.isEnabled()) {
                                    xray.updateRenders();
                                }
                                Seppuku.INSTANCE.getConfigManager().saveAll();
                                Seppuku.INSTANCE.logChat("Added " + block.getLocalizedName() + " to xray");
                            }
                        }else{
                            Seppuku.INSTANCE.logChat("\247c" + split[2] + "\247f is not a valid block");
                        }
                    }else{
                        Seppuku.INSTANCE.errorChat("Cannot add Air to xray");
                    }
                }else{
                    final Block block = Block.getBlockFromName(split[2].toLowerCase());

                    if(block != null) {
                        if(block == Blocks.AIR) {
                            Seppuku.INSTANCE.errorChat("Cannot add Air to xray");
                        }else{
                            if(xray.contains(Block.getIdFromBlock(block))) {
                                Seppuku.INSTANCE.logChat("Xray already contains " + block.getLocalizedName());
                            }else{
                                xray.add(Block.getIdFromBlock(block));
                                if(xray.isEnabled()) {
                                    xray.updateRenders();
                                }
                                Seppuku.INSTANCE.getConfigManager().saveAll();
                                Seppuku.INSTANCE.logChat("Added " + block.getLocalizedName() + " to xray");
                            }
                        }
                    }else{
                        Seppuku.INSTANCE.logChat("\247c" + split[2] + "\247f is not a valid block");
                    }
                }
            }else if (equals(removeAlias, split[1])) {
                if (!this.clamp(input, 3, 3)) {
                    this.printUsage();
                    return;
                }

                if(StringUtil.isInt(split[2])) {
                    final int id = Integer.parseInt(split[2]);

                    if(id > 0) {
                        final Block block = Block.getBlockById(id);

                        if(block != null) {
                            if(xray.contains(Block.getIdFromBlock(block))) {
                                xray.remove(Block.getIdFromBlock(block));
                                if(xray.isEnabled()) {
                                    xray.updateRenders();
                                }
                                Seppuku.INSTANCE.getConfigManager().saveAll();
                                Seppuku.INSTANCE.logChat("Removed " + block.getLocalizedName() + " from xray");
                            }else{
                                Seppuku.INSTANCE.logChat("Xray doesn't contain " + block.getLocalizedName());
                            }
                        }else{
                            Seppuku.INSTANCE.logChat("\247c" + split[2] + "\247f is not a valid block");
                        }
                    }else{
                        Seppuku.INSTANCE.errorChat("Cannot remove Air from xray");
                    }
                }else{
                    final Block block = Block.getBlockFromName(split[2].toLowerCase());

                    if(block != null) {
                        if(block == Blocks.AIR) {
                            Seppuku.INSTANCE.errorChat("Cannot remove Air from xray");
                        }else{
                            if(xray.contains(Block.getIdFromBlock(block))) {
                                xray.remove(Block.getIdFromBlock(block));
                                if(xray.isEnabled()) {
                                    xray.updateRenders();
                                }
                                Seppuku.INSTANCE.getConfigManager().saveAll();
                                Seppuku.INSTANCE.logChat("Removed " + block.getLocalizedName() + " from xray");
                            }else{
                                Seppuku.INSTANCE.logChat("Xray doesn't contain " + block.getLocalizedName());
                            }
                        }
                    }else{
                        Seppuku.INSTANCE.logChat("\247c" + split[2] + "\247f is not a valid block");
                    }
                }
            }else if (equals(clearAlias, split[1])) {
                if (!this.clamp(input, 2, 2)) {
                    this.printUsage();
                    return;
                }
                xray.clear();
                if(xray.isEnabled()) {
                    xray.updateRenders();
                }
                Seppuku.INSTANCE.getConfigManager().saveAll();
                Seppuku.INSTANCE.logChat("Cleared all blocks from xray");
            }else{
                Seppuku.INSTANCE.errorChat("Unknown input " + "\247f\"" + input + "\"");
                this.printUsage();
            }
        }else{
            Seppuku.INSTANCE.errorChat("Xray not present");
        }
    }
}
