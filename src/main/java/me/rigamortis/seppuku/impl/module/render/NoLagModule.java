package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.event.render.EventRenderBlockModel;
import me.rigamortis.seppuku.api.event.world.EventLightUpdate;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.BlockPistonMoving;
import net.minecraft.client.Minecraft;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/11/2019 @ 3:48 AM.
 */
public final class NoLagModule extends Module {

    public final Value<Boolean> light = new Value<Boolean>("Light", new String[]{"Lit"}, "Choose to enable the lighting lag fix. Disables lighting updates.", true);
    public final Value<Boolean> signs = new Value<Boolean>("Signs", new String[]{"Sign"}, "Choose to enable the sign lag fix. Disables the rendering of sign text.", false);
    public final Value<Boolean> sounds = new Value<Boolean>("Sounds", new String[]{"Sound"}, "Choose to enable the sound lag fix. Disable entity swap-item/equip sound.", true);
    public final Value<Boolean> pistons = new Value<Boolean>("Pistons", new String[]{"Piston"}, "Choose to enable the piston lag fix. Disables pistons from rendering.", false);
    public final Value<Boolean> slimes = new Value<Boolean>("Slimes", new String[]{"Slime"}, "Choose to enable the slime lag fix. Disables slimes from spawning.", false);

    //TODO slimes, names, items, sounds
    public NoLagModule() {
        super("NoLag", new String[]{"AntiLag"}, "Fixes malicious lag exploits and bugs that cause lag", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void recievePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (this.slimes.getValue()) {
                if (event.getPacket() instanceof SPacketSpawnMob) {
                    final SPacketSpawnMob packet = (SPacketSpawnMob) event.getPacket();
                    if (packet.getEntityType() == 55) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @Listener
    public void updateLighting(EventLightUpdate event) {
        if (this.light.getValue()) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void renderBlockModel(EventRenderBlockModel event) {
        if (this.pistons.getValue()) {
            final Block block = event.getBlockState().getBlock();
            if (block instanceof BlockPistonMoving || block instanceof BlockPistonExtension) {
                event.setRenderable(false);
                event.setCanceled(true);
            }
        }
    }


    @Listener
    public void renderWorld(EventRender3D event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (this.signs.getValue()) {
            for (TileEntity te : mc.world.loadedTileEntityList) {
                if (te instanceof TileEntitySign) {
                    final TileEntitySign sign = (TileEntitySign) te;
                    sign.signText = new ITextComponent[]{new TextComponentString(""), new TextComponentString(""), new TextComponentString(""), new TextComponentString("")};
                }
            }
        }
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketSoundEffect) {
                if (this.sounds.getValue()) {
                    final SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();
                    if (packet.getCategory() == SoundCategory.PLAYERS && packet.getSound() == SoundEvents.ITEM_ARMOR_EQUIP_GENERIC) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

}
