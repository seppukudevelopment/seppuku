package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.event.render.EventRenderEntity;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.BooleanValue;
import me.rigamortis.seppuku.api.value.OptionalValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_NICEST;

/**
 * Author Seth
 * 4/23/2019 @ 7:55 AM.
 */
public final class ChamsModule extends Module {

    public final BooleanValue players = new BooleanValue("Players", new String[]{"Player"}, true);
    public final BooleanValue mobs = new BooleanValue("Mobs", new String[]{"Mob"}, true);
    public final BooleanValue animals = new BooleanValue("Animals", new String[]{"Animal"}, true);
    public final BooleanValue vehicles = new BooleanValue("Vehicles", new String[]{"Vehic", "Vehicle"}, true);

    public final OptionalValue mode = new OptionalValue("Mode", new String[]{"Mode"}, 0, new String[]{"Normal", "Texture", "Flat", "WireFrame"});

    public ChamsModule() {
        super("Chams", new String[]{"Cham", "Chameleon"}, "Allows you to see entities through walls", "NONE", -1, ModuleType.RENDER);
    }

    @Override
    public String getMetaData() {
        return this.mode.getSelectedOption();
    }

    @Listener
    public void renderEntity(EventRenderEntity event) {
        if (event.getEntity() != null && checkFilter(event.getEntity())) {

            boolean shadow = Minecraft.getMinecraft().getRenderManager().isRenderShadow();

            if (event.getStage() == EventStageable.EventStage.PRE) {

                Minecraft.getMinecraft().getRenderManager().setRenderShadow(false);
                Minecraft.getMinecraft().getRenderManager().setRenderOutlines(false);

                switch (this.mode.getInt()) {
                    case 0:
                        GlStateManager.pushMatrix();
                        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
                        glEnable(GL11.GL_POLYGON_OFFSET_FILL);
                        glPolygonOffset(1.0f, -1100000.0f);
                        GlStateManager.popMatrix();
                        break;
                    case 1:
                        GlStateManager.pushMatrix();
                        glEnable(GL11.GL_POLYGON_OFFSET_FILL);
                        glPolygonOffset(1.0f, -1100000.0f);
                        glDisable(GL11.GL_TEXTURE_2D);
                        GlStateManager.color(1, 1, 1);
                        GlStateManager.popMatrix();
                        break;
                    case 2:
                        GlStateManager.pushMatrix();
                        glEnable(GL11.GL_POLYGON_OFFSET_FILL);
                        glPolygonOffset(1.0f, -1100000.0f);
                        glDisable(GL11.GL_TEXTURE_2D);
                        glDisable(GL11.GL_LIGHTING);
                        GlStateManager.color(1, 1, 1);
                        GlStateManager.popMatrix();
                        break;
                    case 3:
                        GlStateManager.pushMatrix();
                        glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
                        glEnable(GL11.GL_POLYGON_OFFSET_LINE);
                        glPolygonOffset(1.0f, -1100000.0f);
                        glDisable(GL11.GL_TEXTURE_2D);
                        glDisable(GL11.GL_LIGHTING);
                        glEnable(GL_LINE_SMOOTH);
                        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
                        glLineWidth(1);
                        GlStateManager.color(1, 1, 1);
                        GlStateManager.popMatrix();
                        break;
                }
            }
            if (event.getStage() == EventStageable.EventStage.POST) {

                Minecraft.getMinecraft().getRenderManager().setRenderShadow(shadow);

                switch (this.mode.getInt()) {
                    case 0:
                        GlStateManager.pushMatrix();
                        glDisable(GL11.GL_POLYGON_OFFSET_FILL);
                        glPolygonOffset(1.0f, 1100000.0f);
                        GlStateManager.popMatrix();
                        break;
                    case 1:
                        GlStateManager.pushMatrix();
                        glDisable(GL11.GL_POLYGON_OFFSET_FILL);
                        glPolygonOffset(1.0f, 1100000.0f);
                        glEnable(GL11.GL_TEXTURE_2D);
                        GlStateManager.popMatrix();
                        break;
                    case 2:
                        GlStateManager.pushMatrix();
                        glDisable(GL11.GL_POLYGON_OFFSET_FILL);
                        glPolygonOffset(1.0f, 1100000.0f);
                        glEnable(GL11.GL_TEXTURE_2D);
                        glEnable(GL11.GL_LIGHTING);
                        GlStateManager.popMatrix();
                        break;
                    case 3:
                        GlStateManager.pushMatrix();
                        glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
                        glDisable(GL11.GL_POLYGON_OFFSET_LINE);
                        glPolygonOffset(1.0f, 1100000.0f);
                        glEnable(GL11.GL_TEXTURE_2D);
                        glEnable(GL11.GL_LIGHTING);
                        glDisable(GL_LINE_SMOOTH);
                        GlStateManager.popMatrix();
                        break;
                }
            }
        }
    }

    private boolean checkFilter(Entity entity) {
        boolean ret = false;

        if (entity == Minecraft.getMinecraft().player) {
            ret = false;
        }

        final Entity riding = Minecraft.getMinecraft().player.getRidingEntity();

        if(riding != null && entity == riding) {
            ret = false;
        }

        if (this.players.getBoolean() && entity instanceof EntityPlayer && entity != Minecraft.getMinecraft().player) {
            ret = true;
        }

        if (this.animals.getBoolean() && entity instanceof IAnimals) {
            ret = true;
        }

        if (this.mobs.getBoolean() && entity instanceof IMob) {
            ret = true;
        }

        if (this.vehicles.getBoolean() && (entity instanceof EntityBoat || entity instanceof EntityMinecart || entity instanceof EntityMinecartContainer)) {
            ret = true;
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
