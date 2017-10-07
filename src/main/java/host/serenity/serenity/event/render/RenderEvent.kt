package host.serenity.serenity.event.render

import host.serenity.synapse.util.Cancellable
import net.minecraft.client.gui.GuiScreen

class RenderOverlay(val renderPartialTicks: Float)
class RenderWorld(val renderPartialTicks: Float)
class RenderWorldBobbing(val renderPartialTicks: Float)
class RenderEverything(val renderPartialTicks: Float)

class RenderGuiScreen(val guiScreen: GuiScreen) : Cancellable()

class RenderHand : Cancellable()