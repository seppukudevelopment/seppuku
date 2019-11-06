package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.ColorUtil;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.api.value.BooleanValue;
import me.rigamortis.seppuku.api.value.NumberValue;
import me.rigamortis.seppuku.impl.module.player.GodModeModule;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Author Seth
 * 4/29/2019 @ 1:19 AM.
 */
public final class CrystalAuraModule extends Module {

    public final NumberValue range = new NumberValue("Range", new String[]{"Dist"}, 4.5f, Float.class, 0.0f, 5.0f, 0.1f);
    public final NumberValue delay = new NumberValue("Attack_Delay", new String[]{"AttackDelay", "AttackDel", "Del"}, 50.0f, Float.class, 0.0f, 1000.0f, 1.0f);
    public final BooleanValue place = new BooleanValue("Place", new String[]{"AutoPlace"}, true);
    public final NumberValue placeDelay = new NumberValue("Place_Delay", new String[]{"PlaceDelay", "PlaceDel"}, 50.0f, Float.class, 0.0f, 1000.0f, 1.0f);
    public final NumberValue minDamage = new NumberValue("Min_Damage", new String[]{"MinDamage", "Min", "MinDmg"}, 1.0f, Float.class, 0.0f, 20.0f, 0.5f);
    public final BooleanValue ignore = new BooleanValue("Ignore", new String[]{"Ig"}, false);
    public final BooleanValue render = new BooleanValue("Render", new String[]{"R"}, true);

    private Timer attackTimer = new Timer();
    private Timer placeTimer = new Timer();

    private List<PlaceLocation> placeLocations = new CopyOnWriteArrayList<>();

