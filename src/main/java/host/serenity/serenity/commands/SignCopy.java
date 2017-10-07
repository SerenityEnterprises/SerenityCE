package host.serenity.serenity.commands;

import host.serenity.serenity.api.command.Command;
import host.serenity.serenity.api.command.parser.CommandBranch;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.IChatComponent;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class SignCopy extends Command {
    public SignCopy() {
        branches.add(new CommandBranch(ctx -> {
            if (mc.objectMouseOver.getBlockPos() != null) {
                TileEntity tileEntity = mc.theWorld.getTileEntity(mc.objectMouseOver.getBlockPos());
                if (tileEntity != null && tileEntity instanceof TileEntitySign) {
                    TileEntitySign sign = (TileEntitySign) tileEntity;
                    StringBuilder builder = new StringBuilder();
                    for (IChatComponent component : sign.signText) {
                        builder.append(component.getUnformattedText());
                        builder.append(System.getProperty("line.separator"));
                    }

                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(new StringSelection(builder.toString()), null);
                }
            }
        }));
    }
}
