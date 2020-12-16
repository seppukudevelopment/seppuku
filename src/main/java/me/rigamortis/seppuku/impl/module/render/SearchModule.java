package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.event.render.EventRenderBlockModel;
import me.rigamortis.seppuku.api.event.world.EventLoadWorld;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.ColorUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.Block;
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
    public final Value<Integer> range = new Value<Integer>("Range", new String[]{"radius"}, "The range(m) to render search blocks.", 128, 0, 512, 1);
    public final Value<Integer> limit = new Value<Integer>("Limit", new String[]{"max"}, "The maximum amount of blocks that can be rendered.", 3000, 0, 9000, 1);
    public final Value<Integer> alpha = new Value<Integer>("Alpha", new String[]{"opacity"}, "Alpha value for the search bounding box.", 127, 0, 255, 1);
    public final Value<Float> width = new Value<Float>("Width", new String[]{"size"}, "Line width of the search bounding box.", 1.0f, 0.0f, 5.0f, 0.1f);

    public enum Mode {
        BOX, OUTLINE, OUTLINE_BOX, PLANE
    }

    private List<Integer> ids = new ArrayList<>();
    private final List<Vec3d> blocks = new ArrayList<>(512);
    private final ICamera frustum = new Frustum();

    public SearchModule() {
        super("Search", new String[]{"srch", "find", "locate"}, "Search for different types of blocks. Enter the \"search\" command.", "NONE", -1, ModuleType.RENDER);

        if (Seppuku.INSTANCE.getConfigManager().isFirstLaunch())
            this.add("furnace");
    }

    @Listener
    public void onLoadWorld(EventLoadWorld event) {
        if (event.getWorld() != null) {
            this.blocks.clear();
        }
    }

    @Listener
    public void onDrawWorld(EventRender3D event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.getRenderViewEntity() == null)
            return;

        RenderUtil.begin3D();
        for (int i = this.blocks.size() - 1; i >= 0; i--) {
            Vec3d searchBlock = this.blocks.get(i);
            BlockPos blockPos = new BlockPos(searchBlock.x, searchBlock.y, searchBlock.z);
            Block block = mc.world.getBlockState(blockPos).getBlock();
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
        if (this.contains(Block.getIdFromBlock(blockState.getBlock())) && !this.isPosCached(pos.getX(), pos.getY(), pos.getZ()) && this.blocks.size() < this.limit.getValue()) {
            this.blocks.add(new Vec3d(pos));
        }
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

    private boolean isPosCached(int x, int y, int z) {
        boolean temp = false;
        for (int i = this.blocks.size() - 1; i >= 0; i--) {
            Vec3d searchBlock = this.blocks.get(i);
            if (searchBlock.x == x && searchBlock.y == y && searchBlock.z == z)
                temp = true;
        }
        return temp;
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

    public boolean contains(int id) {
        return this.ids.contains(id);
    }

    public boolean contains(String localizedName) {
        final Block blockFromName = Block.getBlockFromName(localizedName);
        if (blockFromName != null) {
            return contains(Block.getIdFromBlock(blockFromName));
        }
        return false;
    }

    public void add(int id) {
        if (!contains(id)) {
            this.ids.add(id);
        }
    }

    public void add(String name) {
        final Block blockFromName = Block.getBlockFromName(name);
        if (blockFromName != null) {
            final int id = Block.getIdFromBlock(blockFromName);
            if (!contains(id)) {
                this.ids.add(id);
            }
        }
    }

    public void remove(int id) {
        for (Integer i : this.ids) {
            if (id == i) {
                this.ids.remove(i);
                break;
            }
        }
    }

    public void remove(String name) {
        final Block blockFromName = Block.getBlockFromName(name);
        if (blockFromName != null) {
            final int id = Block.getIdFromBlock(blockFromName);
            if (contains(id)) {
                this.ids.remove(id);
            }
        }
    }

    public int clear() {
        final int count = this.ids.size();
        this.ids.clear();
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

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }
}
