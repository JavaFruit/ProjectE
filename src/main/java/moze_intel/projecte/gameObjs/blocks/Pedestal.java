package moze_intel.projecte.gameObjs.blocks;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.EnumMatterType;
import moze_intel.projecte.gameObjs.block_entities.DMPedestalBlockEntity;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Pedestal extends Block implements SimpleWaterloggedBlock, PEEntityBlock<DMPedestalBlockEntity>, IMatterBlock {

	private static final VoxelShape SHAPE = Shapes.or(
			Block.box(3, 0, 3, 13, 2, 13),
			Shapes.or(
					Block.box(6, 2, 6, 10, 9, 10),
					Block.box(5, 9, 5, 11, 10, 11)
			)
	);

	public Pedestal(Properties props) {
		super(props);
		this.registerDefaultState(getStateDefinition().any().setValue(BlockStateProperties.WATERLOGGED, false));
	}

	@Override
	protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> props) {
		super.createBlockStateDefinition(props);
		props.add(BlockStateProperties.WATERLOGGED);
	}

	@Override
	@Deprecated
	public boolean isPathfindable(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull PathComputationType type) {
		return false;
	}

	@Nonnull
	@Override
	@Deprecated
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext ctx) {
		return SHAPE;
	}

	/**
	 * @return True if there was an item and it got dropped, false otherwise.
	 */
	private boolean dropItem(Level level, BlockPos pos) {
		DMPedestalBlockEntity pedestal = WorldHelper.getBlockEntity(DMPedestalBlockEntity.class, level, pos);
		if (pedestal != null) {
			ItemStack stack = pedestal.getInventory().getStackInSlot(0);
			if (!stack.isEmpty()) {
				pedestal.getInventory().setStackInSlot(0, ItemStack.EMPTY);
				level.addFreshEntity(new ItemEntity(level, pos.getX(), pos.getY() + 0.8, pos.getZ(), stack));
				return true;
			}
		}
		return false;
	}

	@Override
	@Deprecated
	public void onRemove(BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			dropItem(level, pos);
			super.onRemove(state, level, pos, newState, isMoving);
		}
	}

	@Override
	@Deprecated
	public void attack(@Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull Player player) {
		if (!level.isClientSide) {
			dropItem(level, pos);
		}
	}

	@Override
	public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
		if (player.isCreative() && dropItem(level, pos)) {
			//If the player is creative, try to drop the item, and if we succeeded return false to cancel removing the pedestal
			// Note: we notify the block of an update to make sure that it re-appears visually on the client instead of having there
			// be a desync
			level.sendBlockUpdated(pos, state, state, Block.UPDATE_IMMEDIATE);
			return false;
		}
		return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
	}

	@Nonnull
	@Override
	@Deprecated
	public InteractionResult use(@Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand,
			@Nonnull BlockHitResult rtr) {
		if (!level.isClientSide) {
			DMPedestalBlockEntity pedestal = WorldHelper.getBlockEntity(DMPedestalBlockEntity.class, level, pos, true);
			if (pedestal == null) {
				return InteractionResult.FAIL;
			}
			ItemStack item = pedestal.getInventory().getStackInSlot(0);
			ItemStack stack = player.getItemInHand(hand);
			if (stack.isEmpty() && !item.isEmpty()) {
				item.getCapability(PECapabilities.PEDESTAL_ITEM_CAPABILITY).ifPresent(pedestalItem -> {
					pedestal.setActive(!pedestal.getActive());
					level.sendBlockUpdated(pos, state, state, Block.UPDATE_IMMEDIATE);
				});
			} else if (!stack.isEmpty() && item.isEmpty()) {
				pedestal.getInventory().setStackInSlot(0, stack.split(1));
				if (stack.getCount() <= 0) {
					player.setItemInHand(hand, ItemStack.EMPTY);
				}
			}
		}
		return InteractionResult.SUCCESS;
	}

	// [VanillaCopy] Adapted from NoteBlock
	@Override
	@Deprecated
	public void neighborChanged(@Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull Block neighbor, @Nonnull BlockPos neighborPos, boolean isMoving) {
		boolean hasSignal = level.hasNeighborSignal(pos);
		DMPedestalBlockEntity ped = WorldHelper.getBlockEntity(DMPedestalBlockEntity.class, level, pos);
		if (ped != null && ped.previousRedstoneState != hasSignal) {
			if (hasSignal) {
				ItemStack stack = ped.getInventory().getStackInSlot(0);
				if (!stack.isEmpty() && stack.getCapability(PECapabilities.PEDESTAL_ITEM_CAPABILITY).isPresent()) {
					ped.setActive(!ped.getActive());
					level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL_IMMEDIATE);
				}
			}
			ped.previousRedstoneState = hasSignal;
			ped.markDirty(false);
		}
	}

	@Override
	@Deprecated
	public boolean hasAnalogOutputSignal(@Nonnull BlockState state) {
		return true;
	}

	@Override
	@Deprecated
	public int getAnalogOutputSignal(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos) {
		DMPedestalBlockEntity pedestal = WorldHelper.getBlockEntity(DMPedestalBlockEntity.class, level, pos);
		if (pedestal != null) {
			ItemStack stack = pedestal.getInventory().getStackInSlot(0);
			if (!stack.isEmpty()) {
				if (stack.getCapability(PECapabilities.PEDESTAL_ITEM_CAPABILITY).isPresent()) {
					return pedestal.getActive() ? 15 : 10;
				}
				return 5;
			}
		}
		return 0;
	}

	@Nullable
	@Override
	public BlockEntityTypeRegistryObject<DMPedestalBlockEntity> getType() {
		return PEBlockEntityTypes.DARK_MATTER_PEDESTAL;
	}

	@Override
	@Deprecated
	public boolean triggerEvent(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);
		return triggerBlockEntityEvent(state, level, pos, id, param);
	}

	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable BlockGetter level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flags) {
		super.appendHoverText(stack, level, tooltip, flags);
		tooltip.add(PELang.PEDESTAL_TOOLTIP1.translate());
		tooltip.add(PELang.PEDESTAL_TOOLTIP2.translate());
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) {
		BlockState state = super.getStateForPlacement(context);
		return state == null ? null : state.setValue(BlockStateProperties.WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
	}

	@Nonnull
	@Override
	@Deprecated
	public FluidState getFluidState(BlockState state) {
		return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Nonnull
	@Override
	@Deprecated
	public BlockState updateShape(@Nonnull BlockState state, @Nonnull Direction facing, @Nonnull BlockState facingState, @Nonnull LevelAccessor level,
			@Nonnull BlockPos currentPos, @Nonnull BlockPos facingPos) {
		if (state.getValue(BlockStateProperties.WATERLOGGED)) {
			level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}
		return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
	}

	@Override
	public EnumMatterType getMatterType() {
		return EnumMatterType.DARK_MATTER;
	}
}