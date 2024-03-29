package me.rigamortis.seppuku.impl.module.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.world.EventAddEntity;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.notification.Notification;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.*;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author noil
 */
public final class DonkeyAlertModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "Change between alert modes", Mode.BOTH);

    public final Value<Float> delay = new Value<Float>("Delay", new String[]{"Del"}, "The amount of delay(ms) between entity checks", 500.0f, 0.0f, 2500.0f, 100.0f);
    public final Value<Boolean> donkey = new Value<Boolean>("Donkey", new String[]{"Donkeys", "Donkies", "Donkie", "Donkee", "D"}, "Enables alerts for donkey entities", true);
    public final Value<Boolean> lama = new Value<Boolean>("Llama", new String[]{"Lama", "L"}, "Enables alerts for llama entities", true);
    public final Value<Boolean> horse = new Value<Boolean>("Horse", new String[]{"Hrse", "H"}, "Enables alerts for horse entities", true);
    public final Value<Boolean> mule = new Value<Boolean>("Mule", new String[]{"Mul", "M"}, "Enables alerts for mule entities", true);
    public final Value<Boolean> hoverInfo = new Value<Boolean>("HoverInfo", new String[]{"InfoHover", "Hover", "Info", "IH", "HI"}, "Enables on-hover info for chat messages of alerts", true);
    public final Value<Boolean> tamed = new Value<Boolean>("Tamed", new String[]{"Tamed", "Tame", "Owned", "Owner", "T"}, "Enables alerts on your (tamed) animals as well", false);

    private final Timer timer = new Timer();

    public DonkeyAlertModule() {
        super("DonkeyAlert", new String[]{"Donkellama", "Donkeyllama", "DonkeyFinder", "LlamaFinder", "StorageEntityFinder", "Dllama", "Donkelama", "Donkeylama", "Donk", "DonkAlert", "DonkeyAlert", "LlamaAlert"}, "Alerts you about donkeys and llamas!! (horses, mules, too..)", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onAddEntity(EventAddEntity event) {
        final Entity entity = event.getEntity();
        final Minecraft mc = Minecraft.getMinecraft();
        if (entity != null && mc.player != null && this.timer.passed(this.delay.getValue())) {
            String alertText = "";
            String distance = "";

            if (mc.player.isRiding()) {
                if (mc.player.getRidingEntity() != null) {
                    if (mc.player.getRidingEntity().equals(entity))
                        return;
                }
            }

            if (this.donkey.getValue() && entity instanceof EntityDonkey) {
                final EntityDonkey entityDonkey = (EntityDonkey) entity;
                if (!this.tamed.getValue() && entityDonkey.getOwnerUniqueId() != null)
                    if (entityDonkey.getOwnerUniqueId().equals(mc.player.getUniqueID()))
                        return;
                alertText += "Donkey found";
            } else if (this.lama.getValue() && entity instanceof EntityLlama) {
                final EntityLlama entityLlama = (EntityLlama) entity;
                if (!this.tamed.getValue() && entityLlama.getOwnerUniqueId() != null)
                    if (entityLlama.getOwnerUniqueId().equals(mc.player.getUniqueID()))
                        return;
                alertText += "Llama found";
            } else if (this.horse.getValue() && entity instanceof EntityHorse) {
                final EntityHorse entityHorse = (EntityHorse) entity;
                if (!this.tamed.getValue() && entityHorse.getOwnerUniqueId() != null)
                    if (entityHorse.getOwnerUniqueId().equals(mc.player.getUniqueID()))
                        return;
                alertText += "Horse found";
            } else if (this.horse.getValue() && entity instanceof EntitySkeletonHorse) {
                final EntitySkeletonHorse entitySkeletonHorse = (EntitySkeletonHorse) entity;
                if (!this.tamed.getValue() && entitySkeletonHorse.getOwnerUniqueId() != null)
                    if (entitySkeletonHorse.getOwnerUniqueId().equals(mc.player.getUniqueID()))
                        return;
                alertText += "Skeleton horse found";
            } else if (this.mule.getValue() && entity instanceof EntityMule) {
                final EntityMule entityMule = (EntityMule) entity;
                if (!this.tamed.getValue() && entityMule.getOwnerUniqueId() != null)
                    if (entityMule.getOwnerUniqueId().equals(mc.player.getUniqueID()))
                        return;
                alertText += "Mule found";
            }

            if (!alertText.equals("")) {
                distance += (int) mc.player.getDistance(entity) + "m away";
                alertText += " " + ChatFormatting.GRAY + distance;

                if (this.mode.getValue() == Mode.NOTIFICATION || this.mode.getValue() == Mode.BOTH) {
                    Seppuku.INSTANCE.getNotificationManager().addNotification("", alertText, Notification.Type.INFO, 3000);
                }

                if (this.mode.getValue() == Mode.CHAT || this.mode.getValue() == Mode.BOTH) {
                    final TextComponentString storageEntityToChat = new TextComponentString(ChatFormatting.YELLOW + alertText);

                    if (this.hoverInfo.getValue()) {
                        String coords = String.format("X: %s, Y: %s, Z: %s", entity.getPosition().getX(), entity.getPosition().getY(), entity.getPosition().getZ());
                        String hoverText = entity.getName() + " : " + entity.getClass().getSimpleName() + "\n" + coords + "\n" + distance;
                        storageEntityToChat.setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(hoverText))));
                    }

                    Seppuku.INSTANCE.logcChat(storageEntityToChat);
                }

                this.timer.reset();
            }
        }
    }

    private enum Mode {
        CHAT, NOTIFICATION, BOTH
    }
}
