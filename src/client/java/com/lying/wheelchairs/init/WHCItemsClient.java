package com.lying.wheelchairs.init;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class WHCItemsClient
{
	private static final List<ExtraModelHandler> EXTRA_MODELS = Lists.newArrayList();
	
	public static void registerItemColors()
	{
		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> { return tintIndex == 0 ? ((DyeableItem)stack.getItem()).getColor(stack) : -1; }, 
				WHCItems.WHEELCHAIR_ACACIA,
				WHCItems.WHEELCHAIR_BIRCH,
				WHCItems.WHEELCHAIR_DARK_OAK,
				WHCItems.WHEELCHAIR_JUNGLE,
				WHCItems.WHEELCHAIR_OAK,
				WHCItems.WHEELCHAIR_SPRUCE,
				WHCItems.WHEELCHAIR_CHERRY,
				WHCItems.WHEELCHAIR_MANGROVE,
				WHCItems.WHEELCHAIR_WARPED,
				WHCItems.WHEELCHAIR_CRIMSON,
				WHCItems.WHEELCHAIR_BAMBOO);
		
		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> { return tintIndex > 0 ? ((DyeableItem)stack.getItem()).getColor(stack) : -1; }, 
				WHCItems.CRUTCH_ACACIA,
				WHCItems.CRUTCH_BIRCH,
				WHCItems.CRUTCH_DARK_OAK,
				WHCItems.CRUTCH_JUNGLE,
				WHCItems.CRUTCH_OAK,
				WHCItems.CRUTCH_SPRUCE,
				WHCItems.CRUTCH_CHERRY,
				WHCItems.CRUTCH_MANGROVE,
				WHCItems.CRUTCH_WARPED,
				WHCItems.CRUTCH_CRIMSON,
				WHCItems.CRUTCH_BAMBOO);
	}
	
	private static void addExtraCrutchModel(Item item)
	{
		Identifier itemID = Registries.ITEM.getId(item);
		EXTRA_MODELS.add(new ExtraModelHandler(item, new ModelIdentifier(itemID.getNamespace(), itemID.getPath()+"_in_hand", "inventory"), WHCItemsClient::onPerson));
	}
	
	private static void addExtraCaneModel(Item item)
	{
		Identifier itemID = Registries.ITEM.getId(item);
		EXTRA_MODELS.add(new ExtraModelHandler(item, new ModelIdentifier(itemID.getNamespace(), itemID.getPath()+"_in_gui", "inventory"), WHCItemsClient::inGUI));
	}
	
	private static boolean onPerson(ModelTransformationMode mode)
	{
		return
				mode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND ||
				mode == ModelTransformationMode.THIRD_PERSON_RIGHT_HAND;
	}
	
	private static boolean inGUI(ModelTransformationMode mode)
	{
		return
				mode == ModelTransformationMode.FIXED ||
				mode == ModelTransformationMode.GUI ||
				mode == ModelTransformationMode.GROUND ||
				mode == ModelTransformationMode.NONE;
	}
	
	public static List<ModelIdentifier> getExtraModels()
	{
		List<ModelIdentifier> models = Lists.newArrayList();
			EXTRA_MODELS.forEach(handler -> models.add(handler.model()));
		return models;
	}
	
	@Nullable
	public static ModelIdentifier getExtraModelIfAny(Item item, ModelTransformationMode mode)
	{
		for(ExtraModelHandler handler : EXTRA_MODELS)
			if(handler.shouldApply(item, mode))
				return handler.model();
		return null;
	}
	
	/**
	 * Defines a model to replace the main item model when rendering the item in a specific ModelTransformationMode.<br>
	 * The model is baked in {@link ModelLoaderMixin} and applied in {@link ItemRendererMixin}
	 */
	public static class ExtraModelHandler
	{
		private final Item item;
		private final Predicate<ModelTransformationMode> qualifier;
		private final ModelIdentifier model;
		
		public ExtraModelHandler(Item itemIn, ModelIdentifier modelIn, Predicate<ModelTransformationMode> qualifierIn)
		{
			item = itemIn;
			qualifier = qualifierIn;
			model = modelIn;
		}
		
		public boolean shouldApply(Item itemIn, ModelTransformationMode mode) { return itemIn == item && qualifier.apply(mode); }
		
		public ModelIdentifier model() { return model; }
	}
	
	static
	{
		addExtraCrutchModel(WHCItems.CRUTCH_ACACIA);
		addExtraCrutchModel(WHCItems.CRUTCH_BAMBOO);
		addExtraCrutchModel(WHCItems.CRUTCH_BIRCH);
		addExtraCrutchModel(WHCItems.CRUTCH_CHERRY);
		addExtraCrutchModel(WHCItems.CRUTCH_CRIMSON);
		addExtraCrutchModel(WHCItems.CRUTCH_DARK_OAK);
		addExtraCrutchModel(WHCItems.CRUTCH_JUNGLE);
		addExtraCrutchModel(WHCItems.CRUTCH_MANGROVE);
		addExtraCrutchModel(WHCItems.CRUTCH_OAK);
		addExtraCrutchModel(WHCItems.CRUTCH_SPRUCE);
		addExtraCrutchModel(WHCItems.CRUTCH_WARPED);
		
		addExtraCaneModel(WHCItems.CANE_ACACIA);
		addExtraCaneModel(WHCItems.CANE_BAMBOO);
		addExtraCaneModel(WHCItems.CANE_BIRCH);
		addExtraCaneModel(WHCItems.CANE_CHERRY);
		addExtraCaneModel(WHCItems.CANE_CRIMSON);
		addExtraCaneModel(WHCItems.CANE_DARK_OAK);
		addExtraCaneModel(WHCItems.CANE_JUNGLE);
		addExtraCaneModel(WHCItems.CANE_MANGROVE);
		addExtraCaneModel(WHCItems.CANE_OAK);
		addExtraCaneModel(WHCItems.CANE_SPRUCE);
		addExtraCaneModel(WHCItems.CANE_WARPED);
	}
}
