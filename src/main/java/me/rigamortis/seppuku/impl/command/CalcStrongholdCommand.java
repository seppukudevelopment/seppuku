package me.rigamortis.seppuku.impl.command;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.impl.module.hidden.CommandsModule;
import net.minecraft.client.Minecraft;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 12/12/2019 @ 1:48 PM.
 */
public final class CalcStrongholdCommand extends Command {

    private Vec3d firstStart;
    private Vec3d firstEnd;

    private Vec3d secondStart;
    private Vec3d secondEnd;

    private String[] resetAlias = new String[]{"Reset", "Res", "R"};

    public CalcStrongholdCommand() {
        super("CalcStronghold", new String[]{"CS", "FindStronghold", "cstrong"}, "Calculates where the nearest stronghold is.", "CalcStronghold\n" +
                "CalcStronghold Reset");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 1, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        if (split.length > 1) {
            if (equals(resetAlias, split[1])) {
                Seppuku.INSTANCE.logChat("Reset Stronghold finder");
                this.firstStart = null;
                this.firstEnd = null;
                this.secondStart = null;
                this.secondEnd = null;
                Seppuku.INSTANCE.getEventManager().removeEventListener(this);
            } else {
                Seppuku.INSTANCE.errorChat("Unknown input " + "\247f\"" + input + "\"");
            }
        } else {
            Seppuku.INSTANCE.getEventManager().addEventListener(this);
            Seppuku.INSTANCE.logChat("Please throw the first Eye Of Ender");
        }
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (this.firstStart != null && this.firstEnd != null && this.secondStart != null && this.secondEnd != null) {
                final double[] start = new double[]{this.secondStart.x, this.secondStart.z, this.secondEnd.x, this.secondEnd.z};
                final double[] end = new double[]{this.firstStart.x, this.firstStart.z, this.firstEnd.x, this.firstEnd.z};
                final double[] intersection = MathUtil.calcIntersection(start, end);

                if (Double.isNaN(intersection[0]) || Double.isNaN(intersection[1]) || Double.isInfinite(intersection[0]) || Double.isInfinite(intersection[1])) {
                    Seppuku.INSTANCE.errorChat("Error lines are parallel");
                    Seppuku.INSTANCE.getEventManager().removeEventListener(this);
                    return;
                }

                final double dist = Minecraft.getMinecraft().player.getDistance(intersection[0], Minecraft.getMinecraft().player.posY, intersection[1]);

                final TextComponentString component = new TextComponentString("Stronghold found " + ChatFormatting.GRAY + (int) dist + "m away");
                final String coords = String.format("X: %s, Y: ?, Z: %s\nClick to add a Waypoint", (int) intersection[0], (int) intersection[1]);
                final CommandsModule cmds = (CommandsModule) Seppuku.INSTANCE.getModuleManager().find(CommandsModule.class);

                if (cmds != null) {
                    component.setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(coords))).setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmds.prefix.getValue() + "Waypoints add Stronghold " + intersection[0] + " " + Minecraft.getMinecraft().player.posY + " " + intersection[1])));
                }

                Seppuku.INSTANCE.logcChat(component);
                Seppuku.INSTANCE.getNotificationManager().addNotification("", "Stronghold found " + ChatFormatting.GRAY + (int) dist + "m away");
                this.firstStart = null;
                this.firstEnd = null;
                this.secondStart = null;
                this.secondEnd = null;
                Seppuku.INSTANCE.getEventManager().removeEventListener(this);
            }
        }
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketSpawnObject) {
                final SPacketSpawnObject packet = (SPacketSpawnObject) event.getPacket();
                if (packet.getType() == 72) {
                    if (this.firstStart == null) {
                        this.firstStart = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                        Seppuku.INSTANCE.logChat("Found first Eye Of Ender start");
                    } else {
                        if (this.secondStart == null) {
                            this.secondStart = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                            Seppuku.INSTANCE.logChat("Found second Eye Of Ender start");
                        }
                    }
                }
            }
            if (event.getPacket() instanceof SPacketSoundEffect) {
                final SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();
                if (packet.getSound() == SoundEvents.ENTITY_ENDEREYE_DEATH) {
                    if (this.firstEnd == null) {
                        this.firstEnd = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                        Seppuku.INSTANCE.logChat("Found first Eye Of Ender end");
                        Seppuku.INSTANCE.logChat("Please throw the second Eye Of Ender");
                    } else {
                        if (this.secondEnd == null) {
                            this.secondEnd = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                            Seppuku.INSTANCE.logChat("Found second Eye Of Ender end");
                        }
                    }
                }
            }
        }
    }

}
