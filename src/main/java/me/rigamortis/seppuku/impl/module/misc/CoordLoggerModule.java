package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.BooleanValue;
import me.rigamortis.seppuku.api.value.OptionalValue;
import net.minecraft.client.Minecraft;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/17/2019 @ 12:28 AM.
 */
public final class CoordLoggerModule extends Module {

    public final OptionalValue mode = new OptionalValue("Mode", new String[]{"Mode", "M"}, 0, new String[]{"Vanilla", "Spigot"});

    public final BooleanValue thunder = new BooleanValue("Thunder", new String[]{"thund"}, true);
    public final BooleanValue endPortal = new BooleanValue("EndPortal", new String[]{"portal"}, true);
    public final BooleanValue wither = new BooleanValue("Wither", new String[]{"with"}, true);
    public final BooleanValue endDragon = new BooleanValue("EndDragon", new String[]{"dragon"}, true);
    public final BooleanValue slimes = new BooleanValue("Slimes", new String[]{"slime"}, true);

    public CoordLoggerModule() {
        super("CoordLogger", new String[]{"CoordLog", "CLogger", "CLog"}, "Logs useful coordinates", "NONE", -1, ModuleType.MISC);
    }

    @Override
    public String getMetaData() {
        return this.mode.getSelectedOption();
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {

            if (event.getPacket() instanceof SPacketSpawnMob) {
                final SPacketSpawnMob packet = (SPacketSpawnMob) event.getPacket();
                if (this.slimes.getBoolean()) {
                    final Minecraft mc = Minecraft.getMinecraft();

                    if (packet.getEntityType() == 55 && packet.getY() <= 40 && !mc.world.getBiome(mc.player.getPosition()).getBiomeName().toLowerCase().contains("swamp")) {
                        final BlockPos pos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
                        Seppuku.INSTANCE.logChat("Slime Spawned in chunk X:" + mc.world.getChunk(pos).x + " Z:" + mc.world.getChunk(pos).z);
                    }
                }
            }

            if (event.getPacket() instanceof SPacketSoundEffect) {
                final SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();
                if (this.thunder.getBoolean()) {
                    if (packet.getCategory() == SoundCategory.WEATHER && packet.getSound() == SoundEvents.ENTITY_LIGHTNING_THUNDER) {
                        float yaw = 0;
                        final double difX = packet.getX() - Minecraft.getMinecraft().player.posX;
                        final double difZ = packet.getZ() - Minecraft.getMinecraft().player.posZ;

                        yaw += MathHelper.wrapDegrees((Math.toDegrees(Math.atan2(difZ, difX)) - 90.0f) - yaw);

                        Seppuku.INSTANCE.logChat("Lightning spawned X:" + Minecraft.getMinecraft().player.posX + " Z:" + Minecraft.getMinecraft().player.posZ + " Angle:" + yaw);
                    }
                }
            }
            if (event.getPacket() instanceof SPacketEffect) {
                final SPacketEffect packet = (SPacketEffect) event.getPacket();
                if (this.endPortal.getBoolean()) {
                    if (packet.getSoundType() == 1038) {
                        Seppuku.INSTANCE.logChat("End Portal activated at X:" + packet.getSoundPos().getX() + " Y:" + packet.getSoundPos().getY() + " Z:" + packet.getSoundPos().getZ());
                    }
                }
                if (this.wither.getBoolean()) {
                    if (packet.getSoundType() == 1023) {
                        switch (this.mode.getInt()) {
                            case 0:
                                Seppuku.INSTANCE.logChat("Wither spawned at X:" + packet.getSoundPos().getX() + " Y:" + packet.getSoundPos().getY() + " Z:" + packet.getSoundPos().getZ());
                                break;
                            case 1:
                                float yaw = 0;
                                final double difX = packet.getSoundPos().getX() - Minecraft.getMinecraft().player.posX;
                                final double difZ = packet.getSoundPos().getZ() - Minecraft.getMinecraft().player.posZ;

                                yaw += MathHelper.wrapDegrees((Math.toDegrees(Math.atan2(difZ, difX)) - 90.0f) - yaw);

                                Seppuku.INSTANCE.logChat("Wither spawned X:" + Minecraft.getMinecraft().player.posX + " Z:" + Minecraft.getMinecraft().player.posZ + " Angle:" + yaw);
                                break;
                        }
                    }
                }
                if (this.endDragon.getBoolean()) {
                    if (packet.getSoundType() == 1028) {
                        float yaw = 0;
                        final double difX = packet.getSoundPos().getX() - Minecraft.getMinecraft().player.posX;
                        final double difZ = packet.getSoundPos().getZ() - Minecraft.getMinecraft().player.posZ;

                        yaw += MathHelper.wrapDegrees((Math.toDegrees(Math.atan2(difZ, difX)) - 90.0f) - yaw);

                        Seppuku.INSTANCE.logChat("Ender Dragon killed at X:" + Minecraft.getMinecraft().player.posX + " Z:" + Minecraft.getMinecraft().player.posZ + " Angle:" + yaw);
                    }
                }
            }
        }
    }
}
