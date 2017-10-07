package host.serenity.serenity.util;

import net.minecraft.entity.Entity;

import static java.lang.Double.isNaN;

public class EntityHelper {
    public static float[] getTargetAngles(Entity player, Entity target) {
        double deltaX = target.posX - player.posX;
        double deltaY = target.posY + (target.getEyeHeight() / 2) - (player.posY + player.getEyeHeight());
        double deltaZ = target.posZ - player.posZ;
        double yawToEntity; // tangent degree to entity

        if (deltaZ < 0 && deltaX < 0) { // quadrant 3
            yawToEntity = 90D + Math.toDegrees(Math.atan(deltaZ / deltaX)); // 90
            // degrees
            // forward
        } else if (deltaZ < 0 && deltaX > 0) { // quadrant 4
            yawToEntity = -90D + Math.toDegrees(Math.atan(deltaZ / deltaX)); // 90
            // degrees
            // back
        } else { // quadrants one or two
            yawToEntity = Math.toDegrees(-Math.atan(deltaX / deltaZ));
        }

        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ
                * deltaZ); // distance away for calculating pitch
        double pitchToEntity = -Math.toDegrees(Math.atan(deltaY / distanceXZ)); // tangent

        yawToEntity = wrapAngleTo180((float) yawToEntity);
        pitchToEntity = wrapAngleTo180((float) pitchToEntity);

        yawToEntity = isNaN(yawToEntity) ? 0 : yawToEntity;
        pitchToEntity = isNaN(pitchToEntity) ? 0 : pitchToEntity;

        return new float[] { (float) yawToEntity, (float) pitchToEntity };
    }

    public static float[] getAnglesToPosition(Entity player, double x, double y, double z) {
        double deltaX = x - player.posX;
        double deltaY = y - player.posY - player.getEyeHeight() - 0.3;
        double deltaZ = z - player.posZ;
        double yawToEntity; // tangent degree to entity

        if (deltaZ < 0 && deltaX < 0) { // quadrant 3
            yawToEntity = 90D + Math.toDegrees(Math.atan(deltaZ / deltaX)); // 90
            // degrees
            // forward
        } else if (deltaZ < 0 && deltaX > 0) { // quadrant 4
            yawToEntity = -90D + Math.toDegrees(Math.atan(deltaZ / deltaX)); // 90
            // degrees
            // back
        } else { // quadrants one or two
            yawToEntity = Math.toDegrees(-Math.atan(deltaX / deltaZ));
        }

        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ
                * deltaZ); // distance away for calculating pitch
        double pitchToEntity = -Math.toDegrees(Math.atan(deltaY / distanceXZ)); // tangent

        yawToEntity = wrapAngleTo180((float) yawToEntity);
        pitchToEntity = wrapAngleTo180((float) pitchToEntity);

        yawToEntity = isNaN(yawToEntity) ? 0 : yawToEntity;
        pitchToEntity = isNaN(pitchToEntity) ? 0 : pitchToEntity;

        return new float[] { (float) yawToEntity, (float) pitchToEntity };
    }

    public static float[] getAnglesToPosition(double posX, double posY, double posZ, double x, double y, double z) {
        double deltaX = x - posX;
        double deltaY = y - posY;
        double deltaZ = z - posZ;
        double yawToEntity; // tangent degree to entity

        if (deltaZ < 0 && deltaX < 0) { // quadrant 3
            yawToEntity = 90D + Math.toDegrees(Math.atan(deltaZ / deltaX)); // 90
            // degrees
            // forward
        } else if (deltaZ < 0 && deltaX > 0) { // quadrant 4
            yawToEntity = -90D + Math.toDegrees(Math.atan(deltaZ / deltaX)); // 90
            // degrees
            // back
        } else { // quadrants one or two
            yawToEntity = Math.toDegrees(-Math.atan(deltaX / deltaZ));
        }

        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ
                * deltaZ); // distance away for calculating pitch
        double pitchToEntity = -Math.toDegrees(Math.atan(deltaY / distanceXZ)); // tangent

        yawToEntity = wrapAngleTo180((float) yawToEntity);
        pitchToEntity = wrapAngleTo180((float) pitchToEntity);

        yawToEntity = isNaN(yawToEntity) ? 0 : yawToEntity;
        pitchToEntity = isNaN(pitchToEntity) ? 0 : pitchToEntity;

        return new float[] { (float) yawToEntity, (float) pitchToEntity };
    }

    private static float wrapAngleTo180(float angle) {
        angle %= 360.0F;

        while (angle >= 180.0F) {
            angle -= 360.0F;
        }

        while (angle < -180.0F) {
            angle += 360.0F;
        }
        return angle;
    }
}