package host.serenity.serenity.event.render

import net.minecraft.block.Block
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumWorldBlockLayer

class BlockAmbientLight(var block: Block, var ambientLight: Float)
class BlockLayer(var block: Block, var layer: EnumWorldBlockLayer)
class BlockShouldSideBeRendered(var block: Block, var side: EnumFacing, var shouldSideBeRendered: Boolean)