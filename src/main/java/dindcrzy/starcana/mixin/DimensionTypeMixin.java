package dindcrzy.starcana.mixin;

import dindcrzy.starcana.Helper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.OptionalLong;

@Mixin(DimensionType.class)
public class DimensionTypeMixin {
	@Shadow @Final private OptionalLong fixedTime;

	/**
	 * @author BEE is YOU
	 * @reason simple function, easier to just completely replace
	 */
	@Overwrite()
	public int getMoonPhase(long time) {
		// 27000 = 24000 * 8 / 9
		// return (int)(time / 18000L % 8L + 8L) % 8;
		return (int)(((time + 7500) / 27000L + 4) % 8L + 8) % 8;
	}

	/**
	 * @author BEE is YOU
	 * @reason simple function, easier to just completely replace
	 */
	@Overwrite
	public float getSkyAngle(long time) {
		return Helper.getLinearSunAngle(fixedTime.orElse(time));
	}
}