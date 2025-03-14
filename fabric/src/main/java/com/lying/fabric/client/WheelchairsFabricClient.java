package com.lying.fabric.client;

import com.lying.client.WheelchairsClient;

import net.fabricmc.api.ClientModInitializer;

public final class WheelchairsFabricClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
    	WheelchairsClient.clientInit();
    }
}
