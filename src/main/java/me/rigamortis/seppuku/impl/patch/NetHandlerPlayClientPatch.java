package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.world.EventChunk;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.api.util.ASMUtil;
import me.rigamortis.seppuku.impl.management.PatchManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketChunkData;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

/**
 * created by noil on 11/3/19 at 3:45 PM
 */
public final class NetHandlerPlayClientPatch extends ClassPatch {

    public NetHandlerPlayClientPatch() {
        super("net.minecraft.client.network.NetHandlerPlayClient", "brz");
    }

    @MethodPatch(
            mcpName = "handleChunkData",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/network/play/server/SPacketChunkData;)V",
            notchDesc = "(Lje;)V")
    public void handleChunkData(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(ALOAD, 1));
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "handleChunkDataHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/network/play/server/SPacketChunkData;)V" : "(Lje;)V", false));
        methodNode.instructions.insertBefore(ASMUtil.bottom(methodNode), insnList);
    }

    public static void handleChunkDataHook(SPacketChunkData chunkData) {
        if (chunkData != null) {
            final EventChunk event = new EventChunk(EventChunk.ChunkType.LOAD, Minecraft.getMinecraft().world.getChunk(chunkData.getChunkX(), chunkData.getChunkZ()));
            Seppuku.INSTANCE.getEventManager().dispatchEvent(event);
        }
    }
}
