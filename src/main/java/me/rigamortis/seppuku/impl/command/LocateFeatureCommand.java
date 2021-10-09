package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.util.StringUtil;
import me.rigamortis.seppuku.impl.management.WorldManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;

import java.lang.reflect.Field;

public final class LocateFeatureCommand extends Command {

    public LocateFeatureCommand() {
        super("LocateFeature", new String[]{"LocFeature", "LocateFeat", "LF"}, "Like /locate, but client-side and with an option to override the origin for calculating the nearest feature", "LocateFeature <Feature name>\nLocateFeature <Feature name> <X> <Z>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 4)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        if (split.length == 3) {
            this.printUsage();
            return;
        }

        final BlockPos originPos;
        final String originHint;
        if (split.length == 4) {
            if (StringUtil.isInt(split[2])) {
                if (StringUtil.isInt(split[3])) {
                    originPos = new BlockPos(Integer.parseInt(split[2]), 64, Integer.parseInt(split[3]));
                } else {
                    Seppuku.INSTANCE.errorChat("Unknown number " + "\247f\"" + split[3] + "\"");
                    return;
                }
            } else {
                Seppuku.INSTANCE.errorChat("Unknown number " + "\247f\"" + split[2] + "\"");
                return;
            }
            originHint = "to " + String.valueOf(originPos.getX()) + ", " + String.valueOf(originPos.getZ());
        } else {
            originPos = new BlockPos(Minecraft.getMinecraft().player.posX, 64, Minecraft.getMinecraft().player.posZ);
            originHint = "you";
        }

        final ServerData serverData = Minecraft.getMinecraft().getCurrentServerData();
        if (serverData != null) {
            final WorldManager.WorldData worldData = Seppuku.INSTANCE.getWorldManager().find(serverData.serverIP);
            if (worldData != null) {
                final FakeWorld world = new FakeWorld(worldData.getSeed());
                final BlockPos pos = world.getNearestStructurePos(split[1], originPos);
                if (pos == null) {
                    Seppuku.INSTANCE.errorChat("No structure found " + "\247f\"" + split[1] + "\"");
                } else {
                    Seppuku.INSTANCE.logChat(split[1] + " nearest " + originHint + " is at " + String.valueOf(pos.getX()) + ", " + String.valueOf(pos.getZ()));
                }
            } else {
                Seppuku.INSTANCE.errorChat("Seed not set. Use the seed command first");
            }
        } else {
            Seppuku.INSTANCE.errorChat("Cannot locate feature on localhost, since localhost has no seed");
        }
    }

    private class FakeWorld extends World {
        final IChunkGenerator chunkGenerator;

        public FakeWorld(long seed) {
            this(Minecraft.getMinecraft().world, seed);
        }

        public FakeWorld(WorldClient worldClient, long seed) {
            super(new SaveHandlerMP(), new WorldInfo(new WorldSettings(seed, worldClient.getWorldInfo().getGameType(), true, worldClient.getWorldInfo().isHardcoreModeEnabled(), worldClient.getWorldInfo().getTerrainType()), "MpServer"), DimensionManager.createProviderFor(worldClient.provider.getDimension()), null, true);
            this.provider.setWorld(this);
            this.chunkGenerator = this.provider.createChunkGenerator();

            // I would use getDeclaredField("world") here, but it fails outside the dev environment
            // I also tried using ObfuscationReflectionHelper but it wouldn't compile, so I gave up
            Field worldField = null;
            for (Field field : MapGenBase.class.getDeclaredFields()) {
                if (World.class.isAssignableFrom(field.getType())) {
                    worldField = field;
                    break;
                }
            }

            if (worldField == null) {
                Seppuku.INSTANCE.errorChat("Could not find \"world\" field; feature location will fail");
                return;
            }

            worldField.setAccessible(true);

            for (Field field : this.chunkGenerator.getClass().getDeclaredFields()) {
                try {
                    if (MapGenStructure.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        MapGenBase structureGenerator = (MapGenBase) field.get(this.chunkGenerator);
                        worldField.set(structureGenerator, this);
                    }
                } catch (IllegalAccessException e) {
                    Seppuku.INSTANCE.errorChat("Potential feature type skipped due to exception");
                    e.printStackTrace();
                }
            }
        }

        protected IChunkProvider createChunkProvider() {
            return null;
        }

        protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
            return false;
        }

        public BlockPos getNearestStructurePos(String structureName, BlockPos position) {
            return this.chunkGenerator.getNearestStructurePos(this, structureName, position, false);
        }
    }
}
