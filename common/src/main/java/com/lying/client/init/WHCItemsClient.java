package com.lying.client.init;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.lying.init.WHCItems;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.Identifier;

public class WHCItemsClient
{
	private static final List<ExtraModelHandler> EXTRA_MODELS = Lists.newArrayList();
	
//	private static final Map<ItemColorProvider, Supplier<? extends Item>[]> COLORS = new HashMap<>();
//	
//	static
//	{
//		register((stack, tintIndex) -> { return tintIndex == 0 ? ((DyeableItem)stack.getItem()).getColor(stack) : -1; }, WHCItems.STOOL);
//		register((stack, tintIndex) -> { return tintIndex == 0 ? ((DyeableItem)stack.getItem()).getColor(stack) : -1; }, WHCItems.VEST);
//		register((stack, tintIndex) -> { return tintIndex == 0 ? ((DyeableItem)stack.getItem()).getColor(stack) : -1; }, 
//				WHCItems.WHEELCHAIR_ACACIA,
//				WHCItems.WHEELCHAIR_BIRCH,
//				WHCItems.WHEELCHAIR_DARK_OAK,
//				WHCItems.WHEELCHAIR_JUNGLE,
//				WHCItems.WHEELCHAIR_OAK,
//				WHCItems.WHEELCHAIR_SPRUCE,
//				WHCItems.WHEELCHAIR_CHERRY,
//				WHCItems.WHEELCHAIR_MANGROVE,
//				WHCItems.WHEELCHAIR_WARPED,
//				WHCItems.WHEELCHAIR_CRIMSON,
//				WHCItems.WHEELCHAIR_BAMBOO);
//		register((stack, tintIndex) -> { return tintIndex > 0 ? ((DyeableItem)stack.getItem()).getColor(stack) : -1; }, 
//				WHCItems.CRUTCH_ACACIA,
//				WHCItems.CRUTCH_BIRCH,
//				WHCItems.CRUTCH_DARK_OAK,
//				WHCItems.CRUTCH_JUNGLE,
//				WHCItems.CRUTCH_OAK,
//				WHCItems.CRUTCH_SPRUCE,
//				WHCItems.CRUTCH_CHERRY,
//				WHCItems.CRUTCH_MANGROVE,
//				WHCItems.CRUTCH_WARPED,
//				WHCItems.CRUTCH_CRIMSON,
//				WHCItems.CRUTCH_BAMBOO);
//	}
//	
//	private static void register(ItemColorProvider provider, Supplier<? extends Item>... items)
//	{
//		COLORS.put(provider, items);
//	}
//	
//	public static void registerItemColors(BiConsumer<ItemColorProvider, Supplier<? extends Item>[]> consumer)
//	{
//		COLORS.entrySet().forEach(entry -> consumer.accept(entry.getKey(), entry.getValue()));
//	}
	
	private static void addExtraCrutchModel(RegistrySupplier<Item> item)
	{
		EXTRA_MODELS.add(new ExtraModelHandler(item, "_in_hand", WHCItemsClient::onPerson));
	}
	
	private static void addExtraCaneModel(RegistrySupplier<Item> item)
	{
		EXTRA_MODELS.add(new ExtraModelHandler(item, "_in_gui", WHCItemsClient::inGUI));
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
		return EXTRA_MODELS.stream().filter(ExtraModelHandler::needsRegistration).map(ExtraModelHandler::model).toList();
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
		private final RegistrySupplier<Item> item;
		private final Predicate<ModelTransformationMode> qualifier;
		private final String suffix; 
		private Optional<ModelIdentifier> model = Optional.empty();
		private final boolean shouldRegister;
		
		public ExtraModelHandler(RegistrySupplier<Item> itemIn, String suffixIn, Predicate<ModelTransformationMode> qualifierIn)
		{
			this(itemIn, suffixIn, qualifierIn, true);
		}
		
		public ExtraModelHandler(RegistrySupplier<Item> itemIn, String suffixIn, Predicate<ModelTransformationMode> qualifierIn, boolean shouldRegisterIn)
		{
			item = itemIn;
			qualifier = qualifierIn;
			suffix = suffixIn;
			shouldRegister = shouldRegisterIn;
		}
		
		public boolean needsRegistration() { return this.shouldRegister; }
		
		public boolean shouldApply(Item itemIn, ModelTransformationMode mode) { return itemIn == item.get() && qualifier.apply(mode); }
		
		public ModelIdentifier model()
		{
			if(model.isPresent())
				return model.get();
			
			Identifier itemID = item.getId();
			ModelIdentifier id = new ModelIdentifier(Identifier.of(itemID.getNamespace(), itemID.getPath()+suffix), "inventory");
			model = Optional.of(id);
			return id;
		}
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
