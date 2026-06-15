package net.buildtheearth;


import net.buildtheearth.airocean.Airocean;
import net.buildtheearth.airocean.ConformalEstimate;
import net.buildtheearth.airocean.ModifiedAirocean;

import java.util.HashMap;
import java.util.Map;

/**
 * Support for various projection types.
 * <p>
 * The geographic space is the surface of the earth, parameterized by the usual spherical coordinates system of latitude and longitude.
 * The projected space is a plane on to which the geographic space is being projected, and is parameterized by a 2D Cartesian coordinate system (x and y).
 * <p>
 * A projection as defined here is something that projects a point in the geographic space to a point of the projected space (and vice versa).
 * <p>
 * All geographic coordinates are in degrees.
 * <ul>Note: Projection steps consist of normalization and transformation both <u>forwards</u> and <u>backwards</u>.
 *     <li><u>normalization</u>: The normalization of input units</li>
 *     <li><u>transformation</u>: The actual calculation to project the unit into implementing Geographic Projection.</li>
 * </ul>
 * @see #fromGeo(double, double)
 * @see #toGeo(double, double)
 * @see <a href="https://en.wikipedia.org/wiki/Equirectangular_projection">Wikipedia's article on the equirectangular projection</a>
 */
public abstract class GeographicProjection {

    /**
     * Contains the various projections implemented in Terra121,
     * identified by a String key.
     */
    public static final Map<String, GeographicProjection> projections;

    static {
        projections = new HashMap<>();
        projections.put("airocean", new Airocean());
        projections.put("conformal", new ConformalEstimate());
        projections.put("bteairocean", new ModifiedAirocean());
    }

    /**
     * Orients a projection
     *
     * @param base - the projection to orient
     * @param orientation - the orientation to use
     *
     * @return a projection that warps the base projection but applies the transformation described by the given orientation
     */
    public static GeographicProjection orientProjection(GeographicProjection base, Orientation orientation) {
        if (base.upright()) {
            if (orientation == Orientation.upright) {
                return base;
            }
            base = new UprightOrientationProjectionTransform(base);
        }

        if (orientation == Orientation.swapped) {
            return null;
        } else if (orientation == Orientation.upright) {
            base = new UprightOrientationProjectionTransform(base);
        }

        return base;
    }

    /**
     * The backwards <u>normalization</u> of this projection,
     * or the inverse transform.
     * De-normalize map coordinates back to degrees.
     *
     * @param lambda x map coordinate (normalized)
     * @param phi y map coordinate (normalized)
     * @return {longitude, latitude} in degrees
     * @throws OutOfProjectionBoundsException if the coordinate can't be transformed.
     */
    protected abstract double[] inverseTransform(double lambda, double phi) throws OutOfProjectionBoundsException;

    /**
     * The backwards <u>transformation</u> of this projection, or the inverse transform.
     * transform the map coordinate point to normalized spherical (or ellipse) unit.
     *
     * @param x x map coordinate.
     * @param y y map coordinate.
     * @return {lambda, phi} normalized coordinate point after transforming x, y.
     * @throws OutOfProjectionBoundsException if the coordinate can't be transformed.
     */
    public abstract double[] inverseTransformNormalized(double x, double y) throws OutOfProjectionBoundsException;

    /**
     * Converts map coordinates to geographic coordinates
     *
     * @param x x map coordinate
     * @param y y map coordinate
     * @return {longitude, latitude} in degrees
     * @throws OutOfProjectionBoundsException if the specified point on the projected space cannot be mapped to a point of the geographic space
     */
    public final double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        double[] inverse = inverseTransformNormalized(x, y);
        return inverseTransform(inverse[0], inverse[1]);
    }

    /**
     * Converts geographic coordinates to map coordinates
     *
     * @param longitude longitude, in degrees
     * @param latitude  latitude, in degrees
     * @return {x, y} map coordinates
     * @throws OutOfProjectionBoundsException if the specified point on the geographic space cannot be mapped to a point of the projected space
     */
    public final double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        double[] transform = transform(longitude, latitude);
        return transformNormalized(transform[0], transform[1]);
    }

    /**
     * The forward <u>normalization</u> of this projection.
     * Normalizes the lat, long unit to radians.
     *
     * @param longitude longitude, in degrees
     * @param latitude  latitude, in degrees
     * @return {lambda, phi} in radians after transforming longitude, latitude.
     * @throws OutOfProjectionBoundsException if the coordinate can't be transformed.
     */
    protected abstract double[] transform(double longitude, double latitude) throws OutOfProjectionBoundsException;

    /**
     * The forward <u>transformation</u> of this projection.
     * transform lat, long in radians to the projection's map coordinate.
     *
     * @param lambda longitude (normalized) in radians
     * @param phi    latitude, (normalized) in radians
     * @return {x, y} map coordinates in the projection's unit
     * @throws OutOfProjectionBoundsException if the coordinate can't be transformed.
     */
    public abstract double[] transformNormalized(double lambda, double phi) throws OutOfProjectionBoundsException;

    /**
     * Gives an estimation of the scale of this projection.
     * This is just an estimation, as distortion is inevitable when projecting a sphere onto a flat surface,
     * so this value varies from places to places in reality.
     *
     * @return an estimation of the scale of this projection
     */
    public abstract double metersPerUnit();

    /**
     * Indicates the minimum and maximum X and Y coordinates on the projected space.
     *
     * @return {minimum X, minimum Y, maximum X, maximum Y}
     */
    public double[] bounds() {

        try {
            //get max in by using extreme coordinates
            double[] bounds = {
                    this.fromGeo(-180, 0)[0],
                    this.fromGeo(0, -90)[1],
                    this.fromGeo(180, 0)[0],
                    this.fromGeo(0, 90)[1]
            };

            if (bounds[0] > bounds[2]) {
                double t = bounds[0];
                bounds[0] = bounds[2];
                bounds[2] = t;
            }

            if (bounds[1] > bounds[3]) {
                double t = bounds[1];
                bounds[1] = bounds[3];
                bounds[3] = t;
            }

            return bounds;
        } catch (OutOfProjectionBoundsException e) {
            return new double[] {0, 0, 1, 1};
        }
    }

    /**
     * Indicates whether or not the north pole is projected to the north of the south pole on the projected space,
     * assuming Minecraft's coordinate system cardinal directions for the projected space (north is negative Z).
     *
     * @return north pole Z <= south pole Z
     */
    public boolean upright() {
        try {
            return this.fromGeo(0, 90)[1] <= this.fromGeo(0, -90)[1];
        } catch (OutOfProjectionBoundsException e) {
            return false;
        }
    }

    public enum Orientation {
        none, upright, swapped
    }
}