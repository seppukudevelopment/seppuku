package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.render.EventRenderBlockModel;
import me.rigamortis.seppuku.api.event.render.EventRenderBlockSide;
import me.rigamortis.seppuku.api.event.world.EventSetOpaqueCube;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Author Seth
 * 4/9/2019 @ 12:58 PM.
 */
public final class XrayModule extends Module {

    public final Value<List<Block>> blocks = new Value<List<Block>>("Ids", new String[]{"id", "i"}, "Testing");
    public final Value<List<Item>> items = new Value<List<Item>>("Items", new String[]{"item"}, "Testing");

    private float lastGamma;
    private int lastAO;

    public XrayModule() {
        super("Xray", new String[]{"JadeVision", "Jade"}, "Allows you to filter what the world renders", "NONE", -1, ModuleType.RENDER);

        this.blocks.setValue(new ArrayList<>());
        this.items.setValue(new ArrayList<>());

        this.items.getValue().add(Items.WHEAT);

        if (Seppuku.INSTANCE.getConfigManager().isFirstLaunch())
            this.add("diamond_ore");
    }

    @Override
    public void onEnable() {
        super.onEnable();

        final Minecraft mc = Minecraft.getMinecraft();
        lastGamma = mc.gameSettings.gammaSetting;
        lastAO = mc.gameSettings.ambientOcclusion;

        mc.gameSettings.gammaSetting = 100;
        mc.gameSettings.ambientOcclusion = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();

        Minecraft.getMinecraft().gameSettings.gammaSetting = lastGamma;
        Minecraft.getMinecraft().gameSettings.ambientOcclusion = lastAO;
    }

    @Override
    public void onToggle() {
        super.onToggle();

        Minecraft.getMinecraft().renderGlobal.loadRenderers();
    }

    /**
     * Deprecated
     * @param event
     */
    @Listener
    public void shouldSideBeRendered(EventRenderBlockSide event) {
        /*if (this.contains(Block.getIdFromBlock(event.getBlock()))) {
            event.setRenderable(true);
        }
        event.setCanceled(true);*/
    }

    @Listener
    public void renderBlockModel(EventRenderBlockModel event) {
        final Block block = event.getBlockState().getBlock();
        if (this.contains(block)) {
            if (Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelFlat(event.getBlockAccess(), event.getBakedModel(), event.getBlockState(), event.getBlockPos(), event.getBufferBuilder(), event.isCheckSides(), event.getRand())) {
                event.setRenderable(true);
            }
        }
        event.setCanceled(true);
    }

    @Listener
    public void setOpaqueCube(EventSetOpaqueCube event) {
        event.setCanceled(true);
    }

    public void updateRenders() {
        //Minecraft.getMinecraft().renderGlobal.loadRenderers();
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
        return this.blocks.getValue().contains(block);
    }

    public void add(int id) {
        final Block blockFromID = Block.getBlockById(id);
        if (!contains(blockFromID)) {
            this.blocks.getValue().add(blockFromID);
        }
    }

    public void add(String name) {
        final Block blockFromName = Block.getBlockFromName(name);
        if (blockFromName != null) {
            if (!contains(blockFromName)) {
                this.blocks.getValue().add(blockFromName);
            }
        }
    }

    public void remove(int id) {
        for (Block block : this.blocks.getValue()) {
            final int blockID = Block.getIdFromBlock(block);
            if (blockID == id) {
                this.blocks.getValue().remove(block);
                break;
            }
        }
    }

    public void remove(String name) {
        final Block blockFromName = Block.getBlockFromName(name);
        if (blockFromName != null) {
            if (contains(blockFromName)) {
                this.blocks.getValue().remove(blockFromName);
            }
        }
    }

    public int clear() {
        final int count = this.blocks.getValue().size();
        this.blocks.getValue().clear();
        return count;
    }

    public Value<List<Block>> getBlocks() {
        return blocks;
    }
}
