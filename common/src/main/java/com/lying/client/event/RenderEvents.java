package com.lying.client.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.client.render.entity.EntityRenderDispatcher;

public class RenderEvents
{
	public static final Event<AddFeatureRenderersEvent> ADD_FEATURE_RENDERERS_EVENT = EventFactory.createLoop(AddFeatureRenderersEvent.class);
	
	@FunctionalInterface
	public interface AddFeatureRenderersEvent
	{
		void append(EntityRenderDispatcher dispatcher);
	}

}
