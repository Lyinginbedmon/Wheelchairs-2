package com.lying.wheelchairs.utility;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class WHCUtils
{
	/** Converts a global directional vector to a local vector by rotating it inverse to the local yaw */
	public static Vec3d globalToLocal(Vec3d movement, float yaw)
	{
		return localToGlobal(movement, 1F - yaw);
	}
	
	/** Converts a local directional vector to a global vector by rotating it by the local yaw */
	public static Vec3d localToGlobal(Vec3d movement, float yaw)
	{
		float sin = MathHelper.sin(yaw * ((float)Math.PI / 180));
		float cos = MathHelper.cos(yaw * ((float)Math.PI / 180));
		return new Vec3d(movement.x * cos - movement.z * sin, movement.y, movement.z * cos + movement.x * sin);
	}
	
	/** Clamps the given rotation in degrees to a value between 0 and 360 */
	public static float clampRotation(float value)
	{
		if(value > 0F)
			value %= 360F;
		else if(value < 0F)
			value += MathHelper.ceil(Math.abs(value) / 360F) * 360F;
		
		return value;
	}
	
	/** Calculates how many degrees a wheel will turn when moving a given distance forward or backward */
	public static float calculateSpin(float movement, float diameter)
	{
		if(movement == 0F)
			return 0F;
		
		float radius = diameter * 0.5F;
		float circumference = 2F * (float)Math.PI * radius;
		float spin = (float)(Math.abs(movement) / circumference) * 360F * Math.signum(movement);
		
		return clampRotation(spin);
	}
}
