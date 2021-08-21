package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.render.EventRender2D;
import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.ColorUtil;
import me.rigamortis.seppuku.api.util.GLUProjection;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.awt.*;

/**
 * @author Seth
 * @author noil
 */
public final class TracersModule extends Module {

    public final Value<Boolean> players = new Value<Boolean>("Players", new String[]{"Player"}, "Choose to enable on players.", true);
    public final Value<Color> playersColor = new Value<Color>("PlayersColor", new String[]{"playerscolor", "pc"}, "Change the color of player tracer lines.", new Color(255, 68, 68));

    public final Value<Boolean> mobs = new Value<Boolean>("Mobs", new String[]{"Mob"}, "Choose to enable on mobs.", true);
    public final Value<Color> mobsColor = new Value<Color>("MobsColor", new String[]{"mobscolor", "mc"}, "Change the color of mob tracer lines.", new Color(255, 170, 0));

    public final Value<Boolean> animals = new Value<Boolean>("Animals", new String[]{"Animal"}, "Choose to enable on animals.", true);
    public final Value<Color> animalsColor = new Value<Color>("AnimalsColor", new String[]{"animalscolor", "ac"}, "Change the color of animal tracer lines.", new Color(0, 255, 68));

    public final Value<Boolean> vehicles = new Value<Boolean>("Vehicles", new String[]{"Vehic", "Vehicle"}, "Choose to enable on vehicles.", true);
    public final Value<Color> vehiclesColor = new Value<Color>("VehiclesColor", new String[]{"vehiclescolor", "vc"}, "Change the color of vehicle tracer lines.", new Color(213, 255, 0));

    public final Value<Boolean> items = new Value<Boolean>("Items", new String[]{"Item"}, "Choose to enable on items.", true);
    public final Value<Color> itemsColor = new Value<Color>("ItemsColor", new String[]{"itemscolor", "ic"}, "Change the color of item tracer lines.", new Color(0, 255, 170));

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode"}, "The rendering mode to use for drawing the tracer-line.", Mode.TWO_D);

    public final Value<Color> friendsColor = new Value<Color>("FriendsColor", new String[]{"friendscolor", "fc"}, "Change the color of added friends tracer lines.", new Color(153, 0, 238));

    private enum Mode {
        TWO_D, THREE_D // TWO_DIMENSIONAL, THREE_DIMENSIONAL
    }

    public final Value<Float> width = new Value<Float>("Width", new String[]{"Wid"}, "Pixel width of each tracer-line.", 0.5f, 0.0f, 5.0f, 0.1f);
    public final Value<Integer> alpha = new Value<Integer>("Alpha", new String[]{"Alpha", "A", "Opacity", "Op"}, "Alpha value for each drawn line.", 255, 1, 255, 1);

    public TracersModule() {
        super("Tracers", new String[]{"Trace", "Tracer", "Snapline", "Snaplines"}, "Draws a line to entities", "NONE", -1, ModuleType.RENDER);
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    @Listener
    public void render2D(EventRender2D event) {
        if (this.mode.getValue() == Mode.TWO_D) {
            final Minecraft mc = Minecraft.getMinecraft();

            for (Entity e : mc.world.loadedEntityList) {
                if (e != null) {
                    if (this.checkFilter(e)) {
                        final Vec3d pos = MathUtil.interpolateEntity(e, event.getPartialTicks());
                        final GLUProjection.Projection projection = GLUProjection.getInstance().project(pos.x - mc.getRenderManager().viewerPosX, pos.y - mc.getRenderManager().viewerPosY, pos.z - mc.getRenderManager().viewerPosZ, GLUProjection.ClampMode.NONE, true);
                        RenderUtil.drawLine((float) projection.getX(), (float) projection.getY(), (float) event.getScaledResolution().getScaledWidth() / 2.0f, (float) event.getScaledResolution().getScaledHeight() / 2.0f, this.width.getValue(), this.getColor(e));
                    }
                }
            }
        }
    }

    @Listener
    public void render3D(EventRender3D event) {
        if (this.mode.getValue() == Mode.THREE_D) {
            final Minecraft mc = Minecraft.getMinecraft();

            RenderUtil.begin3D();
            for (Entity e : mc.world.loadedEntityList) {
                if (e != null) {
                    if (this.checkFilter(e)) {
                        final Vec3d pos = MathUtil.interpolateEntity(e, event.getPartialTicks()).subtract(mc.getRenderManager().renderPosX, mc.getRenderManager().renderPosY, mc.getRenderManager().renderPosZ);
                        // need to update modelview matrix or it freaks out when rendering another tracer, not sure why though
                        // XXX this is done in other places, ctrl+shift+f to other files
                        RenderUtil.updateModelViewProjectionMatrix();
                        final GLUProjection.Vector3D forward = GLUProjection.getInstance().getLookVector().sadd(GLUProjection.getInstance().getCamPos());
                        RenderUtil.drawLine3D(forward.x, forward.y, forward.z, pos.x, pos.y, pos.z, this.width.getValue(), this.getColor(e));
                    }
                }
            }
            RenderUtil.end3D();
        }
    }

    private boolean checkFilter(Entity entity) {
        boolean ret = false;

        if (this.players.getValue() && entity instanceof EntityPlayer && entity != Minecraft.getMinecraft().player) {
            ret = true;
        }

        if (this.mobs.getValue() && entity instanceof IMob) {
            ret = true;
        }

        if (this.animals.getValue() && entity instanceof IAnimals && !(entity instanceof IMob)) {
            ret = true;
        }

        if (this.vehicles.getValue() && (entity instanceof EntityBoat || entity instanceof EntityMinecart)) {
            ret = true;
        }

        if (this.items.getValue() && entity instanceof EntityItem) {
            ret = true;
        }

        return ret;
    }

    private int getColor(Entity entity) {
        int ret = -1;

        if (entity instanceof IAnimals && !(entity instanceof IMob)) {
            ret = this.animalsColor.getValue().getRGB();
        }

        if (entity instanceof IMob) {
            ret = this.mobsColor.getValue().getRGB();
        }

        if (entity instanceof EntityBoat || entity instanceof EntityMinecart) {
            ret = this.vehiclesColor.getValue().getRGB();
        }

        if (entity instanceof EntityItem) {
            ret = this.itemsColor.getValue().getRGB();
        }

        if (entity instanceof EntityPlayer) {
            ret = this.playersColor.getValue().getRGB();

            if (Seppuku.INSTANCE.getFriendManager().isFriend(entity) != null) {
                ret = this.friendsColor.getValue().getRGB();
            }
        }

        return ColorUtil.changeAlpha(ret, this.alpha.getValue());
    }

}
