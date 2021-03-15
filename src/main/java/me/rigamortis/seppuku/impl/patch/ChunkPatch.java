package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.world.EventAddEntity;
import me.rigamortis.seppuku.api.event.world.EventChunk;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.api.util.ASMUtil;
import me.rigamortis.seppuku.impl.management.PatchManager;
import net.minecraft.entity.Entity;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import team.stiff.pomelo.EventManager;

import static org.objectweb.asm.Opcodes.*;

/**
 * created by noil on 11/3/19 at 5:59 PM
 */
public final class ChunkPatch extends ClassPatch {

    public ChunkPatch() {
        super("net.minecraft.world.chunk.Chunk", "axw");
    }

    @MethodPatch(
            mcpName = "onUnload",
            notchName = "d",
            mcpDesc = "()V",
            notchDesc = "()V")
    public void onUnload(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList insnList = new InsnList();

        insnList.add(new TypeInsnNode(NEW, Type.getInternalName(EventChunk.class)));
        insnList.add(new InsnNode(DUP));
        insnList.add(new FieldInsnNode(GETSTATIC, "me/rigamortis/seppuku/api/event/world/EventChunk$ChunkType", "UNLOAD", "Lme/rigamortis/seppuku/api/event/world/EventChunk$ChunkType;"));
        insnList.add(new VarInsnNode(ALOAD, 0));
        insnList.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(EventChunk.class), "<init>", env == PatchManager.Environment.IDE ? "(Lme/rigamortis/seppuku/api/event/world/EventChunk$ChunkType;Lnet/minecraft/world/chunk/Chunk;)V" : "(Lme/rigamortis/seppuku/api/event/world/EventChunk$ChunkType;Laxw;)V", false));
        insnList.add(new VarInsnNode(ASTORE, 7));
        insnList.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Seppuku.class), "INSTANCE", "Lme/rigamortis/seppuku/Seppuku;"));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Seppuku.class), "getEventManager", "()Lteam/stiff/pomelo/EventManager;", false));
        insnList.add(new VarInsnNode(ALOAD, 7));
        insnList.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(EventManager.class), "dispatchEvent", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
        insnList.add(new InsnNode(POP));

        methodNode.instructions.insertBefore(ASMUtil.bottom(methodNode), insnList);
    }

    //    public void addEntity(Entity entityIn) {
    //    public addEntity(Lnet/minecraft/entity/Entity;)V
    //    public void a(vg) {
    @MethodPatch(
            mcpName = "addEntity",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/entity/Entity;)V",
            notchDesc = "(Lvg;)V")
    public void addEntity(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(ALOAD, 1));
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "onEntityAddedHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/entity/Entity;)Z" : "(Lvg;)Z", false));
        final LabelNode jmp = new LabelNode();
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        insnList.add(new InsnNode(RETURN));
        insnList.add(jmp);
        methodNode.instructions.insert(insnList);
    }

    public static boolean onEntityAddedHook(Entity entity) {
        final EventAddEntity eventAddEntity = new EventAddEntity(entity);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(eventAddEntity);
        return eventAddEntity.isCanceled();
    }

    /**
     *     // access flags 0x9
     *     public static handleChunkDataHook(Lnet/minecraft/world/chunk/Chunk;)V
     *     NEW me/rigamortis/seppuku/api/event/world/EventChunk
     *     DUP
     *     GETSTATIC me/rigamortis/seppuku/api/event/world/EventChunk$ChunkType.LOAD : Lme/rigamortis/seppuku/api/event/world/EventChunk$ChunkType;
     *     ALOAD 0
     *     INVOKESPECIAL me/rigamortis/seppuku/api/event/world/EventChunk.<init> (Lme/rigamortis/seppuku/api/event/world/EventChunk$ChunkType;Lnet/minecraft/world/chunk/Chunk;)V
     *     ASTORE 1
     *
     *     GETSTATIC me/rigamortis/seppuku/Seppuku.INSTANCE : Lme/rigamortis/seppuku/Seppuku;
     *     INVOKEVIRTUAL me/rigamortis/seppuku/Seppuku.getEventManager ()Lteam/stiff/pomelo/EventManager;
     *     ALOAD 1
     *     INVOKEINTERFACE team/stiff/pomelo/EventManager.dispatchEvent (Ljava/lang/Object;)Ljava/lang/Object; (itf)
     *     POP
     *
     *     LOCALVARIABLE chunk Lnet/minecraft/world/chunk/Chunk; L0 L3 0
     *     LOCALVARIABLE event Lme/rigamortis/seppuku/api/event/world/EventChunk; L1 L3 1
     *     MAXSTACK = 4
     *     MAXLOCALS = 2
     */
}
