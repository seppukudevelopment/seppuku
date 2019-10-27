package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.gui.EventRenderHelmet;
import me.rigamortis.seppuku.api.event.gui.EventRenderPortal;
import me.rigamortis.seppuku.api.event.gui.EventRenderPotions;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.impl.management.PatchManager;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 4/8/2019 @ 8:04 PM.
 */
public final class GuiIngameForgePatch extends ClassPatch {

    public GuiIngameForgePatch() {
        super("net.minecraftforge.client.GuiIngameForge");
    }

    /**
     * This is where minecraft renders a
     * portal overlay effect on your screen
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "renderPortal",
            mcpDesc = "(Lnet/minecraft/client/gui/ScaledResolution;F)V",
            notchDesc = "(Lbit;F)V")
    public void renderPortal(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "renderPortalHook", "()Z", false));
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
     * Our renderPortal hook to remove the portal overlay
     *
     * @return
     */
    public static boolean renderPortalHook() {
        //dispatch our event
        final EventRenderPortal event = new EventRenderPortal();
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

    /**
     * This is where minecraft renders potion icons
     * on the top right of your screen
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "renderPotionIcons",
            mcpDesc = "(Lnet/minecraft/client/gui/ScaledResolution;)V",
            notchDesc = "(Lbit;)V")
    public void renderPotionIcons(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "renderPotionIconsHook", "()Z", false));
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
     * Our renderPotionIcons hook used to remove potion
     * status icons
     *
     * @return
     */
    public static boolean renderPotionIconsHook() {
        //dispatch our event
        final EventRenderPotions event = new EventRenderPotions();
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

    /**
     * This is where minecraft renders overlay effects
     * like pumpkins
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "renderHelmet",
            mcpDesc = "(Lnet/minecraft/client/gui/ScaledResolution;F)V",
            notchDesc = "(Lbit;F)V")
    public void renderHelmet(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "renderHelmetHook", "()Z", false));
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
     * Our renderHelmet hook to remove overlay effects
     *
     * @return
     */
    public static boolean renderHelmetHook() {
        //dispatch our event
        final EventRenderHelmet event = new EventRenderHelmet();
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }
}
