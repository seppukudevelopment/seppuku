package me.rigamortis.seppuku.impl.module.misc;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.FileUtil;
import me.rigamortis.seppuku.api.value.Value;
import me.rigamortis.seppuku.impl.module.hidden.CommandsModule;
import me.rigamortis.seppuku.impl.module.render.LogoutSpotsModule;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Author Seth
 * 8/25/2019 @ 10:27 PM.
 */
public final class StorageAlertModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "Change between alert modes.", Mode.CHAT);
    public final Value<Boolean> saveToFile = new Value<Boolean>("SaveToFile", new String[]{"Save", "Saves"}, "Saves the alert to a file in your Seppuku directory.", false);
    public final Value<Boolean> chests = new Value<Boolean>("Chests", new String[]{"Chests", "chest"}, "Count chests.", true);
    public final Value<Boolean> echests = new Value<Boolean>("EnderChests", new String[]{"EnderChests", "echest", "echest"}, "Count ender chests.", false);
    public final Value<Boolean> shulkers = new Value<Boolean>("ShulkerBoxes", new String[]{"ShulkerBoxes", "shul"}, "Count shulkers.", false);
    public final Value<Boolean> hoppers = new Value<Boolean>("Hoppers", new String[]{"Hoppers", "hopp"}, "Count hoppers.", false);
    public final Value<Boolean> droppers = new Value<Boolean>("Droppers", new String[]{"Droppers", "drop"}, "Count droppers.", false);
    public final Value<Boolean> dispensers = new Value<Boolean>("Dispensers", new String[]{"Dispensers", "disp"}, "Count dispensers.", false);
    public final Value<Boolean> stands = new Value<Boolean>("BrewingStands", new String[]{"BrewingStands", "brew"}, "Count brewing stands.", false);

    private final File locationsFile;
    private CommandsModule commandsModule;

    private enum Mode {
        CHAT, NOTIFICATION, BOTH
    }

    public StorageAlertModule() {
        super("StorageAlert", new String[]{"StorageAlerts", "ChestAlert"}, "Alerts you how many storage blocks are in a chunk when it's loaded", "NONE", -1, ModuleType.MISC);

        this.locationsFile = new File(Seppuku.INSTANCE.getConfigManager().getConfigDir(), "StorageAlerts.txt");
        try {
            if (!locationsFile.exists())
                locationsFile.createNewFile();
        } catch (IOException e) {
            Seppuku.INSTANCE.getLogger().log(Level.WARNING, "Couldn't create StorageAlert locations file.");
        }
    }

    @Listener
    public void recievePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.POST) {
            if (event.getPacket() instanceof SPacketChunkData) {
                final SPacketChunkData packet = (SPacketChunkData) event.getPacket();
                final Vec2f position = new Vec2f(packet.getChunkX() * 16, packet.getChunkZ() * 16);
                final Map<String, Vec2f> foundStorage = Maps.newLinkedHashMap();

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
                        foundStorage.put(id, position);
                    }
                }

                if (foundStorage.size() > 0) {
                    final String message = foundStorage.size() + " storage blocks located at X: " + position.x + " Z: " + position.y;
                    if (this.mode.getValue() == Mode.CHAT || this.mode.getValue() == Mode.BOTH) {
                        if (this.commandsModule == null) {
                            this.commandsModule = (CommandsModule) Seppuku.INSTANCE.getModuleManager().find(CommandsModule.class);
                        } else {
                            final TextComponentString textComponent = new TextComponentString(ChatFormatting.YELLOW + message);
                            textComponent.appendSibling(new TextComponentString("(*)")
                                    .setStyle(new Style()
                                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("\2476" + "Create a waypoint for this position.")))
                                            .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandsModule.getPrefix().getValue() + "waypoint add " + String.format("x%s_z%s", position.x, position.y) + " " + position.x + " 120 " + position.y))));
                            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(textComponent);
                        }
                    }
                    if (this.mode.getValue() == Mode.NOTIFICATION || this.mode.getValue() == Mode.BOTH) {
                        Seppuku.INSTANCE.getNotificationManager().addNotification("", message);
                    }

                    if (this.saveToFile.getValue()) {
                        this.saveStorageToFile(foundStorage);
                    }
                }
            }
        }
    }

    public void saveStorageToFile(Map<String, Vec2f> foundStorage) {
        final String time = new SimpleDateFormat().format(new Date());
        final String host = Minecraft.getMinecraft().getCurrentServerData() != null ? Minecraft.getMinecraft().getCurrentServerData().serverIP : "localhost";
        final List<String> linesToAdd = new ArrayList<>();

        for (String type : foundStorage.keySet()) {
            final Vec2f position = foundStorage.get(type);
            String data = String.format("server: %s, date: %s, type: %s, position: %s", host, time, type, String.format("X: %s, Z: %s", position.x, position.y));
            linesToAdd.add(data);
        }

        FileUtil.write(locationsFile, linesToAdd, false);
    }
}
