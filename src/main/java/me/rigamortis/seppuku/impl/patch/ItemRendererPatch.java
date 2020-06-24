package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.render.EventRenderOverlay;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.impl.management.PatchManager;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 4/9/2019 @ 12:25 AM.
 */
public final class ItemRendererPatch extends ClassPatch {

    public ItemRendererPatch() {
        super("net.minecraft.client.renderer.ItemRenderer", "buu");
    }

    @MethodPatch(
            mcpName = "renderSuffocationOverlay",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V",
            notchDesc = "(Lcdq;)V")
    public void renderSuffocationOverlay(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions and add the needed instructions to call our hook function
        final InsnList insnList = new InsnList();
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "renderSuffocationOverlayHook", "()Z", false));
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

    public static boolean renderSuffocationOverlayHook() {
        final EventRenderOverlay event = new EventRenderOverlay(EventRenderOverlay.OverlayType.BLOCK);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

    @MethodPatch(
            mcpName = "renderWaterOverlayTexture",
            notchName = "e",
            mcpDesc = "(F)V")
    public void renderWaterOverlayTexture(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions and add the needed instructions to call our hook function
        final InsnList insnList = new InsnList();
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "renderWaterOverlayTextureHook", "()Z", false));
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

    public static boolean renderWaterOverlayTextureHook() {
        final EventRenderOverlay event = new EventRenderOverlay(EventRenderOverlay.OverlayType.LIQUID);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

    @MethodPatch(
            mcpName = "renderFireInFirstPerson",
            notchName = "d",
            mcpDesc = "()V")
    public void renderFireInFirstPerson(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions and add the needed instructions to call our hook function
        final InsnList insnList = new InsnList();
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "renderFireInFirstPersonHook", "()Z", false));
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

    public static boolean renderFireInFirstPersonHook() {
        final EventRenderOverlay event = new EventRenderOverlay(EventRenderOverlay.OverlayType.FIRE);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }
}
