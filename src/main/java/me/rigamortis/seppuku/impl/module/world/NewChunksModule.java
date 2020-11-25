package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.util.math.AxisAlignedBB;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Author Seth
 * 5/11/2019 @ 7:46 AM.
 */
public final class NewChunksModule extends Module {

    private ICamera frustum = new Frustum();

    private List<ChunkData> chunkDataList = new ArrayList<>();

    public NewChunksModule() {
        super("NewChunks", new String[]{"ChunkGen"}, "Highlights newly generated chunks", "NONE", -1, ModuleType.WORLD);
    }

    @Listener
    public void onToggle() {
        super.onToggle();
        this.chunkDataList.clear();
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketChunkData) {
                final SPacketChunkData packet = (SPacketChunkData) event.getPacket();
                if (!packet.isFullChunk()) {
                    final ChunkData chunk = new ChunkData(packet.getChunkX() * 16, packet.getChunkZ() * 16);

                    if (!this.chunkDataList.contains(chunk)) {
                        this.chunkDataList.add(chunk);
                    }
                }
            }
        }
    }

    @Listener
    public void render3D(EventRender3D event) {
        for (ChunkData chunkData : this.chunkDataList) {
            if (chunkData != null) {
                this.frustum.setPosition(Minecraft.getMinecraft().getRenderViewEntity().posX, Minecraft.getMinecraft().getRenderViewEntity().posY, Minecraft.getMinecraft().getRenderViewEntity().posZ);

                final AxisAlignedBB bb = new AxisAlignedBB(chunkData.x, 0, chunkData.z, chunkData.x + 16, 1, chunkData.z + 16);

                if (frustum.isBoundingBoxInFrustum(bb)) {
                    RenderUtil.drawPlane(chunkData.x - Minecraft.getMinecraft().getRenderManager().viewerPosX, -Minecraft.getMinecraft().getRenderManager().viewerPosY, chunkData.z - Minecraft.getMinecraft().getRenderManager().viewerPosZ, new AxisAlignedBB(0, 0, 0, 16, 1, 16), 1, 0xFF9900EE);
                }
            }
        }
    }

    public static class ChunkData {
        private int x;
        private int z;

        public ChunkData(int x, int z) {
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
