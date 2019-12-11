package me.rigamortis.seppuku.impl.patch;

import me.rigamortis.seppuku.api.patch.ClassPatch;
import me.rigamortis.seppuku.api.patch.MethodPatch;
import me.rigamortis.seppuku.impl.management.PatchManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author cookiedragon234 10/Dec/2019
 */
public class GuiChatPatch extends ClassPatch
{
	public GuiChatPatch()
	{
		super("net.minecraft.client.gui.GuiChat", "bkn");
	}
	
	@MethodPatch(
		mcpName = "keyTyped",
		notchName = "a",
		mcpDesc = "(CI)V",
		notchDesc = "(CI)V")
	public void keyTyped(MethodNode methodNode, PatchManager.Environment env)
	{
		InsnList instructions = methodNode.instructions;
		
		ListIterator<AbstractInsnNode> iterator = instructions.iterator();
		while(iterator.hasNext())
		{
			AbstractInsnNode insn = iterator.next();
			if(insn instanceof MethodInsnNode)
			{
				MethodInsnNode methodInsnNode = (MethodInsnNode) insn;
				
				if(
					insn.getOpcode() == INVOKEVIRTUAL
					&&
					methodInsnNode.owner.equals(env == PatchManager.Environment.IDE ? "net/minecraft/client/Minecraft" : "bib")
					&&
					methodInsnNode.name.equals(env == PatchManager.Environment.IDE ? "displayGuiScreen" : "a")
					&&
					methodInsnNode.desc.equals(env == PatchManager.Environment.IDE ? "(Lnet/minecraft/client/gui/GuiScreen;)V" : "(Lblk;)V")
				)
				{
					
					InsnList insnsToInsert = new InsnList();
					
					insnsToInsert.add(new MethodInsnNode(
						INVOKESTATIC,
						Type.getInternalName(this.getClass()),
						"shouldNotCloseGui",
						"()Z",
						false
					));
					
					final LabelNode jmp = new LabelNode();
					
					insnsToInsert.add(new JumpInsnNode(IFEQ, jmp));
					insnsToInsert.add(new InsnNode(RETURN));
					insnsToInsert.add(jmp);
					
					instructions.insertBefore(insn.getPrevious().getPrevious().getPrevious(), insnsToInsert);
				}
			}
		}
	}
	
	@Override
	public boolean isDebug()
	{
		return true;
	}
	
	public static boolean shouldNotCloseGui()
	{
		return !(Minecraft.getMinecraft().currentScreen instanceof GuiChat);
	}
}
