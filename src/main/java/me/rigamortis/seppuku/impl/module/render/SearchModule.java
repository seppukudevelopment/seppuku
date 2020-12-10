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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
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

    private List<Integer> ids = new ArrayList<>();
    private List<Vec3d> blocks = new ArrayList<>(512);

    public final Value<Integer> range = new Value<Integer>("Range", new String[]{"radius"}, "The range(m) to render search blocks.", 128, 0, 512, 1);
    public final Value<Integer> limit = new Value<Integer>("Limit", new String[]{"max"}, "The maximum amount of blocks that can be rendered.", 3000, 0, 9000, 1);
    public final Value<Integer> alpha = new Value<Integer>("Alpha", new String[]{"opacity"}, "Alpha value for the search bounding box.", 127, 0, 255, 1);
    public final Value<Float> width = new Value<Float>("Width", new String[]{"size"}, "Line width of the search bounding box.", 1.0f, 0.0f, 5.0f, 0.1f);

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

        RenderUtil.begin3D();
        for (int i = this.blocks.size() - 1; i >= 0; i--) {
            Vec3d searchBlock = this.blocks.get(i);
            BlockPos blockPos = new BlockPos(searchBlock.x, searchBlock.y, searchBlock.z);
            Block block = mc.world.getBlockState(blockPos).getBlock();
            if (this.contains(block.getLocalizedName()) && mc.player.getDistance(searchBlock.x, searchBlock.y, searchBlock.z) < this.range.getValue()) {
                final AxisAlignedBB bb = this.boundingBoxForBlock(blockPos);
                RenderUtil.drawBoundingBox(bb, this.width.getValue(), ColorUtil.changeAlpha(mc.world.getBlockState(blockPos).getMaterial().getMaterialMapColor().colorValue, this.alpha.getValue()));
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
        if (this.contains(blockState.getBlock().getLocalizedName()) && !this.isPosCached(pos.getX(), pos.getY(), pos.getZ()) && this.blocks.size() < this.limit.getValue()) {
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
        for (int i = this.blocks.size() - 1; i >= 0; i--) {
            Vec3d searchBlock = this.blocks.get(i);
            if (searchBlock.x == x && searchBlock.y == y && searchBlock.z == z)
                return true;
        }
        return false;
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

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }
}
