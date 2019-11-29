package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.world.EventLandOnSlime;
import me.rigamortis.seppuku.api.event.world.EventWalkOnSlime;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.impl.management.PatchManager;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 4/16/2019 @ 3:37 AM.
 */
public final class BlockSlimePatch extends ClassPatch {

    public BlockSlimePatch() {
        super("net.minecraft.block.BlockSlime", "atu");
    }

    /**
     * This is where minecraft slows us down while walking on slime blocks
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "onEntityWalk",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V",
            notchDesc = "(Lamu;Let;Lvg;)V")
    public void onEntityWalk(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions and add the needed instructions to call our hook function
        final InsnList insnList = new InsnList();
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "onEntityWalkHook", "()Z", false));
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
     * Our onEntityWalk hook
     * Used to stop minecraft from slowing us down while
     * walking on slime blocks
     * @return
     */
    public static boolean onEntityWalkHook() {
        final EventWalkOnSlime event = new EventWalkOnSlime();
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

    /**
     * This is where minecraft makes us bounce when we land on a slime
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "onLanded",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;)V",
            notchDesc = "(Lamu;Lvg;)V")
    public void onLanded(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions and add the needed instructions to call our hook function
        final InsnList insnList = new InsnList();
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "onLanded", "()Z", false));
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
     * Our onLanded hook used to remove slime bouncing
     * @return
     */
    public static boolean onLanded() {
        final EventLandOnSlime event = new EventLandOnSlime();
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

}
