package moze_intel.projecte.impl;

import com.google.common.collect.ImmutableMap;
import moze_intel.projecte.api.proxy.IConversionProxy;
import moze_intel.projecte.emc.IngredientMap;
import moze_intel.projecte.emc.json.NSSFake;
import moze_intel.projecte.emc.json.NSSFluid;
import moze_intel.projecte.emc.json.NSSItem;
import moze_intel.projecte.emc.json.NSSTag;
import moze_intel.projecte.emc.json.NormalizedSimpleStack;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.fml.javafmlmod.FMLModLoadingContext;
import org.apache.commons.lang3.ClassUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConversionProxyImpl implements IConversionProxy
{
	public static final ConversionProxyImpl instance = new ConversionProxyImpl();

	private final Map<Object, NormalizedSimpleStack> fakes = new ConcurrentHashMap<>();

	@Override
	public void addConversion(int amount, @Nonnull Object output, @Nonnull Map<Object, Integer> ingredients) {
		NormalizedSimpleStack nssOut = objectToNSS(output);
		IngredientMap<NormalizedSimpleStack> ingredientMap = new IngredientMap<>();
		for (Map.Entry<Object, Integer> entry: ingredients.entrySet()) {
			NormalizedSimpleStack nss = objectToNSS(entry.getKey());
			ingredientMap.addIngredient(nss, entry.getValue());
		}
		String modId = getActiveMod();
		List<APIConversion> conversionsFromMod = storedConversions.computeIfAbsent(modId, s -> Collections.synchronizedList(new ArrayList<>()));
		conversionsFromMod.add(new APIConversion(amount, nssOut, ImmutableMap.copyOf(ingredientMap.getMap())));
	}

	public final Map<String, List<APIConversion>> storedConversions = new ConcurrentHashMap<>();

	public NormalizedSimpleStack objectToNSS(Object object)
	{
		if (object instanceof Block) {
			return objectToNSS(new ItemStack((Block) object));
		} else if (object instanceof Item) {
			return objectToNSS(new ItemStack((Item)object));
		}

		if (object instanceof ItemStack) {
			return new NSSItem((ItemStack) object);
		} else if (object instanceof FluidStack) {
			return NSSFluid.create(((FluidStack) object).getFluid());
		} else if (object instanceof ResourceLocation) {
			return NSSTag.create(object.toString());
		} else if (object != null && object.getClass().equals(Object.class)) {
			return fakes.computeIfAbsent(object, o -> NSSFake.create("" + object + " by " + getActiveMod()));
		} else {
			throw new IllegalArgumentException("Can not turn " + object + " (" + ClassUtils.getPackageCanonicalName(object, "") + ") into NormalizedSimpleStack. need ItemStack, FluidStack, String or 'Object'");
		}
	}

	private String getActiveMod() {
		FMLModContainer activeMod = FMLModLoadingContext.get().getActiveContainer();
		return activeMod == null ? "unknown Mod" : activeMod.getModId();
	}

	public static class APIConversion
	{
		public final int amount;
		public final NormalizedSimpleStack output;
		public final ImmutableMap<NormalizedSimpleStack, Integer> ingredients;

		private APIConversion(int amount, NormalizedSimpleStack output, ImmutableMap<NormalizedSimpleStack, Integer> ingredients)
		{
			this.amount = amount;
			this.output = output;
			this.ingredients = ingredients;
		}
	}
}