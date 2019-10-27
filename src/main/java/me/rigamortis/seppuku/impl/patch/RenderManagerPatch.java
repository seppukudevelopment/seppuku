package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.render.EventRenderEntity;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.api.util.ASMUtil;
import me.rigamortis.seppuku.impl.management.PatchManager;
import net.minecraft.entity.Entity;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 4/9/2019 @ 9:37 AM.
 */
public final class RenderManagerPatch extends ClassPatch {

    public RenderManagerPatch() {
        super("net.minecraft.client.renderer.entity.RenderManager", "bzf");
    }

    /**
     * This is where minecraft handles rendering of entities
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "renderEntity",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/entity/Entity;DDDFFZ)V",
            notchDesc = "(Lvg;DDDFFZ)V")
    public void renderEntity(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions and add the needed instructions to call our hook function
        final InsnList preInsn = new InsnList();
        //add ALOAD to pass the entity into our hook function
        preInsn.add(new VarInsnNode(ALOAD, 1));
        //add DLOAD to pass the x into our hook function
        preInsn.add(new VarInsnNode(DLOAD, 2));
        //add DLOAD to pass the y into our hook function
        preInsn.add(new VarInsnNode(DLOAD, 4));
        //add DLOAD to pass the z into our hook function
        preInsn.add(new VarInsnNode(DLOAD, 6));
        //add FLOAD to pass the yaw into our hook function
        preInsn.add(new VarInsnNode(FLOAD, 8));
        //add FLOAD to pass the partialTicks into our hook function
        preInsn.add(new VarInsnNode(FLOAD, 9));
        //EventStageable.EventStage.PRE
        preInsn.add(new FieldInsnNode(GETSTATIC, "me/rigamortis/seppuku/api/event/EventStageable$EventStage", "PRE", "Lme/rigamortis/seppuku/api/event/EventStageable$EventStage;"));
        //call our hook function
        preInsn.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "renderEntityHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/entity/Entity;DDDFFLme/rigamortis/seppuku/api/event/EventStageable$EventStage;)Z" : "(Lvg;DDDFFLme/rigamortis/seppuku/api/event/EventStageable$EventStage;)Z", false));
        //add a label to jump to
        final LabelNode jmp = new LabelNode();
        //add if equals and pass the label
        preInsn.add(new JumpInsnNode(IFEQ, jmp));
        //add return so the rest of the function doesn't get called
        preInsn.add(new InsnNode(RETURN));
        //add our label
        preInsn.add(jmp);
        //insert the list of instructions at the top of the function
        methodNode.instructions.insert(preInsn);

        //create a list of instructions and add the needed instructions to call our hook function
        final InsnList postInsn = new InsnList();
        //add ALOAD to pass the entity into our hook function
        postInsn.add(new VarInsnNode(ALOAD, 1));
        //add DLOAD to pass the x into our hook function
        postInsn.add(new VarInsnNode(DLOAD, 2));
        //add DLOAD to pass the y into our hook function
        postInsn.add(new VarInsnNode(DLOAD, 4));
        //add DLOAD to pass the z into our hook function
        postInsn.add(new VarInsnNode(DLOAD, 6));
        //add FLOAD to pass the yaw into our hook function
        postInsn.add(new VarInsnNode(FLOAD, 8));
        //add FLOAD to pass the partialTicks into our hook function
        postInsn.add(new VarInsnNode(FLOAD, 9));
        //EventStageable.EventStage.POST
        postInsn.add(new FieldInsnNode(GETSTATIC, "me/rigamortis/seppuku/api/event/EventStageable$EventStage", "POST", "Lme/rigamortis/seppuku/api/event/EventStageable$EventStage;"));
        //call our hook function
        postInsn.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "renderEntityHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/entity/Entity;DDDFFLme/rigamortis/seppuku/api/event/EventStageable$EventStage;)Z" : "(Lvg;DDDFFLme/rigamortis/seppuku/api/event/EventStageable$EventStage;)Z", false));
        //insert the list of instructions at the bottom of the function
        methodNode.instructions.insertBefore(ASMUtil.bottom(methodNode), postInsn);
    }

    /**
     * Our renderEntity hook
     * Used to disable rendering of certain entities or modify the
     * way they render
     * @param entity
     * @param x
     * @param y
     * @param z
     * @param yaw
     * @param partialTicks
     * @param stage
     * @return
     */
    public static boolean renderEntityHook(Entity entity, double x, double y, double z, float yaw, float partialTicks, EventStageable.EventStage stage) {
        //dispatch our event and pass the render information into it along with the event stage
        final EventRenderEntity event = new EventRenderEntity(stage, entity, x, y, z, yaw, partialTicks);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

}
