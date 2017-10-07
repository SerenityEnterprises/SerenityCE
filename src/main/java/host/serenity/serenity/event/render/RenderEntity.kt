package host.serenity.serenity.event.render

import net.minecraft.entity.EntityLivingBase

class PreRenderEntity(val entity: EntityLivingBase, val partialTicks: Float)
class PostRenderEntity(val entity: EntityLivingBase, val partialTicks: Float)