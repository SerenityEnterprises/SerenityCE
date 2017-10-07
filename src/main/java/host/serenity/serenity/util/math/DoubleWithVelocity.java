package host.serenity.serenity.util.math;

public class DoubleWithVelocity {
    private double value;
    private double velocity;

    public DoubleWithVelocity(double value) {
        this.value = value;
    }

    public void update(float partialTicks) {
        // partialTicks = Math.max(partialTicks, 0.0001F);
        double diff = velocity * 0.97 - velocity;
        velocity += diff * partialTicks;

        value += velocity * partialTicks;
    }

    public void applyForce(double force) {
        velocity += force;
    }

    public void setValue(double value) {
        velocity = 0;
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}
