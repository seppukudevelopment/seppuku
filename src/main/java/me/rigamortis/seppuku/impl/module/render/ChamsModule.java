package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.event.render.EventRenderEntity;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.util.shader.ShaderProgram;
import me.rigamortis.seppuku.api.value.Shader;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;
import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * Author Seth
 * 4/23/2019 @ 7:55 AM.
 */
public final class ChamsModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode"}, "The chams mode to use.", Mode.NORMAL);

    public final Value<Boolean> players = new Value<Boolean>("Players", new String[]{"Player"}, "Choose to enable on players.", true);
    public final Value<Color> playersColor = new Value<Color>("PlayersColor", new String[]{"playerscolor", "pc"}, "Change the color of players on chams.", new Color(255, 68, 68));

    public final Value<Boolean> mobs = new Value<Boolean>("Mobs", new String[]{"Mob"}, "Choose to enable on mobs.", true);
    public final Value<Color> mobsColor = new Value<Color>("MobsColor", new String[]{"mobscolor", "mc"}, "Change the color of mobs on chams.", new Color(255, 170, 0));

    public final Value<Boolean> animals = new Value<Boolean>("Animals", new String[]{"Animal"}, "Choose to enable on animals.", true);
    public final Value<Color> animalsColor = new Value<Color>("AnimalsColor", new String[]{"animalscolor", "ac"}, "Change the color of animals on chams.", new Color(0, 255, 68));

    public final Value<Boolean> vehicles = new Value<Boolean>("Vehicles", new String[]{"Vehic", "Vehicle"}, "Choose to enable on vehicles.", true);
    public final Value<Color> vehiclesColor = new Value<Color>("VehiclesColor", new String[]{"vehiclescolor", "vc"}, "Change the color of vehicles on chams.", new Color(213, 255, 0));

    public final Value<Boolean> items = new Value<Boolean>("Items", new String[]{"Item", "i"}, "Choose to enable on items.", false);
    public final Value<Color> itemsColor = new Value<Color>("ItemsColor", new String[]{"itemscolor", "ic"}, "Change the color of items on chams.", new Color(0, 255, 170));

    public final Value<Boolean> crystals = new Value<Boolean>("Crystals", new String[]{"crystal", "crystals", "endcrystal", "endcrystals"}, "Choose to enable on end crystals.", true);
    public final Value<Color> crystalsColor = new Value<Color>("CrystalsColor", new String[]{"endercrystalscolor", "endercrystalcolor", "crystalscolor", "crystalcolor", "ecc"}, "Change the color of ender crystals on chams.", new Color(205, 0, 205));

    public final Value<Boolean> unknowns = new Value<Boolean>("Unknowns", new String[]{"unknown", "unknowns"}, "Choose to enable on unknown entity types.", false);
    public final Value<Color> unknownsColor = new Value<Color>("UnknownsColor", new String[]{"unknownscolor", "unknowncolor", "uc"}, "Change the color of unknown entity types on chams.", new Color(60, 60, 60));

    public final Value<Color> friendsColor = new Value<Color>("FriendsColor", new String[]{"friendscolor", "friendcolor", "fc"}, "Change the color of friendly players on esp.", new Color(153, 0, 238));
    public final Value<Color> sneakingColor = new Value<Color>("SneakingColor", new String[]{"sneakingcolor", "sneakcolor", "sc"}, "Change the color of sneaking players on esp.", new Color(238, 153, 0));

    public final Value<Shader> shader = new Value<Shader>("Shader", new String[]{"shader", "program", "shaderprogram", "sp"}, "Change the shader to use when in shader mode.", new Shader("resource:///assets/seppukumod/shaders/chams.json"));

    private enum Mode {
        NORMAL, COLOR, TEXTURE, FLAT, WIREFRAME, SHADER
    }

    private enum EntityType {
        SKIP, UNKNOWN, ANIMAL, MOB, VEHICLE, ITEM, CRYSTAL, NORMAL_PLAYER, FRIENDLY_PLAYER, SNEAKING_PLAYER
    }

    private class QueuedEntity {
        public final Entity entity;
        public final double x;
        public final double y;
        public final double z;
        public final float yaw;
        public final float partialTicks;
        public final EntityType entityType;
        public final boolean hasShadow;

        public QueuedEntity(Entity entity, double x, double y, double z, float yaw, float partialTicks, EntityType entityType, boolean hasShadow) {
            this.entity = entity;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.partialTicks = partialTicks;
            this.entityType = entityType;
            this.hasShadow = hasShadow;
        }

        public QueuedEntity(EventRenderEntity event, EntityType entityType, boolean hasShadow) {
            this(event.getEntity(), event.getX(), event.getY(), event.getZ(), event.getYaw(), event.getPartialTicks(), entityType, hasShadow);
        }
    }

    private final List<QueuedEntity> queuedEntities = new ArrayList<QueuedEntity>();
    private boolean renderShadow = false;

    public ChamsModule() {
        super("Chams", new String[]{"Cham", "Chameleon"}, "Allows you to see entities through walls", "NONE", -1, ModuleType.RENDER);
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    @Listener
    public void renderEntity(EventRenderEntity event) {
        if (event.getEntity() == null) {
            return;
        }

        String mode = this.mode.getValue().name().toLowerCase();
        EntityType entityType = this.getEntityType(event.getEntity());

        if (this.checkFilter(entityType)) {
            if (mode.equalsIgnoreCase("shader")) {
                if (event.getStage() == EventStageable.EventStage.PRE) {
                    this.queuedEntities.add(new QueuedEntity(event, entityType, Minecraft.getMinecraft().getRenderManager().isRenderShadow()));
                }

                event.setCanceled(true);
                return;
            }

            if (event.getStage() == EventStageable.EventStage.PRE) {

                this.renderShadow = Minecraft.getMinecraft().getRenderManager().isRenderShadow();
                Minecraft.getMinecraft().getRenderManager().setRenderShadow(false);

                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
                GlStateManager.doPolygonOffset(1.0f, -2000000.0f);

                switch (mode) {
                    case "normal":
                        GlStateManager.enablePolygonOffset();
                        break;
                    case "flat":
                        GlStateManager.disableLighting();
                    case "color":
                        GlStateManager.disableTexture2D();
                    case "texture":
                        GlStateManager.enablePolygonOffset();
                        RenderUtil.glColor(this.getColor(entityType));
                        break;
                    case "wireframe":
                        // GlStateManager.polygonOffsetState.polygonOffsetLine is private :(
                        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                        glEnable(GL_POLYGON_OFFSET_LINE);
                        GlStateManager.disableTexture2D();
                        GlStateManager.disableLighting();
                        // GlStateManager doesn't keep track of line smoothing
                        glEnable(GL_LINE_SMOOTH);
                        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
                        GlStateManager.glLineWidth(1);
                        RenderUtil.glColor(this.getColor(entityType));
                        break;
                }
            }

            if (event.getStage() == EventStageable.EventStage.POST) {

                Minecraft.getMinecraft().getRenderManager().setRenderShadow(this.renderShadow);

                GlStateManager.doPolygonOffset(0.0f, 0.0f);

                switch (mode) {
                    case "flat":
                        GlStateManager.enableLighting();
                    case "color":
                        GlStateManager.enableTexture2D();
                    case "normal":
                    case "texture":
                        GlStateManager.disablePolygonOffset();
                        break;
                    case "wireframe":
                        // GlStateManager.polygonOffsetState.polygonOffsetLine is private :(
                        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                        glDisable(GL_POLYGON_OFFSET_LINE);
                        GlStateManager.enableTexture2D();
                        GlStateManager.enableLighting();
                        // GlStateManager doesn't keep track of line smoothing
                        glDisable(GL_LINE_SMOOTH);
                        break;
                }
            }
        }
    }

    @Listener
    public void render3D(EventRender3D event) {
        if (!this.mode.getValue().name().equalsIgnoreCase("shader")) {
            this.queuedEntities.clear();
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();
        final RenderManager renderMan = mc.getRenderManager();
        mc.entityRenderer.enableLightmap();
        RenderHelper.enableStandardItemLighting();

        // draw entity shadows and fire without shader so they don't render on
        // top of entity in case you are using a shader that overrides depth
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        for(QueuedEntity qEntity : this.queuedEntities) {
            Render<Entity> render = renderMan.<Entity>getEntityRenderObject(qEntity.entity);

            if (render != null && renderMan.renderEngine != null)
            {
                //OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)(15728880 % 65536), (float)(15728880 / 65536));
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f); // max brightness
                render.doRenderShadowAndFire(qEntity.entity, qEntity.x, qEntity.y, qEntity.z, qEntity.yaw, qEntity.partialTicks);
            }
        }

        // use shader. null if no shader picked or shader is missing
        ShaderProgram prog = this.shader.getValue().getShaderProgram();
        if (prog != null) {
            prog.use();
        }

        // draw entities with shader
        EntityType lastEntityType = EntityType.SKIP;
        for(QueuedEntity qEntity : this.queuedEntities) {
            Render<Entity> render = renderMan.<Entity>getEntityRenderObject(qEntity.entity);
            render.setRenderOutlines(false);

            if (render != null && renderMan.renderEngine != null)
            {
                // set light level (from RenderManager.renderEntityStatic) and color
                final int lightLevel = qEntity.entity.isBurning() ? 15728880 : qEntity.entity.getBrightnessForRender();
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)(lightLevel % 65536), (float)(lightLevel / 65536));
                GlStateManager.color(1.0f, 1.0f, 1.0f);

                if (qEntity.entityType != lastEntityType && prog != null) {
                    lastEntityType = qEntity.entityType;
                    prog.setColorUniform("entitycolor", this.getColor(lastEntityType));
                }

                // render entity (from RenderManager.renderEntity)
                render.doRender(qEntity.entity, qEntity.x, qEntity.y, qEntity.z, qEntity.yaw, qEntity.partialTicks);
            }
        }

        // release shader and clean up context
        if (prog != null) {
            prog.release();
        }
        RenderHelper.disableStandardItemLighting();
        mc.entityRenderer.disableLightmap();
        this.queuedEntities.clear();
    }

    private EntityType getEntityType(Entity entity) {
        if (entity == Minecraft.getMinecraft().player ||
            (entity instanceof EntityLivingBase && entity.ticksExisted <= 0) ||
            Minecraft.getMinecraft().player.getRidingEntity() != null && entity == Minecraft.getMinecraft().player.getRidingEntity()) {
            return EntityType.SKIP;
        } else if(entity instanceof EntityPlayer) {
            if (entity.isSneaking()) {
                return EntityType.SNEAKING_PLAYER;
            } else if(Seppuku.INSTANCE.getFriendManager().isFriend(entity) != null) {
                return EntityType.FRIENDLY_PLAYER;
            } else {
                return EntityType.NORMAL_PLAYER;
            }
        } else if(entity instanceof IAnimals && !(entity instanceof IMob)) {
            return EntityType.ANIMAL;
        } else if(entity instanceof IMob) {
            return EntityType.MOB;
        } else if(entity instanceof EntityItem) {
            return EntityType.ITEM;
        } else if(entity instanceof EntityEnderCrystal) {
            return EntityType.CRYSTAL;
        } else if(entity instanceof EntityBoat || entity instanceof EntityMinecart) {
            return EntityType.VEHICLE;
        } else {
            return EntityType.UNKNOWN;
        }
    }

    private int getColor(EntityType entityType) {
        switch(entityType) {
            case SNEAKING_PLAYER:
                return this.sneakingColor.getValue().getRGB();
            case FRIENDLY_PLAYER:
                return this.friendsColor.getValue().getRGB();
            case NORMAL_PLAYER:
                return this.playersColor.getValue().getRGB();
            case ANIMAL:
                return this.animalsColor.getValue().getRGB();
            case MOB:
                return this.mobsColor.getValue().getRGB();
            case VEHICLE:
                return this.vehiclesColor.getValue().getRGB();
            case ITEM:
                return this.itemsColor.getValue().getRGB();
            case CRYSTAL:
                return this.crystalsColor.getValue().getRGB();
            case UNKNOWN:
                return this.unknownsColor.getValue().getRGB();
            default:
                return 0xFFFFFFFF;
        }
    }

    private boolean checkFilter(EntityType entityType) {
        switch(entityType) {
            case SNEAKING_PLAYER:
            case FRIENDLY_PLAYER:
            case NORMAL_PLAYER:
                return this.players.getValue();
            case ANIMAL:
                return this.animals.getValue();
            case MOB:
                return this.mobs.getValue();
            case VEHICLE:
                return this.vehicles.getValue();
            case ITEM:
                return this.items.getValue();
            case CRYSTAL:
                return this.crystals.getValue();
            case UNKNOWN:
                return this.unknowns.getValue();
            default:
                return false;
        }
    }

}
