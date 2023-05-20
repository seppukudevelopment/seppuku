package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.awt.*;

/**
 * @author Old Chum
 * @since 5/20/2023
 */
public class MapBoundsModule extends Module {
    public Value<Boolean> throughWalls = new Value<Boolean>("ThroughWalls", new String[]{"Walls", "Wall", "w", "Through", "Thru"}, "If rendering should go through walls", false);
    public Value<Color> outlineColor = new Value<Color>("OutlineColor", new String[]{"OC", "OutC", "OColor", "Outline"}, "The color the outline should be", new Color(75, 25, 255, 255));
    public Value<Color> gridColor = new Value<Color>("GridColor", new String[]{"GC", "GridC", "GColor", "Grid"}, "The color the grid should be", new Color(5, 155, 0, 255));

    public MapBoundsModule() {
        super("MapBounds", new String[]{"MBounds", "ShowMaps", "MapBoundaries"},  "Shows the boundaries of the map you are currently standing in.", -1, ModuleType.RENDER);
    }

    @Listener
    public void onRender3d (EventRender3D event) {
        Minecraft mc = Minecraft.getMinecraft();
        double minX = (int) Math.floor((mc.player.posX + 64) / 128) * 128 - 64;
        double minZ = ((int) Math.floor((mc.player.posZ + 64) / 128) * 128 - 64);

        AxisAlignedBB bb = new AxisAlignedBB(minX, 0, minZ, minX + 127, 255, minZ + 127);

        RenderUtil.begin3D();

        // begin3D() disables depth
        if (!this.throughWalls.getValue()) {
            GlStateManager.enableDepth();
        }

        for (EnumFacing face : EnumFacing.HORIZONTALS) {
            RenderUtil.renderFaceMesh(interpolateBB(bb, event.getPartialTicks()), face, 8.0D, 1, gridColor.getValue().getRGB());
        }

        RenderUtil.drawBoundingBox(interpolateBB(bb, event.getPartialTicks()), 2, outlineColor.getValue().getRGB());
        RenderUtil.end3D();
    }

    public static AxisAlignedBB interpolateBB (AxisAlignedBB bb, float partialTicks) {
        EntityPlayer entityplayer = Minecraft.getMinecraft().player;
        double ix = entityplayer.lastTickPosX + (entityplayer.posX - entityplayer.lastTickPosX) * (double)partialTicks;
        double iy = entityplayer.lastTickPosY + (entityplayer.posY - entityplayer.lastTickPosY) * (double)partialTicks;
        double iz = entityplayer.lastTickPosZ + (entityplayer.posZ - entityplayer.lastTickPosZ) * (double)partialTicks;

        return new AxisAlignedBB(
                bb.minX - ix,
                bb.minY - iy,
                bb.minZ - iz,
                bb.maxX - ix + 1,
                bb.maxY - iy + 1,
                bb.maxZ - iz + 1
        );
    }
}
