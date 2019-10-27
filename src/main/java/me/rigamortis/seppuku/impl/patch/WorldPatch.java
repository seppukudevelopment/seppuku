package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.world.EventLightUpdate;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.impl.management.PatchManager;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 4/6/2019 @ 1:25 PM.
 */
public final class WorldPatch extends ClassPatch {

    public WorldPatch() {
        super("net.minecraft.world.World", "amu");
    }

    /**
     * This function is used to update light for blocks
     * It is VERY unoptimized and in some cases it's
     * better off to disable
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "checkLightFor",
            notchName = "c",
            mcpDesc = "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)Z",
            notchDesc = "(Lana;Let;)Z")
    public void checkLightFor(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList list = new InsnList();
        //call our hook function
        list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "checkLightForHook", "()Z", false));
        //create a label to jump to
        final LabelNode jmp = new LabelNode();
        //add "if equals" and pass in the label
        list.add(new JumpInsnNode(IFEQ, jmp));
        //add 0 or false
        list.add(new InsnNode(ICONST_0));
        //return 0 or false
        list.add(new InsnNode(IRETURN));
        //add our label
        list.add(jmp);
        //insert the instructions at the top of the function
        methodNode.instructions.insert(list);
    }

    public static boolean checkLightForHook() {
        final EventLightUpdate event = new EventLightUpdate();
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

}
