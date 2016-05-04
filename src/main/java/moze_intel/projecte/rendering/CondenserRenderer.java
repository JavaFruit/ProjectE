package moze_intel.projecte.rendering;

import moze_intel.projecte.PECore;
import moze_intel.projecte.api.state.PEStateProps;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.tiles.CondenserTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CondenserRenderer extends TileEntitySpecialRenderer
{
	private final ResourceLocation texture = new ResourceLocation(PECore.MODID.toLowerCase(), "textures/blocks/condenser.png");
	private final ModelChest model = new ModelChest();
	
	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float par8, int par9)
	{
		if (!(tile instanceof CondenserTile)) 
		{
			return;
		}
		
		CondenserTile condenser = (CondenserTile) tile;
		EnumFacing direction = null;
		if (condenser.getWorld() != null && !condenser.isInvalid())
		{
			IBlockState state = condenser.getWorld().getBlockState(condenser.getPos());
			direction = state.getBlock() == ObjHandler.condenser ? state.getValue(PEStateProps.FACING) : null;
		}

		this.bindTexture(texture);
		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.translate(x, y + 1.0F, z + 1.0F);
		GlStateManager.scale(1.0F, -1.0F, -1.0F);
		GlStateManager.translate(0.5F, 0.5F, 0.5F);

		short angle = 0;

		if (direction != null)
		{
			switch (direction)
			{
				case NORTH: angle = 180; break;
				case SOUTH: angle = 0; break;
				case WEST: angle = 90; break;
				case EAST: angle = -90; break;
			}
		}

		GlStateManager.rotate(angle, 0.0F, 1.0F, 0.0F);
		GlStateManager.translate(-0.5F, -0.5F, -0.5F);
		float adjustedLidAngle = condenser.prevLidAngle + (condenser.lidAngle - condenser.prevLidAngle) * par8;
		adjustedLidAngle = 1.0F - adjustedLidAngle;
		adjustedLidAngle = 1.0F - adjustedLidAngle * adjustedLidAngle * adjustedLidAngle;
		model.chestLid.rotateAngleX = -(adjustedLidAngle * (float) Math.PI / 2.0F);
		model.renderAll();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}
}
