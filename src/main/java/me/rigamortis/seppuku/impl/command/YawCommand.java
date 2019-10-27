package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.util.StringUtil;
import net.minecraft.client.Minecraft;

/**
 * Author Seth
 * 5/3/2019 @ 5:27 PM.
 */
public final class YawCommand extends Command {

    public YawCommand() {
        super("Yaw", new String[] {"Yw"}, "Allows you to set your yaw", "Yaw <Number>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        if (StringUtil.isDouble(split[1])) {
            final float num = Float.parseFloat(split[1]);

            Minecraft.getMinecraft().player.rotationYaw = num;

            if(Minecraft.getMinecraft().player.getRidingEntity() != null) {
                Minecraft.getMinecraft().player.getRidingEntity().rotationYaw = num;
            }

            Seppuku.INSTANCE.logChat("Set yaw to " + num);
        } else {
            Seppuku.INSTANCE.errorChat("Unknown number " + "\247f\"" + split[1] + "\"");
        }
    }
}
