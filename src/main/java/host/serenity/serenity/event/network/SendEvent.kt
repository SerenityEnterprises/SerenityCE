package host.serenity.serenity.event.network

import host.serenity.synapse.util.Cancellable
import net.minecraft.network.Packet

class SendPacket(var packet: Packet) : Cancellable()
class PostSendPacket(val packet: Packet)

class PushPacketToNetwork(val packet: Packet) : Cancellable()
