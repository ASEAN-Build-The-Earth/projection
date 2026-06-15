package net.buildtheearth;

import com.google.common.base.Preconditions;

/**
 * Scales the warps projection's projected space up or down.
 * More specifically, it multiplies x and y by there respective scale factors.
 */
public class ScaleProjectionTransform extends ProjectionTransform {
    private final double scaleX;
    private final double scaleY;

    /**
     * Creates a new ScaleProjection with different scale factors for the x and y axis.
     *
     * @param input - projection to transform
     * @param scaleX - scaling to apply along the x axis
     * @param scaleY - scaling to apply along the y axis
     */
    public ScaleProjectionTransform(GeographicProjection input, double scaleX, double scaleY) {
        super(input);
        Preconditions.checkArgument(Double.isFinite(scaleX) && Double.isFinite(scaleY), "Projection scales should be finite");
        Preconditions.checkArgument(scaleX != 0 && scaleY != 0, "Projection scale cannot be 0!");
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    @Override
    public double[] inverseTransformNormalized(double x, double y) {
        x /= this.scaleX;
        y /= this.scaleY;
        return new double[] { x, y };
    }

    @Override
    public double[] transformNormalized(double x, double y) {
        x *= this.scaleX;
        y *= this.scaleY;
        return new double[] { x, y };
    }

    @Override
    public boolean upright() {
        return (this.scaleY < 0) ^ this.input.upright();
    }

    @Override
    public double[] bounds() {
        double[] b = this.input.bounds();
        b[0] *= this.scaleX;
        b[1] *= this.scaleY;
        b[2] *= this.scaleX;
        b[3] *= this.scaleY;
        return b;
    }

    @Override
    public double metersPerUnit() {
        return this.input.metersPerUnit() / Math.sqrt((this.scaleX * this.scaleX + this.scaleY * this.scaleY) / 2); //TODO: better transform
    }
}