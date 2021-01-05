package me.rigamortis.seppuku.impl.module.player;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.event.world.EventLoadWorld;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import me.rigamortis.seppuku.impl.module.render.BlockHighlightModule;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author noil
 */
public final class ReachModule extends Module {

    public final Value<Float> distance = new Value<Float>("Distance", new String[]{"Dist", "D"}, "The distance (in blocks) to reach.", 5.0f, 0.0f, 10.0f, 0.5f);
    public final Value<Boolean> highlight = new Value<Boolean>("Highlight", new String[]{"Hover", "H"}, "Enables rendering the BlockHighlight for the extended reach.", true);
    public final Value<Boolean> blocks = new Value<Boolean>("Blocks", new String[]{"Block", "B"}, "Enables reaching for breaking & building blocks.", true);
    //public final Value<Boolean> entities = new Value<Boolean>("Entities", new String[]{"Entity", "Entitie", "E"}, "Enables reaching for attacking and interacting with entities.", false);

    private BlockHighlightModule blockHighlightModule = null;

    private RayTraceResult currentBlockTrace = null;

    public ReachModule() {
        super("Reach", new String[]{"Rch"}, "Extends the player's reach.", "NONE", -1, ModuleType.PLAYER);
    }

    @Listener
    public void onLoadWorld(EventLoadWorld event) {
        if (event.getWorld() == null)
            return;

        this.blockHighlightModule = (BlockHighlightModule) Seppuku.INSTANCE.getModuleManager().find(BlockHighlightModule.class);
    }

    @Listener
    public void onRender3D(EventRender3D event) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.player == null)
            return;

        if (mc.objectMouseOver == null)
            return;

        if (!mc.objectMouseOver.typeOfHit.equals(RayTraceResult.Type.MISS)) {
            this.currentBlockTrace = null;
            return;
        }

        if (this.blocks.getValue()) {
            this.currentBlockTrace = mc.player.rayTrace(this.distance.getValue(), event.getPartialTicks());

            if (!this.highlight.getValue())
                return;

            if (this.blockHighlightModule != null && this.currentBlockTrace != null) {
                if (this.blockHighlightModule.isEnabled()) {
                    this.blockHighlightModule.drawHighlight(this.currentBlockTrace, mc);
                }
            }
        }
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() != EventStageable.EventStage.PRE)
            return;

        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null)
            return;

        if (this.blocks.getValue()) {
            if (this.currentBlockTrace == null)
                return;

            if (this.currentBlockTrace.typeOfHit == RayTraceResult.Type.BLOCK) {
                if (mc.gameSettings.keyBindAttack.pressed) {
                    if (!mc.world.isAirBlock(this.currentBlockTrace.getBlockPos())) {
                        if (mc.player.capabilities.isCreativeMode) {
                            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, this.currentBlockTrace.getBlockPos(), this.currentBlockTrace.sideHit));
                            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, this.currentBlockTrace.getBlockPos(), this.currentBlockTrace.sideHit));
                            mc.playerController.onPlayerDestroyBlock(this.currentBlockTrace.getBlockPos());
                            mc.world.setBlockToAir(this.currentBlockTrace.getBlockPos());
                        } else {
                            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, this.currentBlockTrace.getBlockPos(), this.currentBlockTrace.sideHit));
                            mc.playerController.onPlayerDamageBlock(this.currentBlockTrace.getBlockPos(), this.currentBlockTrace.sideHit);
                        }
                        mc.player.swingArm(EnumHand.MAIN_HAND);
                    }
                } else if (mc.gameSettings.keyBindUseItem.pressed && mc.player.ticksExisted % 3 == 0) {
                    final EnumActionResult actionResult = mc.playerController.processRightClickBlock(mc.player, mc.world, this.currentBlockTrace.getBlockPos(), this.currentBlockTrace.sideHit, new Vec3d(0d, 0d, 0d), EnumHand.MAIN_HAND);
                    if (actionResult.equals(EnumActionResult.SUCCESS)) {
                        mc.player.swingArm(EnumHand.MAIN_HAND);
                    }
                }
            }
        }

        /*
        if (this.entities.getValue()) {
            if (entityTraceResult == null) {
                if (mc.gameSettings.keyBindAttack.pressed) {
                    System.out.println("ATTACJ");
                    mc.playerController.attackEntity(mc.player, entityTraceResult.entityHit);
                } else if (mc.gameSettings.keyBindUseItem.pressed) {
                    mc.playerController.interactWithEntity(mc.player, entityTraceResult.entityHit, EnumHand.MAIN_HAND);
                }
            }
        }
        */
    }
}
