package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.util.StringUtil;
import net.minecraft.client.Minecraft;

/**
 * Author Seth
 * 5/3/2019 @ 5:31 PM.
 */
public final class PitchCommand extends Command {

    public PitchCommand() {
        super("Pitch", new String[] {"Pch"}, "Allows you to set your pitch", "Pitch <Number>");
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

            Minecraft.getMinecraft().player.rotationPitch = num;
            if(Minecraft.getMinecraft().player.getRidingEntity() != null) {
                Minecraft.getMinecraft().player.getRidingEntity().rotationPitch = num;
            }

            Seppuku.INSTANCE.logChat("Set pitch to " + num);
        } else {
            Seppuku.INSTANCE.errorChat("Unknown number " + "\247f\"" + split[1] + "\"");
        }
    }
}
