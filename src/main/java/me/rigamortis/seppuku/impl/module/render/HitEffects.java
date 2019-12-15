package me.rigamortis.seppuku.impl.module.render;

import java.util.*;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import net.minecraft.client.Minecraft;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.impl.management.ModuleManager;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.impl.command.ColorCommand;
import me.rigamortis.seppuku.api.value.NumberValue;
import me.rigamortis.seppuku.api.value.BooleanValue;
import me.rigamortis.seppuku.api.event.render.EventRender2D;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/*
 @author Proto
 11/21/19
 
 Dont skid bro please
 PLEASE BRO DONT SKID
 PLEASE BRO I WORKED SO HARD
 ok this was made by proto ok guys!!!
 */

public final class HitEffects extends Module {

    public final BooleanValue lightning = new BooleanValue("Lightning", new String[]{"Lightning"}, true);
    public final BooleanValue explosion = new BooleanValue("Explosion", new String[]{"Explosion"}, false);
    public final BooleanValue totem = new BooleanValue("Totem", new String[]{"Totem"}, false);
    public final BooleanValue sounds = new BooleanValue("Sounds", new String[]{"Sounds"}, true);

    private final Minecraft mc = Minecraft.getMinecraft();
 
    public final ResourceLocation explodelocal = new ResourceLocation("minecraft", "entity.generic.explode");
    public final ResourceLocation totemlocal = new ResourceLocation("minecraft", "item.totem.use");
    public final ResourceLocation lightningimpactlocal = new ResourceLocation("minecraft", "entity.lightning.impact");
    public final ResourceLocation thunderlocal = new ResourceLocation("minecraft", "entity.lightning.thunder");
 
    public final SoundEvent explosionsound = new SoundEvent(explodelocal);
    public final SoundEvent thundersound = new SoundEvent(thunderlocal);
    public final SoundEvent lightningimpactsound = new SoundEvent(lightningimpactlocal);
    public final SoundEvent totemsound = new SoundEvent(totemlocal);
    
    public HitEffects() {
        super("Hit Effects", new String[]{"Hit Effects"}, "Wacky Hit Effects", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (mc.world != null) {
                    if (event.getPacket() instanceof CPacketUseEntity) {
                        CPacketUseEntity packet = (CPacketUseEntity)event.getPacket();
                        if (packet.getAction().equals(CPacketUseEntity.Action.ATTACK)) {
                            Entity entity = packet.getEntityFromWorld(mc.world);
                            BlockPos NiggaPenis = new BlockPos(entity.posX, entity.posY, entity.posZ);

                            if (lightning.getBoolean()) {
                                mc.world.spawnEntity(new EntityLightningBolt(mc.world, entity.posX, entity.posY + 1, entity.posZ, true));
                                if (sounds.getBoolean()) {
                                    //mc.player.playSound(thundersound, 1, 1);
                                    mc.world.playSound(mc.player,NiggaPenis, thundersound, SoundCategory.WEATHER, 1, 1);
                                    mc.world.playSound(mc.player, NiggaPenis, lightningimpactsound, SoundCategory.WEATHER, 1, 1);
                                }
                            }
                            if (explosion.getBoolean()) {
                                mc.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, entity.posX, entity.posY, entity.posZ, 0, 0, 0);
                                mc.world.spawnParticle(EnumParticleTypes.CLOUD, entity.posX, entity.posY, entity.posZ, 0, 0, 0);
                                mc.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, entity.posX, entity.posY, entity.posZ, 0, 0, 0);
                                if (sounds.getBoolean()) {
                                    //mc.player.playSound(explosionsound, 1, 1);
                                    mc.world.playSound(mc.player, NiggaPenis, explosionsound, SoundCategory.BLOCKS, 1, 1);
                                }
                            }
                            if (totem.getBoolean()) {
                                Random r = new Random();
                                for (int i = 0; i < 300; ++i)
                                    mc.world.spawnParticle(EnumParticleTypes.TOTEM, entity.posX, entity.posY, entity.posZ, (r.nextInt(1 + 1) - 1), (r.nextInt(2 + 2) - 2), (r.nextInt(1 + 1) - 1));
                                if (sounds.getBoolean()) {
                                    //mc.player.playSound(explosionsound, 1, 1);
                                    mc.world.playSound(mc.player, NiggaPenis, totemsound, SoundCategory.BLOCKS, 1, 1);
                                }
                        }
                    }
                }
            }
        }
    }



}
