package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.render.EventRenderBlock;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.impl.management.PatchManager;
import net.minecraft.util.math.BlockPos;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

/**
 * @author noil
 */
public final class BlockRendererDispatcherPatch extends ClassPatch {

    public BlockRendererDispatcherPatch() {
        super("net.minecraft.client.renderer.BlockRendererDispatcher", "bvm");
    }

    //public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder bufferBuilderIn)Z
    //public boolean a(awt , et et1, amy amy1, buk buk1)Z {
    @MethodPatch(
            mcpName = "renderBlock",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/BufferBuilder;)Z",
            notchDesc = "(Lawt;Let;Lamy;Lbuk;)Z")
    public void renderBlock(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();
        //aload the BlockPos
        insnList.add(new VarInsnNode(ALOAD, 2));
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "renderBlockHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/util/math/BlockPos;)V" : "(Let;)V", false));
        //insert instructions
        methodNode.instructions.insert(insnList);
    }

    public static void renderBlockHook(BlockPos pos) {
        final EventRenderBlock event = new EventRenderBlock(pos);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);
    }
}
