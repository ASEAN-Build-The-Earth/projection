package net.buildtheearth;

import net.buildtheearth.model.GeographicalCoordinate;
import net.buildtheearth.model.MinecraftCoordinate;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Projection {

    private static GeographicProjection projection;

    private static final DecimalFormat decFormat1 = new DecimalFormat();

    static {
        decFormat1.setMaximumFractionDigits(1);
        DecimalFormatSymbols usSymbols = new DecimalFormatSymbols(Locale.US);
        decFormat1.setDecimalFormatSymbols(usSymbols);

        projection  = GeographicProjection.projections.get("bteairocean");
        projection = GeographicProjection.orientProjection(projection, GeographicProjection.Orientation.upright);
        projection = new ScaleProjectionTransform(projection, 7318261.522857145, 7318261.522857145);
        projection = new OffsetProjectionTransform(projection, 0, 0);
    }

    /**
     * Converts Minecraft coordinates to geographic coordinates
     *
     * @param coordinate The minecraft coordinates to convert.
     * @return - WG84 EPSG:4979 coordinates as a GeographicalCoordinate object
     */
    public static GeographicalCoordinate toGeo(MinecraftCoordinate coordinate) throws OutOfProjectionBoundsException {
        double[] res = projection.toGeo(coordinate.x(), coordinate.z());
        return new GeographicalCoordinate(res[1], res[0]);
    }

    /**
     * Converts Minecraft coordinates to geographic coordinates
     *
     * @param coordinates The minecraft coordinates to convert.
     * @return - WG84 EPSG:4979 coordinates as a GeographicalCoordinate object
     */
    public static GeographicalCoordinate[] toGeo(MinecraftCoordinate[] coordinates) throws OutOfProjectionBoundsException {
        GeographicalCoordinate[] res = new GeographicalCoordinate[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            res[i] = toGeo(coordinates[i]);
        }
        return res;
    }

    /**
     * Gets in-game coordinates from geographical location
     *
     * @param coordinate Geographical coordinate
     * @return The in-game coordinates (x, z)
     */
    public static MinecraftCoordinate toMinecraft(GeographicalCoordinate coordinate) throws OutOfProjectionBoundsException {
            double[] result = projection.fromGeo(coordinate.longitude(), coordinate.latitude());
            return new MinecraftCoordinate(result[0], result[1]);
    }

    /**
     * Gets in-game coordinates from geographical locations
     *
     * @param coordinate Geographical coordinates
     * @return The in-game coordinates (x, z)
     */
    public static MinecraftCoordinate[] toMinecraft(GeographicalCoordinate[] coordinate) throws OutOfProjectionBoundsException {
        MinecraftCoordinate[] res = new MinecraftCoordinate[coordinate.length];
        for (int i = 0; i < coordinate.length; i++) {
            res[i] = toMinecraft(coordinate[i]);
        }
        return res;
    }

    /**
     * Get formatted numeric geographic coordinates
     *
     * @param coordinates - WG84 EPSG:4979 coordinates as double array
     * @return - Formatted numeric coordinates as String
     */
    private static String formatGeoCoordinatesNumeric(double[] coordinates) {
        return coordinates[1] + "," + coordinates[0];
    }

    /**
     * Get formatted NSEW geographic coordinates
     *
     * @param coordinates - WG84 EPSG:4979 coordinates as double array
     * @return - Formatted NSEW coordinates as String
     */
    private static String formatGeoCoordinatesNSEW(double[] coordinates) {
        double fixedLon = coordinates[0];
        double fixedLat = coordinates[1];
        String eo = fixedLon < 0 ? "W": "E";
        String ns = fixedLat < 0 ? "S" : "N";
        double absLon = Math.abs(fixedLon);
        double absLat = Math.abs(fixedLat);
        int longitudeDegrees = (int) absLon;
        int latitudeDegrees = (int) absLat;
        double minLon = absLon * 60 - longitudeDegrees * 60;
        double minLat = absLat * 60 - latitudeDegrees * 60;
        int longitudeMinutes = (int) minLon;
        int latitudeMinutes = (int) minLat;
        double secLon = minLon * 60 - longitudeMinutes * 60;
        double secLat = minLat * 60 - latitudeMinutes * 60;
        String formattedLongitude = "" + longitudeDegrees + "°" + longitudeMinutes + "'" + decFormat1.format(secLon) + "\"" + eo;
        String formattedLatitude = "" + latitudeDegrees + "°" + latitudeMinutes + "'" + decFormat1.format(secLat) + "\"" + ns;
        return formattedLatitude + " " + formattedLongitude;
    }
}