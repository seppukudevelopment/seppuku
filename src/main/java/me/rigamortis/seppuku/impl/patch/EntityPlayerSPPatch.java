package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.*;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.api.util.ASMUtil;
import me.rigamortis.seppuku.impl.management.PatchManager;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.ForgeHooksClient;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import team.stiff.pomelo.EventManager;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 4/8/2019 @ 2:48 AM.
 */
public final class EntityPlayerSPPatch extends ClassPatch {

    public EntityPlayerSPPatch() {
        super("net.minecraft.client.entity.EntityPlayerSP", "bud");
    }

    /**
     * This is where minecraft handles player updates and movement
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "onUpdate",
            notchName = "B_",
            mcpDesc = "()V")
    public void onUpdate(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList preInsn = new InsnList();
        //EventStageable.EventStage.PRE
        preInsn.add(new FieldInsnNode(GETSTATIC, "me/rigamortis/seppuku/api/event/EventStageable$EventStage", "PRE", "Lme/rigamortis/seppuku/api/event/EventStageable$EventStage;"));
        //call our hook function
        preInsn.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "onUpdateHook", "(Lme/rigamortis/seppuku/api/event/EventStageable$EventStage;)Z", false));
        //create a label to jump to
        final LabelNode jmp = new LabelNode();
        //add "if equals"
        preInsn.add(new JumpInsnNode(IFEQ, jmp));
        //return so the rest of the function doesnt get called
        preInsn.add(new InsnNode(RETURN));
        //add our label
        preInsn.add(jmp);
        //insert the list of instructs at the top of the function
        methodNode.instructions.insert(preInsn);

        //same as above
        final InsnList postInsn = new InsnList();
        //EventStageable.EventStage.POST
        postInsn.add(new FieldInsnNode(GETSTATIC, "me/rigamortis/seppuku/api/event/EventStageable$EventStage", "POST", "Lme/rigamortis/seppuku/api/event/EventStageable$EventStage;"));
        //call our hook function
        postInsn.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "onUpdateHook", "(Lme/rigamortis/seppuku/api/event/EventStageable$EventStage;)Z", false));
        //insert the list of instructions at the bottom of the function
        methodNode.instructions.insertBefore(ASMUtil.bottom(methodNode), postInsn);
    }

    /**
     * Our onUpdate hook
     * This is where minecraft runs movement related code and
     * sends movement packets
     *
     * @param stage
     * @return
     */
    public static boolean onUpdateHook(EventStageable.EventStage stage) {
        //dispatch our event and pass the stage in
        final EventPlayerUpdate event = new EventPlayerUpdate(stage);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

    /**
     * This is where minecraft handles player updates and movement
     * while not in a vehicle
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "onUpdateWalkingPlayer",
            notchName = "N",
            mcpDesc = "()V")
    public void onUpdateWalkingPlayer(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList preInsn = new InsnList();
        //EventStageable.EventStage.PRE
        preInsn.add(new FieldInsnNode(GETSTATIC, "me/rigamortis/seppuku/api/event/EventStageable$EventStage", "PRE", "Lme/rigamortis/seppuku/api/event/EventStageable$EventStage;"));
        //call our hook function
        preInsn.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "onUpdateWalkingPlayerHook", "(Lme/rigamortis/seppuku/api/event/EventStageable$EventStage;)Z", false));
        //create a label to jump to
        final LabelNode jmp = new LabelNode();
        //add "if equals"
        preInsn.add(new JumpInsnNode(IFEQ, jmp));
        //return so the rest of the function doesnt get called
        preInsn.add(new InsnNode(RETURN));
        //add our label
        preInsn.add(jmp);
        //insert the list of instructs at the top of the function
        methodNode.instructions.insert(preInsn);

        //same as above
        final InsnList postInsn = new InsnList();
        //EventStageable.EventStage.POST
        postInsn.add(new FieldInsnNode(GETSTATIC, "me/rigamortis/seppuku/api/event/EventStageable$EventStage", "POST", "Lme/rigamortis/seppuku/api/event/EventStageable$EventStage;"));
        //call our hook function
        postInsn.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "onUpdateWalkingPlayerHook", "(Lme/rigamortis/seppuku/api/event/EventStageable$EventStage;)Z", false));
        //insert the list of instructions at the bottom of the function
        methodNode.instructions.insertBefore(ASMUtil.bottom(methodNode), postInsn);
    }

    /**
     * Our onUpdate hook
     * This is where minecraft runs non vehicle movement related code and
     * sends movement packets
     *
     * @param stage
     * @return
     */
    public static boolean onUpdateWalkingPlayerHook(EventStageable.EventStage stage) {
        if (stage == EventStageable.EventStage.PRE) {
            Seppuku.INSTANCE.getRotationManager().updateRotations();
            Seppuku.INSTANCE.getPositionManager().updatePosition();

            //update all camera fbos after we render
            Seppuku.INSTANCE.getCameraManager().update();
        }

        //dispatch our event and pass the stage in
        final EventUpdateWalkingPlayer event = new EventUpdateWalkingPlayer(stage);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        if (stage == EventStageable.EventStage.POST) {
            Seppuku.INSTANCE.getRotationManager().restoreRotations();
            Seppuku.INSTANCE.getPositionManager().restorePosition();
        }

        return event.isCanceled();
    }

    /**
     * This is where minecraft handles sending chat messages
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "sendChatMessage",
            notchName = "g",
            mcpDesc = "(Ljava/lang/String;)V")
    public void sendChatMessage(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();
        //add ALOAD to pass the message into our hook function
        insnList.add(new VarInsnNode(ALOAD, 1));
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "sendChatMessageHook", "(Ljava/lang/String;)Z", false));
        //create a label to jump to
        final LabelNode jmp = new LabelNode();
        //add "if equals"
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //return so the rest of the function doesnt get called
        insnList.add(new InsnNode(RETURN));
        //add our label
        insnList.add(jmp);
        //insert the list of instructs at the top of the function
        methodNode.instructions.insert(insnList);
    }

    /**
     * Our sendChatMessage hook
     * It allows us to intercept outgoing chat messages
     *
     * @param message
     * @return
     */
    public static boolean sendChatMessageHook(String message) {
        //dispatch our event and pass the message in
        final EventSendChatMessage event = new EventSendChatMessage(message);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

    @MethodPatch(
            mcpName = "swingArm",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/util/EnumHand;)V",
            notchDesc = "(Lub;)V")
    public void swingArm(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();
        //add ALOAD to pass the EnumHand into our hook function
        insnList.add(new VarInsnNode(ALOAD, 1));
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "swingArmHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/util/EnumHand;)Z" : "(Lub;)Z", false));
        //create a label to jump to
        final LabelNode jmp = new LabelNode();
        //add "if equals"
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //return so the rest of the function doesnt get called
        insnList.add(new InsnNode(RETURN));
        //add our label
        insnList.add(jmp);
        //insert the list of instructs at the top of the function
        methodNode.instructions.insert(insnList);
    }

    /**
     * This is our swingArm hook
     * We can cancel to stop our swing animation
     * It's useful for older servers that dont support
     * swinging the OFF_HAND
     *
     * @param hand
     * @return
     */
    public static boolean swingArmHook(EnumHand hand) {
        //dispatch our event and pass the EnumHand in
        final EventSwingArm event = new EventSwingArm(hand);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

    /**
     * This happens when you exit an inventory
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "closeScreen",
            notchName = "p",
            mcpDesc = "()V")
    public void closeScreen(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "closeScreenHook", "()Z", false));
        //create a label to jump to
        final LabelNode jmp = new LabelNode();
        //add "if equals"
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //return so the rest of the function doesnt get called
        insnList.add(new InsnNode(RETURN));
        //add our label
        insnList.add(jmp);
        //insert the list of instructs at the top of the function
        methodNode.instructions.insert(insnList);
    }

    /**
     * Our closeScreen hook
     * Useful for some mods i.e MoreInv
     *
     * @return
     */
    public static boolean closeScreenHook() {
        //dispatch our event
        final EventCloseScreen event = new EventCloseScreen();
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

    @MethodPatch(
            mcpName = "pushOutOfBlocks",
            notchName = "i",
            mcpDesc = "(DDD)Z")
    public void pushOutOfBlocks(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "pushOutOfBlocksHook", "()Z", false));
        //create a label to jump to
        final LabelNode jmp = new LabelNode();
        //add "if equals"
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //add 0 or false
        insnList.add(new InsnNode(ICONST_0));
        //return so the rest of the function doesnt get called
        insnList.add(new InsnNode(IRETURN));
        //add our label
        insnList.add(jmp);
        //insert the list of instructs at the top of the function
        methodNode.instructions.insert(insnList);
    }

    /**
     * Our pushOutOfBlocks hook used to disable being pushed out of blocks
     *
     * @return
     */
    public static boolean pushOutOfBlocksHook() {
        //dispatch our event
        final EventPushOutOfBlocks event = new EventPushOutOfBlocks();
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

    /**
     * This is where minecraft slows you down while your hand is active
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "onLivingUpdate",
            notchName = "n",
            mcpDesc = "()V")
    public void onLivingUpdate(MethodNode methodNode, PatchManager.Environment env) {
        final AbstractInsnNode target = ASMUtil.findMethodInsn(methodNode, INVOKESTATIC, Type.getInternalName(ForgeHooksClient.class), "onInputUpdate", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/MovementInput;)V" : "(Laed;Lbub;)V");

        if (target != null) {
            methodNode.instructions.insert(target, new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "onLivingUpdateHook", "()V", false));
        }
    }

    /**
     * Our onLivingUpdate mid function hook
     * Used to negate slowing down while hands are active
     */
    public static void onLivingUpdateHook() {
        //dispatch our event
        final EventUpdateInput event = new EventUpdateInput();
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);
    }

    @MethodPatch(
            mcpName = "move",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/entity/MoverType;DDD)V",
            notchDesc = "(Lvv;DDD)V")
    public void move(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList insnList = new InsnList();
        insnList.add(new TypeInsnNode(NEW, Type.getInternalName(EventMove.class)));
        insnList.add(new InsnNode(DUP));
        insnList.add(new VarInsnNode(ALOAD, 1));
        insnList.add(new VarInsnNode(DLOAD, 2));
        insnList.add(new VarInsnNode(DLOAD, 4));
        insnList.add(new VarInsnNode(DLOAD, 6));
        insnList.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(EventMove.class), "<init>", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/entity/MoverType;DDD)V" : "(Lvv;DDD)V", false));
        insnList.add(new VarInsnNode(ASTORE, 11));

        insnList.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Seppuku.class), "INSTANCE", "Lme/rigamortis/seppuku/Seppuku;"));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Seppuku.class), "getEventManager", "()Lteam/stiff/pomelo/EventManager;", false));
        insnList.add(new VarInsnNode(ALOAD, 11));
        insnList.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(EventManager.class), "dispatchEvent", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
        insnList.add(new InsnNode(POP));

        insnList.add(new VarInsnNode(ALOAD, 11));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventMove.class), "getX", "()D", false));
        insnList.add(new VarInsnNode(DSTORE, 2));

        insnList.add(new VarInsnNode(ALOAD, 11));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventMove.class), "getY", "()D", false));
        insnList.add(new VarInsnNode(DSTORE, 4));

        insnList.add(new VarInsnNode(ALOAD, 11));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventMove.class), "getZ", "()D", false));
        insnList.add(new VarInsnNode(DSTORE, 6));

        insnList.add(new VarInsnNode(ALOAD, 11));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventMove.class), "isCanceled", "()Z", false));
        final LabelNode jmp = new LabelNode();
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        insnList.add(new InsnNode(RETURN));
        insnList.add(jmp);
        methodNode.instructions.insert(insnList);
    }

}

