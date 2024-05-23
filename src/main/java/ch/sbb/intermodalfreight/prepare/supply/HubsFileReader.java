package ch.sbb.intermodalfreight.prepare.supply;

import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.collections.Tuple;

/**
 * 
 * Reads the hubs file.
 * 
 * @author mdealmeida
 *
 */
public class HubsFileReader {
    private static final Logger log = LogManager.getLogger(HubsFileReader.class);
    
    Map<String, Hub> name2hub = new HashMap<>();
    
	/**
	 * Reads the cst_hubs csv file.
	 * 
	 * @param hubFile
	 */
	public HubsFileReader(String hubFile) {
		
		try {
		    Reader reader = new FileReader(hubFile);
			CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(';').withAllowDuplicateHeaderNames(false));
			
			log.info("headers: " + parser.getHeaderMap().toString());
			
			int rowCounter = 0;
			for (CSVRecord csvRecord : parser) {
				
				log.info("hub: " + csvRecord.get("name"));
				
				String[] modes = csvRecord.get("modes").split(",");
				String[] capacities = csvRecord.get("capacities").split(",");
				String[] openingHour = csvRecord.get("opening_hour").split(",");
				String[] closingHour = csvRecord.get("closing_hour").split(",");
				
				int cnt = 0;
				
				Map<String, Double> mode2capacity = new HashMap<>();
				Map<String, Tuple<Double, Double>> mode2openingClosingTime = new HashMap<>();
				
				for (String mode : modes) {
					
					double capacity = Double.parseDouble(capacities[cnt]);
					double openingTime = Double.parseDouble(openingHour[cnt]) * 3600.; // compute time in seconds
					double closingTime = Double.parseDouble(closingHour[cnt]) * 3600.; // compute time in seconds
					
					mode2capacity.put(mode, capacity);
					mode2openingClosingTime.put(mode, new Tuple<Double, Double>(openingTime, closingTime));
					
					cnt++;
				}
							
				String shortName = fixEncodingIssues(csvRecord.get("short_name"));
				String header = fixEncodingIssues(csvRecord.get("header"));
				
				log.info(shortName);
				log.info(header);
				
				Hub hub = new Hub(csvRecord.get("name"), shortName, header ,
						new Coord(Double.parseDouble(csvRecord.get("x")), Double.parseDouble(csvRecord.get("y"))),
						mode2capacity,
						mode2openingClosingTime);	
				
	        	name2hub.put(csvRecord.get("name"), hub);
	        	rowCounter++;
			}
			
	        log.info("Done reading " + rowCounter + " lines.");
		}
		
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	// setter method: returns the name2hub map, which contains mappings from String keys to Hub values.
	// other classes can use this method to retrieve the name2hub map
	public Map<String, Hub> getName2hub() {
		return name2hub;
	}
	// getter method: allows other classes to set (i.e., update or initialize) the name2hub map with a new Map<String, Hub> provided as an argument.
	public void setName2hub(Map<String, Hub> name2hub) {
		this.name2hub = name2hub;
	}
	
	private String fixEncodingIssues(String route) {
		
		if (route.contains(" ")) {
			log.warn("Replace whitespace: " + route);
			route = route.replaceAll("\\s+","");
			log.warn(" --> " + route);
		}
		if (route.contains("-")) {
			log.warn("Replace - : " + route);
			route = route.replaceAll("-","");
			log.warn(" --> " + route);
		}
		if (route.contains("/")) {
			log.warn("Replace / : " + route);
			route = route.replaceAll("/","");
			log.warn(" --> " + route);
		}
		if (route.contains("ä") || route.contains("ö") || route.contains("ü")) {
			log.warn("Replace Umlaut: " + route);
			route = route.replaceAll("ä","ae").replaceAll("ö", "oe").replaceAll("ü", "ue");
			log.warn(" --> " + route);
		}
		
		if (route.contains("Ä") || route.contains("Ö") || route.contains("Ü")) {
			log.warn("Replace Umlaut: " + route);
			route = route.replaceAll("Ä","AE").replaceAll("Ö", "OE").replaceAll("Ü", "UE");
			log.warn(" --> " + route);
		}
	    if (route.contains("è")|| route.contains("é")) {
	        log.warn("Replace è or é with e: " + route);
	        route = route.replaceAll("è", "e").replaceAll("é", "e");
	        log.warn(" --> " + route);
	    }
		return route;
	}
	
}



