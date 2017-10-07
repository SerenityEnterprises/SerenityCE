package host.serenity.serenity.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.regex.Pattern;

public class NahrFont {
    public enum FontType {
        EMBOSS_BOTTOM, EMBOSS_TOP, NORMAL, OUTLINE_THIN, SHADOW_THICK, SHADOW_THIN
    }

    private BufferedImage bufferedImage;
    private DynamicTexture dynamicTexture;
    private final int endChar;
    private float extraSpacing;
    private final float fontSize;
    private final Pattern patternControlCode;
    private final Pattern patternUnsupported;
    private ResourceLocation resourceLocation;
    private final int startChar;
    private Font theFont;
    private Graphics2D theGraphics;

    private FontMetrics theMetrics;
    private final float[] xPos;
    private final float[] yPos;
    private final int[] colours = new int[32]; {
        for(int i = 0; i < 32; ++i) {
            int var6 = (i >> 3 & 1) * 85;
            int var7 = (i >> 2 & 1) * 170 + var6;
            int var8 = (i >> 1 & 1) * 170 + var6;
            int var9 = (i >> 0 & 1) * 170 + var6;
            if(i == 6) {
                var7 += 85;
            }

            if(i >= 16) {
                var7 /= 4;
                var8 /= 4;
                var9 /= 4;
            }

            this.colours[i] = (var7 & 255) << 16 | (var8 & 255) << 8 | var9 & 255;
        }
    }

    public NahrFont(final Object font, final float size) {
        this(font, size, 0.0f);
    }

    public NahrFont(final Object font, final float size, final float spacing) {
        this.extraSpacing = 0.0f;
        this.patternControlCode = Pattern.compile("(?i)\\u00A7[0-9A-FK-OG]");
        this.patternUnsupported = Pattern.compile("(?i)\\u00A7[K-O]");
        this.fontSize = size;
        this.startChar = 32;
        this.endChar = 255;
        this.extraSpacing = spacing;
        this.xPos = new float[this.endChar - this.startChar];
        this.yPos = new float[this.endChar - this.startChar];
        this.setupGraphics2D();
        this.createFont(font, size);
    }

    private void createFont(final Object font, final float size) {
        try {
            if (font instanceof Font) {
                this.theFont = (Font)font;
            }
            else if (font instanceof File) {
                this.theFont = Font.createFont(0, (File) font).deriveFont(size);
            }
            else if (font instanceof InputStream) {
                this.theFont = Font.createFont(0, (InputStream)font).deriveFont(size);
                ((InputStream) font).close();
            }
            else if (font instanceof String) {
                this.theFont = new Font((String)font, 0, Math.round(size));
            }
            else {
                this.theFont = new Font("Verdana", 0, Math.round(size));
            }
            this.theGraphics.setFont(this.theFont);
        }
        catch (Exception e) {
            e.printStackTrace();
            this.theFont = new Font("Verdana", 0, Math.round(size));
            this.theGraphics.setFont(this.theFont);
        }
        this.theGraphics.setColor(new Color(255, 255, 255, 0));
        this.theGraphics.fillRect(0, 0, 256, 256);
        this.theGraphics.setColor(Color.white);
        this.theMetrics = this.theGraphics.getFontMetrics();
        float x = 5.0f;
        float y = 5.0f;
        for (int i = this.startChar; i < this.endChar; ++i) {
            this.theGraphics.drawString(Character.toString((char)i), x, y + this.theMetrics.getAscent());
            this.xPos[i - this.startChar] = x;
            this.yPos[i - this.startChar] = y - this.theMetrics.getMaxDescent();
            x += this.theMetrics.stringWidth(Character.toString((char)i)) + 2.0f;
            if (x >= 250 - this.theMetrics.getMaxAdvance()) {
                x = 5.0f;
                y += this.theMetrics.getMaxAscent() + this.theMetrics.getMaxDescent() + this.fontSize / 2.0f;
            }
        }
        final TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        final String string = "font" + font.toString() + size;
        final DynamicTexture dynamicTexture = new DynamicTexture(this.bufferedImage);
        this.dynamicTexture = dynamicTexture;
        this.resourceLocation = textureManager.getDynamicTextureLocation(string, dynamicTexture);
    }

