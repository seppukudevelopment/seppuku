package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.gui.EventGetGuiTabName;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.impl.management.PatchManager;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Written by TBM
 */
public final class GuiPlayerTabOverlayPatch extends ClassPatch {

    public GuiPlayerTabOverlayPatch() {
        super("net.minecraft.client.gui.GuiPlayerTabOverlay", "bjq");
    }

    @MethodPatch(
            mcpName = "getPlayerName",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/client/network/NetworkPlayerInfo;)Ljava/lang/String;",
            notchDesc = "(Lbsc;)Ljava/lang/String;"
    )
    public void getPlayerName(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(ALOAD, 1));
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "getPlayerNameHook",  env == PatchManager.Environment.IDE ? "(Lnet/minecraft/client/network/NetworkPlayerInfo;)Ljava/lang/String;" : "(Lbsc;)Ljava/lang/String;", false));
        insnList.add(new InsnNode(ARETURN));
        methodNode.instructions.insert(insnList);
    }

    public static String getPlayerNameHook(NetworkPlayerInfo networkPlayerInfo) {
        final EventGetGuiTabName event = new EventGetGuiTabName(networkPlayerInfo.getDisplayName() != null ? networkPlayerInfo.getDisplayName().getUnformattedComponentText() : networkPlayerInfo.getGameProfile().getName());
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.getName();
    }



}
