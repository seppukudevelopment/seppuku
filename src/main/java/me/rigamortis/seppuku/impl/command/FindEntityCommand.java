package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.api.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

/**
 * Author Seth
 * 8/6/2019 @ 4:34 PM.
 */
public final class FindEntityCommand extends Command {

    public FindEntityCommand() {
        super("FindEntity", new String[]{"FindEnt"}, "Scans nearby chunks for entity spawns", "FindEntity <Entity>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();
        final BlockPos pos = mc.player.getPosition();
        final Chunk chunk = mc.world.getChunk(pos);
        final Biome biome = chunk.getBiome(pos, mc.world.getBiomeProvider());

        System.out.println(biome.getSpawnableList(EnumCreatureType.CREATURE));
    }
}
