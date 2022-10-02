package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.render.EventRenderBlock;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.impl.management.PatchManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author noil
 */
public final class BlockRendererDispatcherPatch extends ClassPatch {

    public BlockRendererDispatcherPatch() {
        super("net.minecraft.client.renderer.BlockRendererDispatcher", "bvm");
    }

    public static boolean renderBlockHook(IBlockState state, BlockPos pos, IBlockAccess access, BufferBuilder bufferBuilder) {
        final EventRenderBlock event = new EventRenderBlock(state, pos, access, bufferBuilder);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);
        return event.isCanceled();
    }

    //public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder bufferBuilderIn)Z
    //public boolean a(awt , et et1, amy amy1, buk buk1)Z {
    @MethodPatch(
            mcpName = "renderBlock",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/BufferBuilder;)Z",
            notchDesc = "(Lawt;Let;Lamy;Lbuk;)Z")
    public void renderBlock(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList insnList = new InsnList();
//        insnList.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Seppuku.class), "INSTANCE", "Lme/rigamortis/seppuku/Seppuku;"));
//        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Seppuku.class), "getCameraManager", "()Lme/rigamortis/seppuku/impl/management/CameraManager;", false));
//        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(CameraManager.class), "isCameraRecording", "()Z", false));
//        insnList.add(new JumpInsnNode(IFNE, jmp));
        //aload the parameters
        insnList.add(new VarInsnNode(ALOAD, 1));
        insnList.add(new VarInsnNode(ALOAD, 2));
        insnList.add(new VarInsnNode(ALOAD, 3));
        insnList.add(new VarInsnNode(ALOAD, 4));
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "renderBlockHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/BufferBuilder;)Z" : "(Lawt;Let;Lamy;Lbuk;)Z", false));
        //create label
        final LabelNode jmp = new LabelNode();
        //add if equals and pass the label
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //add return so the rest of the function doesn't get called
        insnList.add(new InsnNode(ICONST_1));
        insnList.add(new InsnNode(IRETURN));
        //add our label
        insnList.add(jmp);
        //insert instructions
        methodNode.instructions.insert(insnList);
    }
}
