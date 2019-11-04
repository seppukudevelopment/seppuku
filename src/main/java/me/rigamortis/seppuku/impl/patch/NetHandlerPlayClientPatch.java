package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.world.EventChunk;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.api.util.ASMUtil;
import me.rigamortis.seppuku.impl.management.PatchManager;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.chunk.Chunk;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * created by noil on 11/3/19 at 3:45 PM
 */
public final class NetHandlerPlayClientPatch extends ClassPatch {

    public NetHandlerPlayClientPatch() {
        super("net.minecraft.client.network.NetHandlerPlayClient", "brx");
    }

    @MethodPatch(
            mcpName = "handleChunkData",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/network/play/server/SPacketChunkData;)V",
            notchDesc = "(Lje;)V")
    public void handleChunkData(MethodNode methodNode, PatchManager.Environment env) {
        final AbstractInsnNode target = ASMUtil.findMethodInsn(methodNode, INVOKEVIRTUAL, Type.getInternalName(WorldClient.class), "markBlockRangeForRenderUpdate", "(IIIIII)V");

        if (target != null) {
            final InsnList insnList = new InsnList();
            insnList.add(new VarInsnNode(ALOAD, 2));
            insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "handleChunkDataHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/world/chunk/Chunk;)V" : "(Laxu;)V", false));
            methodNode.instructions.insert(target, insnList);
        }
    }

    public static void handleChunkDataHook(Chunk chunk) {
        if (chunk != null) {
            final EventChunk event = new EventChunk(EventChunk.ChunkType.LOAD, chunk);
            Seppuku.INSTANCE.getEventManager().dispatchEvent(event);
        }
    }
}
