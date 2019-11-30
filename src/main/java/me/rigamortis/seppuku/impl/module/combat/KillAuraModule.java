package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/30/2019 @ 4:04 PM.
 */
public final class KillAuraModule extends Module {

    public final Value<Boolean> players = new Value("Players", new String[]{"Player"}, "Choose to target players.", true);
    public final Value<Boolean> mobs = new Value("Mobs", new String[]{"Mob"}, "Choose to target mobs.", true);
    public final Value<Boolean> animals = new Value("Animals", new String[]{"Animal"}, "Choose to target animals.", true);
    public final Value<Boolean> vehicles = new Value("Vehicles", new String[]{"Vehic", "Vehicle"}, "Choose to target vehicles.", true);
    public final Value<Boolean> projectiles = new Value("Projectile", new String[]{"Proj"}, "Choose to target projectiles", true);

    public final Value<Float> range = new Value<>("Range", new String[]{"Dist"}, "The minimum range to attack.", 4.5f, 0.0f, 5.0f, 0.1f);
    public final Value<Boolean> coolDown = new Value("CoolDown", new String[]{"CoolD"}, "Delay your hits to gain damage.", true);
    public final Value<Boolean> sync = new Value("Sync", new String[]{"snc"}, "Sync your hits with the server's estimated TPS.", true);
    public final Value<Boolean> teleport = new Value("Teleport", new String[]{"tp"}, "Teleports to your target(Only works on vanilla).", false);

    public KillAuraModule() {
        super("KillAura", new String[]{"Aura"}, "Automatically aims and attacks enemies", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            final Entity target = findTarget();

            if(target != null) {
                final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), target.getPositionEyes(mc.getRenderPartialTicks()));
                Seppuku.INSTANCE.getRotationManager().setPlayerRotations(angle[0], angle[1]);

                final float ticks = 20.0f - Seppuku.INSTANCE.getTickRateManager().getTickRate();

                final boolean canAttack = this.coolDown.getValue() ? (mc.player.getCooledAttackStrength(this.sync.getValue() ? -ticks : 0.0f) >= 1) : true;

                final ItemStack stack = mc.player.getHeldItem(EnumHand.OFF_HAND);

                //TODO interp
                if(this.teleport.getValue()) {
                    Seppuku.INSTANCE.getPositionManager().setPlayerPosition(target.posX, target.posY, target.posZ);
                }

                if(canAttack) {
                    if(stack != null && stack.getItem() == Items.SHIELD) {
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
                    }

                    mc.player.connection.sendPacket(new CPacketUseEntity(target));
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                    mc.player.resetCooldown();
                }
            }
        }
    }

    private Entity findTarget() {
        Entity ent = null;

        final Minecraft mc = Minecraft.getMinecraft();

        float maxDist = this.range.getValue();

        for (Entity e : mc.world.loadedEntityList) {
            if (e != null) {
                if (this.checkFilter(e)) {
                    float currentDist = mc.player.getDistance(e);

                    if(currentDist <= maxDist) {
                        maxDist = currentDist;
                        ent = e;
                    }
                }
            }
        }

        return ent;
    }

    private boolean checkFilter(Entity entity) {
        boolean ret = false;

        if (this.players.getValue() && entity instanceof EntityPlayer && entity != Minecraft.getMinecraft().player && Seppuku.INSTANCE.getFriendManager().isFriend(entity) == null && !entity.getName().equals(Minecraft.getMinecraft().player.getName())) {
            ret = true;
        }

        if (this.mobs.getValue() && entity instanceof IMob) {
            ret = true;
        }

        if (this.animals.getValue() && entity instanceof IAnimals && !(entity instanceof IMob)) {
            ret = true;
        }

        if (this.vehicles.getValue() && (entity instanceof EntityBoat || entity instanceof EntityMinecart || entity instanceof EntityMinecartContainer)) {
            ret = true;
        }

        if(this.projectiles.getValue() && (entity instanceof EntityShulkerBullet || entity instanceof EntityFireball)) {
            ret = true;
        }

        if(entity instanceof EntityLivingBase) {
            final EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
            if(entityLivingBase.getHealth() <= 0) {
                ret = false;
            }
        }

        return ret;
    }

}
