package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author noil
 */
public final class BridgeModule extends Module {

    public final Value<Integer> reach = new Value<Integer>("Reach", new String[]{"Reach", "range", "r"}, "The reach in blocks from the player to end bridging", 4, 1, 10, 1);

    public BridgeModule() {
        super("Bridge", new String[]{"Bridge", "AutoBridge"}, "Builds a bridge under you with your held item", "NONE", -1, ModuleType.WORLD);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage().equals(EventStageable.EventStage.POST))
            return;

        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null)
            return;

        if (mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock) {
            ItemBlock itemBlock = (ItemBlock) mc.player.getHeldItemMainhand().getItem();
            if (this.shouldPlace(itemBlock.getBlock())) {
                EnumFacing facing = mc.player.getHorizontalFacing();
                Vec3i position = getNextBlock(facing.getDirectionVec(), mc);
                if (position != null) {
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(new BlockPos(position.getX(), position.getY(), position.getZ()), facing, EnumHand.MAIN_HAND, (float) facing.getDirectionVec().getX() / 2.0F, (float) facing.getDirectionVec().getY() / 2.0F, (float) facing.getDirectionVec().getZ() / 2.0F));
                }
            }
        }
    }

    private Vec3i getNextBlock(Vec3i direction, final Minecraft mc) {
        for (int i = 0; i <= reach.getValue(); i++) {
            Vec3i position = new Vec3i(mc.player.posX + direction.getX() * i, mc.player.posY - 1, mc.player.posZ + direction.getZ() * i);
            Vec3i before = new Vec3i(mc.player.posX + direction.getX() * (i - 1), mc.player.posY - 1, mc.player.posZ + direction.getZ() * (i - 1));
            if ((mc.world.getBlockState(new BlockPos(position.getX(), position.getY(), position.getZ())).getMaterial() == Material.AIR) && (mc.world.getBlockState(new BlockPos(before.getX(), before.getY(), before.getZ())).getMaterial() != Material.AIR)) {
                return before;
            }
        }
        return null;
    }

    private boolean shouldPlace(Block block) {
        return block != null && !block.hasTileEntity();
    }
}

