package moze_intel.projecte.api.proxy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.Map;

public interface IConversionProxy
{
	/**
	 * Add a Conversion to the EMC Calculation.
	 *
	 * Adding a Conversion allows ProjectE to calculate the EMC value for the output based on the specified ingredients.
	 * These do not need to be actually Conversions. You can use it to make the EMC value of an item relative to the EMC value of other items.
	 * ProjectE will automatically select the Conversion with the lowest EMC value.
	 *
	 * Call this during any of the main loading phases
	 * This method can be called during parallel mod loading.
	 *
	 * You can use the following things for the {@code output}-Parameter and the keys in the {@code ingredients} Map:
	 * <ul>
	 *     <li>{@link ItemStack} - The ItemId will be used to identify this ItemStack (May contain a {@code Block} or {@code Item}).</li>
	 *     <li>{@link Block} - Same as calling it with {@code new ItemStack(block)}.</li>
	 *     <li>{@link Item} - Same as calling it with {@code new ItemStack(item)}.</li>
	 *     <li>{@link FluidStack} - {@link FluidStack#getFluid()} and {@link Fluid#getName()} will be used to identify this Fluid.</li>
	 *     <li>{@link net.minecraft.util.ResourceLocation} - will be interpreted as an Item Tag ID.</li>
	 *     <li>{@link Object} - (No subclasses of {@code Object} - only {@code Object}!) can be used as a intermediate fake object for complex conversion.</li>
	 * </ul>
	 * All {@code Object}s will be assumed to be a single instance. No stacksize will be used.
	 *
	 * Use the {@code amount} parameter to specify how many {@code output}s are created.
	 * Use the value in the {@code ingredients}-Map to specify how much of an ingredient is required.
	 * (Use Millibuckets for Fluids)
	 *
	 * Examples:
	 *
	 * <pre>{@code
	 * //Furnace Crafting Recipe:
	 * addConversion(1, Blocks.FURNACE, ImmutableMap.of((Object)Blocks.COBBLESTONE, 8));
	 * //alternatively:
	 * addConversion(1, Blocks.FURNACE, ImmutableMap.<Object, Integer>of(Blocks.COBBLESTONE, 8));
	 *
	 * //Bed Crafting Recipe with OreDictionary Names:
	 * //3 "plankWood" and 3 "blockWool" turn into 1 Blocks.BED
	 * addConversion(1, Blocks.BED, ImmutableMap.<Object, Integer>of(new ResourceLocation("minecraft:planks"), 3, new ResourceLocation("minecraft:wool), 3));
	 *
	 * //For Recipes that have multiple possible Ingredients, that don't belong to a known OreDict entry you can use a fake-item Object:
	 * Object blackOrWhite = new Object();
	 * //1 White Wool can be turned into 1 'blackOrWhite'
	 * addConversion(1, blackOrWhite, ImmutableMap.of((Object)new ItemStack(Blocks.WHITE_WOOL), 1));
	 * //1 Black Wool can be turned into 1 'blackOrWhite'
	 * addConversion(1, blackOrWhite, ImmutableMap.of((Object)new ItemStack(Blocks.BLACK_WOOL), 1));
	 * //Bed created with black or white wool only
	 * addConversion(1, Blocks.BED, ImmutableMap.of(blackOrWhite, 3, "plankWood", 3));
	 * }
	 * </pre>
	 *
	 * @param amount
	 * @param output
	 * @param ingredients
	 */
	void addConversion(int amount, @Nonnull Object output, @Nonnull Map<Object, Integer> ingredients);
}