package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.api.patch.ClassPatch;

/**
 * Author Seth
 * 5/24/2019 @ 3:42 AM.
 */
public final class EntityPatch extends ClassPatch {

    public EntityPatch() {
        super("net.minecraft.entity.Entity", "vg");
    }
//
//    @MethodPatch(
//            mcpName = "move",
//            notchName = "a",
//            mcpDesc = "(Lnet/minecraft/entity/MoverType;DDD)V",
//            notchDesc = "(Lvv;DDD)V")
//    public void move(MethodNode methodNode, PatchManager.Environment env) {
//        final AbstractInsnNode insnNode = ASMUtil.findPatternInsn(methodNode, new int[] {Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ICONST_0, Opcodes.FCMPL, Opcodes.IFLE});
//        if(insnNode != null) {
//            methodNode.instructions.insertBefore(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(this.getClass()), "moveHook", "()V", false));
//        }
//    }
//
//    public static void moveHook() {
//        Seppuku.INSTANCE.getEventManager().dispatchEvent(new EventStep());
//    }

}
