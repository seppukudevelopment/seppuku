package me.rigamortis.seppuku.impl.patch;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.player.EventFovModifier;
import me.rigamortis.seppuku.api.event.player.EventGetMouseOver;
import me.rigamortis.seppuku.api.event.player.EventPlayerReach;
import me.rigamortis.seppuku.api.event.render.EventHurtCamEffect;
import me.rigamortis.seppuku.api.event.render.EventOrientCamera;
import me.rigamortis.seppuku.api.event.render.EventRender2D;
import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.api.util.ASMUtil;
import me.rigamortis.seppuku.impl.management.PatchManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import team.stiff.pomelo.EventManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 4/6/2019 @ 12:46 PM.
 */
public final class EntityRendererPatch extends ClassPatch {

    public EntityRendererPatch() {
        super("net.minecraft.client.renderer.EntityRenderer", "buq");
    }

    /**
     * This is where we place our 2d rendering context
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "updateCameraAndRender",
            notchName = "a",
            mcpDesc = "(FJ)V")
    public void updateCameraAndRender(MethodNode methodNode, PatchManager.Environment env) {
        //find the instruction that calls renderGameOverlay
        final AbstractInsnNode target = ASMUtil.findMethodInsn(methodNode, INVOKEVIRTUAL, env == PatchManager.Environment.IDE ? "net/minecraft/client/gui/GuiIngame" : "biq", env == PatchManager.Environment.IDE ? "renderGameOverlay" : "a", "(F)V");

        if (target != null) {
            //create a list of instructions
            final InsnList insnList = new InsnList();
            //add FLOAD to pass partialTicks param into our hook function
            insnList.add(new VarInsnNode(FLOAD, 1));
            //call our hook function
            insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "updateCameraAndRenderHook", "(F)V", false));
            //insert our instructions after the renderGameOverlay call
            methodNode.instructions.insert(target, insnList);
        }
    }

    /**
     * This is our 2d render context
     * Anything rendered here will be in screen space
     * It should be called after forge and have top priority
     *
     * @param partialTicks
     */
    public static void updateCameraAndRenderHook(float partialTicks) {
        //dispatch our event so we can render stuff on our screen
        Seppuku.INSTANCE.getEventManager().dispatchEvent(new EventRender2D(partialTicks, new ScaledResolution(Minecraft.getMinecraft())));
    }

    /**
     * This is where we place our 3d rendering context
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "renderWorldPass",
            notchName = "a",
            mcpDesc = "(IFJ)V")
    public void renderWorldPass(MethodNode methodNode, PatchManager.Environment env) {
        //find the LDC instruction with the value "hand"
        //there is only 1 in this function and its passed into the call
        //mc.mcProfiler.endStartSection("hand");
        final AbstractInsnNode target = ASMUtil.findInsnLdc(methodNode, "hand");

        if (target != null) {
            //make a list of instructions
            final InsnList list = new InsnList();
            //add FLOAD to pass the partialTicks param into our hook function
            list.add(new VarInsnNode(FLOAD, 2));
            //call our hook function
            list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "renderWorldPassHook", "(F)V", false));
            //insert the list of instructions 1 instruction after the LDC
            methodNode.instructions.insert(target.getNext(), list);
        }
    }

    /**
     * This is our 3d render context
     * Anything rendered here will be rendered in world space
     * before your hand
     *
     * @param partialTicks
     */
    public static void renderWorldPassHook(float partialTicks) {
        if (Seppuku.INSTANCE.getCameraManager().isCameraRecording()) {
            return;
        }
        //dispatch our event and pass partial ticks in
        Seppuku.INSTANCE.getEventManager().dispatchEvent(new EventRender3D(partialTicks));
    }

    /**
     * This is where minecraft rotates and shakes your screen
     * while taking damage
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "hurtCameraEffect",
            notchName = "d",
            mcpDesc = "(F)V")
    public void hurtCameraEffect(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions and add the needed instructions to call our hook function
        final InsnList insnList = new InsnList();
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "hurtCameraEffectHook", "()Z", false));
        //add a label to jump to
        final LabelNode jmp = new LabelNode();
        //add if equals and pass the label
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //add return so the rest of the function doesn't get called
        insnList.add(new InsnNode(RETURN));
        //add our label
        insnList.add(jmp);
        //insert the list of instructions at the top of the function
        methodNode.instructions.insert(insnList);
    }

    /**
     * Our hurtCameraEffect hook
     * Used to disable the screen shaking effect while taking damage
     *
     * @return
     */
    public static boolean hurtCameraEffectHook() {
        //dispatch our event
        final EventHurtCamEffect event = new EventHurtCamEffect();
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

    @MethodPatch(
            mcpName = "orientCamera",
            notchName = "f",
            mcpDesc = "(F)V")
    public void orientCamera(MethodNode methodNode, PatchManager.Environment env) {
        final AbstractInsnNode target = ASMUtil.findMethodInsn(methodNode, INVOKEVIRTUAL, env == PatchManager.Environment.IDE ? "net/minecraft/client/multiplayer/WorldClient" : "bsb", env == PatchManager.Environment.IDE ? "rayTraceBlocks" : "a", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/RayTraceResult;" : "(Lbhe;Lbhe;)Lbhc;");

        if (target != null) {
            final InsnList insnList = new InsnList();
            insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "orientCameraHook", "()Z", false));
            final LabelNode jmp = new LabelNode();
            insnList.add(new JumpInsnNode(IFEQ, jmp));
            insnList.add(new InsnNode(ACONST_NULL));
            insnList.add(new VarInsnNode(ASTORE, 24));
            insnList.add(jmp);
            methodNode.instructions.insert(target.getNext(), insnList);
        }
    }

