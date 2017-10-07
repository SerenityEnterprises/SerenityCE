package host.serenity.serenity.util;


import java.awt.*;
import java.util.Random;

public class ColourUtilities {

    public static final int[] RAINBOW_COLORS = new int[] { 0xff0000, 0xff4000, 0xff7f00, 0xffff00, 0x80ff00, 0x00ff00, 0x00ffff, 0x0080ff, 0x0000ff };

    private static Random random = new Random();

    public static double[] toRGBA(int hex) {
        return (new double[] { ((hex >> 16) & 255) / 255.0, ((hex >> 8) & 255) / 255.0, (hex & 255) / 255.0, ((hex >> 24) & 255) / 255.0, ((hex >> 24) & 0xFF) / 255.0 });
    }

    public static int generateColor() {
        final float hue = random.nextFloat();
        // final float saturation = (random.nextInt(20000) + 10000) / 10000f;
        float saturation = random.nextInt(5000) / 10000F + 0.5F;
        float brightness = random.nextInt(5000) / 10000F + 0.5F;
        return Color.HSBtoRGB(hue, saturation, brightness);
    }

    public static int generateWaypointColour() {
        return Color.HSBtoRGB((float) Math.random(), (float) Math.random() / 4F + 0.75F, (float) Math.random() / 2F + 0.25F);
    }

    public static Color blend(Color color1, Color color2, float ratio) {
        if (ratio < 0)
            return color2;
        if (ratio > 1)
            return color1;
        float ratio2 = (float) 1.0 - ratio;
        float rgb1[] = new float[3];
        float rgb2[] = new float[3];
        color1.getColorComponents(rgb1);
        color2.getColorComponents(rgb2);
        return new Color((rgb1[0] * ratio) + (rgb2[0] * ratio2), (rgb1[1] * ratio) + (rgb2[1] * ratio2), (rgb1[2] * ratio) + (rgb2[2] * ratio2));
    }

    public static int parseColor(String color) {
        if (color.startsWith("#")) {
            color = color.substring(1);
        }
        if (color.toLowerCase().startsWith("0x")) {
            color = color.substring(2);
        }
        try {
            return (int) Long.parseLong(color, 16);
        } catch (Exception ignored) {
        }
        return 0xffffffff;
    }
}
