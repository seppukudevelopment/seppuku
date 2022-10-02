package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.player.EventChatKeyTyped;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.impl.management.PatchManager;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 12/23/2019 @ 4:29 AM.
 */
public final class GuiChatPatch extends ClassPatch {

    public GuiChatPatch() {
        super("net.minecraft.client.gui.GuiChat", "bkn");
    }

    public static boolean keyTypedHook(char typedChar, int keyCode) {
        final EventChatKeyTyped event = new EventChatKeyTyped(typedChar, keyCode);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);
        return event.isCanceled();
    }

    @MethodPatch(
            mcpName = "keyTyped",
            notchName = "a",
            mcpDesc = "(CI)V")
    public void keyTyped(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(ILOAD, 1));
        insnList.add(new VarInsnNode(ILOAD, 2));
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "keyTypedHook", "(CI)Z", false));
        final LabelNode jmp = new LabelNode();
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        insnList.add(new InsnNode(RETURN));
        insnList.add(jmp);
        methodNode.instructions.insert(insnList);
    }
}