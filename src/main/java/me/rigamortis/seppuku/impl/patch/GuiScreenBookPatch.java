package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.gui.EventBookPage;
import me.rigamortis.seppuku.api.event.gui.EventBookTitle;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.impl.management.PatchManager;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 4/8/2019 @ 7:08 PM.
 */
public final class GuiScreenBookPatch extends ClassPatch {

    public GuiScreenBookPatch() {
        super("net.minecraft.client.gui.GuiScreenBook", "bmj");
    }

    /**
     * This is where minecraft appends your text
     * to a page in books
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "pageInsertIntoCurrent",
            notchName = "b",
            mcpDesc = "(Ljava/lang/String;)V")
    public void pageInsertIntoCurrent(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();
        //add ALOAD to pass the page param to our hook function
        insnList.add(new VarInsnNode(ALOAD, 1));
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "pageInsertIntoCurrentHook", "(Ljava/lang/String;)Ljava/lang/String;", false));
        //save the value to the param
        insnList.add(new VarInsnNode(ASTORE, 1));
        //insert our instructions
        methodNode.instructions.insert(insnList);
    }

    /**
     * This is our pageInsertIntoCurrent hook
     * We can use it to modify book pages
     *
     * @param page
     * @return
     */
    public static String pageInsertIntoCurrentHook(String page) {
        //dispatch our event and pass in the page
        final EventBookPage event = new EventBookPage(page);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.getPage();
    }

    /**
     * This is where minecraft handles
     * typing in the title of a book
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "keyTypedInTitle",
            notchName = "c",
            mcpDesc = "(CI)V")
    public void keyTypedInTitle(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();

        //add ALOAD to access class fields
        insnList.add(new VarInsnNode(ALOAD, 0));
        insnList.add(new VarInsnNode(ALOAD, 0));
        //get the "bookTitle" field
        insnList.add(new FieldInsnNode(GETFIELD, env == PatchManager.Environment.IDE ? "net/minecraft/client/gui/GuiScreenBook" : "bmj", env == PatchManager.Environment.IDE ? "bookTitle" : "A", "Ljava/lang/String;"));
        //call our hook function and pass the "bookTitle" field
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "keyTypedInTitleHook", "(Ljava/lang/String;)Ljava/lang/String;", false));
        //store the value to the "bookTitle" field
        insnList.add(new FieldInsnNode(PUTFIELD, env == PatchManager.Environment.IDE ? "net/minecraft/client/gui/GuiScreenBook" : "bmj", env == PatchManager.Environment.IDE ? "bookTitle" : "A", "Ljava/lang/String;"));
        //insert our list of instructions
        methodNode.instructions.insert(insnList);
    }

    /**
     * This is our keyTypedInTitle
     * We can use it to modify the title text of a book
     *
     * @param title
     * @return
     */
    public static String keyTypedInTitleHook(String title) {
        //dispatch our event and pass in the title which can be null
        final EventBookTitle event = new EventBookTitle(title);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.getTitle();
    }

}
