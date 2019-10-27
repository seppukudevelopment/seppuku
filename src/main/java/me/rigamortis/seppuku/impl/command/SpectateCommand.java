package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Author Seth
 * 4/21/2019 @ 2:18 PM.
 */
public final class SpectateCommand extends Command {

    public SpectateCommand() {
        super("Spectate", new String[]{"Spec"}, "Allows you to spectate nearby players", "Spectate <Username>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        EntityPlayer target = null;

        for (Entity e : Minecraft.getMinecraft().world.loadedEntityList) {
            if (e != null) {
                if (e instanceof EntityPlayer && e.getName().equalsIgnoreCase(split[1])) {
                    target = (EntityPlayer) e;
                    break;
                }
            }
        }

        if (target != null) {
            Seppuku.INSTANCE.logChat("Now spectating " + target.getName());
            Minecraft.getMinecraft().setRenderViewEntity(target);
        }else{
            Seppuku.INSTANCE.errorChat("\"" + split[1] + "\" is not within range");
        }
    }

}