    private void drawChar(final char character, final float x, final float y) throws ArrayIndexOutOfBoundsException {
        final Rectangle2D bounds = this.theMetrics.getStringBounds(Character.toString(character), this.theGraphics);
        this.drawTexturedModalRect(x, y, this.xPos[character - this.startChar], this.yPos[character - this.startChar], (float)bounds.getWidth(), (float)bounds.getHeight() + this.theMetrics.getMaxDescent() + 1.0f);
    }

    private void drawer(final String text, float x, float y, final int color) {
        x *= 2.0f;
        y *= 2.0f;
        GL11.glEnable(3553);
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.resourceLocation);
        final float alpha = (color >> 24 & 0xFF) / 255.0f;
        final float red = (color >> 16 & 0xFF) / 255.0f;
        final float green = (color >> 8 & 0xFF) / 255.0f;
        final float blue = (color & 0xFF) / 255.0f;
        GL11.glColor4f(red, green, blue, alpha);
        final float startX = x;
        for (int i = 0; i < text.length(); ++i) {
            if (text.charAt(i) == '�' && i + 1 < text.length()) {
                final char oneMore = Character.toLowerCase(text.charAt(i + 1));
                if (oneMore == 'n') {
                    y += this.theMetrics.getAscent() + 2;
                    x = startX;
                }
                final int colorCode = "0123456789abcdefklmnorg".indexOf(oneMore);
                if (colorCode < 16) {
                    try {
                        final int newColor = colours[colorCode];
                        GL11.glColor4f((newColor >> 16) / 255.0f, (newColor >> 8 & 0xFF) / 255.0f, (newColor & 0xFF) / 255.0f, alpha);
                    }
                    catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
                else if (oneMore == 'f') {
                    GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
                }
                else if (oneMore == 'r') {
                    GL11.glColor4f(red, green, blue, alpha);
                }
                else if (oneMore == 'g') {
                    GL11.glColor4f(0.3f, 0.7f, 1.0f, alpha);
                }
                ++i;
            }
            else {
                try {
                    final char c = text.charAt(i);
                    this.drawChar(c, x, y);
                    x += this.getStringWidth(Character.toString(c)) * 2.0f;
                }
                catch (ArrayIndexOutOfBoundsException indexException) {
                    text.charAt(i);
                }
            }
        }
    }

    public void drawString(String text, final float x, final float y) {
        drawString(text, x, y, FontType.NORMAL, 0xFFFFFFFF, 0x00000000);
    }

    public void drawString(String text, final float x, final float y, final FontType fontType, final int textColor, final int shadowColor) {
        text = this.stripUnsupported(text);
        GL11.glEnable(3042);
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        final String text2 = this.stripControlCodes(text);
        switch (fontType.ordinal()) {
            case 4: {
                this.drawer(text2, x + 1.0f, y + 1.0f, textColor);
                break;
            }
            case 5: {
                this.drawer(text2, x + 0.5f, y + 0.5f, textColor);
                break;
            }
            case 3: {
                this.drawer(text2, x + 0.5f, y, textColor);
                this.drawer(text2, x - 0.5f, y, textColor);
                this.drawer(text2, x, y + 0.5f, textColor);
                this.drawer(text2, x, y - 0.5f, textColor);
                break;
            }
            case 2: {
                this.drawer(text2, x, y + 0.5f, textColor);
                break;
            }
            case 1: {
                this.drawer(text2, x, y - 0.5f, textColor);
                break;
            }
        }
        this.drawer(text, x, y, shadowColor);
        GL11.glScalef(2.0f, 2.0f, 2.0f);
    }

    // jesus fuck i'm sorry
    public void drawTexturedModalRect(float x, float y, float textureX, float textureY, float width, float height) {
        float var7 = 0.00390625F;
        float var8 = 0.00390625F;
        Tessellator var9 = Tessellator.getInstance();
        WorldRenderer var10 = var9.getWorldRenderer();
        var10.startDrawingQuads();
        var10.addVertexWithUV((double) (x + 0), (double) (y + height), (double) 0, (double) ((float) (textureX + 0) * var7), (double) ((float) (textureY + height) * var8));
        var10.addVertexWithUV((double) (x + width), (double) (y + height), (double) 0, (double) ((float) (textureX + width) * var7), (double) ((float) (textureY + height) * var8));
        var10.addVertexWithUV((double) (x + width), (double) (y + 0), (double) 0, (double) ((float) (textureX + width) * var7), (double) ((float) (textureY + 0) * var8));
        var10.addVertexWithUV((double) (x + 0), (double) (y + 0), (double) 0, (double) ((float) (textureX + 0) * var7), (double) ((float) (textureY + 0) * var8));
        var9.draw();
    }

