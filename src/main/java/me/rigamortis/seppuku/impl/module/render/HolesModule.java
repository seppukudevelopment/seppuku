package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/**
 * created by noil on 10/4/2019 at 6:05 PM
 */
public final class HolesModule extends Module {

    public final Value<Integer> radius = new Value<Integer>("Radius", new String[]{"Radius", "Range", "Distance"}, "Radius in blocks to scan for holes.", 8, 0, 32, 1);
    public final Value<Boolean> fade = new Value<Boolean>("Fade", new String[]{"f"}, "Fades the opacity of the hole the closer your player is to it when enabled.", true);

    public final List<Hole> holes = new ArrayList<>();

    private ICamera camera = new Frustum();

    public HolesModule() {
        super("Holes", new String[]{"Hole", "HoleESP"}, "Shows areas the player could fall into, holes.", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.player == null)
            return;

        this.holes.clear();

        final Vec3i playerPos = new Vec3i(mc.player.posX, mc.player.posY, mc.player.posZ);

        for (int x = playerPos.getX() - radius.getValue(); x < playerPos.getX() + radius.getValue(); x++) {
            for (int z = playerPos.getZ() - radius.getValue(); z < playerPos.getZ() + radius.getValue(); z++) {
                for (int y = playerPos.getY(); y > playerPos.getY() - 4; y--) {
                    final BlockPos blockPos = new BlockPos(x, y, z);
                    final IBlockState blockState = mc.world.getBlockState(blockPos);
                    if (this.isBlockValid(blockState, blockPos)) {
                        final IBlockState downBlockState = mc.world.getBlockState(blockPos.down());
                        if (downBlockState.getBlock() == Blocks.AIR) {
                            final BlockPos downPos = blockPos.down();
                            if (this.isBlockValid(downBlockState, downPos)) {
                                this.holes.add(new Hole(downPos.getX(), downPos.getY(), downPos.getZ(), true));
                            }
                        } else {
                            this.holes.add(new Hole(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                        }
                    }
                }
            }
        }
    }

    @Listener
    public void onRender(EventRender3D event) {
        final Minecraft mc = Minecraft.getMinecraft();

        for (Hole hole : this.holes) {
            final AxisAlignedBB bb = new AxisAlignedBB(
                    hole.getX() - mc.getRenderManager().viewerPosX,
                    hole.getY() - mc.getRenderManager().viewerPosY,
                    hole.getZ() - mc.getRenderManager().viewerPosZ,
                    hole.getX() + 1 - mc.getRenderManager().viewerPosX,
                    hole.getY() + (hole.isTall() ? 2 : 1) - mc.getRenderManager().viewerPosY,
                    hole.getZ() + 1 - mc.getRenderManager().viewerPosZ);

            camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

            if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                    bb.minY + mc.getRenderManager().viewerPosY,
                    bb.minZ + mc.getRenderManager().viewerPosZ,
                    bb.maxX + mc.getRenderManager().viewerPosX,
                    bb.maxY + mc.getRenderManager().viewerPosY,
                    bb.maxZ + mc.getRenderManager().viewerPosZ))) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.disableDepth();
                GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                glEnable(GL_LINE_SMOOTH);
                glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
                glLineWidth(1.5f);

                final double dist = mc.player.getDistance(hole.getX() + 0.5f, hole.getY() + 0.5f, hole.getZ() + 0.5f) * 0.75f;

                float alpha = MathUtil.clamp((float) (dist * 255.0f / (this.radius.getValue()) / 255.0f), 0.0f, 0.3f);

                RenderGlobal.renderFilledBox(bb, 0, 1, 0, this.fade.getValue() ? alpha : 0.25f);
                RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, 0, 1, 0, this.fade.getValue() ? alpha : 0.25f);
                glDisable(GL_LINE_SMOOTH);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    }

    private boolean isBlockValid(IBlockState blockState, BlockPos blockPos) {
        if (this.holes.contains(blockPos))
            return false;

        if (blockState.getBlock() != Blocks.AIR)
            return false;

        if (Minecraft.getMinecraft().player.getDistanceSq(blockPos) < 1)
            return false;

        if (Minecraft.getMinecraft().world.getBlockState(blockPos.up()).getBlock() != Blocks.AIR)
            return false;

        if (Minecraft.getMinecraft().world.getBlockState(blockPos.up(2)).getBlock() != Blocks.AIR) // ensure the area is tall enough for the player
            return false;

        final BlockPos[] touchingBlocks = new BlockPos[]{
                blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west()
        };

        int validHorizontalBlocks = 0;
        for (BlockPos touching : touchingBlocks) {
            final IBlockState touchingState = Minecraft.getMinecraft().world.getBlockState(touching);
            if ((touchingState.getBlock() != Blocks.AIR) && touchingState.isFullBlock()) {
                validHorizontalBlocks++;
            }
        }

        if (validHorizontalBlocks < 4)
            return false;

        return true;
    }

    private class Hole extends Vec3i {

        private boolean tall;

        Hole(int x, int y, int z) {
            super(x, y, z);
        }

        Hole(int x, int y, int z, boolean tall) {
            super(x, y, z);
            this.tall = true;
        }

        public boolean isTall() {
            return tall;
        }

        public void setTall(boolean tall) {
            this.tall = tall;
        }
    }
}