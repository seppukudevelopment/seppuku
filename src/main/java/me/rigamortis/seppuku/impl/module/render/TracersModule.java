package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.render.EventRender2D;
import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.GLUProjection;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.BooleanValue;
import me.rigamortis.seppuku.api.value.NumberValue;
import me.rigamortis.seppuku.api.value.OptionalValue;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/23/2019 @ 4:05 AM.
 */
public final class TracersModule extends Module {

    public final BooleanValue players = new BooleanValue("Players", new String[]{"Player"}, true);
    public final BooleanValue mobs = new BooleanValue("Mobs", new String[]{"Mob"}, true);
    public final BooleanValue animals = new BooleanValue("Animals", new String[]{"Animal"}, true);
    public final BooleanValue vehicles = new BooleanValue("Vehicles", new String[]{"Vehic", "Vehicle"}, true);
    public final BooleanValue items = new BooleanValue("Items", new String[]{"Item"}, true);

    public final OptionalValue mode = new OptionalValue("Mode", new String[]{"Mode"}, 0, new String[]{"2D", "3D"});

    public final NumberValue width = new NumberValue("Width", new String[]{"Wid"}, 0.5f, Float.class, 0.0f, 5.0f, 0.1f);

    public TracersModule() {
        super("Tracers", new String[]{"Trace", "Tracer", "Snapline", "Snaplines"}, "Draws a line to entities", "NONE", -1, ModuleType.RENDER);
    }

    @Override
    public String getMetaData() {
        return this.mode.getSelectedOption();
    }

    @Listener
    public void render2D(EventRender2D event) {
        if (this.mode.getInt() == 0) {
            final Minecraft mc = Minecraft.getMinecraft();

            for (Entity e : mc.world.loadedEntityList) {
                if (e != null) {
                    if (this.checkFilter(e)) {
                        final Vec3d pos = MathUtil.interpolateEntity(e, event.getPartialTicks());

                        if (pos != null) {
                            final GLUProjection.Projection projection = GLUProjection.getInstance().project(pos.x - mc.getRenderManager().viewerPosX, pos.y - mc.getRenderManager().viewerPosY, pos.z - mc.getRenderManager().viewerPosZ, GLUProjection.ClampMode.NONE, true);
                            if (projection != null) {
                                RenderUtil.drawLine((float) projection.getX(), (float) projection.getY(), event.getScaledResolution().getScaledWidth() / 2, event.getScaledResolution().getScaledHeight() / 2, this.width.getFloat(), this.getColor(e));
                            }
                        }
                    }
                }
            }
        }
    }

    @Listener
    public void render3D(EventRender3D event) {
        if (this.mode.getInt() == 1) {
            final Minecraft mc = Minecraft.getMinecraft();

            for (Entity e : mc.world.loadedEntityList) {
                if (e != null) {
                    if (this.checkFilter(e)) {
                        final Vec3d pos = MathUtil.interpolateEntity(e, event.getPartialTicks()).subtract(mc.getRenderManager().renderPosX, mc.getRenderManager().renderPosY, mc.getRenderManager().renderPosZ);

                        if (pos != null) {
                            final boolean bobbing = mc.gameSettings.viewBobbing;
                            mc.gameSettings.viewBobbing = false;
                            mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);
                            final Vec3d forward = new Vec3d(0, 0, 1).rotatePitch(-(float) Math.toRadians(Minecraft.getMinecraft().player.rotationPitch)).rotateYaw(-(float) Math.toRadians(Minecraft.getMinecraft().player.rotationYaw));
                            RenderUtil.drawLine3D((float) forward.x, (float) forward.y + mc.player.getEyeHeight(), (float) forward.z, (float) pos.x, (float) pos.y, (float) pos.z, this.width.getFloat(), this.getColor(e));
                            mc.gameSettings.viewBobbing = bobbing;
                            mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);
                        }
                    }
                }
            }
        }
    }

    private boolean checkFilter(Entity entity) {
        boolean ret = false;

        if (this.players.getBoolean() && entity instanceof EntityPlayer && entity != Minecraft.getMinecraft().player) {
            ret = true;
        }
        if (this.mobs.getBoolean() && entity instanceof IMob) {
            ret = true;
        }
        if (this.animals.getBoolean() && entity instanceof IAnimals && !(entity instanceof IMob)) {
            ret = true;
        }
        if (this.vehicles.getBoolean() && (entity instanceof EntityBoat || entity instanceof EntityMinecart || entity instanceof EntityMinecartContainer)) {
            ret = true;
        }
        if (this.items.getBoolean() && entity instanceof EntityItem) {
            ret = true;
        }

        return ret;
    }

    private int getColor(Entity entity) {
        int ret = -1;

        if (entity instanceof IAnimals && !(entity instanceof IMob)) {
            ret = 0xFF00FF44;
        }
        if (entity instanceof IMob) {
            ret = 0xFFFFAA00;
        }
        if (entity instanceof EntityBoat || entity instanceof EntityMinecart || entity instanceof EntityMinecartContainer) {
            ret = 0xFF00FFAA;
        }
        if (entity instanceof EntityItem) {
            ret = 0xFF00FFAA;
        }
        if (entity instanceof EntityPlayer) {
            ret = 0xFFFF4444;

            if (Seppuku.INSTANCE.getFriendManager().isFriend(entity) != null) {
                ret = 0xFF9900EE;
            }
        }
        return ret;
    }

}
