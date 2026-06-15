package net.buildtheearth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.buildtheearth.model.GeographicalCoordinate;
import net.buildtheearth.model.MinecraftCoordinate;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class ProjectionTest {

	private static final double EPSILON = 1.0e-3;

	@Test
	void allCoordinatePairsFromJsonAreProjected() throws IOException, OutOfProjectionBoundsException {
		Path coordinatePairsFile = resolveCoordinatePairsFile();

		ObjectMapper mapper = new ObjectMapper();
        assertNotNull(coordinatePairsFile, "Couldn't find coordinatePairsFile");
        CoordinatePairs coordinatePairs = mapper.readValue(coordinatePairsFile.toFile(), CoordinatePairs.class);

		assertNotNull(coordinatePairs, "Coordinate pair payload must be present");
		assertNotNull(coordinatePairs.pairs, "Coordinate pair list must be present");
		assertEquals(
				coordinatePairs.count,
				coordinatePairs.pairs.size(),
				"JSON count field must match number of coordinate pairs"
		);

		for (int i = 0; i < coordinatePairs.pairs.size(); i++) {
			CoordinatePair pair = coordinatePairs.pairs.get(i);

			assertNotNull(pair, "Coordinate pair at index " + i + " must not be null");
			GeographicalCoordinate geographicalCoordinate = new GeographicalCoordinate(pair.lat, pair.lon);

			MinecraftCoordinate projected = Projection.toMinecraft(geographicalCoordinate);
			assertEquals(pair.x, projected.x(), EPSILON, "x mismatch at pair index " + i);
			assertEquals(pair.z, projected.z(), EPSILON, "z mismatch at pair index " + i);

			GeographicalCoordinate inversed = Projection.toGeo(projected);
			assertEquals(pair.lat, inversed.latitude(), EPSILON, "latitude mismatch at pair index " + i);
			assertEquals(pair.lon, inversed.longitude(), EPSILON, "longitude mismatch at pair index " + i);
		}
	}

	private static Path resolveCoordinatePairsFile() {
		Path candidate = Path.of("..", "util", "coord-pairs", "out", "coord-pairs.json");
		if (!candidate.toFile().exists()) {
			fail("Could not find coord-pairs JSON file in expected location " + candidate);
			return null;
		}
		return candidate;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	static class CoordinatePairs {
		public int count;
		public List<CoordinatePair> pairs;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	static class CoordinatePair {
		public double lat;
		public double lon;
		public double x;
		public double z;
	}

}
