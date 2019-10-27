package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.minecraft.EventDisplayGui;
import me.rigamortis.seppuku.api.event.minecraft.EventKeyPress;
import me.rigamortis.seppuku.api.event.minecraft.EventRunTick;
import me.rigamortis.seppuku.api.event.minecraft.EventUpdateFramebufferSize;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.api.util.ASMUtil;
import me.rigamortis.seppuku.impl.management.PatchManager;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 4/4/2019 @ 11:27 PM.
 */
public final class MinecraftPatch extends ClassPatch {

    public MinecraftPatch() {
        super("net.minecraft.client.Minecraft", "bib");
    }

    /**
     * Patch the method "updateFramebufferSize"
     * Mainly used for shaders
     * @param methodNode
     */
    @MethodPatch(
            mcpName = "updateFramebufferSize",
            notchName = "aC",
            mcpDesc = "()V")
    public void updateFramebufferSize(MethodNode methodNode, PatchManager.Environment env) {
        //inset a static method call to our method "updateFramebufferSizeHook"
        methodNode.instructions.insert(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "updateFramebufferSizeHook", "()V", false));
    }

    /**
     * This is called when we resize our game
     */
    public static void updateFramebufferSizeHook() {
        //dispatch our event "EventUpdateFramebufferSize"
        Seppuku.INSTANCE.getEventManager().dispatchEvent(new EventUpdateFramebufferSize());
    }

    /**
     * Patch the method "runTick"
     * The bytecode we are inserting here replicates this call
     * MinecraftPatch.runTickHook(EventStageable.EventStage.PRE);
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "runTick",
            notchName = "t",
            mcpDesc = "()V")
    public void runTick(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions and add the needed instructions to call our hook function
        final InsnList preInsn = new InsnList();
        //EventStageable.EventStage.PRE
        preInsn.add(new FieldInsnNode(GETSTATIC, "me/rigamortis/seppuku/api/event/EventStageable$EventStage", "PRE", "Lme/rigamortis/seppuku/api/event/EventStageable$EventStage;"));
        //MinecraftPatch.runTickHook();
        preInsn.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "runTickHook", "(Lme/rigamortis/seppuku/api/event/EventStageable$EventStage;)V", false));
        //insert the list of instructions at the top of the function
        methodNode.instructions.insert(preInsn);

        //do the same thing as above but insert the list at the bottom of the method
        final InsnList postInsn = new InsnList();
        //EventStageable.EventStage.POST
        postInsn.add(new FieldInsnNode(GETSTATIC, "me/rigamortis/seppuku/api/event/EventStageable$EventStage", "POST", "Lme/rigamortis/seppuku/api/event/EventStageable$EventStage;"));
        //MinecraftPatch.runTickHook();
        postInsn.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "runTickHook", "(Lme/rigamortis/seppuku/api/event/EventStageable$EventStage;)V", false));
        //insert the list of instructions at the bottom of the function
        methodNode.instructions.insertBefore(ASMUtil.bottom(methodNode), postInsn);
    }

    /**
     * This is twice called every tick
     */
    public static void runTickHook(EventStageable.EventStage stage) {
        //dispatch our event "EventRunTick" and pass in the stage(pre, post)
        Seppuku.INSTANCE.getEventManager().dispatchEvent(new EventRunTick(stage));
    }

    /**
     * This is where key input is handled
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "runTickKeyboard",
            notchName = "aD",
            mcpDesc = "()V")
    public void runTickKeyboard(MethodNode methodNode, PatchManager.Environment env) {
        //find the instruction that calls dispatchKeypresses
        final AbstractInsnNode target = ASMUtil.findMethodInsn(methodNode, INVOKEVIRTUAL, env == PatchManager.Environment.IDE ? "net/minecraft/client/Minecraft" : "bib", env == PatchManager.Environment.IDE ? "dispatchKeypresses" : "W", "()V");

        if(target != null) {
            //make a list of instructions
            final InsnList insnList = new InsnList();
            //we use ILOAD to pass the "keycode" into our call
            insnList.add(new VarInsnNode(ILOAD, 1));
            //call our hook function
            insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "runTickKeyboardHook", "(I)V", false));
            //inset the instructions after the call "dispatchKeypresses"
            methodNode.instructions.insert(target, insnList);
        }
    }

    /**
     * This is a hacky way to intercept key presses
     */
    public static void runTickKeyboardHook(int key) {
        //check if the key was just pressed
        if(Keyboard.getEventKeyState()) {
            //dispatch our event for key presses and pass in the keycode
            Seppuku.INSTANCE.getEventManager().dispatchEvent(new EventKeyPress(key));
        }
    }

    /**
     * This is used to tell if we just opened a gui screen
     * It can be cancelled
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "displayGuiScreen",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/client/gui/GuiScreen;)V",
            notchDesc = "(Lblk;)V")
    public void displayGuiScreen(MethodNode methodNode, PatchManager.Environment env) {
        //make a list of our instructions
        final InsnList insnList = new InsnList();
        //we use ALOAD to pass the first param to our call
        insnList.add(new VarInsnNode(ALOAD, 1));
        //call our hook function
        //note the desc is ()Z because our hook function is a boolean
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "displayGuiScreenHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/client/gui/GuiScreen;)Z" : "(Lblk;)Z", false));
        //create a LabelNode to jump to
        final LabelNode jmp = new LabelNode();
        //add an "if equals" and pass our label
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //add a return
        insnList.add(new InsnNode(RETURN));
        //finally add the label
        insnList.add(jmp);
        //insert our instructions at the top
        methodNode.instructions.insert(insnList);
    }

    /**
     * Our display gui hook called when we open a gui
     * @param screen can be null!
     * @return
     */
    public static boolean displayGuiScreenHook(GuiScreen screen) {
        //dispatch our event and pass the gui
        final EventDisplayGui event = new EventDisplayGui(screen);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        //return event.isCanceled() to allow us to cancel the original function
        return event.isCanceled();
    }

}
