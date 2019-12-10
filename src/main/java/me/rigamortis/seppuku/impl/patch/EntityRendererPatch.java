package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
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
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

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

        //update all camera fbos after we render
        Seppuku.INSTANCE.getCameraManager().update();
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

        if(target != null) {
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

}
