package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.player.*;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.impl.management.PatchManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import team.stiff.pomelo.EventManager;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 4/6/2019 @ 6:01 PM.
 */
public final class PlayerControllerMPPatch extends ClassPatch {

    public PlayerControllerMPPatch() {
        super("net.minecraft.client.multiplayer.PlayerControllerMP", "bsa");
    }

    /**
     * This is called when we finish mining a block
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "onPlayerDestroyBlock",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/util/math/BlockPos;)Z",
            notchDesc = "(Let;)Z")
    public void onPlayerDestroyBlock(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions and add the needed instructions to call our hook function
        final InsnList insnList = new InsnList();
        //add ALOAD to pass the BlockPos into our hook function
        insnList.add(new VarInsnNode(ALOAD, 1));
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "onPlayerDestroyBlockHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/util/math/BlockPos;)Z" : "(Let;)Z", false));
        //add a label to jump to
        final LabelNode jmp = new LabelNode();
        //add if equals and pass the label
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //add a 0 or false
        insnList.add(new InsnNode(ICONST_0));
        //add return so the rest of the function doesn't get called
        insnList.add(new InsnNode(IRETURN));
        //add our label
        insnList.add(jmp);
        //insert the list of instructions at the top of the function
        methodNode.instructions.insert(insnList);
    }

    /**
     * Our onPlayerDestroyBlock hook used to get block coordinates
     * of what we just broke
     *
     * @param pos
     * @return
     */
    public static boolean onPlayerDestroyBlockHook(BlockPos pos) {
        //dispatch our event and pass the BlockPos
        final EventDestroyBlock event = new EventDestroyBlock(pos);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

    /**
     * This is where minecraft starts mining
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "clickBlock",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z",
            notchDesc = "(Let;Lfa;)Z")
    public void clickBlock(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions and add the needed instructions to call our hook function
        final InsnList insnList = new InsnList();
        //add ALOAD to pass the BlockPos into our hook function
        insnList.add(new VarInsnNode(ALOAD, 1));
        //add ALOAD to pass the EnumFacing into our hook function
        insnList.add(new VarInsnNode(ALOAD, 2));
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "clickBlockHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z" : "(Let;Lfa;)Z", false));
        //add a label to jump to
        final LabelNode jmp = new LabelNode();
        //add if equals and pass the label
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //add a 0 or false
        insnList.add(new InsnNode(ICONST_0));
        //add return so the rest of the function doesn't get called
        insnList.add(new InsnNode(IRETURN));
        //add our label
        insnList.add(jmp);
        //insert the list of instructions at the top of the function
        methodNode.instructions.insert(insnList);
    }

    /**
     * Our clickBlock hook used to detect when we first
     * click on a block
     *
     * @param pos
     * @param face
     * @return
     */
    public static boolean clickBlockHook(BlockPos pos, EnumFacing face) {
        //dispatch our event and pass the BlockPos and EnumFacing
        final EventClickBlock event = new EventClickBlock(pos, face);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

    /**
     * This is where minecraft handles abort destroying blocks
     * and resetting break progress
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "resetBlockRemoving",
            notchName = "c",
            mcpDesc = "()V")
    public void resetBlockRemoving(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions and add the needed instructions to call our hook function
        final InsnList insnList = new InsnList();
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "resetBlockRemovingHook", "()Z", false));
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
     * Our resetBlockRemoving used to detect when we stop mining
     * It is cancellable so we can save break progress
     *
     * @return
     */
    public static boolean resetBlockRemovingHook() {
        //dispatch the event
        final EventResetBlockRemoving event = new EventResetBlockRemoving();
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

    /**
     * This is where minecraft handles breaking blocks and calculates
     * block hit delay/current damage
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "onPlayerDamageBlock",
            notchName = "b",
            mcpDesc = "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z",
            notchDesc = "(Let;Lfa;)Z")
    public void onPlayerDamageBlock(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions and add the needed instructions to call our hook function
        final InsnList insnList = new InsnList();
        //add ALOAD to pass the BlockPos into our hook function
        insnList.add(new VarInsnNode(ALOAD, 1));
        //add ALOAD to pass the EnumFacing into our hook function
        insnList.add(new VarInsnNode(ALOAD, 2));
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "onPlayerDamageBlockHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z" : "(Let;Lfa;)Z", false));
        //add a label to jump to
        final LabelNode jmp = new LabelNode();
        //add if equals and pass the label
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //add a 0 or false
        insnList.add(new InsnNode(ICONST_0));
        //add return so the rest of the function doesn't get called
        insnList.add(new InsnNode(IRETURN));
        //add our label
        insnList.add(jmp);
        //insert the list of instructions at the top of the function
        methodNode.instructions.insert(insnList);
    }

    /**
     * Our onPlayerDamageBlock hook used to detect if we are
     * currently mining a block
     *
     * @param pos
     * @param face
     * @return
     */
    public static boolean onPlayerDamageBlockHook(BlockPos pos, EnumFacing face) {
        //dispatch the event
        final EventPlayerDamageBlock event = new EventPlayerDamageBlock(pos, face);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

    /**
     * This is where minecraft handles right clicking
     * on blocks
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "processRightClickBlock",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/client/entity/EntityPlayerSP;Lnet/minecraft/client/multiplayer/WorldClient;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/util/EnumActionResult;",
            notchDesc = "(Lbud;Lbsb;Let;Lfa;Lbhe;Lub;)Lud;")
    public void processRightClickBlock(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();

        //create a new instance of "EventRightClickBlock" and dupe the top val on the stack
        insnList.add(new TypeInsnNode(NEW, Type.getInternalName(EventRightClickBlock.class)));
        insnList.add(new InsnNode(DUP));
        //add ALOAD to pass BlockPos into our call
        insnList.add(new VarInsnNode(ALOAD, 3));
        //add ALOAD to pass EnumFacing into our call
        insnList.add(new VarInsnNode(ALOAD, 4));
        //add ALOAD to pass Vec3d into our call
        insnList.add(new VarInsnNode(ALOAD, 5));
        //add ALOAD to pass EnumHand into our call
        insnList.add(new VarInsnNode(ALOAD, 6));
        //call EventRightClickBlock's constructor
        insnList.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(EventRightClickBlock.class), "<init>", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/EnumHand;)V" : "(Let;Lfa;Lbhe;Lub;)V", false));
        //store a reference to the instance in the local vars
        insnList.add(new VarInsnNode(ASTORE, 19));

        //get "Seppuku.INSTNANCE"
        insnList.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Seppuku.class), "INSTANCE", "Lme/rigamortis/seppuku/Seppuku;"));
        //call "getEventManager"
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Seppuku.class), "getEventManager", "()Lteam/stiff/pomelo/EventManager;", false));

        //add ALOAD to get our event we stored
        insnList.add(new VarInsnNode(ALOAD, 19));
        //call "dispatchEvent" and pass the event
        insnList.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(EventManager.class), "dispatchEvent", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
        //remove the top val on the stack because we duped it
        insnList.add(new InsnNode(POP));

        //add ALOAD to get our event
        insnList.add(new VarInsnNode(ALOAD, 19));
        //call "isCanceled"
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventRightClickBlock.class), "isCanceled", "()Z", false));
        //add a label to jump to
        final LabelNode jmp = new LabelNode();
        //add "if equals"
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //return "EnumActionResult.FAIL"
        insnList.add(new FieldInsnNode(GETSTATIC, env == PatchManager.Environment.IDE ? "net/minecraft/util/EnumActionResult" : "ud", env == PatchManager.Environment.IDE ? "FAIL" : "c", env == PatchManager.Environment.IDE ? "Lnet/minecraft/util/EnumActionResult;" : "Lud;"));
        //add ARETURN because we are returning an object...
        insnList.add(new InsnNode(ARETURN));
        //add our label
        insnList.add(jmp);

        //insert the list of instructions at the top
        methodNode.instructions.insert(insnList);
    }

    @MethodPatch(
            mcpName = "processRightClick",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/util/EnumActionResult;",
            notchDesc = "(Laed;Lamu;Lub;)Lud;")
    public void processRightClick(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();

        //create a new instance of "EventRightClick" and dupe the top val on the stack
        insnList.add(new TypeInsnNode(NEW, Type.getInternalName(EventRightClick.class)));
        insnList.add(new InsnNode(DUP));
        //add ALOAD to pass EnumHand into our call
        insnList.add(new VarInsnNode(ALOAD, 3));
        //call EventRightClick's constructor
        insnList.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(EventRightClick.class), "<init>", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/util/EnumHand;)V" : "(Lub;)V", false));
        //store a reference to the instance in the local vars
        insnList.add(new VarInsnNode(ASTORE, 10));

        //get "Seppuku.INSTNANCE"
        insnList.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Seppuku.class), "INSTANCE", "Lme/rigamortis/seppuku/Seppuku;"));
        //call "getEventManager"
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Seppuku.class), "getEventManager", "()Lteam/stiff/pomelo/EventManager;", false));

        //add ALOAD to get our event we stored
        insnList.add(new VarInsnNode(ALOAD, 10));
        //call "dispatchEvent" and pass the event
        insnList.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(EventManager.class), "dispatchEvent", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
        //remove the top val on the stack because we duped it
        insnList.add(new InsnNode(POP));

        //add ALOAD to get our event
        insnList.add(new VarInsnNode(ALOAD, 10));
        //call "isCanceled"
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventRightClick.class), "isCanceled", "()Z", false));
        //add a label to jump to
        final LabelNode jmp = new LabelNode();
        //add "if equals"
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //return "EnumActionResult.FAIL"
        insnList.add(new FieldInsnNode(GETSTATIC, env == PatchManager.Environment.IDE ? "net/minecraft/util/EnumActionResult" : "ud", env == PatchManager.Environment.IDE ? "FAIL" : "c", env == PatchManager.Environment.IDE ? "Lnet/minecraft/util/EnumActionResult;" : "Lud;"));
        //add ARETURN because we are returning an object...
        insnList.add(new InsnNode(ARETURN));
        //add our label
        insnList.add(jmp);

        //insert the list of instructions at the top
        methodNode.instructions.insert(insnList);
    }

    @MethodPatch(
            mcpName = "isHittingPosition",
            notchName = "b",
            mcpDesc = "(Lnet/minecraft/util/math/BlockPos;)Z",
            notchDesc = "(Let;)Z")
    public void isHittingPosition(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions and add the needed instructions to call our hook function
        final InsnList insnList = new InsnList();
        //add ALOAD to pass the BlockPos into our call
        insnList.add(new VarInsnNode(ALOAD, 1));
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "isHittingPositionHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/util/math/BlockPos;)Z" : "(Let;)Z", false));
        //add a label to jump to
        final LabelNode jmp = new LabelNode();
        //add if equals and pass the label
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //add a 0 or false
        insnList.add(new InsnNode(ICONST_0));
        //add return so the rest of the function doesn't get called
        insnList.add(new InsnNode(IRETURN));
        //add our label
        insnList.add(jmp);
        //insert the list of instructions at the top of the function
        methodNode.instructions.insert(insnList);
    }

    public static boolean isHittingPositionHook(BlockPos pos) {
        final EventHittingPosition event = new EventHittingPosition(pos);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

    @MethodPatch(
            mcpName = "getIsHittingBlock",
            notchName = "m",
            mcpDesc = "()Z")
    public void getIsHittingBlock(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "getIsHittingBlockHook", "()Z", false));
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
     * Our getIsHittingBlockHook hook used to override block-hitting hand activity
     *
     * @return true if the event is cancelled
     */
    public static boolean getIsHittingBlockHook() {
        //dispatch our event
        final EventHittingBlock event = new EventHittingBlock();
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);
        return event.isCanceled();
    }

    @MethodPatch(
            mcpName = "getBlockReachDistance",
            notchName = "d",
            mcpDesc = "()F")
    public void getBlockReachDistance(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList insnList = new InsnList();
        insnList.add(new TypeInsnNode(NEW, Type.getInternalName(EventPlayerReach.class)));
        insnList.add(new InsnNode(DUP));
        insnList.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(EventPlayerReach.class), "<init>", "()V", false));
        insnList.add(new VarInsnNode(ASTORE, 2));
        insnList.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Seppuku.class), "INSTANCE", "Lme/rigamortis/seppuku/Seppuku;"));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Seppuku.class), "getEventManager", "()Lteam/stiff/pomelo/EventManager;", false));
        insnList.add(new VarInsnNode(ALOAD, 2));
        insnList.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(EventManager.class), "dispatchEvent", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
        insnList.add(new InsnNode(POP));
        insnList.add(new VarInsnNode(ALOAD, 2));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventPlayerReach.class), "isCanceled", "()Z", false));
        final LabelNode jmp = new LabelNode();
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        insnList.add(new VarInsnNode(ALOAD, 2));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventPlayerReach.class), "getReach", "()F", false));
        insnList.add(new InsnNode(FRETURN));
        insnList.add(jmp);
        methodNode.instructions.insert(insnList);
    }

    @MethodPatch(
            mcpName = "extendedReach",
            notchName = "i",
            mcpDesc = "()Z")
    public void extendedReach(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList insnList = new InsnList();
        insnList.add(new TypeInsnNode(NEW, Type.getInternalName(EventExtendPlayerReach.class)));
        insnList.add(new InsnNode(DUP));
        insnList.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(EventExtendPlayerReach.class), "<init>", "()V", false));
        insnList.add(new VarInsnNode(ASTORE, 2));
        insnList.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Seppuku.class), "INSTANCE", "Lme/rigamortis/seppuku/Seppuku;"));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Seppuku.class), "getEventManager", "()Lteam/stiff/pomelo/EventManager;", false));
        insnList.add(new VarInsnNode(ALOAD, 2));
        insnList.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(EventManager.class), "dispatchEvent", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
        insnList.add(new InsnNode(POP));
        insnList.add(new VarInsnNode(ALOAD, 2));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventExtendPlayerReach.class), "isCanceled", "()Z", false));
        final LabelNode jmp = new LabelNode();
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        insnList.add(new InsnNode(ICONST_1));
        insnList.add(new InsnNode(IRETURN));
        insnList.add(jmp);
        methodNode.instructions.insert(insnList);
    }
}
