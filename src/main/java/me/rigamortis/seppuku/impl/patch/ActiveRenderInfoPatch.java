package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.impl.management.PatchManager;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

/**
 * Author Seth
 * 4/16/2019 @ 2:39 AM.
 */
public final class ActiveRenderInfoPatch extends ClassPatch {

    public ActiveRenderInfoPatch() {
        super("net.minecraft.client.renderer.ActiveRenderInfo", "bhv");
    }

    public static void updateRenderInfoHook() {
        //update our model view projection matrix used to converting 3D world coordinates
        //to 2D screen coordinates
        RenderUtil.updateModelViewProjectionMatrix();
    }

    /**
     * This is where minecraft updates the ModelViewProjection matrix
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "updateRenderInfo",
            notchName = "updateRenderInfo",
            mcpDesc = "(Lnet/minecraft/entity/Entity;Z)V",
            notchDesc = "(Lvg;Z)V")
    public void updateRenderInfo(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "updateRenderInfoHook", "()V", false));
        //insert the list of instructions at the bottom of the function
        methodNode.instructions.insert(insnList);
    }

}