    private Rectangle2D getBounds(final String text) {
        return this.theMetrics.getStringBounds(text, this.theGraphics);
    }

    public Font getFont() {
        return this.theFont;
    }

    private String getFormatFromString(final String par0Str) {
        String var1 = "";
        int var2 = -1;
        final int var3 = par0Str.length();
        while ((var2 = par0Str.indexOf(167, var2 + 1)) != -1) {
            if (var2 < var3 - 1) {
                final char var4 = par0Str.charAt(var2 + 1);
                if (this.isFormatColor(var4)) {
                    var1 = "�" + var4;
                }
                else {
                    if (!this.isFormatSpecial(var4)) {
                        continue;
                    }
                    var1 = String.valueOf(var1) + "�" + var4;
                }
            }
        }
        return var1;
    }

    public Graphics2D getGraphics() {
        return this.theGraphics;
    }

    public float getStringHeight(final String text) {
        return (float)this.getBounds(text).getHeight() / 2.0f;
    }

    public float getStringWidth(final String text) {
        return (float)(this.getBounds(text).getWidth() + this.extraSpacing) / 2.0f;
    }

    private boolean isFormatColor(final char par0) {
        return (par0 >= '0' && par0 <= '9') || (par0 >= 'a' && par0 <= 'f') || (par0 >= 'A' && par0 <= 'F');
    }

    private boolean isFormatSpecial(final char par0) {
        return (par0 >= 'k' && par0 <= 'o') || (par0 >= 'K' && par0 <= 'O') || par0 == 'r' || par0 == 'R';
    }

    /*public List listFormattedStringToWidth(final String s, final int width) {
        return Arrays.asList(this.wrapFormattedStringToWidth(s, width).split("\n"));
    }*/

    private void setupGraphics2D() {
        this.bufferedImage = new BufferedImage(256, 256, 2);
        (this.theGraphics = (Graphics2D)this.bufferedImage.getGraphics()).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    private int sizeStringToWidth(final String par1Str, final float par2) {
        int var3 = par1Str.length();
        float var2 = 0.0f;
        var3 = 0;
        int var4 = -1;
        boolean var5 = false;
        while (var3 < var3) {
            final char var6 = par1Str.charAt(var3);
            Label_0207: {
                switch (var6) {
                    case '\n': {
                        --var3;
                        break Label_0207;
                    }
                    case '�': {
                        if (var3 >= var3 - 1) {
                            break Label_0207;
                        }
                        ++var3;
                        final char var7 = par1Str.charAt(var3);
                        if (var7 == 'l' || var7 == 'L') {
                            var5 = true;
                            break Label_0207;
                        }
                        if (var7 == 'r' || var7 == 'R' || this.isFormatColor(var7)) {
                            var5 = false;
                        }
                        break Label_0207;
                    }
                    case ' ': {
                        var4 = var3;
                    }
                    case '-': {
                        var4 = var3;
                    }
                    case '_': {
                        var4 = var3;
                    }
                    case ':': {
                        var4 = var3;
                        break;
                    }
                }
                final String text = String.valueOf(var6);
                var2 += this.getStringWidth(text);
                if (var5) {
                    ++var2;
                }
            }
            if (var6 == '\n') {
                var4 = ++var3;
            }
            else if (var2 > par2) {
                break;
            }
            ++var3;
        }
        return (var3 != var3 && var4 != -1 && var4 < var3) ? var4 : var3;
    }

    public String stripControlCodes(final String s) {
        return this.patternControlCode.matcher(s).replaceAll("");
    }

    public String stripUnsupported(final String s) {
        return this.patternUnsupported.matcher(s).replaceAll("");
    }

    public String wrapFormattedStringToWidth(final String s, final float width) {
        final int wrapWidth = this.sizeStringToWidth(s, width);
        if (s.length() <= wrapWidth) {
            return s;
        }
        final String split = s.substring(0, wrapWidth);
        final String split2 = String.valueOf(this.getFormatFromString(split)) + s.substring(wrapWidth + ((s.charAt(wrapWidth) == ' ' || s.charAt(wrapWidth) == '\n') ? 1 : 0));
        try {
            return String.valueOf(split) + "\n" + this.wrapFormattedStringToWidth(split2, width);
        }
        catch (Exception e) {
            System.out.println("Cannot wrap string to width.");
            return "";
        }
    }
}