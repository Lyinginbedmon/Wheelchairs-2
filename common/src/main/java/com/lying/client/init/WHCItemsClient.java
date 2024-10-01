package com.lying.client.init;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.lying.init.WHCItems;
import com.lying.mixin.ItemRendererMixin;
import com.lying.mixin.ModelLoaderMixin;

import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

@SuppressWarnings("unchecked")
public class WHCItemsClient
{
	private static final List<ExtraModelHandler> EXTRA_MODELS = Lists.newArrayList();
	
	private static final Map<ItemColorProvider, Supplier<? extends Item>[]> COLORS = new HashMap<>();
	
	static
	{
		register((stack, tintIndex) -> { return tintIndex == 0 ? ((DyeableItem)stack.getItem()).getColor(stack) : -1; }, WHCItems.STOOL);
		register((stack, tintIndex) -> { return tintIndex == 0 ? ((DyeableItem)stack.getItem()).getColor(stack) : -1; }, WHCItems.VEST);
		register((stack, tintIndex) -> { return tintIndex == 0 ? ((DyeableItem)stack.getItem()).getColor(stack) : -1; }, 
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
		register((stack, tintIndex) -> { return tintIndex > 0 ? ((DyeableItem)stack.getItem()).getColor(stack) : -1; }, 
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
	
	private static void register(ItemColorProvider provider, Supplier<? extends Item>... items)
	{
		COLORS.put(provider, items);
	}
	
	public static void registerItemColors(BiConsumer<ItemColorProvider, Supplier<? extends Item>[]> consumer)
	{
		COLORS.entrySet().forEach(entry -> consumer.accept(entry.getKey(), entry.getValue()));
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
	
	public static List<ModelIdentifier> getExtraModelsToRegister()
	{
		List<ModelIdentifier> models = Lists.newArrayList();
			EXTRA_MODELS.forEach(handler -> { if(handler.needsRegistration()) models.add(handler.model()); });
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
		private final boolean shouldRegister;
		
		public ExtraModelHandler(Item itemIn, ModelIdentifier modelIn, Predicate<ModelTransformationMode> qualifierIn)
		{
			this(itemIn, modelIn, qualifierIn, true);
		}
		
		public ExtraModelHandler(Item itemIn, ModelIdentifier modelIn, Predicate<ModelTransformationMode> qualifierIn, boolean shouldRegisterIn)
		{
			item = itemIn;
			qualifier = qualifierIn;
			model = modelIn;
			shouldRegister = shouldRegisterIn;
		}
		
		public boolean needsRegistration() { return this.shouldRegister; }
		
		public boolean shouldApply(Item itemIn, ModelTransformationMode mode) { return itemIn == item && qualifier.apply(mode); }
		
		public ModelIdentifier model() { return model; }
	}
	
	static
	{
		addExtraCrutchModel(WHCItems.CRUTCH_ACACIA.get());
		addExtraCrutchModel(WHCItems.CRUTCH_BAMBOO.get());
		addExtraCrutchModel(WHCItems.CRUTCH_BIRCH.get());
		addExtraCrutchModel(WHCItems.CRUTCH_CHERRY.get());
		addExtraCrutchModel(WHCItems.CRUTCH_CRIMSON.get());
		addExtraCrutchModel(WHCItems.CRUTCH_DARK_OAK.get());
		addExtraCrutchModel(WHCItems.CRUTCH_JUNGLE.get());
		addExtraCrutchModel(WHCItems.CRUTCH_MANGROVE.get());
		addExtraCrutchModel(WHCItems.CRUTCH_OAK.get());
		addExtraCrutchModel(WHCItems.CRUTCH_SPRUCE.get());
		addExtraCrutchModel(WHCItems.CRUTCH_WARPED.get());
		
		addExtraCaneModel(WHCItems.CANE_ACACIA.get());
		addExtraCaneModel(WHCItems.CANE_BAMBOO.get());
		addExtraCaneModel(WHCItems.CANE_BIRCH.get());
		addExtraCaneModel(WHCItems.CANE_CHERRY.get());
		addExtraCaneModel(WHCItems.CANE_CRIMSON.get());
		addExtraCaneModel(WHCItems.CANE_DARK_OAK.get());
		addExtraCaneModel(WHCItems.CANE_JUNGLE.get());
		addExtraCaneModel(WHCItems.CANE_MANGROVE.get());
		addExtraCaneModel(WHCItems.CANE_OAK.get());
		addExtraCaneModel(WHCItems.CANE_SPRUCE.get());
		addExtraCaneModel(WHCItems.CANE_WARPED.get());
	}
}
