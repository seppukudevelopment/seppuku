package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.world.EventAddEntity;
import me.rigamortis.seppuku.api.event.world.EventLightUpdate;
import me.rigamortis.seppuku.api.event.world.EventRainStrength;
import me.rigamortis.seppuku.api.event.world.EventRemoveEntity;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.impl.management.PatchManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
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
     *
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

        if (Minecraft.getMinecraft().isSingleplayer()) {
            return false;
        }

        return event.isCanceled();
    }

    @MethodPatch(
            mcpName = "getRainStrength",
            notchName = "j",
            mcpDesc = "(F)F",
            notchDesc = "(F)F")
    public void getRainStrength(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList list = new InsnList();
        list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "getRainStrengthHook", "()Z", false));
        final LabelNode jmp = new LabelNode();
        list.add(new JumpInsnNode(IFEQ, jmp));
        list.add(new InsnNode(FCONST_0));
        list.add(new InsnNode(FRETURN));
        list.add(jmp);
        methodNode.instructions.insert(list);
    }

    public static boolean getRainStrengthHook() {
        final EventRainStrength event = new EventRainStrength();
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

    @MethodPatch(
            mcpName = "onEntityAdded",
            notchName = "b",
            mcpDesc = "(Lnet/minecraft/entity/Entity;)V",
            notchDesc = "(Lvg;)V")
    public void onEntityAdded(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD, 1));
        list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "onEntityAddedHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/entity/Entity;)V" : "(Lvg;)V", false));
        methodNode.instructions.insert(list);
    }

    public static void onEntityAddedHook(Entity entity) {
        Seppuku.INSTANCE.getEventManager().dispatchEvent(new EventAddEntity(entity));
    }

    @MethodPatch(
            mcpName = "onEntityRemoved",
            notchName = "c",
            mcpDesc = "(Lnet/minecraft/entity/Entity;)V",
            notchDesc = "(Lvg;)V")
    public void onEntityRemoved(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD, 1));
        list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "onEntityRemovedHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/entity/Entity;)V" : "(Lvg;)V", false));
        methodNode.instructions.insert(list);
    }

    public static void onEntityRemovedHook(Entity entity) {
        Seppuku.INSTANCE.getEventManager().dispatchEvent(new EventRemoveEntity(entity));
    }

}
