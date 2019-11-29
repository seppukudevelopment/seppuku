package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.api.event.render.EventRenderBlockModel;
import me.rigamortis.seppuku.api.event.render.EventRenderBlockSide;
import me.rigamortis.seppuku.api.event.world.EventSetOpaqueCube;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Author Seth
 * 4/9/2019 @ 12:58 PM.
 */
public final class XrayModule extends Module {

    private List<Integer> ids = new ArrayList<>();

    private float lastGamma;
    private int lastAO;

    public XrayModule() {
        super("Xray", new String[]{"JadeVision", "Jade"}, "Allows you to filter what the world renders", "NONE", -1, ModuleType.RENDER);
        this.setHidden(true);
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

    @Listener
    public void shouldSideBeRendered(EventRenderBlockSide event) {
        if(this.contains(Block.getIdFromBlock(event.getBlock()))) {
            event.setRenderable(true);
        }
        event.setCanceled(true);
    }

    @Listener
    public void renderBlockModel(EventRenderBlockModel event) {
        final Block block = event.getBlockState().getBlock();
        if(this.contains(Block.getIdFromBlock(block))) {
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
                (int)mc.player.posX - 256,
                (int)mc.player.posY - 256,
                (int)mc.player.posZ - 256,
                (int)mc.player.posX + 256,
                (int)mc.player.posY + 256,
                (int)mc.player.posZ + 256);
    }

    public boolean contains(int id) {
        return this.ids.contains(id);
    }

    public void add(int id) {
        if(!contains(id)) {
            this.ids.add(id);
        }
    }

    public void add(String name) {
        final int id = Block.getIdFromBlock(Block.getBlockFromName(name));
        if(!contains(id)) {
            this.ids.add(id);
        }
    }

    public void remove(int id) {
        for(Integer i : this.ids) {
            if(id == i.intValue()) {
                this.ids.remove(i);
                break;
            }
        }
    }

    public void remove(String name) {
        final int id = Block.getIdFromBlock(Block.getBlockFromName(name));
        if(contains(id)) {
            this.ids.remove(id);
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
