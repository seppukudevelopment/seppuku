package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.impl.management.WorldManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.util.math.AxisAlignedBB;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Author Seth
 * 6/11/2019 @ 4:23 AM.
 */
public final class SlimeChunksModule extends Module {

    private ICamera frustum = new Frustum();

    private List<SlimeChunk> slimeChunkList = new ArrayList<>();

    public SlimeChunksModule() {
        super("SlimeChunks", new String[]{"SlimesChunks", "SlimeC"}, "Highlights slime chunks(Requires the seed)", "NONE", -1, ModuleType.WORLD);
    }

    @Listener
    public void onToggle() {
        super.onToggle();
        this.slimeChunkList.clear();
    }

    @Listener
    public void render3D(EventRender3D event) {
        for (SlimeChunk slimeChunk : this.slimeChunkList) {
            if (slimeChunk != null) {
                this.frustum.setPosition(Minecraft.getMinecraft().getRenderViewEntity().posX, Minecraft.getMinecraft().getRenderViewEntity().posY, Minecraft.getMinecraft().getRenderViewEntity().posZ);

                final AxisAlignedBB bb = new AxisAlignedBB(slimeChunk.x, 0, slimeChunk.z, slimeChunk.x + 16, 1, slimeChunk.z + 16);

                if (frustum.isBoundingBoxInFrustum(bb)) {
                    RenderUtil.drawPlane(slimeChunk.x - Minecraft.getMinecraft().getRenderManager().viewerPosX, -Minecraft.getMinecraft().getRenderManager().viewerPosY, slimeChunk.z - Minecraft.getMinecraft().getRenderManager().viewerPosZ, new AxisAlignedBB(0, 0, 0, 16, 1, 16), 2, 0xFF00FF00);
                }
            }
        }
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketChunkData) {
                final SPacketChunkData packet = (SPacketChunkData) event.getPacket();
                final SlimeChunk chunk = new SlimeChunk(packet.getChunkX() * 16, packet.getChunkZ() * 16);

                final ServerData serverData = Minecraft.getMinecraft().getCurrentServerData();
                if (serverData != null) {
                    final WorldManager.WorldData worldData = Seppuku.INSTANCE.getWorldManager().find(serverData.serverIP);
                    if (worldData != null) {
                        if (!this.slimeChunkList.contains(chunk) && this.isSlimeChunk(worldData.getSeed(), packet.getChunkX(), packet.getChunkZ())) {
                            this.slimeChunkList.add(chunk);
                        }
                    }
                }
            }
        }
    }

    private boolean isSlimeChunk(final long seed, int x, int z) {
        final Random rand = new Random(seed +
                (long) (x * x * 0x4c1906) +
                (long) (x * 0x5ac0db) +
                (long) (z * z) * 0x4307a7L +
                (long) (z * 0x5f24f) ^ 0x3ad8025f);
        return rand.nextInt(10) == 0;
    }

    public static class SlimeChunk {
        private int x;
        private int z;

        public SlimeChunk(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getZ() {
            return z;
        }

        public void setZ(int z) {
            this.z = z;
        }
    }


}
