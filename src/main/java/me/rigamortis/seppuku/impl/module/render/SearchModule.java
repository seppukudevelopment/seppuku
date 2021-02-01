package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.gui.hud.modulelist.EventUIListValueChanged;
import me.rigamortis.seppuku.api.event.player.EventDestroyBlock;
import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.event.render.EventRenderBlockModel;
import me.rigamortis.seppuku.api.event.world.EventLoadWorld;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.ColorUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author noil
 * <p>
 * this will be worked on much more
 * just trying to get a smooth working version out
 */
public final class SearchModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"M", "type", "t"}, "Select which mode to draw the search visual.", Mode.OUTLINE);
    public final Value<Integer> range = new Value<Integer>("Range", new String[]{"radius"}, "The range(m) to render search blocks.", 128, 1, 512, 1);
    public final Value<Integer> limit = new Value<Integer>("Limit", new String[]{"max"}, "The maximum amount of blocks that can be rendered.", 3000, 1, 9000, 1);
    public final Value<Integer> alpha = new Value<Integer>("Alpha", new String[]{"opacity"}, "Alpha value for the search bounding box.", 127, 1, 255, 1);
    public final Value<Float> width = new Value<Float>("Width", new String[]{"size"}, "Line width of the search bounding box.", 1.0f, 0.1f, 5.0f, 0.1f);
    public final Value<Boolean> tracer = new Value<Boolean>("Tracer", new String[]{"trace", "line"}, "Draw a tracer line to each search result.", false);

    public enum Mode {
        BOX, OUTLINE, OUTLINE_BOX, PLANE
    }

    private final Value<List<Block>> blockIds = new Value<>("Ids", new String[]{"id", "i", "blocks"}, "Blocks to search for.");
    private final List<Vec3d> blocks = new ArrayList<>(512);
    private final ICamera frustum = new Frustum();

    public SearchModule() {
        super("Search", new String[]{"srch", "xray"}, "Search for different types of blocks. Enter the \"search\" command.", "NONE", -1, ModuleType.RENDER);

        this.blockIds.setValue(new ArrayList<>());

        if (Seppuku.INSTANCE.getConfigManager().isFirstLaunch())
            this.add("furnace");
    }

    @Override
    public void onEnable() {
        this.blocks.clear();
        super.onEnable();
    }

    @Override
    public void onToggle() {
        super.onToggle();
        Minecraft.getMinecraft().renderGlobal.loadRenderers();
    }

    @Listener
    public void onLoadWorld(EventLoadWorld event) {
        if (event.getWorld() != null) {
            this.blocks.clear();
        }
    }

    @Listener
    public void onDestroyBlock(EventDestroyBlock event) {
        if (event.getPos() != null) {
            if (this.isPosCached(event.getPos())) {
                this.removeBlock(event.getPos());
            }
        }
    }

    @Listener
    public void onDrawWorld(EventRender3D event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.getRenderViewEntity() == null)
            return;

        RenderUtil.begin3D();
        for (int i = this.blocks.size() - 1; i >= 0; i--) {
            final Vec3d searchBlock = this.blocks.get(i);
            if (searchBlock == null)
                continue;

            final BlockPos blockPos = new BlockPos(searchBlock.x, searchBlock.y, searchBlock.z);
            final Block block = mc.world.getBlockState(blockPos).getBlock();

            if (block instanceof BlockAir)
                continue;

            if (mc.player.getDistance(searchBlock.x, searchBlock.y, searchBlock.z) < this.range.getValue()) {
                final AxisAlignedBB bb = this.boundingBoxForBlock(blockPos);

                this.frustum.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

                if (this.frustum.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                        bb.minY + mc.getRenderManager().viewerPosY,
                        bb.minZ + mc.getRenderManager().viewerPosZ,
                        bb.maxX + mc.getRenderManager().viewerPosX,
                        bb.maxY + mc.getRenderManager().viewerPosY,
                        bb.maxZ + mc.getRenderManager().viewerPosZ))) {
                    final int color = ColorUtil.changeAlpha(this.getColor(blockPos, block), this.alpha.getValue());
                    switch (this.mode.getValue()) {
                        case BOX:
                            RenderUtil.drawFilledBox(bb, ColorUtil.changeAlpha(color, this.alpha.getValue()));
                            break;
                        case OUTLINE:
                            RenderUtil.drawBoundingBox(bb, this.width.getValue(), color);
                            break;
                        case OUTLINE_BOX:
                            RenderUtil.drawFilledBox(bb, ColorUtil.changeAlpha(color, this.alpha.getValue()));
                            RenderUtil.drawBoundingBox(bb, this.width.getValue(), color);
                            break;
                        case PLANE:
                            RenderUtil.drawPlane(
                                    searchBlock.x - mc.getRenderManager().viewerPosX,
                                    searchBlock.y - mc.getRenderManager().viewerPosY,
                                    searchBlock.z - mc.getRenderManager().viewerPosZ,
                                    new AxisAlignedBB(0, 0, 0, 1, 1, 1),
                                    this.width.getValue(),
                                    color);
                            break;
                    }

                    if (this.tracer.getValue()) {
                        final Vec3d pos = new Vec3d(searchBlock.x, searchBlock.y, searchBlock.z).subtract(mc.getRenderManager().renderPosX, mc.getRenderManager().renderPosY, mc.getRenderManager().renderPosZ);
                        final boolean bobbing = mc.gameSettings.viewBobbing;
                        mc.gameSettings.viewBobbing = false;
                        mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);
                        final Vec3d forward = new Vec3d(0, 0, 1).rotatePitch(-(float) Math.toRadians(Minecraft.getMinecraft().player.rotationPitch)).rotateYaw(-(float) Math.toRadians(Minecraft.getMinecraft().player.rotationYaw));
                        RenderUtil.drawLine3D(forward.x, forward.y + mc.player.getEyeHeight(), forward.z, pos.x, pos.y, pos.z, this.width.getValue(), color);
                        mc.gameSettings.viewBobbing = bobbing;
                        mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);
                    }
                }
            } else {
                this.blocks.remove(searchBlock);
            }
        }
        RenderUtil.end3D();
    }

    @Listener
    public void onRenderBlock(EventRenderBlockModel event) {
        final BlockPos pos = event.getBlockPos();
        final IBlockState blockState = event.getBlockState();
        if (this.contains(blockState.getBlock()) && !this.isPosCached(pos.getX(), pos.getY(), pos.getZ()) && this.blocks.size() < this.limit.getValue()) {
            this.blocks.add(new Vec3d(pos));
        } else if (!this.contains(blockState.getBlock()) && this.isPosCached(pos.getX(), pos.getY(), pos.getZ())) {
            this.blocks.remove(new Vec3d(pos));
        }
    }

    @Listener
    public void onUIListValueChanged(EventUIListValueChanged event) {
        this.updateRenders();
    }

    private void removeBlock(BlockPos pos) {
        for (int i = this.blocks.size() - 1; i >= 0; i--) {
            final Vec3d searchBlock = this.blocks.get(i);
            if (searchBlock.x == pos.getX() && searchBlock.y == pos.getY() && searchBlock.z == pos.getZ()) {
                this.blocks.remove(i);
            }
        }
    }

    public void clearBlocks() {
        this.blocks.clear();
    }

    private boolean isPosCached(int x, int y, int z) {
        boolean temp = false;
        for (int i = this.blocks.size() - 1; i >= 0; i--) {
            Vec3d searchBlock = this.blocks.get(i);
            if (searchBlock.x == x && searchBlock.y == y && searchBlock.z == z)
                temp = true;
        }
        return temp;
    }

    private boolean isPosCached(BlockPos pos) {
        return this.isPosCached(pos.getX(), pos.getY(), pos.getZ());
    }

    private AxisAlignedBB boundingBoxForBlock(BlockPos blockPos) {
        final Minecraft mc = Minecraft.getMinecraft();
        return new AxisAlignedBB(
                blockPos.getX() - mc.getRenderManager().viewerPosX,
                blockPos.getY() - mc.getRenderManager().viewerPosY,
                blockPos.getZ() - mc.getRenderManager().viewerPosZ,

                blockPos.getX() + 1 - mc.getRenderManager().viewerPosX,
                blockPos.getY() + 1 - mc.getRenderManager().viewerPosY,
                blockPos.getZ() + 1 - mc.getRenderManager().viewerPosZ);
    }

    public void updateRenders() {
        //mc.renderGlobal.loadRenderers();
        final Minecraft mc = Minecraft.getMinecraft();
        mc.renderGlobal.markBlockRangeForRenderUpdate(
                (int) mc.player.posX - 256,
                (int) mc.player.posY - 256,
                (int) mc.player.posZ - 256,
                (int) mc.player.posX + 256,
                (int) mc.player.posY + 256,
                (int) mc.player.posZ + 256);
    }

    public boolean contains(Block block) {
        return this.blockIds.getValue().contains(block);
    }

    public void add(int id) {
        final Block blockFromID = Block.getBlockById(id);
        if (!contains(blockFromID)) {
            this.blockIds.getValue().add(blockFromID);
        }
    }

    public void add(String name) {
        final Block blockFromName = Block.getBlockFromName(name);
        if (blockFromName != null) {
            if (!contains(blockFromName)) {
                this.blockIds.getValue().add(blockFromName);
            }
        }
    }

    public void remove(int id) {
        for (Block block : this.blockIds.getValue()) {
            final int blockID = Block.getIdFromBlock(block);
            if (blockID == id) {
                this.blockIds.getValue().remove(block);
                break;
            }
        }
    }

    public void remove(String name) {
        final Block blockFromName = Block.getBlockFromName(name);
        if (blockFromName != null) {
            if (contains(blockFromName)) {
                this.blockIds.getValue().remove(blockFromName);
            }
        }
    }

    public int clear() {
        final int count = this.blockIds.getValue().size();
        this.blockIds.getValue().clear();
        return count;
    }

    private int getColor(final BlockPos pos, final Block block) {
        if (block instanceof BlockEnderChest)
            return 0xFF624FFF;
        else if (block == Blocks.CRAFTING_TABLE)
            return 0xFFFFC853;
        else if (block == Blocks.FURNACE || block == Blocks.LIT_FURNACE)
            return 0xFF76A7B4;
        else if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA)
            return 0xFFFF8639;
        else if (block == Blocks.WATER || block == Blocks.FLOWING_WATER)
            return 0xFF4CD1FF;
        else if (block == Blocks.ANVIL)
            return 0xFFB6BA9E;
        else if (block == Blocks.DISPENSER || block == Blocks.DROPPER)
            return 0xFFD3E2CA;
        else if (block == Blocks.CAULDRON || block == Blocks.PORTAL)
            return 0xFF9E97BF;

        final int mapColor = Minecraft.getMinecraft().world.getBlockState(pos).getMaterial().getMaterialMapColor().colorValue;
        if (mapColor > 0)
            return mapColor;

        return 0xFFFFFFFF;
    }

    public Value<List<Block>> getBlockIds() {
        return blockIds;
    }
}
