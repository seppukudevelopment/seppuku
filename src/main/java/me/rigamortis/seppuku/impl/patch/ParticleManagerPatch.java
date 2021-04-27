package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.render.EventAddEffect;
import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.impl.management.PatchManager;
import net.minecraft.client.particle.Particle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author noil
 */
public class ParticleManagerPatch extends ClassPatch {

    public ParticleManagerPatch() {
        super("net.minecraft.client.particle.ParticleManager", "btg");
    }

    /*
     * public void a(btf ) {
     *     this.h.add();
     *   }
     */
    @MethodPatch(
            mcpName = "addEffect",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/client/particle/Particle;)V",
            notchDesc = "(Lbtf;)V")
    public void addEffect(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();
        //aload in our Particle effect
        insnList.add(new VarInsnNode(ALOAD, 1));
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "addEffectHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/client/particle/Particle;)Z" : "(Lbtf;)Z", false));
        //create a label to jump to
        final LabelNode jmp = new LabelNode();
        //add "if equals"
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //return so the rest of the function doesn't get called
        insnList.add(new InsnNode(RETURN));
        //add our label
        insnList.add(jmp);
        //insert the list of instructs at the top of the function
        methodNode.instructions.insert(insnList);
    }

    public static boolean addEffectHook(Particle particle) {
        final EventAddEffect event = new EventAddEffect(particle);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);
        return event.isCanceled();
    }

    /*
    // access flags 0x1
    public emitParticleAtEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/EnumParticleTypes;)V

    // access flags 0x1
    public emitParticleAtEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/EnumParticleTypes;I)V
    */

//    @MethodPatch(
//            mcpName = "emitParticleAtEntity",
//            notchName = "a",
//            mcpDesc = "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/EnumParticleTypes;)V",
//            notchDesc = "(Lvg;Lfj;)V")
//    public void emitParticleAtEntity1(MethodNode methodNode, PatchManager.Environment env) {
//        //create a list of instructions
//        final InsnList insnList = new InsnList();
//        //aload in our Particle effect
//        insnList.add(new VarInsnNode(ALOAD, 2));
//        //call our hook function
//        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "emitParticleAtEntityHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/util/EnumParticleTypes;)Z" : "(Lfj;)Z", false));
//        //create a label to jump to
//        final LabelNode jmp = new LabelNode();
//        //add "if equals"
//        insnList.add(new JumpInsnNode(IFEQ, jmp));
//        //return so the rest of the function doesn't get called
//        insnList.add(new InsnNode(RETURN));
//        //add our label
//        insnList.add(jmp);
//        //insert the list of instructs at the top of the function
//        methodNode.instructions.insert(insnList);
//    }
//
//    @MethodPatch(
//            mcpName = "emitParticleAtEntity",
//            notchName = "a",
//            mcpDesc = "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/EnumParticleTypes;I)V",
//            notchDesc = "(Lvg;Lfj;I)V")
//    public void emitParticleAtEntity2(MethodNode methodNode, PatchManager.Environment env) {
//        //create a list of instructions
//        final InsnList insnList = new InsnList();
//        //aload in our Particle effect
//        insnList.add(new VarInsnNode(ALOAD, 2));
//        //call our hook function
//        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "emitParticleAtEntityHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/util/EnumParticleTypes;)Z" : "(Lfj;)Z", false));
//        //create a label to jump to
//        final LabelNode jmp = new LabelNode();
//        //add "if equals"
//        insnList.add(new JumpInsnNode(IFEQ, jmp));
//        //return so the rest of the function doesn't get called
//        insnList.add(new InsnNode(RETURN));
//        //add our label
//        insnList.add(jmp);
//        //insert the list of instructs at the top of the function
//        methodNode.instructions.insert(insnList);
//    }

    // public Particle spawnEffectParticle(int particleId, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
    // public btf a(int , double d1, double d2, double d3, double d4, double d5, double d6, int... arrayOfInt)
    //  public varargs spawnEffectParticle(IDDDDDD[I)Lnet/minecraft/client/particle/Particle;
    /*
    @MethodPatch(
            mcpName = "spawnEffectParticle",
            notchName = "a",
            mcpDesc = "(IDDDDDD[I)Lnet/minecraft/client/particle/Particle;",
            notchDesc = "(IDDDDDD[I)Lbtf;")
    public void spawnEffectParticle(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();
        //aload in our Particle effect
        insnList.add(new VarInsnNode(ALOAD, 1));
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "spawnEffectParticleHook", "(I)Z", false));
        //create a label to jump to
        final LabelNode jmp = new LabelNode();
        //add "if equals"
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //return so the rest of the function doesn't get called
        insnList.add(new InsnNode(RETURN));
        //add our label
        insnList.add(jmp);
        //insert the list of instructs at the top of the function
        methodNode.instructions.insert(insnList);
    }
    */

    /*
    public static boolean spawnEffectParticleHook(int particleID) {
        final EventSpawnEffect event = new EventSpawnEffect(particleID);
        Seppuku.INSTANCE.getEventManager().dispatchEvent(event);
        return event.isCanceled();
    }
    */
}
