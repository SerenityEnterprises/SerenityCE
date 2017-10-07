package host.serenity.serenity.event.network

import host.serenity.synapse.util.Cancellable
import net.minecraft.network.Packet

class ReceivePacket(val packet: Packet) : Cancellable()
class PostReceivePacket(val packet: Packet)
