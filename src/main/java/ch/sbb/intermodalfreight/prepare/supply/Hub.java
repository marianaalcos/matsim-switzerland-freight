
package ch.sbb.intermodalfreight.prepare.supply;

import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

/**
 * 
 * @author mdealmeida
 *
 */
public class Hub {

	private final String name;
	private Link hubLink;
	private TransitStopFacility stop;
	private final String shortName;
	private final String header;
	private final Coord coord;
	private final Map<String, Double> mode2hubCapacity;
	private final Map<String, Tuple<Double, Double>> mode2operatingTimes;
	
	/**
	 * 
	 * Creates a hub with the following specifications.
	 * 
	 * @param name the name we will mainly use internally for naming the links, and so on.
	 * @param shortName the short name used in the cargo line description in each row of the xlsx schedule file
	 * @param header the name used in the column headers in the xlsx schedule file
	 * @param coord x and y coordinates from this location, the hub will be connected to the (nearest) link of the road network
	 * @param mode2hubCapacity flow capacity (containers per hour) for each mode (during the time when the hub is open)
	 * @param mode2operatingTimes operation times for each mode for the 24h period, will be the same on the following day(s) if a supply is generated for multiple days
	 */
	public Hub(String name, String shortName, String header, Coord coord, Map<String, Double> mode2hubCapacity, Map<String, Tuple<Double, Double>> mode2operatingTimes) {
		this.name = name;
		this.shortName = shortName;
		this.header = header;
		this.coord = coord;
		this.mode2hubCapacity = mode2hubCapacity;
		this.mode2operatingTimes = mode2operatingTimes;
		
		// check data consistency
		for (String mode : this.mode2hubCapacity.keySet()) {
			if (this.mode2operatingTimes.get(mode) == null) {
				throw new RuntimeException("Missing information for mode " + mode + " at hub " + name + ". Aborting...");
			}
		}
		for (String mode : this.mode2operatingTimes.keySet()) {
			if (this.mode2hubCapacity.get(mode) == null) {
				throw new RuntimeException("Missing information for mode " + mode + " at hub " + name + ". Aborting...");
			}
		}
	}

	public Link getHubLink() {
		if (hubLink == null) throw new RuntimeException("Trying to access the hub before connecting the hubs to the network. Aborting...");
		return hubLink;
	}

	public void setHubLink(Link hubLink) {
		this.hubLink = hubLink;
	}

	public TransitStopFacility getStop() {
		if (hubLink == null) throw new RuntimeException("Trying to access the stop before creating the stop. Aborting...");
		return stop;
	}

	public void setStop(TransitStopFacility stop) {
		this.stop = stop;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}

	public String getHeader() {
		return header;
	}

	public Coord getCoord() {
		return coord;
	}

	public Map<String, Double> getMode2hubCapacity() {
		return mode2hubCapacity;
	}

	public Map<String, Tuple<Double, Double>> getMode2operatingTimes() {
		return mode2operatingTimes;
	}

}