    public CrystalAuraModule() {
        super("CrystalAura", new String[]{"AutoCrystal", "Crystal"}, "Automatically places crystals near enemies and detonates them", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (mc.player.inventory.getCurrentItem().getItem() != Items.END_CRYSTAL) {
                return;
            }

            if (this.place.getBoolean()) {
                if (this.placeTimer.passed(this.placeDelay.getFloat())) {
                    final float radius = this.range.getFloat();

                    float damage = 0;
                    double maxDist = 6.0f;
                    BlockPos pos = null;
                    EntityPlayer target = null;

                    for (float x = radius; x >= -radius; x--) {
                        for (float y = radius; y >= -radius; y--) {
                            for (float z = radius; z >= -radius; z--) {
                                final BlockPos blockPos = new BlockPos(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z);

                                if (canPlaceCrystal(blockPos)) {
                                    for (Entity entity : mc.world.loadedEntityList) {
                                        if (entity != null && entity instanceof EntityPlayer) {
                                            final EntityPlayer player = (EntityPlayer) entity;
                                            if (player != mc.player && !player.getName().equals(mc.player.getName()) && player.getHealth() > 0 && Seppuku.INSTANCE.getFriendManager().isFriend(player) == null) {
                                                final double distToBlock = entity.getDistance(blockPos.getX() + 0.5f, blockPos.getY() + 1, blockPos.getZ() + 0.5f);
                                                final double distToLocal = entity.getDistance(mc.player.posX, mc.player.posY, mc.player.posZ);
                                                if (distToBlock <= 14 && distToLocal <= maxDist) {
                                                    target = player;
                                                    maxDist = distToLocal;
                                                }
                                            }
                                        }
                                    }

                                    if (target != null) {
                                        final float currentDamage = calculateExplosionDamage(target, 6.0f, blockPos.getX() + 0.5f, blockPos.getY() + 1.0f, blockPos.getZ() + 0.5f) / 2.0f;

                                        float localDamage = calculateExplosionDamage(mc.player, 6.0f, blockPos.getX() + 0.5f, blockPos.getY() + 1.0f, blockPos.getZ() + 0.5f) / 2.0f;

                                        if (!canTakeDamage()) {
                                            localDamage = -1;
                                        }

                                        if (currentDamage > damage && currentDamage >= this.minDamage.getFloat() && localDamage <= currentDamage) {
                                            damage = currentDamage;
                                            pos = blockPos;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (pos != null && damage > 0) {
                        final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f));
                        Seppuku.INSTANCE.getRotationManager().setPlayerRotations(angle[0], angle[1]);
                        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, EnumFacing.UP, EnumHand.MAIN_HAND, 0, 0, 0));
                        this.placeLocations.add(new PlaceLocation(pos.getX(), pos.getY(), pos.getZ()));
                    }
                    this.placeTimer.reset();
                }
            }

            for (Entity entity : mc.world.loadedEntityList) {
                if (entity != null && entity instanceof EntityEnderCrystal) {
                    if (mc.player.getDistance(entity) <= this.range.getFloat()) {
                        for (Entity ent : mc.world.loadedEntityList) {
                            if (ent != null && ent != mc.player && (ent.getDistance(entity) <= 14.0f) && ent != entity && ent instanceof EntityPlayer) {
                                final EntityPlayer player = (EntityPlayer) ent;
                                float currentDamage = calculateExplosionDamage(player, 6.0f, (float) entity.posX, (float) entity.posY, (float) entity.posZ) / 2.0f;
                                float localDamage = calculateExplosionDamage(mc.player, 6.0f, (float) entity.posX, (float) entity.posY, (float) entity.posZ) / 2.0f;

                                if (!canTakeDamage()) {
                                    localDamage = -1;
                                }

                                if (localDamage <= currentDamage && currentDamage >= this.minDamage.getFloat()) {
                                    final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), entity.getPositionVector());
                                    Seppuku.INSTANCE.getRotationManager().setPlayerRotations(angle[0], angle[1]);
                                    if (this.attackTimer.passed(this.delay.getFloat())) {
                                        mc.player.swingArm(EnumHand.MAIN_HAND);
                                        mc.playerController.attackEntity(mc.player, entity);
                                        this.attackTimer.reset();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Listener
    public void onReceivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.POST) {
            if (event.getPacket() instanceof SPacketSpawnObject) {
                final SPacketSpawnObject packetSpawnObject = (SPacketSpawnObject) event.getPacket();
                if (packetSpawnObject.getType() == 51) {
                    for (PlaceLocation placeLocation : this.placeLocations) {
                        if (placeLocation.getDistance((int) packetSpawnObject.getX(), (int) packetSpawnObject.getY() - 1, (int) packetSpawnObject.getZ()) <= 1) {
                            placeLocation.placed = true;
                        }
                    }
                }
            }
        }
    }

    @Listener
    public void onRender(EventRender3D event) {
        if (!this.render.getBoolean())
            return;

        final Minecraft mc = Minecraft.getMinecraft();

        for (PlaceLocation placeLocation : this.placeLocations) {
            if (placeLocation.alpha <= 0) {
                this.placeLocations.remove(placeLocation);
                continue;
            }

            placeLocation.update();

            if (placeLocation.placed) {
                final AxisAlignedBB bb = new AxisAlignedBB(
                        placeLocation.getX() - mc.getRenderManager().viewerPosX,
                        placeLocation.getY() - mc.getRenderManager().viewerPosY,
                        placeLocation.getZ() - mc.getRenderManager().viewerPosZ,
                        placeLocation.getX() + 1 - mc.getRenderManager().viewerPosX,
                        placeLocation.getY() + 1 - mc.getRenderManager().viewerPosY,
                        placeLocation.getZ() + 1 - mc.getRenderManager().viewerPosZ);

                RenderUtil.drawFilledBox(bb, ColorUtil.changeAlpha(0xAA9900EE, placeLocation.alpha / 2));
                RenderUtil.drawBoundingBox(bb, 1, ColorUtil.changeAlpha(0xAAAAAAAA, placeLocation.alpha));
            }
        }
    }

    private boolean canTakeDamage() {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.player.capabilities.isCreativeMode) {
            return false;
        }

        final GodModeModule mod = (GodModeModule) Seppuku.INSTANCE.getModuleManager().find(GodModeModule.class);

        if (mod != null && mod.isEnabled()) {
            return false;
        }

        if (this.ignore.getBoolean()) {
            return false;
        }

        return true;
    }

    private boolean canPlaceCrystal(BlockPos pos) {
        final Minecraft mc = Minecraft.getMinecraft();

        final Block block = mc.world.getBlockState(pos).getBlock();

        if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK) {
            final Block floor = mc.world.getBlockState(pos.add(0, 1, 0)).getBlock();
            final Block ceil = mc.world.getBlockState(pos.add(0, 2, 0)).getBlock();

            if (floor == Blocks.AIR && ceil == Blocks.AIR) {
                if (mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.add(0, 1, 0))).isEmpty()) {
                    if (mc.player.getDistance(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f) <= this.range.getFloat()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private float calculateExplosionDamage(EntityLivingBase entity, float size, float x, float y, float z) {
        final Minecraft mc = Minecraft.getMinecraft();
        final float scale = size * 2.0F;
        final Vec3d pos = MathUtil.interpolateEntity(entity, mc.getRenderPartialTicks());
        final double dist = MathUtil.getDistance(pos, x, y, z) / (double) scale;
        //final double dist = entity.getDistance(x, y, z) / (double) scale;
        final Vec3d vec3d = new Vec3d(x, y, z);
        final double density = (double) entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        final double densityScale = (1.0D - dist) * density;

        float unscaledDamage = (float) ((int) ((densityScale * densityScale + densityScale) / 2.0d * 7.0d * (double) scale + 1.0d));

        unscaledDamage *= 0.5f * mc.world.getDifficulty().getId();

        return scaleExplosionDamage(entity, new Explosion(mc.world, null, x, y, z, size, false, true), unscaledDamage);
    }

    private float scaleExplosionDamage(EntityLivingBase entity, Explosion explosion, float damage) {
        damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getTotalArmorValue(), (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());

        damage *= (1.0F - MathHelper.clamp(EnchantmentHelper.getEnchantmentModifierDamage(entity.getArmorInventoryList(), DamageSource.causeExplosionDamage(explosion)), 0.0F, 20.0F) / 25.0F);

        damage = Math.max(damage - entity.getAbsorptionAmount(), 0.0F);
        return damage;
    }

    private final class PlaceLocation extends Vec3i {

        private int alpha = 0xAA;
        private boolean placed = false;

        private PlaceLocation(int xIn, int yIn, int zIn) {
            super(xIn, yIn, zIn);
        }

        private void update() {
            if (this.alpha > 0)
                this.alpha -= 1;
        }
    }

}
