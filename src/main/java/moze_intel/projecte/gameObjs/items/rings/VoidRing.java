package moze_intel.projecte.gameObjs.items.rings;

import java.util.List;
import javax.annotation.Nonnull;
import moze_intel.projecte.api.capabilities.item.IAlchBagItem;
import moze_intel.projecte.api.capabilities.item.IAlchChestItem;
import moze_intel.projecte.api.capabilities.item.IExtraFunction;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.api.block_entity.IDMPedestal;
import moze_intel.projecte.capability.ExtraFunctionItemCapabilityWrapper;
import moze_intel.projecte.capability.PedestalItemCapabilityWrapper;
import moze_intel.projecte.gameObjs.items.GemEternalDensity;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.PlayerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.items.IItemHandler;

public class VoidRing extends GemEternalDensity implements IPedestalItem, IExtraFunction {

	public VoidRing(Properties props) {
		super(props);
		addItemCapability(PedestalItemCapabilityWrapper::new);
		addItemCapability(ExtraFunctionItemCapabilityWrapper::new);
	}

	@Override
	public void inventoryTick(@Nonnull ItemStack stack, Level level, @Nonnull Entity entity, int slot, boolean isHeld) {
		super.inventoryTick(stack, level, entity, slot, isHeld);
		PEItems.BLACK_HOLE_BAND.get().inventoryTick(stack, level, entity, slot, isHeld);
	}

	@Override
	public <PEDESTAL extends BlockEntity & IDMPedestal> boolean updateInPedestal(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull BlockPos pos,
			@Nonnull PEDESTAL pedestal) {
		return ((IPedestalItem) PEItems.BLACK_HOLE_BAND.get()).updateInPedestal(stack, level, pos, pedestal);
	}

	@Nonnull
	@Override
	public List<Component> getPedestalDescription() {
		return ((IPedestalItem) PEItems.BLACK_HOLE_BAND.get()).getPedestalDescription();
	}

	@Override
	public boolean doExtraFunction(@Nonnull ItemStack stack, @Nonnull Player player, InteractionHand hand) {
		if (player.getCooldowns().isOnCooldown(this)) {
			return false;
		}
		BlockHitResult lookingAt = PlayerHelper.getBlockLookingAt(player, 64);
		BlockPos c;
		if (lookingAt.getType() == Type.MISS) {
			c = new BlockPos(PlayerHelper.getLookVec(player, 32).getRight());
		} else {
			c = lookingAt.getBlockPos();
		}
		EntityTeleportEvent event = new EntityTeleportEvent(player, c.getX(), c.getY(), c.getZ());
		if (!MinecraftForge.EVENT_BUS.post(event)) {
			if (player.isPassenger()) {
				player.stopRiding();
			}
			player.teleportTo(event.getTargetX(), event.getTargetY(), event.getTargetZ());
			player.getCommandSenderWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1, 1);
			player.fallDistance = 0.0F;
			player.getCooldowns().addCooldown(this, 10);
			return true;
		}
		return false;
	}

	@Override
	public boolean updateInAlchBag(@Nonnull IItemHandler inv, @Nonnull Player player, @Nonnull ItemStack stack) {
		// super is Gem of Eternal Density
		return super.updateInAlchBag(inv, player, stack) | ((IAlchBagItem) PEItems.BLACK_HOLE_BAND.get()).updateInAlchBag(inv, player, stack);
	}

	@Override
	public boolean updateInAlchChest(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull ItemStack stack) {
		// super is Gem of Eternal Density
		return super.updateInAlchChest(level, pos, stack) | ((IAlchChestItem) PEItems.BLACK_HOLE_BAND.get()).updateInAlchChest(level, pos, stack);
	}
}