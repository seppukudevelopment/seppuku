package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.render.EventRenderBlockSide;
import me.rigamortis.seppuku.api.event.world.EventCanCollide;
import me.rigamortis.seppuku.api.event.world.EventLiquidCollisionBB;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.impl.management.PatchManager;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import team.stiff.pomelo.EventManager;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 4/16/2019 @ 7:32 AM.
 */
public final class BlockLiquidPatch extends ClassPatch {

    public BlockLiquidPatch() {
        super("net.minecraft.block.BlockLiquid", "aru");
    }

    /**
     * This is where minecraft handles the collision boundingboxes for liquids
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "getCollisionBoundingBox",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/AxisAlignedBB;",
            notchDesc = "(Lawt;Lamy;Let;)Lbhb;")
    public void getCollisionBoundingBox(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();
        //create a new instance of "EventLiquidCollisionBB" and dupe the top val on the stack
        insnList.add(new TypeInsnNode(NEW, Type.getInternalName(EventLiquidCollisionBB.class)));
        insnList.add(new InsnNode(DUP));
        //add ALOAD to pass the BlockPos into our call
        insnList.add(new VarInsnNode(ALOAD, 3));
        //call "EventLiquidCollisionBB" constructor
        insnList.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(EventLiquidCollisionBB.class), "<init>", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/util/math/BlockPos;)V" : "(Let;)V", false));
        //add ASTORE to store our event in the local vars
        insnList.add(new VarInsnNode(ASTORE, 4));
        //Seppuku.INSTANCE.getEventManager
        insnList.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Seppuku.class), "INSTANCE", "Lme/rigamortis/seppuku/Seppuku;"));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Seppuku.class), "getEventManager", "()Lteam/stiff/pomelo/EventManager;", false));
        //add ALOAD to access our event from the local vars
        insnList.add(new VarInsnNode(ALOAD, 4));
        //call dispatchEvent and pass our event in
        insnList.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(EventManager.class), "dispatchEvent", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
        //remove the top value on the stack
        insnList.add(new InsnNode(POP));
        //add ALOAD to access our event from the local vars
        insnList.add(new VarInsnNode(ALOAD, 4));
        //call EventLiquidCollisionBB.isCanceled
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventLiquidCollisionBB.class), "isCanceled", "()Z", false));
        //create a label to jump to
        final LabelNode jmp = new LabelNode();
        //add "if equals" and pass in our label
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //add ALOAD to access our event from the local vars
        insnList.add(new VarInsnNode(ALOAD, 4));
        //call getBoundingBox
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventLiquidCollisionBB.class), "getBoundingBox", env == PatchManager.Environment.IDE ? "()Lnet/minecraft/util/math/AxisAlignedBB;" : "()Lbhb;", false));
        //add ARETURN to return our bb
        insnList.add(new InsnNode(ARETURN));
        //add our label
        insnList.add(jmp);
        //insert the list of instructions
        methodNode.instructions.insert(insnList);
    }

    @MethodPatch(
            mcpName = "shouldSideBeRendered",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z",
            notchDesc = "(Lawt;Lamy;Let;Lfa;)Z")
    public void shouldSideBeRendered(MethodNode methodNode, PatchManager.Environment env) {
//create a list of instructions
        final InsnList insnList = new InsnList();
        //create a new instance of "EventRenderBlockSide" and dupe the top value on the stack
        insnList.add(new TypeInsnNode(NEW, Type.getInternalName(EventRenderBlockSide.class)));
        insnList.add(new InsnNode(DUP));
        //add ALOAD 0 to pass "this" into the event
        insnList.add(new VarInsnNode(ALOAD, 0));
        //call "EventRenderBlockSide" constructor
        insnList.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(EventRenderBlockSide.class), "<init>", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/block/Block;)V" : "(Laow;)V", false));
        //store our event in the local vars
        insnList.add(new VarInsnNode(ASTORE, 5));
        //Seppuku.INSTANCE
        insnList.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Seppuku.class), "INSTANCE", "Lme/rigamortis/seppuku/Seppuku;"));
        //getEventManager
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Seppuku.class), "getEventManager", "()Lteam/stiff/pomelo/EventManager;", false));
        //add ALOAD to access our event
        insnList.add(new VarInsnNode(ALOAD, 5));
        //call EventManager.dispatchEvent and pass our event in
        insnList.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(EventManager.class), "dispatchEvent", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
        //remove the top value on the stack
        insnList.add(new InsnNode(POP));
        //add ALOAD to access our event
        insnList.add(new VarInsnNode(ALOAD, 5));
        //call EventRenderBlockSide.isCanceled
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventRenderBlockSide.class), "isCanceled", "()Z", false));
        //create a label to jump to
        final LabelNode jmp = new LabelNode();
        //add "if equals"
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //add ALOAD to access our event
        insnList.add(new VarInsnNode(ALOAD, 5));
        //call EventRenderBlockSide.isRenderable
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventRenderBlockSide.class), "isRenderable", "()Z", false));
        //return the value of isRenderable
        insnList.add(new InsnNode(IRETURN));
        //add our label
        insnList.add(jmp);
        //add our instructions at the top of the function
        methodNode.instructions.insert(insnList);
    }

    @MethodPatch(
            mcpName = "canCollideCheck",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/block/state/IBlockState;Z)Z",
            notchDesc = "(Lawt;Z)Z")
    public void canCollideCheck(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList preInsn = new InsnList();
        preInsn.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "canCollideCheckHook", "()Z", false));
        final LabelNode jmp = new LabelNode();
        preInsn.add(new JumpInsnNode(IFEQ, jmp));
        preInsn.add(new InsnNode(ICONST_1));
        preInsn.add(new InsnNode(IRETURN));
        preInsn.add(jmp);
        methodNode.instructions.insert(preInsn);
    }

    public static boolean canCollideCheckHook() {
        final EventCanCollide event = new EventCanCollide();
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);
        return event.isCanceled();
    }

}
