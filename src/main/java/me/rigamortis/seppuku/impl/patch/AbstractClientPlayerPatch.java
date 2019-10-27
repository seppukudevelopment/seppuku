package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.impl.management.PatchManager;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 7/9/2019 @ 3:34 AM.
 */
public final class AbstractClientPlayerPatch extends ClassPatch {

    public AbstractClientPlayerPatch() {
        super("net.minecraft.client.entity.AbstractClientPlayer", "bua");
    }

    @MethodPatch(
            mcpName = "getLocationCape",
            notchName = "q",
            mcpDesc = "()Lnet/minecraft/util/ResourceLocation;",
            notchDesc = "()Lnf;")
    public void getLocationCape(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList insnList = new InsnList();
        insnList.add(new FieldInsnNode(GETSTATIC, "me/rigamortis/seppuku/Seppuku", "INSTANCE", "Lme/rigamortis/seppuku/Seppuku;"));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "me/rigamortis/seppuku/Seppuku", "getCapeManager", "()Lme/rigamortis/seppuku/impl/management/CapeManager;", false));
        insnList.add(new VarInsnNode(ALOAD, 0));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "me/rigamortis/seppuku/impl/management/CapeManager", "getCape", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/client/entity/AbstractClientPlayer;)Lnet/minecraft/util/ResourceLocation;" : "(Lbua;)Lnf;", false));
        insnList.add(new VarInsnNode(ASTORE, 2));

        insnList.add(new VarInsnNode(ALOAD, 2));
        final LabelNode labelNode = new LabelNode();
        insnList.add(new JumpInsnNode(IFNULL, labelNode));
        insnList.add(new VarInsnNode(ALOAD, 2));
        insnList.add(new InsnNode(ARETURN));
        insnList.add(labelNode);
        methodNode.instructions.insert(insnList);
    }

}
