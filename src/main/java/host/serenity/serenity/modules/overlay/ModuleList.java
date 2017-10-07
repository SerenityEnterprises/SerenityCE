package host.serenity.serenity.modules.overlay;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.help.ModuleDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.util.overlay.OverlayArea;
import host.serenity.serenity.util.overlay.OverlayContextManager;

import java.util.ArrayList;
import java.util.List;

@ModuleDescription("Shows the list of enabled modules at the top right of the screen.")
public class ModuleList extends Module {
    public static boolean listDirty = false;
    private int[] indices;

    public ModuleList() {
        super("Module List", 0x80FFB8, ModuleCategory.OVERLAY);
        setHidden(true);

        listDirty = true;
        redoList();

        OverlayContextManager.INSTANCE.register(OverlayArea.TOP_RIGHT, 1000, ctx -> {
            if (listDirty) {
                redoList();
                listDirty = false;
            }

            for (int idx : indices) {
                Module module = Serenity.getInstance().getModuleManager().getModules().get(idx);
                if (module.isHidden() || !module.isEnabled())
                    continue;

                ctx.drawString(module.getDisplay(), module.getColour(), true);
            }
        }, this::isEnabled, () -> !mc.gameSettings.showDebugInfo);

        setState(true);
    }

    private void redoList() {
        int size = Serenity.getInstance().getModuleManager().getModules().size();
        indices = new int[size];

        List<Module> copiedModuleList = new ArrayList<>(Serenity.getInstance().getModuleManager().getModules());
        copiedModuleList.sort((m1, m2) -> {
            int w1 = mc.fontRendererObj.getStringWidth(m1.getDisplay());
            int w2 = mc.fontRendererObj.getStringWidth(m2.getDisplay());

            return Integer.compare(w2, w1); // DESCENDING order
        });

        for (int i = 0; i < copiedModuleList.size(); i++) {
            Module module = copiedModuleList.get(i);
            int originalIndex = Serenity.getInstance().getModuleManager().getModules().indexOf(module);

            indices[i] = originalIndex;
        }
    }
}
