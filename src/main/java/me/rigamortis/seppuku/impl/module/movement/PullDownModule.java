package me.rigamortis.seppuku.impl.module.movement;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.NumberValue;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
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
			RayTraceResult rayTraceResult = mc.world.rayTraceBlocks(
				mc.player.getPositionVector(),
				mc.player.getPositionVector()
					.add(
						new Vec3d(
							0,
							-3,
							0
						)
					)
			);
			
			if(rayTraceResult == null || rayTraceResult.typeOfHit == RayTraceResult.Type.MISS)
			{
				// Pull the player down
				mc.player.motionY = -(speed.getFloat());
			}
		}
	}
}
