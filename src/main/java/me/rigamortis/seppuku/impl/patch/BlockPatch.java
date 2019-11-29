package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.render.EventRenderBlockSide;
import me.rigamortis.seppuku.api.event.world.EventAddCollisionBox;
import me.rigamortis.seppuku.api.event.world.EventGetBlockLayer;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.impl.management.PatchManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import team.stiff.pomelo.EventManager;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 4/9/2019 @ 12:10 PM.
 */
public final class BlockPatch extends ClassPatch {

    public BlockPatch() {
        super("net.minecraft.block.Block", "aow");
    }

    /**
     * This is where minecraft handles what side of a
     * block should be rendered
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "shouldSideBeRendered",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z",
            notchDesc = "(Lawt;Lamy;Let;Lfa;)Z")
    public void shouldSideBeRendered(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();
        //create a new instance of "EventRenderBlockSide" and dupe the top value on the stack
        insnList.add(new TypeInsnNode(NEW, Type.getInternalName(EventRenderBlockSide.class)));
        insnList.add(new InsnNode(DUP));
        //add ALOAD 0 to pass "this" into the event
        insnList.add(new VarInsnNode(ALOAD, 0));
        //call "EventRenderBlockSide" constructor
        insnList.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(EventRenderBlockSide.class), "<init>", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/block/Block;)V" : "(Laow;)V", false));
        //store our event in the local vars
        insnList.add(new VarInsnNode(ASTORE, 6));
        //Seppuku.INSTANCE
        insnList.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Seppuku.class), "INSTANCE", "Lme/rigamortis/seppuku/Seppuku;"));
        //getEventManager
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Seppuku.class), "getEventManager", "()Lteam/stiff/pomelo/EventManager;", false));
        //add ALOAD to access our event
        insnList.add(new VarInsnNode(ALOAD, 6));
        //call EventManager.dispatchEvent and pass our event in
        insnList.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(EventManager.class), "dispatchEvent", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
        //remove the top value on the stack
        insnList.add(new InsnNode(POP));
        //add ALOAD to access our event
        insnList.add(new VarInsnNode(ALOAD, 6));
        //call EventRenderBlockSide.isCanceled
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventRenderBlockSide.class), "isCanceled", "()Z", false));
        //create a label to jump to
        final LabelNode jmp = new LabelNode();
        //add "if equals"
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //add ALOAD to access our event
        insnList.add(new VarInsnNode(ALOAD, 6));
        //call EventRenderBlockSide.isRenderable
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventRenderBlockSide.class), "isRenderable", "()Z", false));
        //return the value of isRenderable
        insnList.add(new InsnNode(IRETURN));
        //add our label
        insnList.add(jmp);
        //add our instructions at the top of the function
        methodNode.instructions.insert(insnList);
    }

    @MethodPatch(
            mcpName = "getBlockLayer",
            notchName = "f",
            mcpDesc = "Lnet/minecraft/util/BlockRenderLayer;",
            notchDesc = "Lamm;")
    public void getBlockLayer(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();
        //create a new instance of "EventGetBlockLayer" and dupe the top value on the stack
        insnList.add(new TypeInsnNode(NEW, Type.getInternalName(EventGetBlockLayer.class)));
        insnList.add(new InsnNode(DUP));
        //add ALOAD 0 to pass "this" into the event
        insnList.add(new VarInsnNode(ALOAD, 0));
        //call "EventGetBlockLayer" constructor
        insnList.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(EventGetBlockLayer.class), "<init>", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/block/Block;)V" : "(Laow;)V", false));
        //store our event in the local vars
        insnList.add(new VarInsnNode(ASTORE, 1));
        //Seppuku.INSTANCE
        insnList.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Seppuku.class), "INSTANCE", "Lme/rigamortis/seppuku/Seppuku;"));
        //getEventManager
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Seppuku.class), "getEventManager", "()Lteam/stiff/pomelo/EventManager;", false));
        //add ALOAD to access our event
        insnList.add(new VarInsnNode(ALOAD, 1));
        //call EventManager.dispatchEvent and pass our event in
        insnList.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(EventManager.class), "dispatchEvent", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
        //remove the top value on the stack
        insnList.add(new InsnNode(POP));
        //add ALOAD to access our event
        insnList.add(new VarInsnNode(ALOAD, 1));
        //call EventGetBlockLayer.isCanceled
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventGetBlockLayer.class), "isCanceled", "()Z", false));
        //create a label to jump to
        final LabelNode jmp = new LabelNode();
        //add "if equals"
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //add ALOAD to access our event
        insnList.add(new VarInsnNode(ALOAD, 1));
        //call EventGetBlockLayer.getLayer
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventGetBlockLayer.class), "getLayer", env == PatchManager.Environment.IDE ? "()Lnet/minecraft/util/BlockRenderLayer;" : "()Lamm;", false));
        //return the value of getLayer
        insnList.add(new InsnNode(ARETURN));
        //add our label
        insnList.add(jmp);
        //add our instructions at the top of the function
        methodNode.instructions.insert(insnList);
    }

    /**
     * This is where minecraft adds aabb's for block collision
     * @param methodNode
     * @param env
     */
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

    /**
     * Our addCollisionBoxToList hook used to disable block collision
     * @param entity
     * @return
     */
    public static boolean addCollisionBoxToListHook(BlockPos pos, Entity entity) {
        //dispatch our event and pass the block and entity in
        final EventAddCollisionBox event = new EventAddCollisionBox(pos, entity);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

}
