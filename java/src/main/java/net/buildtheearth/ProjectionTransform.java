package net.buildtheearth;

public abstract class ProjectionTransform extends GeographicProjection {
    protected final GeographicProjection input;

    /**
     * @param input - projection to transform
     */
    public ProjectionTransform(GeographicProjection input) {
        this.input = input;
    }

    public abstract double[] transform(double[] xy);

    public double[] inverseTransform(double[] xy) {
        return transform(xy);
    };

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
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        double[] p = inverseTransform(new double[] { x, y });
        return this.input.toGeo(p[0], p[1]);
    }

    @Override
    public double[] toGeoNormalized(double lambda, double phi) {
        double[] p = input.toGeoNormalized(lambda, phi);
        return inverseTransform(p);
    }

    @Override
    public double[] fromGeoNormalized(double lambda, double phi) {
        double[] p = input.fromGeoNormalized(lambda, phi);
        return transform(p);
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        double[] p = this.input.fromGeo(longitude, latitude);
        return transform(p);
    }
}