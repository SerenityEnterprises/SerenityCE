package host.serenity.serenity.util.math;

import net.minecraft.util.MathHelper;

import java.util.List;

public class VectorUtilities {
    public static double wrapDegrees(double value) {
        value = value % 360;

        if (value >= 180) {
            value -= 360;
        }

        if (value < -180) {
            value += 360;
        }

        return value;
    }

    public static Vector3 faceOffset(Vector3 vector) {
        double distance = MathHelper.sqrt_double((vector.x * vector.x) + (vector.z * vector.z));
        float yaw = (float) Math.toDegrees(Math.atan2(vector.z, vector.x)) - 90.0F;
        float pitch = (float) - Math.toDegrees(Math.atan2(vector.y, distance));
        return new Vector3(VectorUtilities.wrapDegrees(yaw), VectorUtilities.wrapDegrees(pitch), 0);
    }

    public static Vector3 closestToLook(Vector3 eyePos, Vector3 lookVec, double range, List<Vector3> vectors) {
        Vector3 best = null;
        double closest = range;

        for (Vector3 v : vectors) {
            Vector3 offset = v.subtract(eyePos);
            double distance = offset.length();

            if (distance > range) {
                continue;
            }

            Vector3 projected = offset.subtract(lookVec.scale(distance));
            double newDistance = projected.length();

            if (newDistance < closest) {
                best = v;
                closest = newDistance;
            }
        }
        return best;
    }
}