    public static boolean orientCameraHook() {
        final EventOrientCamera event = new EventOrientCamera();
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);
        return event.isCanceled();
    }

    @MethodPatch(
            mcpName = "getFOVModifier",
            notchName = "a",
            mcpDesc = "(FZ)F")
    public void getFovModifier(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList insnList = new InsnList();
        insnList.add(new TypeInsnNode(NEW, Type.getInternalName(EventFovModifier.class)));
        insnList.add(new InsnNode(DUP));
        insnList.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(EventFovModifier.class), "<init>", "()V", false));
        insnList.add(new VarInsnNode(ASTORE, 5));

        insnList.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Seppuku.class), "INSTANCE", "Lme/rigamortis/seppuku/Seppuku;"));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Seppuku.class), "getEventManager", "()Lteam/stiff/pomelo/EventManager;", false));
        insnList.add(new VarInsnNode(ALOAD, 5));
        insnList.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(EventManager.class), "dispatchEvent", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
        insnList.add(new InsnNode(POP));

        insnList.add(new VarInsnNode(ALOAD, 5));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventFovModifier.class), "isCanceled", "()Z", false));
        final LabelNode label = new LabelNode();
        insnList.add(new JumpInsnNode(IFEQ, label));
        insnList.add(new VarInsnNode(ALOAD, 5));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventFovModifier.class), "getFov", "()F", false));
        insnList.add(new InsnNode(FRETURN));
        insnList.add(label);
        methodNode.instructions.insert(insnList);
    }

    @MethodPatch(
            mcpName = "getMouseOver",
            notchName = "a",
            mcpDesc = "(F)V")
    public void getMouseOver(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(FLOAD, 1));
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "getMouseOverHook", "(F)V", false));
        insnList.add(new InsnNode(RETURN));
        methodNode.instructions.insert(insnList);
    }

    /**
     * getMouseOver (original game function with modified event handling)
     *
     * @param partialTicks
     */
    public static void getMouseOverHook(float partialTicks) {
        final Minecraft mc = Minecraft.getMinecraft();
        final Entity entity = mc.getRenderViewEntity();
        if (entity != null && mc.world != null) {
            mc.profiler.startSection("pick");
            mc.pointedEntity = null;
            double d0 = mc.playerController.getBlockReachDistance();
            mc.objectMouseOver = entity.rayTrace(d0, partialTicks);
            Vec3d vec3d = entity.getPositionEyes(partialTicks);
            boolean flag = false;

            double d1 = d0;
            if (mc.playerController.extendedReach()) {
                final EventPlayerReach event = new EventPlayerReach();
                Seppuku.INSTANCE.getEventManager().dispatchEvent(event);
                d1 = event.isCanceled() ? event.getReach() : 6.0d;
                d0 = d1;
            } else if (d0 > 3.0D) {
                flag = true;
            }

            if (mc.objectMouseOver != null) {
                d1 = mc.objectMouseOver.hitVec.distanceTo(vec3d);
            }

            Vec3d vec3d1 = entity.getLook(1.0F);
            Vec3d vec3d2 = vec3d.add(vec3d1.x * d0, vec3d1.y * d0, vec3d1.z * d0);
            mc.entityRenderer.pointedEntity = null;
            Vec3d vec3d3 = null;
            float f = 1.0F;
            List<Entity> list = mc.world.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().expand(vec3d1.x * d0, vec3d1.y * d0, vec3d1.z * d0).grow(1.0D, 1.0D, 1.0D), Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>() {
                public boolean apply(@Nullable Entity p_apply_1_) {
                    return p_apply_1_ != null && p_apply_1_.canBeCollidedWith();
                }
            }));

            final EventGetMouseOver event = new EventGetMouseOver();
            Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

            if (event.isCanceled()) {
                list = new ArrayList<>();
            }

            double d2 = d1;

            for (int j = 0; j < list.size(); ++j) {
                Entity entity1 = (Entity) list.get(j);
                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow((double) entity1.getCollisionBorderSize());
                RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);
                if (axisalignedbb.contains(vec3d)) {
                    if (d2 >= 0.0D) {
                        mc.entityRenderer.pointedEntity = entity1;
                        vec3d3 = raytraceresult == null ? vec3d : raytraceresult.hitVec;
                        d2 = 0.0D;
                    }
                } else if (raytraceresult != null) {
                    double d3 = vec3d.distanceTo(raytraceresult.hitVec);
                    if (d3 < d2 || d2 == 0.0D) {
                        if (entity1.getLowestRidingEntity() == entity.getLowestRidingEntity() && !entity1.canRiderInteract()) {
                            if (d2 == 0.0D) {
                                mc.entityRenderer.pointedEntity = entity1;
                                vec3d3 = raytraceresult.hitVec;
                            }
                        } else {
                            mc.entityRenderer.pointedEntity = entity1;
                            vec3d3 = raytraceresult.hitVec;
                            d2 = d3;
                        }
                    }
                }
            }

            if (mc.entityRenderer.pointedEntity != null && flag && vec3d.distanceTo(vec3d3) > 3.0D) {
                mc.entityRenderer.pointedEntity = null;
                mc.objectMouseOver = new RayTraceResult(RayTraceResult.Type.MISS, vec3d3, null, new BlockPos(vec3d3));
            }

            if (mc.entityRenderer.pointedEntity != null && (d2 < d1 || mc.objectMouseOver == null)) {
                mc.objectMouseOver = new RayTraceResult(mc.entityRenderer.pointedEntity, vec3d3);
                if (mc.entityRenderer.pointedEntity instanceof EntityLivingBase || mc.entityRenderer.pointedEntity instanceof EntityItemFrame) {
                    mc.pointedEntity = mc.entityRenderer.pointedEntity;
                }
            }

            mc.profiler.endSection();
        }

    }
}
