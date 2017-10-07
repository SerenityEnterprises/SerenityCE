package host.serenity.serenity.event.player

import host.serenity.synapse.util.Cancellable

class SendChat(var message: String) : Cancellable()