package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.event.render.EventRender2D;
import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.event.world.EventLoadWorld;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.ColorUtil;
import me.rigamortis.seppuku.api.util.GLUProjection;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.util.glu.Sphere;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * Author Seth
 * 5/8/2019 @ 6:46 AM.
 */
public final class WaypointsModule extends Module {

    public final Value<Boolean> death = new Value<Boolean>("Death", new String[]{"deathpoint", "d"}, "Creates a waypoint on death.", true);
    public final Value<Float> hideDistance = new Value<Float>("HideDistance", new String[]{"hidedist", "hd"}, "Distance (blocks) away from a waypoint to hide it.", 5.0f, 1.0f, 10.0f, 0.5f);

    public final Value<Boolean> tracers = new Value<Boolean>("Tracers", new String[]{"Tracer", "Trace"}, "Draws a line from the center of the screen to each waypoint.", false);
    public final Value<Float> tracersWidth = new Value<Float>("TracersWidth", new String[]{"twidth", "tw"}, "Pixel width of each tracer line.", 1.0f, 0.1f, 5.0f, 0.1f);
    public final Value<Integer> tracersAlpha = new Value<Integer>("TracersAlpha", new String[]{"talpha", "ta", "topacity", "top"}, "Alpha value for each drawn line.", 255, 1, 255, 1);

    public final Value<Boolean> box = new Value<Boolean>("Box", new String[]{"b"}, "Renders a 3D object at each waypoint.", true);
    public final Value<Shape> shape = new Value<Shape>("Shape", new String[]{"s"}, "Selects what shape should be rendered.", Shape.CUBE);
    public final Value<Boolean> boxRotate = new Value<Boolean>("BoxRotate", new String[]{"brotate", "rotate", "br"}, "Rotates each 3D object around in a circle.", true);
    public final Value<Float> boxRotateSpeed = new Value<Float>("BoxRotateSpeed", new String[]{"brotatespeed", "rotatespeed", "spinspeed", "brs"}, "The speed at which the 3D object rotates around.", 0.5f, 0.1f, 2.0f, 0.1f);
    public final Value<Float> boxWidth = new Value<Float>("BoxWidth", new String[]{"width", "w"}, "Pixel width of the 3D objects lines.", 1f, 0.1f, 5.0f, 0.1f);
    public final Value<Integer> boxAlpha = new Value<Integer>("BoxAlpha", new String[]{"balpha", "ba", "bopacity", "bop"}, "Alpha value for the 3D rendered object.", 127, 1, 255, 1);
    public final Value<Float> boxSize = new Value<Float>("BoxSize", new String[]{"size", "scale", "s"}, "Size of the 3D rendered object.", 0.5f, 0.1f, 3.0f, 0.1f);
    public final Value<Float> boxYOffset = new Value<Float>("BoxYOffset", new String[]{"byoffset", "byoff", "byo"}, "Y-level offset of the 3D rendered object.", 0.0f, -1.0f, 1.0f, 0.1f);
    public final Value<Float> diamondYIncrease = new Value<Float>("DiamondExtraY", new String[]{"de", "dextray", "diamondy"}, "Extra height added to the top of the diamond object.", 0.5f, 0.1f, 3.0f, 0.1f);

    public enum Shape {
        CUBE, PYRAMID, DIAMOND, SPHERE
    }


    private final Minecraft mc = Minecraft.getMinecraft();
    private String host = "";
    private float angle = 0;

    public WaypointsModule() {
        super("Waypoints", new String[]{"Wp", "Waypoint"}, "Highlights waypoints", "NONE", -1, ModuleType.WORLD);
    }

