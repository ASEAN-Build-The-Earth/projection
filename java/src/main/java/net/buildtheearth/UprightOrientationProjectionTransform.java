package net.buildtheearth;

/**
 * Mirrors the warped projection vertically.
 * I.E. x' = x and y' = -y
 */
public class UprightOrientationProjectionTransform extends ProjectionTransform {

    /**
     * @param input - projection to transform
     */
    public UprightOrientationProjectionTransform(GeographicProjection input) {
        super(input);
    }

    @Override
    public double[] transform(double[] p) {
        p[1] = -p[1];
        return p;
    }

    @Override
    public boolean upright() {
        return !this.input.upright();
    }

    @Override
    public double[] bounds() {
        double[] b = this.input.bounds();
        return new double[]{ b[0], -b[3], b[2], -b[1] };
    }
}