package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.render.EventRenderBlockModel;
import me.rigamortis.seppuku.api.event.render.EventRenderFluid;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.impl.management.PatchManager;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import team.stiff.pomelo.EventManager;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;

/**
 * Author Seth
 * 4/11/2019 @ 3:18 AM.
 */
public final class BlockFluidRendererPatch extends ClassPatch {

    public BlockFluidRendererPatch() {
        super("net.minecraft.client.renderer.BlockFluidRenderer", "bvn");
    }

    @MethodPatch(
            mcpName = "renderFluid",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/BufferBuilder;)Z",
            notchDesc = "(Lamy;Lawt;Let;Lbuk;)Z")
    public void renderFluid(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();
        //create a new instance of "EventRenderBlockSide" and dupe the top value on the stack
        insnList.add(new TypeInsnNode(NEW, Type.getInternalName(EventRenderFluid.class)));
        insnList.add(new InsnNode(DUP));
        //add ALOAD to pass IBlockAccess into our call
        insnList.add(new VarInsnNode(ALOAD, 1));
        //add ALOAD to pass IBlockState into our call
        insnList.add(new VarInsnNode(ALOAD, 2));
        //add ALOAD to pass BlockPos into our call
        insnList.add(new VarInsnNode(ALOAD, 3));
        //add ALOAD to pass BufferBuilder into our call
        insnList.add(new VarInsnNode(ALOAD, 4));
        //call "EventRenderFluid" constructor
        insnList.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(EventRenderFluid.class), "<init>", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/BufferBuilder;)V" : "(Lamy;Lawt;Let;Lbuk;)V", false));
        //store our event in the local vars
        insnList.add(new VarInsnNode(ASTORE, 60));
        //Seppuku.INSTANCE
        insnList.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Seppuku.class), "INSTANCE", "Lme/rigamortis/seppuku/Seppuku;"));
        //getEventManager
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Seppuku.class), "getEventManager", "()Lteam/stiff/pomelo/EventManager;", false));
        //add ALOAD to access our event
        insnList.add(new VarInsnNode(ALOAD, 60));
        //call EventManager.dispatchEvent and pass our event in
        insnList.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(EventManager.class), "dispatchEvent", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
        //remove the top value on the stack
        insnList.add(new InsnNode(POP));
        //add ALOAD to access our event
        insnList.add(new VarInsnNode(ALOAD, 60));
        //call EventRenderFluid.isCanceled
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventRenderFluid.class), "isCanceled", "()Z", false));
        //create a label to jump to
        final LabelNode jmp = new LabelNode();
        //add "if equals"
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //add ALOAD to access our event
        insnList.add(new VarInsnNode(ALOAD, 60));
        //call EventRenderFluid.isRenderable
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventRenderFluid.class), "isRenderable", "()Z", false));
        //return the value of isRenderable
        insnList.add(new InsnNode(IRETURN));
        //add our label
        insnList.add(jmp);
        //add our instructions at the top of the function
        methodNode.instructions.insert(insnList);
    }

}
