package moze_intel.projecte.gameObjs.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import moze_intel.projecte.gameObjs.block_entities.InterdictionTorchBlockEntity;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface InterdictionTorchEntityBlock extends PEEntityBlock<InterdictionTorchBlockEntity> {

	@Nullable
	@Override
	default BlockEntityTypeRegistryObject<InterdictionTorchBlockEntity> getType() {
		return PEBlockEntityTypes.INTERDICTION_TORCH;
	}

	class InterdictionTorch extends TorchBlock implements InterdictionTorchEntityBlock {

		public InterdictionTorch(Properties props) {
			super(props, ParticleTypes.SOUL_FIRE_FLAME);
		}

		@Override
		@Deprecated
		public boolean triggerEvent(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, int id, int param) {
			super.triggerEvent(state, level, pos, id, param);
			return triggerBlockEntityEvent(state, level, pos, id, param);
		}
	}

	class InterdictionTorchWall extends WallTorchBlock implements InterdictionTorchEntityBlock {

		public InterdictionTorchWall(Properties props) {
			super(props, ParticleTypes.SOUL_FIRE_FLAME);
		}

		@Override
		@Deprecated
		public boolean triggerEvent(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, int id, int param) {
			super.triggerEvent(state, level, pos, id, param);
			return triggerBlockEntityEvent(state, level, pos, id, param);
		}
	}
}