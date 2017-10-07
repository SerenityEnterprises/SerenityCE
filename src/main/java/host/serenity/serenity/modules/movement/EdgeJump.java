package host.serenity.serenity.modules.movement;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.event.player.MoveInput;
import host.serenity.serenity.util.BlockHelper;
import host.serenity.synapse.Listener;

public class EdgeJump extends Module {
    private boolean hasJumped;

    public EdgeJump() {
        super("Edge Jump", 0xFFEC8C, ModuleCategory.MOVEMENT);

        listeners.add(new Listener<MoveInput>() {
            @Override
            public void call(MoveInput event) {
                if (!event.getMovementInput().jump)
                    hasJumped = false;

                if (hasJumped) {
                    if (BlockHelper.isOnFloor(-0.5001) && !BlockHelper.isInLiquid()) {
                        event.getMovementInput().jump = false;
                        return;
                    }
                }

                hasJumped = event.getMovementInput().jump;
            }
        });
    }
}
