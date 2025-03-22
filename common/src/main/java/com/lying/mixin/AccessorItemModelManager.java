package com.lying.mixin;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.util.Identifier;

@Mixin(ItemModelManager.class)
public interface AccessorItemModelManager
{
	@Accessor("modelGetter")
	public Function<Identifier, ItemModel> modelGetter();
}
