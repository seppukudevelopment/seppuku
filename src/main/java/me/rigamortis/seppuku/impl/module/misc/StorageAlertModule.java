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
public final class StorageAlertModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "Change between alert modes.", Mode.CHAT);
    public final Value<Boolean> chests = new Value<Boolean>("Chests", new String[]{"Chests", "chest"}, "Count chests.", true);
    public final Value<Boolean> echests = new Value<Boolean>("EnderChests", new String[]{"EnderChests", "echest", "echest"}, "Count ender chests.", false);
    public final Value<Boolean> shulkers = new Value<Boolean>("ShulkerBoxes", new String[]{"ShulkerBoxes", "shul"}, "Count shulkers.", false);
    public final Value<Boolean> hoppers = new Value<Boolean>("Hoppers", new String[]{"Hoppers", "hopp"}, "Count hoppers.", false);
    public final Value<Boolean> droppers = new Value<Boolean>("Droppers", new String[]{"Droppers", "drop"}, "Count droppers.", false);
    public final Value<Boolean> dispensers = new Value<Boolean>("Dispensers", new String[]{"Dispensers", "disp"}, "Count dispensers.", false);
    public final Value<Boolean> stands = new Value<Boolean>("BrewingStands", new String[]{"BrewingStands", "brew"}, "Count brewing stands.", false);

    private enum Mode {
        CHAT, NOTIFICATION, BOTH
    }

    public StorageAlertModule() {
        super("StorageAlert", new String[]{"StorageAlerts"}, "Alerts you how many storage blocks are in a chunk when it's loaded", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void recievePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.POST) {
            if (event.getPacket() instanceof SPacketChunkData) {
                final SPacketChunkData packet = (SPacketChunkData) event.getPacket();

                final Minecraft mc = Minecraft.getMinecraft();

                int count = 0;

                for (NBTTagCompound tag : packet.getTileEntityTags()) {
                    final String id = tag.getString("id");

                    if (
                            (this.chests.getValue() && (id.equals("minecraft:chest") || id.equals("minecraft:trapped_chest"))) ||
                                    (this.echests.getValue() && id.equals("minecraft:ender_chest")) ||
                                    (this.shulkers.getValue() && id.equals("minecraft:shulker_box")) ||
                                    (this.hoppers.getValue() && id.equals("minecraft:hopper")) ||
                                    (this.droppers.getValue() && id.equals("minecraft:dropper")) ||
                                    (this.dispensers.getValue() && id.equals("minecraft:dispenser")) ||
                                    (this.stands.getValue() && id.equals("minecraft:brewing_stand"))
                    ) {
                        count++;
                    }
                }

                if (count > 0) {
                    final String message = count + " storage blocks located at X: " + packet.getChunkX() * 16 + " Z: " + packet.getChunkZ() * 16;
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
