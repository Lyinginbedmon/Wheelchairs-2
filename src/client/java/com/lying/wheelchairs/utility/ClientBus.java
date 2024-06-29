package com.lying.wheelchairs.utility;

import com.lying.wheelchairs.entity.IFlyingMount;
import com.lying.wheelchairs.network.StartFlyingPacket;

public class ClientBus
{
	public static void registerEventCallbacks()
	{
		ServerEvents.ON_DOUBLE_JUMP.register((living) -> 
		{
			if(living instanceof IFlyingMount && ((IFlyingMount)living).canStartFlying())
				StartFlyingPacket.send();
		});
	}
}
