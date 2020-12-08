package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.event.render.EventRender2D;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.ColorUtil;
import me.rigamortis.seppuku.api.util.GLUProjection;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketClientStatus;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * Author Seth
 * 5/8/2019 @ 6:46 AM.
 */
public final class WaypointsModule extends Module {

    public final Value<Boolean> tracers = new Value<Boolean>("Tracers", new String[]{"Tracer", "Trace"}, "Draws a line from the center of the screen to each waypoint.", false);
    public final Value<Boolean> death = new Value<Boolean>("Death", new String[]{"deathpoint", "d"}, "Creates a waypoint on death.", true);

    public final Value<Float> width = new Value<Float>("Width", new String[]{"Wid"}, "Pixel width of each tracer line.", 0.5f, 0.0f, 5.0f, 0.1f);

    public WaypointsModule() {
        super("Waypoints", new String[]{"Wp", "Waypoint"}, "Highlights waypoints", "NONE", -1, ModuleType.WORLD);
    }

    @Listener
    public void render2D(EventRender2D event) {
        final Minecraft mc = Minecraft.getMinecraft();

        final String host = mc.getCurrentServerData() == null ? "localhost" : mc.getCurrentServerData().serverIP;

        for (WaypointData waypointData : Seppuku.INSTANCE.getWaypointManager().getWaypointDataList()) {
            if (waypointData != null) {
                if (host.equalsIgnoreCase(waypointData.getHost()) && mc.player.dimension == waypointData.dimension) {
                    final double dist = mc.player.getDistance(waypointData.getX(), waypointData.getY(), waypointData.getZ());
                    if (dist >= 5.0f) {
                        final GLUProjection.Projection projection = GLUProjection.getInstance().project(waypointData.getX() - mc.getRenderManager().viewerPosX, waypointData.getY() - mc.getRenderManager().viewerPosY, waypointData.getZ() - mc.getRenderManager().viewerPosZ, GLUProjection.ClampMode.NONE, false);

                        if (projection != null && projection.getType() == GLUProjection.Projection.Type.INSIDE) {
                            final String name = "\247f" + waypointData.getName() + " \247r(\2477" + new DecimalFormat("#.#").format(dist) + "m\247r)";
                            mc.fontRenderer.drawStringWithShadow(name, (float) projection.getX() - mc.fontRenderer.getStringWidth(name) / 2, (float) projection.getY() - mc.fontRenderer.FONT_HEIGHT / 2, waypointData.getColor());
                        }

                        if (this.tracers.getValue()) {
                            final GLUProjection.Projection screen = GLUProjection.getInstance().project(waypointData.getX() - mc.getRenderManager().viewerPosX, waypointData.getY() - mc.getRenderManager().viewerPosY, waypointData.getZ() - mc.getRenderManager().viewerPosZ, GLUProjection.ClampMode.NONE, true);

                            if (screen != null) {
                                RenderUtil.drawLine((float) screen.getX(), (float) screen.getY(), event.getScaledResolution().getScaledWidth() / 2, event.getScaledResolution().getScaledHeight() / 2, this.width.getValue(), -1);
                            }
                        }
                    }
                }
            }
        }
    }

    @Listener
    public void onSendPacket(EventSendPacket event) {
        if (event.getStage() != EventStageable.EventStage.PRE)
            return;

        if (event.getPacket() instanceof CPacketClientStatus) {
            if (!this.death.getValue())
                return;

            if (((CPacketClientStatus) event.getPacket()).getStatus().equals(CPacketClientStatus.State.PERFORM_RESPAWN)) {
                final String host = Minecraft.getMinecraft().getCurrentServerData() != null ? Minecraft.getMinecraft().getCurrentServerData().serverIP : "localhost";
                Seppuku.INSTANCE.getWaypointManager().getWaypointDataList().add(new WaypointData(host, "death-" + new SimpleDateFormat("yyyy-MM-dd@HH:mm:ss").format(new Timestamp(System.currentTimeMillis())), Minecraft.getMinecraft().player.dimension, Minecraft.getMinecraft().player.posX, Minecraft.getMinecraft().player.posY + Minecraft.getMinecraft().player.getEyeHeight(), Minecraft.getMinecraft().player.posZ));
            }
        }
    }

    public static final class WaypointData {
        private String host;
        private String name;
        private int dimension;
        private double x;
        private double y;
        private double z;
        private int color;

        public WaypointData(String host, String name, int dimension, double x, double y, double z) {
            this.host = host;
            this.name = name;
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            this.color = ColorUtil.getRandomColor();
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getDimension() {
            return dimension;
        }

        public void setDimension(int dimension) {
            this.dimension = dimension;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getZ() {
            return z;
        }

        public void setZ(double z) {
            this.z = z;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }
    }

}
