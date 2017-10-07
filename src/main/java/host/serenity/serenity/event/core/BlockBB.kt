package host.serenity.serenity.event.core

import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.util.AxisAlignedBB

class BlockBB(var block: Block, var blockState: IBlockState, var x: Int, var y: Int, var z: Int, var boundingBox: AxisAlignedBB?)
