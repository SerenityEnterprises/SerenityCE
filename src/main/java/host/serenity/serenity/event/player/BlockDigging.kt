package host.serenity.serenity.event.player

import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

class BlockDigging(val pos: BlockPos, val facing: EnumFacing, var hitDelay: Int)
class PostBlockDigging(val pos: BlockPos, val facing: EnumFacing)