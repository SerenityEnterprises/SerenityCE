package host.serenity.serenity.util.render;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public final class GLUProjection {

    private static GLUProjection instance;

    private final FloatBuffer coords = BufferUtils.createFloatBuffer(3);

    private IntBuffer viewport;

    private FloatBuffer modelview;

    private FloatBuffer projection;

    private Vector3D frustumPos;

    private Vector3D[] frustum;

    private Vector3D[] invFrustum;

    private Vector3D viewVec;

    private double displayWidth;

    private double displayHeight;

    private double widthScale;

    private double heightScale;

    private double bra, bla, tra, tla;

    private Line tb, bb, lb, rb;

    private float fovY;

    private float fovX;

    private Vector3D lookVec;

    private GLUProjection() {
    }

    public static GLUProjection getInstance() {
        if (instance == null) {
            instance = new GLUProjection();
        }
        return instance;
    }

    public void updateMatrices(IntBuffer viewport, FloatBuffer modelview, FloatBuffer projection, double widthScale, double heightScale) {
        this.viewport = viewport;
        this.modelview = modelview;
        this.projection = projection;
        this.widthScale = widthScale;
        this.heightScale = heightScale;

        //Get fov and display dimensions
        float fov = (float) Math.toDegrees(Math.atan(1.0D / this.projection.get(5)) * 2.0D);
        fovY = fov;
        displayWidth = this.viewport.get(2);
        displayHeight = this.viewport.get(3);
        fovX = (float) Math.toDegrees(2.0D * Math.atan((displayWidth / displayHeight) * Math.tan(Math.toRadians(fovY) / 2.0D)));
        //Getting modelview vectors
        Vector3D ft = new Vector3D(this.modelview.get(12), this.modelview.get(13), this.modelview.get(14));
        Vector3D lv = new Vector3D(this.modelview.get(0), this.modelview.get(1), this.modelview.get(2));
        Vector3D uv = new Vector3D(this.modelview.get(4), this.modelview.get(5), this.modelview.get(6));
        Vector3D fv = new Vector3D(this.modelview.get(8), this.modelview.get(9), this.modelview.get(10));
        //Default axes
        Vector3D nuv = new Vector3D(0, 1.0D, 0);
        Vector3D nlv = new Vector3D(1.0D, 0, 0);
        Vector3D nfv = new Vector3D(0, 0, 1.0D);
        //Calculate yaw and pitch from modelview
        double yaw = Math.toDegrees(Math.atan2(nlv.cross(lv).length(), nlv.dot(lv))) + 180.0D;
        if (fv.x < 0.0D) {
            yaw = 360.0D - yaw;
        }
        double pitch = 0.0D;
        if ((-fv.y > 0.0D && yaw >= 90.0D && yaw < 270.0D) || (fv.y > 0.0D && !(yaw >= 90.0D && yaw < 270.0D))) {
            pitch = Math.toDegrees(Math.atan2(nuv.cross(uv).length(), nuv.dot(uv)));
        } else {
            pitch = -Math.toDegrees(Math.atan2(nuv.cross(uv).length(), nuv.dot(uv)));
        }
        lookVec = getRotationVector(yaw, pitch);
        //Get modelview matrix and invert it
        Matrix4f modelviewMatrix = new Matrix4f();
        modelviewMatrix.load(this.modelview.asReadOnlyBuffer());
        modelviewMatrix.invert();
        //Get frustum position
        frustumPos = new Vector3D(modelviewMatrix.m30, modelviewMatrix.m31, modelviewMatrix.m32);
        frustum = getFrustum(frustumPos.x, frustumPos.y, frustumPos.z, yaw, pitch, fov, 1.0F, displayWidth / displayHeight);
        invFrustum = getFrustum(frustumPos.x, frustumPos.y, frustumPos.z, yaw - 180, -pitch, fov, 1.0F, displayWidth / displayHeight);
        //Set view vec
        viewVec = getRotationVector(yaw, pitch).normalized();
        //Calculate screen border angles
        bra = Math.toDegrees(Math.acos((displayHeight * heightScale) / Math.sqrt(displayWidth * widthScale * displayWidth * widthScale + displayHeight * heightScale * displayHeight * heightScale)));
        bla = 360 - bra;
        tra = bla - 180;
        tla = bra + 180;
        //Create screen border lines
        rb = new Line(displayWidth * this.widthScale, 0, 0, 0, 1, 0);
        tb = new Line(0, 0, 0, 1, 0, 0);
        lb = new Line(0, 0, 0, 0, 1, 0);
        bb = new Line(0, displayHeight * this.heightScale, 0, 1, 0, 0);
    }

    public Projection project(double x, double y, double z, ClampMode clampModeOutside, boolean extrudeInverted) {
        if (viewport != null && modelview != null && projection != null) {
            Vector3D posVec = new Vector3D(x, y, z);
            boolean frustum[] = doFrustumCheck(this.frustum, frustumPos, x, y, z);
            boolean outsideFrustum = frustum[0] || frustum[1] || frustum[2] || frustum[3];
            //Check if point is inside frustum
            if (outsideFrustum) {
                //Check if point is on opposite side of the near clip plane
                boolean opposite = posVec.sub(frustumPos).dot(viewVec) <= 0.0D;
                //Get inverted frustum check
                boolean invFrustum[] = doFrustumCheck(this.invFrustum, frustumPos, x, y, z);
                boolean outsideInvertedFrustum = invFrustum[0] || invFrustum[1] || invFrustum[2] || invFrustum[3];
                if ((extrudeInverted && !outsideInvertedFrustum) || (outsideInvertedFrustum && clampModeOutside != ClampMode.NONE)) {
                    if ((extrudeInverted && !outsideInvertedFrustum) ||
                            (clampModeOutside == ClampMode.DIRECT && outsideInvertedFrustum)) {
                        //Point in inverted frustum, has to be clamped
                        double vecX = 0.0D;
                        double vecY = 0.0D;
                        if (GLU.gluProject((float) x, (float) y, (float) z, modelview, projection, viewport, coords)) {
                            //Get projected coordinates
                            if (opposite) {
                                //Invert coordinates
                                vecX = displayWidth * widthScale - (double) coords.get(0) * widthScale - displayWidth * widthScale / 2.0F;
                                vecY = displayHeight * heightScale - (displayHeight - (double) coords.get(1)) * heightScale - displayHeight * heightScale / 2.0F;
                            } else {
                                vecX = (double) coords.get(0) * widthScale - displayWidth * widthScale / 2.0F;
                                vecY = (displayHeight - (double) coords.get(1)) * heightScale - displayHeight * heightScale / 2.0F;
                            }
                        } else {
                            return new Projection(0, 0, Projection.Type.FAIL);
                        }
                        //Normalize point direction vector
                        Vector3D vec = new Vector3D(vecX, vecY, 0).snormalize();
                        vecX = vec.x;
                        vecY = vec.y;
                        //Get vector line
                        Line vectorLine = new Line(displayWidth * widthScale / 2.0F, displayHeight * heightScale / 2.0F, 0, vecX, vecY, 0);
                        //Calculate angle of point on 2D plane relative to the screen center
                        double angle = Math.toDegrees(Math.acos((vec.y) / Math.sqrt(vec.x * vec.x + vec.y * vec.y)));
                        if (vecX < 0.0D) {
                            angle = 360.0D - angle;
                        }
                        //Calculate screen border intersections
                        Vector3D intersect = new Vector3D(0, 0, 0);
                        //Check which screen border to intersect
                        if (angle >= bra && angle < tra) {
                            //Right
                            intersect = rb.intersect(vectorLine);
                        } else if (angle >= tra && angle < tla) {
                            //Top
                            intersect = tb.intersect(vectorLine);
                        } else if (angle >= tla && angle < bla) {
                            //Left
                            intersect = lb.intersect(vectorLine);
                        } else {
                            //Bottom
                            intersect = bb.intersect(vectorLine);
                        }
                        return new Projection(intersect.x, intersect.y, outsideInvertedFrustum ? Projection.Type.OUTSIDE : Projection.Type.INVERTED);
                    } else if ((clampModeOutside == ClampMode.ORTHOGONAL && outsideInvertedFrustum)) {
                        if (GLU.gluProject((float) x, (float) y, (float) z, modelview, projection, viewport, coords)) {
                            //Get projected coordinates
                            double guiX = (double) coords.get(0) * widthScale;
                            double guiY = (displayHeight - (double) coords.get(1)) * heightScale;
                            if (opposite) {
                                //Invert coordinates
                                guiX = displayWidth * widthScale - guiX;
                                guiY = displayHeight * heightScale - guiY;
                            }
                            if (guiX < 0) {
                                guiX = 0;
                            } else if (guiX > displayWidth * widthScale) {
                                guiX = displayWidth * widthScale;
                            }
                            if (guiY < 0) {
                                guiY = 0;
                            } else if (guiY > displayHeight * heightScale) {
                                guiY = displayHeight * heightScale;
                            }
                            return new Projection(guiX, guiY, outsideInvertedFrustum ? Projection.Type.OUTSIDE : Projection.Type.INVERTED);
                        } else {
                            return new Projection(0, 0, Projection.Type.FAIL);
                        }
                    }
                } else {
                    //Return point without clamping
                    if (GLU.gluProject((float) x, (float) y, (float) z, modelview, projection, viewport, coords)) {
                        //Get projected coordinates
                        double guiX = (double) coords.get(0) * widthScale;
                        double guiY = (displayHeight - (double) coords.get(1)) * heightScale;
                        if (opposite) {
                            //Invert coordinates
                            guiX = displayWidth * widthScale - guiX;
                            guiY = displayHeight * heightScale - guiY;
                        }
                        return new Projection(guiX, guiY, outsideInvertedFrustum ? Projection.Type.OUTSIDE : Projection.Type.INVERTED);
                    } else {
                        return new Projection(0, 0, Projection.Type.FAIL);
                    }
                }
            } else {
                //Point inside frustum, can be projected normally
                if (GLU.gluProject((float) x, (float) y, (float) z, modelview, projection, viewport, coords)) {
                    //Get projected coordinates
                    double guiX = (double) coords.get(0) * widthScale;
                    double guiY = (displayHeight - (double) coords.get(1)) * heightScale;
                    return new Projection(guiX, guiY, Projection.Type.INSIDE);
                } else {
                    return new Projection(0, 0, Projection.Type.FAIL);
                }
            }
        }
        return new Projection(0, 0, Projection.Type.FAIL);
    }

    public boolean[] doFrustumCheck(Vector3D[] frustumCorners, Vector3D frustumPos, double x, double y, double z) {
        Vector3D point = new Vector3D(x, y, z);
        boolean c1 = crossPlane(new Vector3D[]{frustumPos, frustumCorners[3], frustumCorners[0]}, point);
        boolean c2 = crossPlane(new Vector3D[]{frustumPos, frustumCorners[0], frustumCorners[1]}, point);
        boolean c3 = crossPlane(new Vector3D[]{frustumPos, frustumCorners[1], frustumCorners[2]}, point);
        boolean c4 = crossPlane(new Vector3D[]{frustumPos, frustumCorners[2], frustumCorners[3]}, point);
        return new boolean[]{c1, c2, c3, c4};
    }

    public boolean crossPlane(Vector3D[] plane, Vector3D point) {
        Vector3D z = new Vector3D(0.0D, 0.0D, 0.0D);
        Vector3D e0 = plane[1].sub(plane[0]);
        Vector3D e1 = plane[2].sub(plane[0]);
        Vector3D normal = e0.cross(e1).snormalize();
        double D = (z.sub(normal)).dot(plane[2]);
        double dist = normal.dot(point) + D;
        return dist >= 0.0D;
    }

    public Vector3D[] getFrustum(double x, double y, double z, double rotationYaw, double rotationPitch, double fov, double farDistance, double aspectRatio) {
        Vector3D viewVec = getRotationVector(rotationYaw, rotationPitch).snormalize();
        double hFar = 2D * Math.tan(Math.toRadians(fov / 2D)) * farDistance;
        double wFar = hFar * aspectRatio;
        Vector3D view = getRotationVector(rotationYaw, rotationPitch).snormalize();
        Vector3D up = getRotationVector(rotationYaw, rotationPitch - 90).snormalize();
        Vector3D right = getRotationVector(rotationYaw + 90, 0).snormalize();
        Vector3D camPos = new Vector3D(x, y, z);
        Vector3D view_camPos_product = view.add(camPos);
        Vector3D fc = new Vector3D(view_camPos_product.x * farDistance, view_camPos_product.y * farDistance, view_camPos_product.z * farDistance);
        Vector3D topLeftfrustum = new Vector3D(fc.x + (up.x * hFar / 2D) - (right.x * wFar / 2D), fc.y + (up.y * hFar / 2D) - (right.y * wFar / 2D), fc.z + (up.z * hFar / 2D) - (right.z * wFar / 2D));
        Vector3D downLeftfrustum = new Vector3D(fc.x - (up.x * hFar / 2D) - (right.x * wFar / 2D), fc.y - (up.y * hFar / 2D) - (right.y * wFar / 2D), fc.z - (up.z * hFar / 2D) - (right.z * wFar / 2D));
        Vector3D topRightfrustum = new Vector3D(fc.x + (up.x * hFar / 2D) + (right.x * wFar / 2D), fc.y + (up.y * hFar / 2D) + (right.y * wFar / 2D), fc.z + (up.z * hFar / 2D) + (right.z * wFar / 2D));
        Vector3D downRightfrustum = new Vector3D(fc.x - (up.x * hFar / 2D) + (right.x * wFar / 2D), fc.y - (up.y * hFar / 2D) + (right.y * wFar / 2D), fc.z - (up.z * hFar / 2D) + (right.z * wFar / 2D));
        return new Vector3D[]{topLeftfrustum, downLeftfrustum, downRightfrustum, topRightfrustum};
    }

    public Vector3D[] getFrustum() {
        return frustum;
    }

    public float getFovX() {
        return fovX;
    }

    public float getFovY() {
        return fovY;
    }

    public Vector3D getLookVector() {
        return lookVec;
    }

    public Vector3D getRotationVector(double rotYaw, double rotPitch) {
        double c = Math.cos(-rotYaw * 0.017453292F - Math.PI);
        double s = Math.sin(-rotYaw * 0.017453292F - Math.PI);
        double nc = -Math.cos(-rotPitch * 0.017453292F);
        double ns = Math.sin(-rotPitch * 0.017453292F);
        return new Vector3D(s * nc, ns, c * nc);
    }

    public enum ClampMode {
        ORTHOGONAL,
        DIRECT,
        NONE
    }

    public static class Line {
        public Vector3D sourcePoint = new Vector3D(0, 0, 0);
        public Vector3D direction = new Vector3D(0, 0, 0);

        public Line(double sx, double sy, double sz, double dx, double dy, double dz) {
            sourcePoint.x = sx;
            sourcePoint.y = sy;
            sourcePoint.z = sz;
            direction.x = dx;
            direction.y = dy;
            direction.z = dz;
        }

        public Vector3D intersect(Line line) {
            double a = sourcePoint.x;
            double b = direction.x;
            double c = line.sourcePoint.x;
            double d = line.direction.x;
            double e = sourcePoint.y;
            double f = direction.y;
            double g = line.sourcePoint.y;
            double h = line.direction.y;
            double te = -(a * h - c * h - d * (e - g));
            double be = b * h - d * f;
            if (be == 0) {
                return intersectXZ(line);
            }
            double t = te / be;
            Vector3D result = new Vector3D(0, 0, 0);
            result.x = sourcePoint.x + direction.x * t;
            result.y = sourcePoint.y + direction.y * t;
            result.z = sourcePoint.z + direction.z * t;
            return result;
        }

        private Vector3D intersectXZ(Line line) {
            double a = sourcePoint.x;
            double b = direction.x;
            double c = line.sourcePoint.x;
            double d = line.direction.x;
            double e = sourcePoint.z;
            double f = direction.z;
            double g = line.sourcePoint.z;
            double h = line.direction.z;
            double te = -(a * h - c * h - d * (e - g));
            double be = b * h - d * f;
            if (be == 0) {
                return intersectYZ(line);
            }
            double t = te / be;
            Vector3D result = new Vector3D(0, 0, 0);
            result.x = sourcePoint.x + direction.x * t;
            result.y = sourcePoint.y + direction.y * t;
            result.z = sourcePoint.z + direction.z * t;
            return result;
        }

        private Vector3D intersectYZ(Line line) {
            double a = sourcePoint.y;
            double b = direction.y;
            double c = line.sourcePoint.y;
            double d = line.direction.y;
            double e = sourcePoint.z;
            double f = direction.z;
            double g = line.sourcePoint.z;
            double h = line.direction.z;
            double te = -(a * h - c * h - d * (e - g));
            double be = b * h - d * f;
            if (be == 0) {
                return null;
            }
            double t = te / be;
            Vector3D result = new Vector3D(0, 0, 0);
            result.x = sourcePoint.x + direction.x * t;
            result.y = sourcePoint.y + direction.y * t;
            result.z = sourcePoint.z + direction.z * t;
            return result;
        }

        public Vector3D intersectPlane(Vector3D pointOnPlane, Vector3D planeNormal) {
            Vector3D result = new Vector3D(sourcePoint.x, sourcePoint.y, sourcePoint.z);
            double d = pointOnPlane.sub(sourcePoint).dot(planeNormal) / direction.dot(planeNormal);
            result.sadd(direction.mul(d));
            if (direction.dot(planeNormal) == 0.0D) {
                return null;
            }
            return result;
        }
    }

    public static class Vector3D {
        public double x,
                y,
                z;

        public Vector3D(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Vector3D add(Vector3D v) {
            return new Vector3D(x + v.x, y + v.y, z + v.z);
        }

        public Vector3D add(double x, double y, double z) {
            return new Vector3D(this.x + x, this.y + y, this.z + z);
        }

        public Vector3D sub(Vector3D v) {
            return new Vector3D(x - v.x, y - v.y, z - v.z);
        }

        public Vector3D sub(double x, double y, double z) {
            return new Vector3D(this.x - x, this.y - y, this.z - z);
        }

        public Vector3D normalized() {
            double len = Math.sqrt(x * x + y * y + z * z);
            return new Vector3D(x / len, y / len, z / len);
        }

        public double dot(Vector3D v) {
            return x * v.x + y * v.y + z * v.z;
        }

        public Vector3D cross(Vector3D v) {
            return new Vector3D(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x);
        }

        public Vector3D mul(double m) {
            return new Vector3D(x * m, y * m, z * m);
        }

        public Vector3D div(double d) {
            return new Vector3D(x / d, y / d, z / d);
        }

        public double length() {
            return Math.sqrt(x * x + y * y + z * z);
        }

        public Vector3D sadd(Vector3D v) {
            x += v.x;
            y += v.y;
            z += v.z;
            return this;
        }

        public Vector3D sadd(double x, double y, double z) {
            this.x += x;
            this.y += y;
            this.z += z;
            return this;
        }

        public Vector3D ssub(Vector3D v) {
            x -= v.x;
            y -= v.y;
            z -= v.z;
            return this;
        }

        public Vector3D ssub(double x, double y, double z) {
            this.x -= x;
            this.y -= y;
            this.z -= z;
            return this;
        }

        public Vector3D snormalize() {
            double len = Math.sqrt(x * x + y * y + z * z);
            x /= len;
            y /= len;
            z /= len;
            return this;
        }

        public Vector3D scross(Vector3D v) {
            x = y * v.z - z * v.y;
            y = z * v.x - x * v.z;
            z = x * v.y - y * v.x;
            return this;
        }

        public Vector3D smul(double m) {
            x *= m;
            y *= m;
            z *= m;
            return this;
        }

        public Vector3D sdiv(double d) {
            x /= d;
            y /= d;
            z /= d;
            return this;
        }

        @Override
        public String toString() {
            return "(X: " + x + " Y: " + y + " Z: " + z + ")";
        }
    }

    public static class Projection {
        private final double x;
        private final double y;
        private final Type t;

        public Projection(double x, double y, Type t) {
            this.x = x;
            this.y = y;
            this.t = t;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public Type getType() {
            return t;
        }

        public boolean isType(Type type) {
            return t == type;
        }

        public enum Type {
            INSIDE,
            OUTSIDE,
            INVERTED,
            FAIL
        }
    }
}
