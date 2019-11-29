package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.util.StringUtil;
import net.minecraft.client.Minecraft;

/**
 * Author Seth
 * 4/16/2019 @ 9:07 PM.
 */
public final class VClipCommand extends Command {

    public VClipCommand() {
        super("VClip", new String[] {"VC", "VerticalClip", "Up", "Down"}, "Allows you to teleport vertically", "VClip <Amount>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        if (StringUtil.isDouble(split[1])) {
            final double num = Double.parseDouble(split[1]);

            if(Minecraft.getMinecraft().player.getRidingEntity() != null) {
                Minecraft.getMinecraft().player.getRidingEntity().setPosition(Minecraft.getMinecraft().player.getRidingEntity().posX, Minecraft.getMinecraft().player.getRidingEntity().posY + num, Minecraft.getMinecraft().player.getRidingEntity().posZ);
            }else{
                Minecraft.getMinecraft().player.setPosition(Minecraft.getMinecraft().player.posX, Minecraft.getMinecraft().player.posY + num, Minecraft.getMinecraft().player.posZ);
            }
            Seppuku.INSTANCE.logChat("Teleported you " + ((num > 0) ? "up" : "down") + " " + num);
        } else {
            Seppuku.INSTANCE.errorChat("Unknown number " + "\247f\"" + split[1] + "\"");
        }
    }
}
