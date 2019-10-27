package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.render.EventRenderBlockModel;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.impl.management.PatchManager;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import team.stiff.pomelo.EventManager;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 4/10/2019 @ 4:04 AM.
 */
public final class BlockModelRendererPatch extends ClassPatch {

    public BlockModelRendererPatch() {
        super("net.minecraft.client.renderer.BlockModelRenderer", "bvo");
    }

    @MethodPatch(
            mcpName = "renderModel",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/BufferBuilder;ZJ)Z",
            notchDesc = "(Lamy;Lcfy;Lawt;Let;Lbuk;ZJ)Z")
    public void renderModel(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();
        //create a new instance of "EventRenderBlockSide" and dupe the top value on the stack
        insnList.add(new TypeInsnNode(NEW, Type.getInternalName(EventRenderBlockModel.class)));
        insnList.add(new InsnNode(DUP));
        //add ALOAD to pass IBlockAccess into our call
        insnList.add(new VarInsnNode(ALOAD, 1));
        //add ALOAD to pass IBakedModel into our call
        insnList.add(new VarInsnNode(ALOAD, 2));
        //add ALOAD to pass IBlockState into our call
        insnList.add(new VarInsnNode(ALOAD, 3));
        //add ALOAD to pass BlockPos into our call
        insnList.add(new VarInsnNode(ALOAD, 4));
        //add ALOAD to pass BufferBuilder into our call
        insnList.add(new VarInsnNode(ALOAD, 5));
        //add ILOAD to pass checkSides into our call
        insnList.add(new VarInsnNode(ILOAD, 6));
        //add LLOAD to pass rand into our call
        insnList.add(new VarInsnNode(LLOAD, 7));
        //call "EventRenderBlockModel" constructor
        insnList.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(EventRenderBlockModel.class), "<init>", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/BufferBuilder;ZJ)V" : "(Lamy;Lcfy;Lawt;Let;Lbuk;ZJ)V", false));
        //store our event in the local vars
        insnList.add(new VarInsnNode(ASTORE, 13));
        //Seppuku.INSTANCE
        insnList.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Seppuku.class), "INSTANCE", "Lme/rigamortis/seppuku/Seppuku;"));
        //getEventManager
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Seppuku.class), "getEventManager", "()Lteam/stiff/pomelo/EventManager;", false));
        //add ALOAD to access our event
        insnList.add(new VarInsnNode(ALOAD, 13));
        //call EventManager.dispatchEvent and pass our event in
        insnList.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(EventManager.class), "dispatchEvent", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
        //remove the top value on the stack
        insnList.add(new InsnNode(POP));
        //add ALOAD to access our event
        insnList.add(new VarInsnNode(ALOAD, 13));
        //call EventRenderBlockModel.isCanceled
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventRenderBlockModel.class), "isCanceled", "()Z", false));
        //create a label to jump to
        final LabelNode jmp = new LabelNode();
        //add "if equals"
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //add ALOAD to access our event
        insnList.add(new VarInsnNode(ALOAD, 13));
        //call EventRenderBlockModel.isRenderable
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventRenderBlockModel.class), "isRenderable", "()Z", false));
        //return the value of isRenderable
        insnList.add(new InsnNode(IRETURN));
        //add our label
        insnList.add(jmp);
        //add our instructions at the top of the function
        methodNode.instructions.insert(insnList);
    }

}
