package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChunkData;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 8/25/2019 @ 10:27 PM.
 */
public final class ChestAlertModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "Change between alert modes.", Mode.CHAT);

    private enum Mode {
        CHAT, NOTIFICATION, BOTH
    }

    public ChestAlertModule() {
        super("ChestAlert", new String[]{"ChestAlerts"}, "Alerts you how many chests are in a chunk when it's loaded", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void recievePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.POST) {
            if (event.getPacket() instanceof SPacketChunkData) {
                final SPacketChunkData packet = (SPacketChunkData) event.getPacket();

                final Minecraft mc = Minecraft.getMinecraft();

                int count = 0;

                for(NBTTagCompound tag : packet.getTileEntityTags()) {
                    final String id = tag.getString("id");

                    if(id.equals("minecraft:chest")) {
                        count++;
                    }
                }

                if(count > 0) {
                    final String message = count + " Chests located at X: " + packet.getChunkX() * 16 + " Z: " + packet.getChunkZ() * 16;
                    if (this.mode.getValue() == Mode.CHAT || this.mode.getValue() == Mode.BOTH) {
                        Seppuku.INSTANCE.logChat(message);
                    }
                    if (this.mode.getValue() == Mode.NOTIFICATION || this.mode.getValue() == Mode.BOTH) {
                        Seppuku.INSTANCE.getNotificationManager().addNotification("", message);
                    }
                }

            }
        }
    }

}
