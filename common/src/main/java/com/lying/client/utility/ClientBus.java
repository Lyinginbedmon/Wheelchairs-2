package com.lying.client.utility;

import com.lying.client.network.StartFlyingPacket;
import com.lying.entity.IFlyingMount;
import com.lying.utility.ServerEvents;

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
