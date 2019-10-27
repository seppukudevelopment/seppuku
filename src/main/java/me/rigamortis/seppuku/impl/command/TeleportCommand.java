package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.util.StringUtil;
import net.minecraft.client.Minecraft;

import java.text.DecimalFormat;

/**
 * Author Seth
 * 6/18/2019 @ 9:24 AM.
 */
public final class TeleportCommand extends Command {

    public TeleportCommand() {
        super("Teleport", new String[] {"Tp"}, "Allows you to teleport to coordinates", "Teleport <X> <Y> <Z>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 4, 4)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        if (StringUtil.isDouble(split[1])) {
            if (StringUtil.isDouble(split[2])) {
                if (StringUtil.isDouble(split[3])) {
                    final double x = Double.parseDouble(split[1]);
                    final double y = Double.parseDouble(split[2]);
                    final double z = Double.parseDouble(split[3]);
                    if (Minecraft.getMinecraft().player.getRidingEntity() != null) {
                        Minecraft.getMinecraft().player.getRidingEntity().setPosition(x, y, z);
                    } else {
                        Minecraft.getMinecraft().player.setPosition(x, y, z);
                    }
                    final DecimalFormat format = new DecimalFormat("##.##");
                    Seppuku.INSTANCE.logChat("Teleported you to X: " + format.format(x) + " Y: " + format.format(y) + " Z: " + format.format(z));
                }else{
                    Seppuku.INSTANCE.errorChat("Unknown number " + "\247f\"" + split[3] + "\"");
                }
            }else{
                Seppuku.INSTANCE.errorChat("Unknown number " + "\247f\"" + split[2] + "\"");
            }
        }else{
            Seppuku.INSTANCE.errorChat("Unknown number " + "\247f\"" + split[1] + "\"");
        }
    }
}
