package net.buildtheearth;

public abstract class ProjectionTransform extends GeographicProjection {
    protected final GeographicProjection input;

    /**
     * @param input - projection to transform
     */
    public ProjectionTransform(GeographicProjection input) {
        this.input = input;
    }

    @Override
    public boolean upright() {
        return this.input.upright();
    }

    @Override
    public double[] bounds() {
        return this.input.bounds();
    }

    @Override
    public double metersPerUnit() {
        return this.input.metersPerUnit();
    }

    @Override
    protected double[] inverseTransform(double x, double y) throws OutOfProjectionBoundsException {
        return this.input.toGeo(x, y);
    }

    @Override
    protected double[] transform(double longitude, double latitude) throws OutOfProjectionBoundsException {
        return this.input.fromGeo(longitude, latitude);
    }
}