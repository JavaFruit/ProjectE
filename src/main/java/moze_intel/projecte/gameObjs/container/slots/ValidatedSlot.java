package moze_intel.projecte.gameObjs.container.slots;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

// Partial copy of SlotItemHandler with a validator
public class ValidatedSlot extends InventoryContainerSlot {

	private final Predicate<ItemStack> validator;

	public ValidatedSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, Predicate<ItemStack> validator) {
		super(itemHandler, index, xPosition, yPosition);
		this.validator = validator;
	}

	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) {
		return super.mayPlace(stack) && validator.test(stack);
	}
}