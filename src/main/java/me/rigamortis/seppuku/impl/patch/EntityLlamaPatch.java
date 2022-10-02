package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.entity.EventSteerEntity;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.impl.management.PatchManager;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 4/9/2019 @ 11:38 AM.
 */
public final class EntityLlamaPatch extends ClassPatch {

    public EntityLlamaPatch() {
        super("net.minecraft.entity.passive.EntityLlama", "aas");
    }

    /**
     * Our canBeSteered hook
     * Used to allow us to steer and control llamas
     *
     * @return
     */
    public static boolean canBeSteeredHook() {
        //dispatch our event
        final EventSteerEntity event = new EventSteerEntity();
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

    /**
     * This is where minecraft checks if you can steer llamas
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "canBeSteered",
            notchName = "cV",
            mcpDesc = "()Z")
    public void canBeSteered(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions and add the needed instructions to call our hook function
        final InsnList insnList = new InsnList();
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "canBeSteeredHook", "()Z", false));
        //add a label to jump to
        final LabelNode jmp = new LabelNode();
        //add if equals and pass the label
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //add 1 or true
        insnList.add(new InsnNode(ICONST_1));
        //add return so the rest of the function doesn't get called
        insnList.add(new InsnNode(IRETURN));
        //add our label
        insnList.add(jmp);
        //insert the list of instructions at the top of the function
        methodNode.instructions.insert(insnList);
    }
}