    @Listener
    public void render2D(EventRender2D event) {
        for (WaypointData waypointData : Seppuku.INSTANCE.getWaypointManager().getWaypointDataList()) {
            if (waypointData != null) {
                if (host.equalsIgnoreCase(waypointData.getHost()) && mc.player.dimension == waypointData.dimension) {
                    final double dist = mc.player.getDistance(waypointData.getX(), waypointData.getY(), waypointData.getZ());
                    if (dist >= this.hideDistance.getValue()) {
                        final int color = ColorUtil.changeAlpha(waypointData.color, this.tracersAlpha.getValue());
                        final GLUProjection.Projection projection = GLUProjection.getInstance().project(waypointData.getX() - mc.getRenderManager().viewerPosX, waypointData.getY() - mc.getRenderManager().viewerPosY, waypointData.getZ() - mc.getRenderManager().viewerPosZ, GLUProjection.ClampMode.NONE, false);

                        if (projection.getType() == GLUProjection.Projection.Type.INSIDE) {
                            final String name = "\247f" + waypointData.getName() + " \247r(\2477" + new DecimalFormat("#.#").format(dist) + "m\247r)";
                            mc.fontRenderer.drawStringWithShadow(name, (float) projection.getX() - mc.fontRenderer.getStringWidth(name) / 2.0f, (float) projection.getY() - mc.fontRenderer.FONT_HEIGHT / 2.0f, color);
                        }

                        if (this.tracers.getValue()) {
                            final GLUProjection.Projection screen = GLUProjection.getInstance().project(waypointData.getX() - mc.getRenderManager().viewerPosX, waypointData.getY() - mc.getRenderManager().viewerPosY, waypointData.getZ() - mc.getRenderManager().viewerPosZ, GLUProjection.ClampMode.NONE, true);
                            RenderUtil.drawLine((float) screen.getX(), (float) screen.getY(), event.getScaledResolution().getScaledWidth() / 2.0f, event.getScaledResolution().getScaledHeight() / 2.0f, this.tracersWidth.getValue(), color);
                        }
                    }
                }
            }
        }
    }

    @Listener
    public void onRender3D(EventRender3D event) {
        if (!this.box.getValue()) // doesn't want to render the 3D object
            return;

        if (this.boxRotate.getValue()) {
            if (this.angle > 360.0f)
                this.angle = 0.0f;
            else
                this.angle += this.boxRotateSpeed.getValue();
        }


        RenderUtil.begin3D();
        for (WaypointData waypointData : Seppuku.INSTANCE.getWaypointManager().getWaypointDataList()) {
            if (waypointData != null) {
                if (host.equalsIgnoreCase(waypointData.getHost()) && mc.player.dimension == waypointData.dimension) {
                    final double dist = mc.player.getDistance(waypointData.getX(), waypointData.getY(), waypointData.getZ());
                    if (dist >= this.hideDistance.getValue()) {
                        final int color = ColorUtil.changeAlpha(waypointData.color, this.boxAlpha.getValue());
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(waypointData.x - mc.getRenderManager().viewerPosX, waypointData.y - mc.getRenderManager().viewerPosY, waypointData.z - mc.getRenderManager().viewerPosZ);
                        if (this.boxRotate.getValue()) {
                            GlStateManager.rotate(this.angle, 0, 1, 0);
                        }
                        final AxisAlignedBB bb = new AxisAlignedBB(
                                -this.boxSize.getValue(),
                                -this.boxSize.getValue() + this.boxYOffset.getValue(),
                                -this.boxSize.getValue(),
                                this.boxSize.getValue(),
                                this.boxSize.getValue() + this.boxYOffset.getValue(),
                                this.boxSize.getValue());

                        switch (shape.getValue()) {
                            case CUBE:
                                RenderUtil.drawFilledBox(bb, color);
                                RenderUtil.drawBoundingBox(bb, this.boxWidth.getValue(), color);
                                break;
                            case PYRAMID:
                                RenderUtil.drawFilledPyramid(bb, color);
                                RenderUtil.drawBoundingBoxPyramid(bb, this.boxWidth.getValue(), color);
                                break;
                            case DIAMOND:
                                RenderUtil.drawFilledDiamond(bb, this.boxYOffset.getValue(), this.diamondYIncrease.getValue(), color);
                                RenderUtil.drawBoundingBoxDiamond(bb, this.boxWidth.getValue(), this.boxYOffset.getValue(), this.diamondYIncrease.getValue(), color);
                                break;
                            case SPHERE:
                                RenderUtil.drawSphere(boxSize.getValue(), 32, 32, color);
                                break;
                        }

                        GlStateManager.popMatrix();
                    }
                }
            }
        }
        RenderUtil.end3D();
    }

    @Listener
    public void onLoadWorld(EventLoadWorld event) {
        if (event.getWorld() != null) {
            this.host = mc.getCurrentServerData() == null ? "localhost" : mc.getCurrentServerData().serverIP;
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
