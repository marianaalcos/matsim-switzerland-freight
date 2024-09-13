package ch.sbb.intermodalfreight.prepare.supply;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * Reads the hubs distances file.
 * 
 * @author mdealmeida
 *
 */
public class HubDistanceReader {
    private static final Logger log = LogManager.getLogger(HubDistanceReader.class);

	/**
	 * 
	 * Reads the hubs distances csv file in which the distances are given in kilometers
	 * and returns a map with distances in meters for each terminal pair.
	 * 
	 * @param inputDistancescstCSV A file which provides the distances between all hubs. Uses a ';' as delimiter. Provides the distances in kilometers.
	 * 
	 * @return the distance of each hub pair in meters. Each hub pair is separated by an underscore.
	 * 
	 * @throws IOException
	 */
	public Map<String, Double> getHubDistances(String inputDistancescstCSV) throws IOException {

	    log.info("Reading relation distances...");
	    Map<String, Double> relation2distance = new HashMap<>(); // Double distances
		// Map<String, Integer> relation2distance = new HashMap<>();

		Reader distancescstCSVfile = new FileReader(new File(inputDistancescstCSV));
		CSVParser parser = CSVParser.parse(distancescstCSVfile, CSVFormat.DEFAULT.withDelimiter(';'));
		
		int rowCounter = 0;
		CSVRecord headers = null;
		for (CSVRecord csvRecord : parser) {
			if (rowCounter == 0) {
				headers = csvRecord;
			} else {				
				int columnCounter = 0;
				String from = null;
				for (String cell : csvRecord) {
					if (columnCounter == 0) {
						from = cell;
						if (from.contains("_")) throw new RuntimeException("An underscore is not allowed as hub name: " + from);
					} else {
						String to = headers.get(columnCounter);
						if (to.contains("_")) throw new RuntimeException("An underscore is not allowed as hub name: " + to);
						if (!cell.equals("")) {
							relation2distance.put(from + "_" + to, 1000 * Double.valueOf(cell));
						}
					}
					columnCounter++;
				}
			}
			rowCounter++;
		}
		
	    log.info("Reading relation distances... Done.");

		return relation2distance;
	}

}
