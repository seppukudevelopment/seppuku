package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.render.EventRenderEntity;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

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

    public final Value<Color> friendsColor = new Value<Color>("FriendsColor", new String[]{"friendscolor", "friendcolor", "fc"}, "Change the color of friendly players on esp.", new Color(153, 0, 238));
    public final Value<Color> sneakingColor = new Value<Color>("SneakingColor", new String[]{"sneakingcolor", "sneakcolor", "sc"}, "Change the color of sneaking players on esp.", new Color(238, 153, 0));

    private enum Mode {
        NORMAL, TEXTURE, FLAT, WIREFRAME
    }

    public ChamsModule() {
        super("Chams", new String[]{"Cham", "Chameleon"}, "Allows you to see entities through walls", "NONE", -1, ModuleType.RENDER);
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    @Listener
    public void renderEntity(EventRenderEntity event) {
        if (event.getEntity() != null && checkFilter(event.getEntity())) {

            boolean shadow = Minecraft.getMinecraft().getRenderManager().isRenderShadow();

            if (event.getStage() == EventStageable.EventStage.PRE) {

                Minecraft.getMinecraft().getRenderManager().setRenderShadow(false);
                Minecraft.getMinecraft().getRenderManager().setRenderOutlines(false);

                GlStateManager.pushMatrix();
                switch (this.mode.getValue().name().toLowerCase()) {
                    case "normal":
                        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
                        glEnable(GL11.GL_POLYGON_OFFSET_FILL);
                        glPolygonOffset(1.0f, -1100000.0f);
                        break;
                    case "texture":
                        glEnable(GL11.GL_POLYGON_OFFSET_FILL);
                        glPolygonOffset(1.0f, -1100000.0f);
                        glDisable(GL11.GL_TEXTURE_2D);
                        RenderUtil.glColor(this.getColor(event.getEntity()));
                        break;
                    case "flat":
                        glEnable(GL11.GL_POLYGON_OFFSET_FILL);
                        glPolygonOffset(1.0f, -1100000.0f);
                        glDisable(GL11.GL_TEXTURE_2D);
                        glDisable(GL11.GL_LIGHTING);
                        RenderUtil.glColor(this.getColor(event.getEntity()));
                        break;
                    case "wireframe":
                        glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
                        glEnable(GL11.GL_POLYGON_OFFSET_LINE);
                        glPolygonOffset(1.0f, -1100000.0f);
                        glDisable(GL11.GL_TEXTURE_2D);
                        glDisable(GL11.GL_LIGHTING);
                        glEnable(GL_LINE_SMOOTH);
                        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
                        glLineWidth(1);
                        RenderUtil.glColor(this.getColor(event.getEntity()));
                        break;
                }
                GlStateManager.popMatrix();
            }
            if (event.getStage() == EventStageable.EventStage.POST) {

                Minecraft.getMinecraft().getRenderManager().setRenderShadow(shadow);

                GlStateManager.pushMatrix();
                switch (this.mode.getValue().name().toLowerCase()) {
                    case "normal":
                        glDisable(GL11.GL_POLYGON_OFFSET_FILL);
                        glPolygonOffset(1.0f, 1100000.0f);
                        break;
                    case "texture":
                        glDisable(GL11.GL_POLYGON_OFFSET_FILL);
                        glPolygonOffset(1.0f, 1100000.0f);
                        glEnable(GL11.GL_TEXTURE_2D);
                        break;
                    case "flat":
                        glDisable(GL11.GL_POLYGON_OFFSET_FILL);
                        glPolygonOffset(1.0f, 1100000.0f);
                        glEnable(GL11.GL_TEXTURE_2D);
                        glEnable(GL11.GL_LIGHTING);
                        break;
                    case "wireframe":
                        glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
                        glDisable(GL11.GL_POLYGON_OFFSET_LINE);
                        glPolygonOffset(1.0f, 1100000.0f);
                        glEnable(GL11.GL_TEXTURE_2D);
                        glEnable(GL11.GL_LIGHTING);
                        glDisable(GL_LINE_SMOOTH);
                        break;
                }
                GlStateManager.popMatrix();
            }
        }
    }

    private int getColor(Entity entity) {
        int ret = 0xFFFFFFFF;

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
        if (entity instanceof EntityEnderCrystal) {
            ret = this.crystalsColor.getValue().getRGB();
        }
        if (entity instanceof EntityPlayer) {
            ret = this.playersColor.getValue().getRGB();

            if (entity == Minecraft.getMinecraft().player) {
                ret = -1;
            }

            if (entity.isSneaking()) {
                ret = this.sneakingColor.getValue().getRGB();
            }

            if (Seppuku.INSTANCE.getFriendManager().isFriend(entity) != null) {
                ret = this.friendsColor.getValue().getRGB();
            }
        }
        return ret;
    }

    private boolean checkFilter(Entity entity) {
        boolean ret = false;

        if (this.players.getValue() && entity instanceof EntityPlayer && entity != Minecraft.getMinecraft().player) {
            ret = true;
        } else if (this.animals.getValue() && entity instanceof IAnimals && !(entity instanceof IMob)) {
            ret = true;
        } else if (this.mobs.getValue() && entity instanceof IMob) {
            ret = true;
        } else if (this.items.getValue() && entity instanceof EntityItem) {
            ret = true;
        } else if (this.crystals.getValue() && entity instanceof EntityEnderCrystal) {
            ret = true;
        } else if (this.vehicles.getValue() && (entity instanceof EntityBoat || entity instanceof EntityMinecart)) {
            ret = true;
        }

        if (Minecraft.getMinecraft().player.getRidingEntity() != null && entity == Minecraft.getMinecraft().player.getRidingEntity()) {
            ret = false;
        }
        if (entity instanceof EntityLivingBase) {
            final EntityLivingBase entityLiving = (EntityLivingBase) entity;
            if (entityLiving.ticksExisted <= 0) {
                ret = false;
            }
        }

        return ret;
    }

}
