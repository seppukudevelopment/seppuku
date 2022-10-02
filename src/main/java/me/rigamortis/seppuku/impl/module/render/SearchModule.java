package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.gui.hud.EventUIValueChanged;
import me.rigamortis.seppuku.api.event.gui.hud.modulelist.EventUIListValueChanged;
import me.rigamortis.seppuku.api.event.player.EventDestroyBlock;
import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.event.render.EventRenderBlock;
import me.rigamortis.seppuku.api.event.world.EventChunk;
import me.rigamortis.seppuku.api.event.world.EventLoadWorld;
import me.rigamortis.seppuku.api.event.world.EventSetOpaqueCube;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.ColorUtil;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.util.Timer;
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
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.chunk.Chunk;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author noil
 * <p>
 * this will be worked on much more
 * just trying to get a smooth working version out
 */
public final class SearchModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"M", "type", "t"}, "Select which mode to draw the search visual", Mode.OUTLINE);
    public final Value<Integer> range = new Value<Integer>("Range", new String[]{"radius"}, "The range(m) to render search blocks", 128, 1, 512, 1);
    public final Value<Integer> limit = new Value<Integer>("Limit", new String[]{"max"}, "The maximum amount of blocks that can be rendered", 3000, 1, 9000, 1);
    public final Value<Integer> alpha = new Value<Integer>("Alpha", new String[]{"opacity"}, "Alpha value for the search bounding box", 127, 1, 255, 1);
    public final Value<Float> width = new Value<Float>("Width", new String[]{"size"}, "Line width of the search bounding box", 1.0f, 0.1f, 5.0f, 0.1f);
    public final Value<Float> renderTime = new Value<Float>("RenderDelay", new String[]{"rendelay", "delay", "rd"}, "Delay(ms) between render updates if chunks are queued", 4000.0f, 500f, 8000f, 100f);
    public final Value<Boolean> tracer = new Value<Boolean>("Tracer", new String[]{"trace", "line"}, "Draw a tracer line to each search result", false);
    private final Value<List<Block>> blockIds = new Value<>("Ids", new String[]{"id", "i", "blocks"}, "Blocks to search for");
    private final List<Vec3i> blocks = new CopyOnWriteArrayList<>();
    private final List<Chunk> renderQueue = new CopyOnWriteArrayList<>();
    private final Timer renderTimer = new Timer();
    private final ICamera frustum = new Frustum();
    public SearchModule() {
        super("Search", new String[]{"srch", "src"}, "Search for different types of blocks. Enter the \"search\" command", "NONE", -1, ModuleType.RENDER);

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
        if (mc.getRenderViewEntity() == null || mc.world == null || mc.player == null)
            return;

        this.frustum.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

        if (!this.renderQueue.isEmpty() && this.renderTimer.passed(this.renderTime.getValue())) {
            this.updateRenders();
            this.renderQueue.clear();
            this.renderTimer.reset();
        }

        RenderUtil.begin3D();
        for (Vec3i searchBlock : this.blocks) {
            final BlockPos blockPos = new BlockPos(searchBlock);
            final Block block = mc.world.getBlockState(blockPos).getBlock();

            if (block instanceof BlockAir)
                continue;

            if (MathUtil.getDistance(mc.player.posX, mc.player.posZ, searchBlock.getX(), searchBlock.getZ()) > this.range.getValue() || !this.contains(block)) {
                this.blocks.remove(searchBlock);
                continue;
            }

            if (this.contains(block)) {
                final AxisAlignedBB bb = this.boundingBoxForBlock(blockPos);

                if (this.frustum.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                        bb.minY + mc.getRenderManager().viewerPosY,
                        bb.minZ + mc.getRenderManager().viewerPosZ,
                        bb.maxX + mc.getRenderManager().viewerPosX,
                        bb.maxY + mc.getRenderManager().viewerPosY,
                        bb.maxZ + mc.getRenderManager().viewerPosZ))) {
                    final int color = ColorUtil.changeAlpha(this.getColor(blockPos, block), this.alpha.getValue());
                    switch (this.mode.getValue()) {
                        case BOX:
                            RenderUtil.drawFilledBox(bb, color);
                            break;
                        case OUTLINE:
                            RenderUtil.drawBoundingBox(bb, this.width.getValue(), color);
                            break;
                        case OUTLINE_BOX:
                            RenderUtil.drawFilledBox(bb, color);
                            RenderUtil.drawBoundingBox(bb, this.width.getValue(), color);
                            break;
                        case PLANE:
                            RenderUtil.drawPlane(
                                    searchBlock.getX() - mc.getRenderManager().viewerPosX,
                                    searchBlock.getY() - mc.getRenderManager().viewerPosY,
                                    searchBlock.getZ() - mc.getRenderManager().viewerPosZ,
                                    new AxisAlignedBB(0, 0, 0, 1, 1, 1),
                                    this.width.getValue(),
                                    color);
                            break;
                    }

                    if (this.tracer.getValue()) {
                        final Vec3d pos = new Vec3d(searchBlock.getX(), searchBlock.getY(), searchBlock.getZ()).subtract(mc.getRenderManager().renderPosX, mc.getRenderManager().renderPosY, mc.getRenderManager().renderPosZ);
                        final boolean bobbing = mc.gameSettings.viewBobbing;
                        mc.gameSettings.viewBobbing = false;
                        mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);
                        final Vec3d forward = new Vec3d(0, 0, 1).rotatePitch(-(float) Math.toRadians(Minecraft.getMinecraft().player.rotationPitch)).rotateYaw(-(float) Math.toRadians(Minecraft.getMinecraft().player.rotationYaw));
                        RenderUtil.drawLine3D(forward.x, forward.y + mc.player.getEyeHeight(), forward.z, pos.x, pos.y, pos.z, this.width.getValue(), color);
                        mc.gameSettings.viewBobbing = bobbing;
                        mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);
                    }
                }
            }
        }
        RenderUtil.end3D();
    }

    @Listener
    public void onRenderBlock(EventRenderBlock event) {
        final BlockPos pos = event.getPos();
        final IBlockState blockState = event.getState();
        if (this.contains(blockState.getBlock())) {
            final Vec3i vec3i = new Vec3i(pos.getX(), pos.getY(), pos.getZ());
            final double dist = MathUtil.getDistance(Minecraft.getMinecraft().player.posX, Minecraft.getMinecraft().player.posZ, pos.getX(), pos.getZ());
            final boolean inRange = dist < this.range.getValue();
            if (inRange && !this.isPosCached(pos.getX(), pos.getY(), pos.getZ()) && this.blocks.size() < this.limit.getValue()) {
                this.blocks.add(vec3i);
            } else if (this.isPosCached(pos) && !inRange) {
                this.blocks.remove(vec3i);
            }
        }
    }

    @Listener
    public void setOpaqueCube(EventSetOpaqueCube event) {
        event.setCanceled(true);
    }

    @Listener
    public void onChunk(EventChunk event) {
        if (event.getType().equals(EventChunk.ChunkType.LOAD)) {
            if (this.renderQueue.size() > 32) {
                this.renderQueue.clear();
            }
            this.renderQueue.add(event.getChunk());
        }
    }

    @Listener
    public void onUIListValueChanged(EventUIListValueChanged event) {
        if (event.getValue().getName().equals(this.blockIds.getName())) {
            this.updateRenders();
        }
    }

    @Listener
    public void onUIValueChanged(EventUIValueChanged event) {
        for (Value value : this.getValueList()) {
            if (value.getName().equals(event.getValue().getName())) {
                this.updateRenders();
            }
        }
    }

    private void removeBlock(BlockPos pos) {
        this.blocks.removeIf(vec3i -> vec3i.getX() == pos.getX() && vec3i.getY() == pos.getY() && vec3i.getZ() == pos.getZ());
    }

    public void clearBlocks() {
        this.blocks.clear();
    }

    private boolean isPosCached(int x, int y, int z) {
        for (Vec3i vector : this.blocks) {
            if (vector.getX() == x && vector.getY() == y && vector.getZ() == z) {
                return true;
            }
        }
        return false;
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
                (int) mc.player.posX - this.range.getValue(),
                0,
                (int) mc.player.posZ - this.range.getValue(),
                (int) mc.player.posX + this.range.getValue(),
                256,
                (int) mc.player.posZ + this.range.getValue());
    }

    public void updateRenders(Chunk chunk) {
        //mc.renderGlobal.loadRenderers();
        final Minecraft mc = Minecraft.getMinecraft();
        //System.out.println(chunk.x * 16);
        mc.renderGlobal.markBlockRangeForRenderUpdate(
                chunk.x * 16,
                0,
                chunk.z * 16,
                (chunk.x * 16) + 16,
                256,
                (chunk.z * 16) + 16);
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
            return 0x624FFF;
        else if (block == Blocks.CRAFTING_TABLE)
            return 0xFFC853;
        else if (block == Blocks.FURNACE || block == Blocks.LIT_FURNACE)
            return 0x76A7B4;
        else if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA)
            return 0xFF8639;
        else if (block == Blocks.WATER || block == Blocks.FLOWING_WATER)
            return 0x4CD1FF;
        else if (block == Blocks.ANVIL)
            return 0xB6BA9E;
        else if (block == Blocks.DISPENSER || block == Blocks.DROPPER)
            return 0xD3E2CA;
        else if (block == Blocks.CAULDRON || block == Blocks.PORTAL)
            return 0x9E97BF;
        else if (block == Blocks.DIAMOND_ORE || block == Blocks.DIAMOND_BLOCK)
            return 0x27E6FF;
        else if (block == Blocks.EMERALD_ORE || block == Blocks.EMERALD_BLOCK)
            return 0x40FF33;
        else if (block == Blocks.COAL_ORE || block == Blocks.COAL_BLOCK)
            return 0x817B6F;
        else if (block == Blocks.IRON_ORE || block == Blocks.IRON_BLOCK)
            return 0xF6F6F6;
        else if (block == Blocks.GOLD_ORE || block == Blocks.GOLD_BLOCK)
            return 0xFFFD33;
        else if (block == Blocks.LAPIS_ORE || block == Blocks.LAPIS_BLOCK)
            return 0x2A4DFF;
        else if (block == Blocks.LIT_REDSTONE_ORE || block == Blocks.REDSTONE_BLOCK)
            return 0xFF0816;
        else if (block == Blocks.REDSTONE_ORE)
            return 0xCB0713;
        else if (block == Blocks.QUARTZ_ORE || block == Blocks.QUARTZ_BLOCK)
            return 0xFFF2B7;

        final int mapColor = Minecraft.getMinecraft().world.getBlockState(pos).getMaterial().getMaterialMapColor().colorValue;
        if (mapColor > 0)
            return mapColor;

        return 0xFFFFFF;
    }

    public Value<List<Block>> getBlockIds() {
        return blockIds;
    }

    public enum Mode {
        BOX, OUTLINE, OUTLINE_BOX, PLANE
    }
}
