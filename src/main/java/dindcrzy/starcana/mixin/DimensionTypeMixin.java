package dindcrzy.starcana.mixin;

import dindcrzy.starcana.Helper;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

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
		return Helper.getMoonPhase(time);
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