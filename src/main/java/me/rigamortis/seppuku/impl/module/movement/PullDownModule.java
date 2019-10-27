package me.rigamortis.seppuku.impl.module.movement;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.NumberValue;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author cookiedragon234
 */
public class PullDownModule extends Module
{
	public final NumberValue<Float> speed = new NumberValue<>("Speed", new String[]{"velocity"}, 10f, Float.class, 0f, 20f, 1f);
	
	public PullDownModule()
	{
		super("PullDown", new String[]{"FastFall"}, "Increase your downwards velocity when falling", "NONE", -1, ModuleType.MOVEMENT);
	}
	
	@Listener
	public void onUpdate(EventPlayerUpdate event)
	{
		if (event.getStage() == EventStageable.EventStage.PRE)
		{
			final Minecraft mc = Minecraft.getMinecraft();
			
			// obvs dont do this when flying or when using elytras
			if(mc.player.isElytraFlying() || mc.player.capabilities.isFlying || mc.player.onGround) return;
			
			// dont trigger when they could just be jumping, 3 blocks is maybe overkill? But its probably the best thing to do
			boolean isBlockBelow =
				!mc.world.isAirBlock(mc.player.getPosition().add(0, -1, 0))
				||
				!mc.world.isAirBlock(mc.player.getPosition().add(0, -2, 0))
				||
				!mc.world.isAirBlock(mc.player.getPosition().add(0, -3, 0))
			;
			
			if(!isBlockBelow)
			{
				// Pull the player down
				mc.player.motionY = -(speed.getFloat());
				// kk thx that ends epic module bsb on top
			}
		}
	}
}
