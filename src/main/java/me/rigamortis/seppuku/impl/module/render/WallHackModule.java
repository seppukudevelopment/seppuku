package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.render.EventRender2D;
import me.rigamortis.seppuku.api.event.render.EventRenderName;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.*;
import me.rigamortis.seppuku.api.value.BooleanValue;
import me.rigamortis.seppuku.api.value.OptionalValue;
import me.rigamortis.seppuku.impl.module.render.NametagsModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Author Seth
 * 4/20/2019 @ 10:07 AM.
 */
public final class WallHackModule extends Module {

    public final OptionalValue mode = new OptionalValue("Mode", new String[]{"Mode", "M"}, 0, new String[]{"None", "Box"});

    public final BooleanValue players = new BooleanValue("Players", new String[]{"Player"}, true);
    public final BooleanValue mobs = new BooleanValue("Mobs", new String[]{"Mob"}, true);
    public final BooleanValue animals = new BooleanValue("Animals", new String[]{"Animal"}, true);
    public final BooleanValue vehicles = new BooleanValue("Vehicles", new String[]{"Vehic", "Vehicle"}, true);
    public final BooleanValue endcrystals = new BooleanValue("EndCrystals", new String[]{"Crystals", "Cryst"}, true);
    public final BooleanValue local = new BooleanValue("Local", new String[]{"Self"}, true);
    public final BooleanValue items = new BooleanValue("Items", new String[]{"Item"}, true);
    public final BooleanValue footsteps = new BooleanValue("FootSteps", new String[]{"FootStep", "Steps"}, false);
    public final BooleanValue armorStand = new BooleanValue("ArmorStands", new String[]{"ArmorStand", "ArmourStand", "ArmourStands", "ArmStand"}, true);

    public final OptionalValue hpMode = new OptionalValue("Hp", new String[]{"Health", "HpMode"}, 0, new String[]{"None", "Bar", "BarText"});

    private ICamera camera = new Frustum();

    //i cba
    private List<FootstepData> footstepDataList = new CopyOnWriteArrayList<>();

    public static List<Entity> entitylist = new ArrayList<>();

    public static GLUProjection.Projection projection = null;

    public WallHackModule() {
        super("WallHack", new String[]{"Esp"}, "Highlights entities", "NONE", -1, ModuleType.RENDER);
    }



