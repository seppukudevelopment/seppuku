package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.world.EventAddCollisionBox;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.impl.management.PatchManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 5/20/2019 @ 3:38 PM.
 */
public final class BlockFencePatch extends ClassPatch {

    public BlockFencePatch() {
        super("net.minecraft.block.BlockFence", "aqo");
    }

    @MethodPatch(
            mcpName = "addCollisionBoxToList",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V",
            notchDesc = "(Lawt;Lamu;Let;Lbhb;Ljava/util/List;Lvg;Z)V")
    public void addCollisionBoxToList(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();
        //add ALOAD to pass the BlockPos in
        insnList.add(new VarInsnNode(ALOAD, 3));
        //add ALOAD to pass the entity in
        insnList.add(new VarInsnNode(ALOAD, 6));
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "addCollisionBoxToListHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)Z" : "(Let;Lvg;)Z", false));
        //create a label to jump to
        final LabelNode jmp = new LabelNode();
        //add "if equals" and pass our label in
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //add return so the rest of the function doesn't get called
        insnList.add(new InsnNode(RETURN));
        //add our label
        insnList.add(jmp);
        //insert our instructions
        methodNode.instructions.insert(insnList);
    }

    public static boolean addCollisionBoxToListHook(BlockPos pos, Entity entity) {
        //dispatch our event and pass the block and entity in
        final EventAddCollisionBox event = new EventAddCollisionBox(pos, entity);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

}