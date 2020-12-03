package me.rigamortis.seppuku.impl.module.player;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketSetPassengers;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.Arrays;
import java.util.OptionalInt;

public final class EntityDesyncModule extends Module {

    public final Value<Boolean> noDismountPlugin = new Value<Boolean>("NoDismountPlugin", new String[]{"NoDismount", "nd", "ndp", "AntiDismount"}, "Prevents server plugin from dismounting you while riding.", false);
    public final Value<Boolean> dismountEntity = new Value<Boolean>("DismountEntity", new String[]{"Dismount", "d", "de"}, "Dismounts the riding entity client-side. (debug)", true);
    public final Value<Boolean> removeEntity = new Value<Boolean>("RemoveEntity", new String[]{"Remove", "r", "re"}, "Removes the entity from the world client-side. (debug)", true);
    public final Value<Boolean> respawnEntity = new Value<Boolean>("RespawnEntity", new String[]{"Respawn", "res", "resp"}, "Forces the riding entity's 'isDead' value to be false on respawn. (debug)", true);
    public final Value<Boolean> sendMovePackets = new Value<Boolean>("SendMovePackets", new String[]{"MovePackets", "sendmp", "SendMove", "sm"}, "Sends CPacketVehicleMove packets for the riding entity. (debug)", true);
    public final Value<Boolean> forceOnGround = new Value<Boolean>("ForceOnGround", new String[]{"ForceGound", "fog", "fg", "ground"}, "Forces player.onGround = true when de-syncing. (debug)", true);
    public final Value<Boolean> setMountPosition = new Value<Boolean>("SetMountPosition", new String[]{"SetMountPos", "setmp", "setmpos"}, "Updates the riding entity position & bounding-box client-side. (debug)", true);

    private Entity originalRidingEntity;

    public EntityDesyncModule() {
        super("EntityDesync", new String[]{"EntityDesync", "EDesync", "Desync"}, "NONE", -1, ModuleType.PLAYER);
        this.setDesc("Dismounts you from an entity client-side.");
    }

    @Listener
    public void onReceivePacket(EventReceivePacket event) {
        if (event.getStage().equals(EventStageable.EventStage.POST)) {
            if (event.getPacket() instanceof SPacketSetPassengers) {
                if (this.hasOriginalRidingEntity() && Minecraft.getMinecraft().world != null) {
                    SPacketSetPassengers packetSetPassengers = (SPacketSetPassengers) event.getPacket();
                    if (this.originalRidingEntity.equals(Minecraft.getMinecraft().world.getEntityByID(packetSetPassengers.getEntityId()))) {
                        OptionalInt isPlayerAPassenger = Arrays.stream(packetSetPassengers.getPassengerIds()).filter(value -> Minecraft.getMinecraft().world.getEntityByID(value) == Minecraft.getMinecraft().player).findAny();
                        if (!isPlayerAPassenger.isPresent()) { // local player is not a passenger
                            Seppuku.INSTANCE.logChat("You've been dismounted."); // notify the player
                            this.toggle(); // toggle the module
                        }
                    }
                }
            }

            if (event.getPacket() instanceof SPacketDestroyEntities) {
                SPacketDestroyEntities packetDestroyEntities = (SPacketDestroyEntities) event.getPacket();
                boolean isEntityNull = Arrays.stream(packetDestroyEntities.getEntityIDs()).filter(value -> value == originalRidingEntity.getEntityId()).findAny().isPresent();
                if (isEntityNull) { // found the entity in the packet
                    Seppuku.INSTANCE.logChat("The current riding entity is now null (destroyed or deleted)."); // notify the player
                }
            }
        }
    }

    /**
     * NoDismount for 9b9t, and possibly others.
     *
     * @param event
     * @author Seth
     */
    @Listener
    public void onSendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (this.noDismountPlugin.getValue()) {
                if (event.getPacket() instanceof CPacketPlayer.Position) {
                    event.setCanceled(true);
                    CPacketPlayer.Position packet = (CPacketPlayer.Position) event.getPacket();
                    Minecraft.getMinecraft().player.connection.sendPacket(new CPacketPlayer.PositionRotation(packet.x, packet.y, packet.z, packet.yaw, packet.pitch, packet.onGround));
                }
                if (event.getPacket() instanceof CPacketPlayer && !(event.getPacket() instanceof CPacketPlayer.PositionRotation))
                    event.setCanceled(true);
            }
        }
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage().equals(EventStageable.EventStage.POST)) {
            final Minecraft mc = Minecraft.getMinecraft();
            if (mc.world != null && mc.player != null) {
                if (!mc.player.isRiding() && this.hasOriginalRidingEntity()) { // the local (client) player is not riding something, but server-side we have a mount
                    if (this.forceOnGround.getValue())
                        mc.player.onGround = true; // force on ground

                    if (this.setMountPosition.getValue())
                        this.originalRidingEntity.setPosition(mc.player.posX, mc.player.posY, mc.player.posZ); // begin moving

                    if (this.sendMovePackets.getValue())
                        mc.player.connection.sendPacket(new CPacketVehicleMove(this.originalRidingEntity)); // send movement packets for the entity
                }
            }
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.originalRidingEntity = null;

        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null && mc.world != null) {
            if (mc.player.isRiding()) {
                this.originalRidingEntity = mc.player.getRidingEntity();

                // dismount
                if (this.dismountEntity.getValue()) {
                    mc.player.dismountRidingEntity();
                    Seppuku.INSTANCE.logChat("Dismounted entity.");
                }

                // remove
                if (this.removeEntity.getValue()) {
                    mc.world.removeEntity(this.originalRidingEntity);
                    Seppuku.INSTANCE.logChat("Removed entity from world.");
                }
            } else {
                Seppuku.INSTANCE.logChat("Please mount an entity before enabling this module.");
                this.toggle(); // disable module
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (this.hasOriginalRidingEntity()) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (this.respawnEntity.getValue())
                this.originalRidingEntity.isDead = false; // bring the riding entity to life client-side

            // if we aren't riding now, we need to remount
            if (!mc.player.isRiding()) {
                mc.world.spawnEntity(this.originalRidingEntity); // spawn the entity back in
                mc.player.startRiding(this.originalRidingEntity, true); // begin riding the entity (forced)
                Seppuku.INSTANCE.logChat("Spawned & mounted original entity."); // notify the player we successfully remounted
            }

            // delete the old entity now
            this.originalRidingEntity = null;
        }
    }

    private boolean hasOriginalRidingEntity() {
        return this.originalRidingEntity != null;
    }
}