    @Listener
    public void render2D(EventRender2D event) {
        final Minecraft mc = Minecraft.getMinecraft();

        entitylist = Minecraft.getMinecraft().world.loadedEntityList;


        if (this.footsteps.getBoolean()) {
            for (FootstepData data : this.footstepDataList) {
                final GLUProjection.Projection projection = GLUProjection.getInstance().project(data.x - mc.getRenderManager().viewerPosX, data.y - mc.getRenderManager().viewerPosY, data.z - mc.getRenderManager().viewerPosZ, GLUProjection.ClampMode.NONE, false);
                if (projection != null && projection.getType() == GLUProjection.Projection.Type.INSIDE) {
                    mc.fontRenderer.drawStringWithShadow("*step*", (float) projection.getX() - mc.fontRenderer.getStringWidth("*step*") / 2, (float) projection.getY(), -1);
                }

                if (Math.abs(System.currentTimeMillis() - data.getTime()) >= 3000) {
                    this.footstepDataList.remove(data);
                }
            }
        }

        for (Entity e : entitylist) {
            if (e != null) {
                if (this.checkFilter(e)) {
                    final float[] bounds = this.convertBounds(e, event.getPartialTicks(), event.getScaledResolution().getScaledWidth(), event.getScaledResolution().getScaledHeight());

                    if (bounds != null) {
                        if (this.mode.getInt() == 1) {
                            RenderUtil.drawOutlineRect(bounds[0], bounds[1], bounds[2], bounds[3], 1.5f, 0xAA000000);
                            RenderUtil.drawOutlineRect(bounds[0] - 0.5f, bounds[1] - 0.5f, bounds[2] + 0.5f, bounds[3] + 0.5f, 0.5f, this.getColor(e));
                        }


                        if (e instanceof EntityLivingBase) {

                            if (this.hpMode.getInt() != 0) {
                                RenderUtil.drawRect(bounds[2] - 0.5f, bounds[1], bounds[2] - 2, bounds[3], 0xAA000000);
                                final float hpHeight = ((((EntityLivingBase) e).getHealth() * (bounds[3] - bounds[1])) / ((EntityLivingBase) e).getMaxHealth());

                                RenderUtil.drawRect(bounds[2] - 1, bounds[1] - 0.5f, bounds[2] - 1.5f, (bounds[1] - bounds[3]) + bounds[3] + hpHeight + 0.5f, getHealthColor(e));

                                if (this.hpMode.getInt() == 2) {
                                    if (((EntityLivingBase) e).getHealth() < ((EntityLivingBase) e).getMaxHealth() && ((EntityLivingBase) e).getHealth() > 0) {
                                        final String hp = new DecimalFormat("#.#").format((int) ((EntityLivingBase) e).getHealth());
                                        mc.fontRenderer.drawStringWithShadow(hp, (bounds[2] - 1 - mc.fontRenderer.getStringWidth(hp) / 2), ((bounds[1] - bounds[3]) + bounds[3] + hpHeight + 0.5f - mc.fontRenderer.FONT_HEIGHT / 2), -1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketSoundEffect) {
                final SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();

                if (packet.getCategory() == SoundCategory.NEUTRAL || packet.getCategory() == SoundCategory.PLAYERS) {
                    final String sound = packet.getSound().getSoundName().getPath();
                    if (sound.endsWith(".step") || sound.endsWith(".paddle_land") || sound.endsWith(".gallop")) {
                        this.footstepDataList.add(new FootstepData(packet.getX(), packet.getY(), packet.getZ(), System.currentTimeMillis()));
                    }
                }
            }
        }
    }

    private int getHealthColor(Entity entity) {
        int scale = (int) Math.round(255.0 - (double) ((EntityLivingBase) entity).getHealth() * 255.0 / (double) ((EntityLivingBase) entity).getMaxHealth());
        int damageColor = 255 - scale << 8 | scale << 16;

        return (255 << 24) | damageColor;
    }

    @Listener
    public void renderName(EventRenderName event) {
        if (event.getEntity() instanceof EntityPlayer) {
            event.setCanceled(true);
        }
    }


    private boolean checkFilter(Entity entity) {
        boolean ret = false;

        if (this.local.getBoolean() && (entity == Minecraft.getMinecraft().player) && (Minecraft.getMinecraft().gameSettings.thirdPersonView != 0)) {
            ret = true;
        }
        if (this.players.getBoolean() && entity instanceof EntityPlayer && entity != Minecraft.getMinecraft().player) {
            ret = true;
        }
        if (this.mobs.getBoolean() && entity instanceof IMob) {
            ret = true;
        }
        if (this.animals.getBoolean() && entity instanceof IAnimals && !(entity instanceof IMob)) {
            ret = true;
        }
        if (this.items.getBoolean() && entity instanceof EntityItem) {
            ret = true;
        }
        if (this.vehicles.getBoolean() && (entity instanceof EntityBoat || entity instanceof EntityMinecart || entity instanceof EntityMinecartContainer)) {
            ret = true;
        }
        if (this.armorStand.getBoolean() && entity instanceof EntityArmorStand) {
            ret = true;
        }
        if (this.endcrystals.getBoolean() && entity instanceof EntityEnderCrystal){
            ret = true;
        }
        if (Minecraft.getMinecraft().player.getRidingEntity() != null && entity == Minecraft.getMinecraft().player.getRidingEntity()) {
            ret = false;
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
        if (entity instanceof EntityEnderCrystal){
            ret =0xFFCD00CD;
        }

        if (entity instanceof EntityItem) {
            ret = 0xFF00FFAA;
        }
        if (entity instanceof EntityPlayer) {
            ret = 0xFFFF4444;

            if (entity == Minecraft.getMinecraft().player) {
                ret = -1;
            }

            if (entity.isSneaking()) {
                ret = 0xFFEE9900;
            }

            if (Seppuku.INSTANCE.getFriendManager().isFriend(entity) != null) {
                ret = 0xFF9900EE;
            }
        }
        return ret;
    }

    private float[] convertBounds(Entity e, float partialTicks, int width, int height) {
        float x = -1;
        float y = -1;
        float w = width + 1;
        float h = height + 1;

        final Vec3d pos = MathUtil.interpolateEntity(e, partialTicks);

        if (pos == null) {
            return null;
        }

        AxisAlignedBB bb = e.getEntityBoundingBox();

        if (e instanceof EntityEnderCrystal) {
            bb = new AxisAlignedBB(bb.minX + 0.3f, bb.minY + 0.2f, bb.minZ + 0.3f, bb.maxX - 0.3f, bb.maxY, bb.maxZ - 0.3f);
        }

        if (e instanceof EntityItem) {
            bb = new AxisAlignedBB(bb.minX, bb.minY + 0.7f, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
        }

        bb = bb.expand(0.15f, 0.1f, 0.15f);

        camera.setPosition(Minecraft.getMinecraft().getRenderViewEntity().posX, Minecraft.getMinecraft().getRenderViewEntity().posY, Minecraft.getMinecraft().getRenderViewEntity().posZ);

        if (!camera.isBoundingBoxInFrustum(bb)) {
            return null;
        }

        final Vec3d corners[] = {
                new Vec3d(bb.minX - bb.maxX + e.width / 2, 0, bb.minZ - bb.maxZ + e.width / 2),
                new Vec3d(bb.maxX - bb.minX - e.width / 2, 0, bb.minZ - bb.maxZ + e.width / 2),
                new Vec3d(bb.minX - bb.maxX + e.width / 2, 0, bb.maxZ - bb.minZ - e.width / 2),
                new Vec3d(bb.maxX - bb.minX - e.width / 2, 0, bb.maxZ - bb.minZ - e.width / 2),

                new Vec3d(bb.minX - bb.maxX + e.width / 2, bb.maxY - bb.minY, bb.minZ - bb.maxZ + e.width / 2),
                new Vec3d(bb.maxX - bb.minX - e.width / 2, bb.maxY - bb.minY, bb.minZ - bb.maxZ + e.width / 2),
                new Vec3d(bb.minX - bb.maxX + e.width / 2, bb.maxY - bb.minY, bb.maxZ - bb.minZ - e.width / 2),
                new Vec3d(bb.maxX - bb.minX - e.width / 2, bb.maxY - bb.minY, bb.maxZ - bb.minZ - e.width / 2)
        };

        for (Vec3d vec : corners) {
            projection = GLUProjection.getInstance().project(pos.x + vec.x - Minecraft.getMinecraft().getRenderManager().viewerPosX, pos.y + vec.y - Minecraft.getMinecraft().getRenderManager().viewerPosY, pos.z + vec.z - Minecraft.getMinecraft().getRenderManager().viewerPosZ, GLUProjection.ClampMode.NONE, false);

            if (projection == null) {
                return null;
            }

            x = Math.max(x, (float) projection.getX());
            y = Math.max(y, (float) projection.getY());

            w = Math.min(w, (float) projection.getX());
            h = Math.min(h, (float) projection.getY());
        }

        if (x != -1 && y != -1 && w != width + 1 && h != height + 1) {
            return new float[]{x, y, w, h};
        }

        return null;
    }

    public static class FootstepData {
        private double x;
        private double y;
        private double z;
        private long time;

        public FootstepData(double x, double y, double z, long time) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.time = time;
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

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }
    }

}
