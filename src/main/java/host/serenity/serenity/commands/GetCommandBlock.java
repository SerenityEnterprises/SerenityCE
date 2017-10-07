package host.serenity.serenity.commands;

import host.serenity.serenity.api.command.Command;
import host.serenity.serenity.api.command.parser.CommandBranch;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;

public class GetCommandBlock extends Command {
    public GetCommandBlock() {
        branches.add(new CommandBranch(ctx -> {
            if (mc.objectMouseOver.getBlockPos() != null) {
                TileEntity tileEntity = mc.theWorld.getTileEntity(mc.objectMouseOver.getBlockPos());
                if (tileEntity != null && tileEntity instanceof TileEntityCommandBlock) {
                    out(((TileEntityCommandBlock) tileEntity).getCommandBlockLogic().getCommand());
                }
            }
        }));
    }
}
