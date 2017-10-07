package host.serenity.serenity.util.math;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class Vector3 {
    private static final String TO_STRING_FORMAT = "[%.2f | %.2f | %.2f]";

    public static final Vector3 ZERO = new Vector3(0, 0, 0);
    public static final Vector3 ONE = new Vector3(1, 1, 1);
    public static final Vector3 X_AXIS = new Vector3(1, 0, 0);
    public static final Vector3 Y_AXIS = new Vector3(0, 1, 0);
    public static final Vector3 Z_AXIS = new Vector3(0, 0, 1);
    public static final Vector3 ORIGIN = ZERO, I_HAT = X_AXIS, J_HAT = Y_AXIS, K_HAT = Z_AXIS;

    @SerializedName("x")
    public final double x;
    @SerializedName("y")
    public final double y;
    @SerializedName("z")
    public final double z;

    private final double length;

    public Vector3(double x, double y) {
        this(x, y, 0);
    }

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.length = Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Returns the magnitude of the Vector3.
     *
     * @return the magnitude of the Vector3
     */
    public double length() {
        return length;
    }

    /**
     * Returns the unit Vector3 parallel to this Vector3.
     *
     * @return the Vector3
     */
    public Vector3 normalize() {
        double length = length();
        if (length == 0) {
            return ZERO;
        }
        return new Vector3(x / length, y / length, z / length);
    }

    /**
     * Returns a scalar multiple of the unit Vector3 parallel to this Vector3.
     *
     * @param length the desired length of the Vector3.
     * @return the Vector3
     */
    public Vector3 normalize(double length) {
        return normalize().scale(length);
    }

    /**
     * Returns a scalar multiple of this Vector3.
     *
     * @param scale the desired scale factor for the Vector3.
     * @return the Vector3
     */
    public Vector3 scale(double scale) {
        return new Vector3(x * scale, y * scale, z * scale);
    }

    /**
     * Returns the Vector3 that is opposite to this Vector3.
     *
     * @return the Vector3
     */
    public Vector3 reverse() {
        return new Vector3(-x, -y, -z);
    }

    public Vector3 reverseIf(boolean condition) {
        if (condition) {
            return reverse();
        }
        return this;
    }


    /**
     * Returns the Vector3 that is this Vector3 with floored coordinates.
     *
     * @return the Vector3
     */
    public Vector3 floor() {
        return new Vector3(Math.floor(x), Math.floor(y), Math.floor(z));
    }

    /**
     * Returns the Vector3 that is this Vector3 with ceilinged coordinates.
     *
     * @return the Vector3
     */
    public Vector3 ceil() {
        return new Vector3(Math.ceil(x), Math.ceil(y), Math.ceil(z));
    }

    /**
     * Returns the Vector3 that is the sum of this Vector3 and <b>vector</b>.
     *
     * @param vector the Vector3 to be added to this Vector3.
     * @return the Vector3
     */
    public Vector3 add(Vector3 vector) {
        return new Vector3(x + vector.x, y + vector.y, z + vector.z);
    }

    /**
     * Returns the Vector3 that is <b>distance</b> above this vector.
     *
     * @param distance the distance to increase the height of this Vector3.
     * @return the Vector3
     */
    public Vector3 up(double distance) {
        return new Vector3(x, y + distance, z);
    }

    /**
     * Returns the Vector3 that is <b>distance</b> below this vector.
     *
     * @param distance the distance to decrease the height of this Vector3.
     * @return the Vector3
     */
    public Vector3 down(double distance) {
        return new Vector3(x, y - distance, z);
    }

    /**
     * Returns the Vector3 that is the difference of this Vector3 and <b>vector</b>.
     *
     * @param vector the Vector3 to be subtracted from this Vector3.
     * @return the Vector3
     */
    public Vector3 subtract(Vector3 vector) {
        return new Vector3(x - vector.x, y - vector.y, z - vector.z);
    }

    /**
     * Returns the Vector3 that is the dot product of this Vector3 and <b>vector</b>.
     *
     * @param vector the Vector3 to be dot multiplied with this Vector3.
     * @return the Vector3
     */
    public Vector3 dot(Vector3 vector) {
        return new Vector3(x * vector.x, y * vector.y, z * vector.z);
    }

    /**
     * Returns the Vector3 that is the cross product of this Vector3 and <b>vector</b>.
     *
     * @param vector the Vector3 to be crossed with this Vector3.
     * @return the Vector3
     */
    public Vector3 cross(Vector3 vector) {
        return new Vector3(y * vector.z - z * vector.y, z * vector.x - x * vector.z, x * vector.y - y * vector.x);
    }

    /**
     * Returns the Vector3 that is this Vector3 rotated <b>theta</b> radians about the z-axis.
     *
     * @param theta the angle to rotate.
     * @return the Vector3
     */
    public Vector3 rotateAboutX(double theta) {
        double sin = Math.sin(theta);
        double cos = Math.cos(theta);
        return new Vector3(x, y * cos - z * sin, y * sin + z * cos);
    }

    /**
     * Returns the Vector3 that is this Vector3 rotated <b>theta</b> radians about the y-axis.
     *
     * @param theta the angle to rotate.
     * @return the Vector3
     */
    public Vector3 rotateAboutY(double theta) {
        double sin = Math.sin(theta);
        double cos = Math.cos(theta);
        return new Vector3(x * cos + z * sin, y, -x * sin + z * cos);
    }

    /**
     * Returns the Vector3 that is this Vector3 rotated <b>theta</b> radians about the z-axis.
     *
     * @param theta the angle to rotate.
     * @return the Vector3
     */
    public Vector3 rotateAboutZ(double theta) {
        double sin = Math.sin(theta);
        double cos = Math.cos(theta);
        return new Vector3(x * cos - y * sin, x * sin + y * cos, z);
    }

    /**
     * Returns the Vector3 that is this Vector3 rotated <b>theta</b> degrees about the x-axis.
     *
     * @param theta the angle to rotate.
     * @return the Vector3
     */
    public Vector3 rotateAboutXDeg(double theta) {
        return rotateAboutX(Math.toRadians(theta));
    }

    /**
     * Returns the Vector3 that is this Vector3 rotated <b>theta</b> degrees about the y-axis.
     *
     * @param theta the angle to rotate.
     * @return the Vector3
     */
    public Vector3 rotateAboutYDeg(double theta) {
        return rotateAboutY(Math.toRadians(theta));
    }

    /**
     * Returns the Vector3 that is this Vector3 rotated <b>theta</b> degrees about the z-axis.
     *
     * @param theta the angle to rotate.
     * @return the Vector3
     */
    public Vector3 rotateAboutZDeg(double theta) {
        return rotateAboutZ(Math.toRadians(theta));
    }

    @Override
    public String toString() {
        return String.format(TO_STRING_FORMAT, x, y, z);
    }

    public double[] toArray() {
        return new double[] { x, y, z };
    }

    public int[] toArrayZ() {
        return new int[] { MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z) };
    }


    /**
     * Returns the unit Vector3 that is parallel to <b>yaw</b> and <b>pitch</b> (radians).
     *
     * @param yaw   the angle about the y-axis.
     * @param pitch the angle above the horizon.
     * @return the Vector3
     */
    public static Vector3 fromAngles(double yaw, double pitch) {
        return fromAngles(1, yaw, pitch);
    }

    /**
     * Returns the unit Vector3 that is parallel to <b>yaw</b> and <b>pitch</b> (degrees).
     *
     * @param yaw   the angle about the y-axis.
     * @param pitch the angle above the horizon.
     * @return the Vector3
     */
    public static Vector3 fromAnglesDeg(double yaw, double pitch) {
        return fromAnglesDeg(1, yaw, pitch);
    }

    /**
     * Returns a Vector3 (magnitude: <b>length</b>) that is parallel to <b>yaw</b> and <b>pitch</b> (degrees).
     *
     * @param yaw   the angle about the y-axis.
     * @param pitch the angle above the horizon.
     * @return the Vector3
     */
    public static Vector3 fromAnglesDeg(double length, double yaw, double pitch) {
        return fromAngles(length, Math.toRadians(yaw), Math.toRadians(pitch));
    }

    /**
     * Returns a Vector3 (magnitude: <b>length</b>) that is parallel to <b>yaw</b> and <b>pitch</b> (radians).
     *
     * @param yaw   the angle about the y-axis.
     * @param pitch the angle above the horizon.
     * @return the Vector3
     */
    public static Vector3 fromAngles(double length, double yaw, double pitch) {
        double x = -Math.sin(yaw);
        double z = Math.cos(yaw);

        double hScale = Math.cos(pitch);

        return new Vector3(x * hScale * length, Math.sin(-pitch) * length, z * hScale * length);
    }



    /**
     * Returns a Vector3 that is <b>ratio</b> between <b>previous</b> and <b>current</b>.
     *
     * @param current  the current vector.
     * @param previous the previous vector.
     * @param ratio the ratio of time between the previous vector and the current vector.
     * @return the Vector3
     */
    public static Vector3 interpolate(Vector3 current, Vector3 previous, double ratio) {
        if (current == null && previous == null) {
            return Vector3.ORIGIN;
        }
        if (previous == null) {
            return current;
        }
        if (current == null) {
            return previous;
        }
        return previous.add(current.subtract(previous).scale(ratio));
    }

    // Hacks for the rotation aspect
    public double getRotationYaw() {
        return x;
    }

    public double getRotationPitch() {
        return y;
    }

    public double getRotationRoll() {
        return z;
    }

    public Vector3 wrapDegrees() {
        return new Vector3(VectorUtilities.wrapDegrees(x), VectorUtilities.wrapDegrees(y), VectorUtilities.wrapDegrees(z));
    }

    public static Vec3 convertToVec3(Vector3 vector) {
        return new Vec3(vector.x, vector.y, vector.z);
    }

    public static Vector3 convertFromVec3(Vec3 vec3) {
        return new Vector3(vec3.xCoord, vec3.yCoord, vec3.zCoord);
    }
}
