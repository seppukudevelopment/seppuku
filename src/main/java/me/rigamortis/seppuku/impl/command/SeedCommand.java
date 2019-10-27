package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.util.StringUtil;
import me.rigamortis.seppuku.impl.management.WorldManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

/**
 * Author Seth
 * 6/11/2019 @ 7:15 AM.
 */
public final class SeedCommand extends Command {

    public SeedCommand() {
        super("Seed", new String[] {"RandomSeed"}, "Sets the client-side seed used by certain features", "Seed <Number>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        if(StringUtil.isLong(split[1], 10)) {
            final ServerData serverData = Minecraft.getMinecraft().getCurrentServerData();
            if(serverData != null) {
                final WorldManager.WorldData worldData = Seppuku.INSTANCE.getWorldManager().find(serverData.serverIP);
                if(worldData != null) {
                    final long seed = Long.parseLong(split[1]);
                    worldData.setSeed(seed);
                    Seppuku.INSTANCE.logChat("Set " + serverData.serverIP + "'s seed to " + seed);
                }else{
                    final long seed = Long.parseLong(split[1]);
                    Seppuku.INSTANCE.getWorldManager().getWorldDataList().add(new WorldManager.WorldData(serverData.serverIP, seed));
                    Seppuku.INSTANCE.logChat("Set " + serverData.serverIP + "'s seed to " + seed);
                }
                Seppuku.INSTANCE.getConfigManager().saveAll();
            }else{
                Seppuku.INSTANCE.errorChat("Cannot set seed for localhost");
            }
        }else{
            Seppuku.INSTANCE.errorChat("Unknown number " + "\247f\"" + split[1] + "\"");
        }
    }
}
