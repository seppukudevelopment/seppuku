package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * AutoMount
 *
 * @author noil
 * - Future improvements:
 * - this would be cool to have mod-compatibility with dragon riding mods & etc.
 * - possibly have different options for sorting through entities rather than just distance
 * - add an auto-dropping option for dropping all items inside the entity's chest when ridden
 */
public final class AutoMountModule extends Module {

    public final Value<Float> range = new Value<>("Range", new String[]{"Dist"}, "The minimum range to begin scanning for mounts.", 4.0f, 0.1f, 5.0f, 0.1f);
    public final Value<Float> delay = new Value<>("Delay", new String[]{"del"}, "The delay(ms) between mounts.", 500.0f, 0.0f, 1000.0f, 1f);
    public final Value<Boolean> autoChest = new Value<Boolean>("AutoChest", new String[]{"Chest"}, "Tries to place a chest onto the donkey or llama before mounting (hold a chest for this to work).", false);
    public final Value<Boolean> forceStand = new Value<Boolean>("ForceStand", new String[]{"Stand", "NoSneak"}, "Forces the player to stand (un-sneak) before sending the packet to ride the entity.", true);
    public final Value<Boolean> boat = new Value<Boolean>("Boat", new String[]{"Boat"}, "Enables auto-mounting onto boats.", true);
    public final Value<Boolean> minecart = new Value<Boolean>("Minecart", new String[]{"Minecart"}, "Enables auto-mounting onto minecarts.", true);
    public final Value<Boolean> donkey = new Value<Boolean>("Donkey", new String[]{"Donkey", "Ass"}, "Enables auto-mounting onto donkeys.", true);
    public final Value<Boolean> llama = new Value<Boolean>("Llama", new String[]{"Llama"}, "Enables auto-mounting onto llamas.", true);
    public final Value<Boolean> horse = new Value<Boolean>("Horse", new String[]{"Horse"}, "Enables auto-mounting onto horses.", true);
    public final Value<Boolean> skeletonHorse = new Value<Boolean>("SkeletonHorse", new String[]{"SkeleHorse"}, "Enables auto-mounting onto skeleton horses.", true);
    public final Value<Boolean> pig = new Value<Boolean>("Pig", new String[]{"Pig"}, "Enables auto-mounting onto pigs.", true);

    private final Timer delayTimer = new Timer();

    public AutoMountModule() {
        super("AutoMount", new String[]{"AutoMount", "autmount", "amount", "auto_mount"}, "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage().equals(EventStageable.EventStage.POST)) {
            final Minecraft mc = Minecraft.getMinecraft();
            if (mc.player != null && mc.world != null) {
                if (!mc.player.isRiding() && !mc.gameSettings.keyBindSneak.isKeyDown()) { // the player is not riding anything currently
                    Entity nearestValidMount = this.findMount(mc, this.range.getValue()); // find a mount nearby
                    if (nearestValidMount != null) { // mountable entity found
                        // start delay
                        if (this.delayTimer.passed(this.delay.getValue())) {
                            if (nearestValidMount instanceof AbstractChestHorse) { // this type of entity can have a chest on it
                                AbstractChestHorse nearestAbstractChestHorse = (AbstractChestHorse) nearestValidMount;
                                if (this.autoChest.getValue()) {
                                    if (mc.player.getHeldItemMainhand().getItem().equals(Item.getItemFromBlock(Blocks.CHEST))) { // checks if the player is holding a chest
                                        if (!nearestAbstractChestHorse.hasChest() && nearestAbstractChestHorse.isTame()) { // filter for entities that don't already have chests on them, and must be tamed
                                            mc.player.connection.sendPacket(new CPacketUseEntity(nearestValidMount, EnumHand.MAIN_HAND));
                                            return;
                                        }
                                    }
                                }
                            }

                            if (!nearestValidMount.isBeingRidden()) { // mountable entity has no rider, so let's try to mount it!
                                //mc.player.startRiding(nearestValidMount); // note: this function is client sided.

                                // force standing before mounting
                                if (this.forceStand.getValue()) {
                                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                                }

                                // attempts to get on the mount
                                mc.player.connection.sendPacket(new CPacketUseEntity(nearestValidMount, EnumHand.MAIN_HAND));
                            }

                            // reset delay
                            this.delayTimer.reset();
                        }
                    }
                } else {
                    // reset delay
                    this.delayTimer.reset();
                }
            }
        }
    }

    /**
     * Finds a nearby valid entity. see {@link #isValid;
     *
     * @param mc       the minecraft instance
     * @param distance minimum distance to find mountable entities
     * @return the closest mountable entity
     */
    private Entity findMount(Minecraft mc, float distance) {
        Entity closestEntity = null;
        float maxDist = distance;

        for (Entity e : mc.world.loadedEntityList) {
            if (e != null) {
                if (this.isValid(e)) {
                    float currentDist = mc.player.getDistance(e);
                    if (currentDist <= maxDist) {
                        maxDist = currentDist;
                        closestEntity = e;
                    }
                }
            }
        }

        return closestEntity;
    }

    /**
     * Returns true when the filtered values are met & if the entity can be a child, it will return false.
     *
     * @param entity the entity to check
     * @return true if filtered values are met & the entity is alive
     */
    public boolean isValid(Entity entity) {
        boolean valid = false;
        if (this.boat.getValue() && entity instanceof EntityBoat) {
            valid = true;
        } else if (this.minecart.getValue() && entity instanceof EntityMinecart) {
            valid = true;
        } else if (this.donkey.getValue() && entity instanceof EntityDonkey) {
            EntityDonkey entityDonkey = (EntityDonkey) entity;
            if (!entityDonkey.isChild())
                valid = true;
        } else if (this.llama.getValue() && entity instanceof EntityLlama) {
            EntityLlama entityLlama = (EntityLlama) entity;
            if (!entityLlama.isChild())
                valid = true;
        } else if (this.horse.getValue() && entity instanceof EntityHorse) {
            EntityHorse entityHorse = (EntityHorse) entity;
            if (!entityHorse.isChild())
                valid = true;
        } else if (this.skeletonHorse.getValue() && entity instanceof EntitySkeletonHorse) {
            EntitySkeletonHorse entitySkeletonHorse = (EntitySkeletonHorse) entity;
            if (!entitySkeletonHorse.isChild())
                valid = true;
        } else if (this.pig.getValue() && entity instanceof EntityPig) {
            EntityPig entityPig = (EntityPig) entity;
            if (!entityPig.isChild())
                valid = true;
        }
        return valid && entity.isEntityAlive();
    }
}
