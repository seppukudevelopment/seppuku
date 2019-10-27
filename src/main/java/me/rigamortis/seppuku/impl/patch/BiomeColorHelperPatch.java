package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.world.EventFoliageColor;
import me.rigamortis.seppuku.api.event.world.EventGrassColor;
import me.rigamortis.seppuku.api.event.world.EventWaterColor;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.impl.management.PatchManager;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import team.stiff.pomelo.EventManager;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 8/11/2019 @ 1:46 AM.
 */
public final class BiomeColorHelperPatch extends ClassPatch {

    public BiomeColorHelperPatch() {
        super("net.minecraft.world.biome.BiomeColorHelper", "anj");
    }

    @MethodPatch(
            mcpName = "getGrassColorAtPos",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)I",
            notchDesc = "(Lamy;Let;)I")
    public void getGrassColorAtPos(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList insnList = new InsnList();
        insnList.add(new TypeInsnNode(NEW, Type.getInternalName(EventGrassColor.class)));
        insnList.add(new InsnNode(DUP));
        insnList.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(EventGrassColor.class), "<init>", "()V", false));
        insnList.add(new VarInsnNode(ASTORE, 2));

        insnList.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Seppuku.class), "INSTANCE", "Lme/rigamortis/seppuku/Seppuku;"));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Seppuku.class), "getEventManager", "()Lteam/stiff/pomelo/EventManager;", false));
        insnList.add(new VarInsnNode(ALOAD, 2));
        insnList.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(EventManager.class), "dispatchEvent", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
        insnList.add(new InsnNode(POP));

        insnList.add(new VarInsnNode(ALOAD, 2));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventGrassColor.class), "isCanceled", "()Z", false));
        final LabelNode label = new LabelNode();
        insnList.add(new JumpInsnNode(IFEQ, label));
        insnList.add(new VarInsnNode(ALOAD, 2));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventGrassColor.class), "getColor", "()I", false));
        insnList.add(new InsnNode(IRETURN));
        insnList.add(label);
        methodNode.instructions.insert(insnList);
    }

    @MethodPatch(
            mcpName = "getFoliageColorAtPos",
            notchName = "b",
            mcpDesc = "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)I",
            notchDesc = "(Lamy;Let;)I")
    public void getFoliageColorAtPos(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList insnList = new InsnList();
        insnList.add(new TypeInsnNode(NEW, Type.getInternalName(EventFoliageColor.class)));
        insnList.add(new InsnNode(DUP));
        insnList.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(EventFoliageColor.class), "<init>", "()V", false));
        insnList.add(new VarInsnNode(ASTORE, 2));

        insnList.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Seppuku.class), "INSTANCE", "Lme/rigamortis/seppuku/Seppuku;"));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Seppuku.class), "getEventManager", "()Lteam/stiff/pomelo/EventManager;", false));
        insnList.add(new VarInsnNode(ALOAD, 2));
        insnList.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(EventManager.class), "dispatchEvent", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
        insnList.add(new InsnNode(POP));

        insnList.add(new VarInsnNode(ALOAD, 2));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventFoliageColor.class), "isCanceled", "()Z", false));
        final LabelNode label = new LabelNode();
        insnList.add(new JumpInsnNode(IFEQ, label));
        insnList.add(new VarInsnNode(ALOAD, 2));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventFoliageColor.class), "getColor", "()I", false));
        insnList.add(new InsnNode(IRETURN));
        insnList.add(label);
        methodNode.instructions.insert(insnList);
    }

    @MethodPatch(
            mcpName = "getWaterColorAtPos",
            notchName = "c",
            mcpDesc = "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)I",
            notchDesc = "(Lamy;Let;)I")
    public void getWaterColorAtPos(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList insnList = new InsnList();
        insnList.add(new TypeInsnNode(NEW, Type.getInternalName(EventWaterColor.class)));
        insnList.add(new InsnNode(DUP));
        insnList.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(EventWaterColor.class), "<init>", "()V", false));
        insnList.add(new VarInsnNode(ASTORE, 2));

        insnList.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Seppuku.class), "INSTANCE", "Lme/rigamortis/seppuku/Seppuku;"));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Seppuku.class), "getEventManager", "()Lteam/stiff/pomelo/EventManager;", false));
        insnList.add(new VarInsnNode(ALOAD, 2));
        insnList.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(EventManager.class), "dispatchEvent", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
        insnList.add(new InsnNode(POP));

        insnList.add(new VarInsnNode(ALOAD, 2));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventWaterColor.class), "isCanceled", "()Z", false));
        final LabelNode label = new LabelNode();
        insnList.add(new JumpInsnNode(IFEQ, label));
        insnList.add(new VarInsnNode(ALOAD, 2));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventWaterColor.class), "getColor", "()I", false));
        insnList.add(new InsnNode(IRETURN));
        insnList.add(label);
        methodNode.instructions.insert(insnList);
    }

}
