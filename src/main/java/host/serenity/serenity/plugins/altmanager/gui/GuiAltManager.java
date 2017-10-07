package host.serenity.serenity.plugins.altmanager.gui;


import host.serenity.serenity.plugins.altmanager.AccountManager;
import host.serenity.serenity.plugins.altmanager.SerenityPluginAltManager;
import host.serenity.serenity.plugins.altmanager.account.Account;
import host.serenity.serenity.plugins.altmanager.util.LoginThread;
import host.serenity.serenity.util.RenderUtilities;
import net.minecraft.client.gui.*;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GuiAltManager extends GuiScreen {
    private AccountManager accountManager = SerenityPluginAltManager.getAccountManager();
    private int offset;
    private GuiButton login = null,
            remove = null;

    /* package-private */ Account selected = null;
    private int index = 0;

    private LoginThread loginThread;
    private GuiTextField searchBar;

    public void initGui() {
        ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        Keyboard.enableRepeatEvents(true);
        this.buttonList.add(new GuiButton(0, this.width / 2 + 4 + 50, this.height - 24, 100, 20, "Cancel"));
        this.buttonList.add(login = new GuiButton(1, this.width / 2 - 154, this.height - 48, 100, 20, "Login"));
        this.buttonList.add(remove = new GuiButton(2, this.width / 2 - 50, this.height - 24, 100, 20, "Remove"));
        this.buttonList.add(new GuiButton(5, this.width / 2 + 4 + 50, this.height - 48, 100, 20, "Random"));
        this.buttonList.add(new GuiButton(4, this.width / 2 - 50, this.height - 48, 100, 20, "Direct Login"));
        this.buttonList.add(new GuiButton(3, this.width / 2 - 154, this.height - 24, 100, 20, "Add"));

        // int altyWidth = mc.fontRendererObj.getStringWidth("Alt Dispenser - Alty") + 10;
        // this.buttonList.add(new GuiButton(6, scaledResolution.getScaledWidth() - (altyWidth + 10), 2, altyWidth, 20, "Alt Dispenser - Alty"));

        this.searchBar = new GuiTextField(1, mc.fontRendererObj, this.width / 2 - 100, 14, 200, 16);

        login.enabled = false;
        remove.enabled = false;

        index = 0;
        selected = null;
    }

    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    public void prepareScissorBox(float x, float y, float x2, float y2) {
        final int factor = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight).getScaleFactor();
        GL11.glScissor((int) (x * factor), (int) ((new ScaledResolution(mc, mc.displayWidth, mc.displayHeight).getScaledHeight() - y2) * factor), (int) ((x2 - x) * factor), (int) ((y2 - y) * factor));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        drawCenteredString(mc.fontRendererObj, "Account Manager (" + accountManager.getAccounts().size() + (accountManager.getAccounts().size() == 1 ? " alt.)" : " alts.)"), width / 2, 2, 0xFFFFFF);
        drawString(mc.fontRendererObj, loginThread == null ? EnumChatFormatting.GRAY + "Idle..." : loginThread.status, 10, 2, 0xFFFFFF);
        drawString(mc.fontRendererObj, mc.getSession().getUsername(), 10, 12, 0x888888);
        RenderUtilities.drawBorderedRect(50, 33, width - 50, height - 50, 1, 0xFF000000, 0x80000000);

        List<Account> accounts = new ArrayList<>(accountManager.getAccounts());
        if (!searchBar.getText().isEmpty()) {
            accounts.clear();
            accounts.addAll(accountManager.getAccounts().stream().filter(account -> account.getDisplayName().toLowerCase().contains(searchBar.getText().toLowerCase())).collect(Collectors.toList()));
        }

        prepareScissorBox(0, 33, width, height - 50);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        if (offset > (accounts.size() - 1) * 26) {
            offset = (accounts.size() - 1) * 26;
        }
        int y = 12;
        for (Account account : accounts) {
            y += 26;
            if (!isInArea(offset, y))
                continue;

            if (account == selected) {
                if (isMouseOverAlt(mouseX, mouseY, y - offset) && Mouse.isButtonDown(0)) {
                    RenderUtilities.drawBorderedRect(52, y - offset - 4, width - 52, y - offset + 20, 1, 0xFF000000, 0x80454545);
                } else if (isMouseOverAlt(mouseX, mouseY, y - offset)) {
                    RenderUtilities.drawBorderedRect(52, y - offset - 4, width - 52, y - offset + 20, 1, 0xFF000000, 0x80525252);
                } else {
                    RenderUtilities.drawBorderedRect(52, y - offset - 4, width - 52, y - offset + 20, 1, 0xFF000000, 0x80313131);
                }
            } else if (isMouseOverAlt(mouseX, mouseY, y - offset)) {
                RenderUtilities.drawBorderedRect(52, y - offset - 4, width - 52, y - offset + 20, 1, 0xFF000000, 0x80525252);
            }

            drawCenteredString(mc.fontRendererObj, account.getDisplayName(), width / 2, y - offset, 0xFFFFFF);
            drawCenteredString(mc.fontRendererObj, account.getAuthPassword().isEmpty() ? EnumChatFormatting.RED + "Cracked" : account.getAuthPassword().replaceAll(".", "*"), width / 2, y - offset + 10, 5592405);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        super.drawScreen(mouseX, mouseY, partialTicks);
        if (selected == null) {
            login.enabled = false;
            remove.enabled = false;
        } else {
            login.enabled = true;
            remove.enabled = true;
        }
        searchBar.drawTextBox();

        if (Mouse.hasWheel()) {
            final int wheel = Mouse.getDWheel();
            if (wheel < 0) {
                offset += 26;
                if (offset < 0) {
                    offset = 0;
                }
            } else if (wheel > 0) {
                offset -= 26;
                if (offset < 0) {
                    offset = 0;
                }
            }
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
            offset -= 13;
            if (offset < 0) {
                offset = 0;
            }
        } else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
            offset += 13;
            if (offset < 0) {
                offset = 0;
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                if (loginThread == null || !loginThread.status.contains("Logging in")) {
                    mc.displayGuiScreen(new GuiMainMenu());
                }
                break;
            case 1:
                loginThread = new LoginThread(selected);
                loginThread.start();
                break;
            case 2:
                if (loginThread != null) {
                    loginThread = null;
                }
                accountManager.getAccounts().remove(selected);
                break;
            case 3:
                if (loginThread != null) {
                    loginThread = null;
                }
                mc.displayGuiScreen(new GuiAddAlt(this));
                break;
            case 4:
                if (loginThread != null) {
                    loginThread = null;
                }
                mc.displayGuiScreen(new GuiAltLogin(this));
                break;
            case 5:
                try {
                    Random rand = new Random();
                    Account randomAlt = accountManager.getAccounts().get(rand.nextInt(accountManager.getAccounts().size() - 1));
                    loginThread = new LoginThread(randomAlt);
                    loginThread.start();
                } catch (Exception e) {
                }
                break;
            case 6:
                // mc.displayGuiScreen(new GuiAltyListener(this));
                break;
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        List<Account> accounts = new ArrayList<>(accountManager.getAccounts());
        if (!searchBar.getText().isEmpty()) {
            accounts.clear();
            accounts.addAll(accountManager.getAccounts().stream().filter(account -> account.getDisplayName().toLowerCase().contains(searchBar.getText().toLowerCase())).collect(Collectors.toList()));
        }

        int y = 12 - offset;
        for (Account account : accounts) {
            y += 26;
            if (isMouseOverAlt(mouseX, mouseY, y)) {
                if (account == selected) {
                    actionPerformed((GuiButton) buttonList.get(1));
                    return;
                }
                selected = account;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void keyTyped(char character, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            super.keyTyped(character, keyCode);
            return;
        }
        searchBar.textboxKeyTyped(character, keyCode);
    }

    private boolean isInArea(double offset, int y) {
        return y - offset <= height - 50;
    }

    private boolean isMouseOverAlt(int x, int y, int y1) {
        return x >= 52 && y >= y1 - 4 && x <= width - 52 && y <= y1 + 20 && x >= 0 && y >= 33 && x <= width && y <= height - 50;
    }
}
