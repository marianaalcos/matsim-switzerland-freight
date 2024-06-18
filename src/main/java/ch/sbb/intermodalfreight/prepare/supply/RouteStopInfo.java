/**
 * 
 */
package ch.sbb.intermodalfreight.prepare.supply;

import org.matsim.api.core.v01.network.Link;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

/**
 * 
 * Stores the relevant route stop information.
 * 
 * @author ikaddoura
 *
 */
public class RouteStopInfo {

	private final Link link;
	private final double tArrival;
	private final double tDeparture;
	private final TransitStopFacility transitStop;
	private final String routeType;

	public RouteStopInfo(Link link, TransitStopFacility transitStopFacility, double tArrival, double tDeparture, String routeType) {
		this.link = link;
		this.tArrival = tArrival;
		this.tDeparture = tDeparture;
		this.transitStop = transitStopFacility;
		this.routeType = routeType;
	}

	/**
	 * @return the link
	 */
	public Link getLink() {
		return link;
	}

	/**
	 * @return the tArrival
	 */
	public double gettArrival() {
		return tArrival;
	}

	/**
	 * @return the tDeparture
	 */
	public double gettDeparture() {
		return tDeparture;
	}

	/**
	 * @return the transitStop
	 */
	public TransitStopFacility getTransitStop() {
		return transitStop;
	}

	public String getRouteType() {
        return routeType;
    }
	
}
