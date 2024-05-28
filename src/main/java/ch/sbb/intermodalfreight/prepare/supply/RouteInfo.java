/**
 * 
 */
package ch.sbb.intermodalfreight.prepare.supply;

import java.util.List;
import java.util.Map;

/**
 * 
 * Stores the relevant route information.
 * 
 * @author ikaddoura
 *
 */
public class RouteInfo {

	public enum RouteType {
        TERMINAL,
        HUB
    }
	
	private final String line;
	private final String route;
	// private final Map<String, List<Double>> terminalFromHeader2Times;
	private final Map<String, List<Double>> times;
	private List<RouteStopInfo> routeStopInfos;
	private final RouteType routeType;

	public RouteInfo(String line, String route, Map<String, List<Double>> times, RouteType routeType) {
		this.line = line;
		this.route = route;
		this.times = times;
	    this.routeType = routeType;
		// this.terminalFromHeader2Times = terminal2Times;
	}

	/**
	 * @return the line
	 */
	public String getLine() {
		return line;
	}

	/**
	 * @return the route
	 */
	public String getRoute() {
		return route;
	}

	/**
     * @return the route type
     */
    public RouteType getRouteType() {
        return routeType;
    }
    /**
     * @return the times
     */
    public Map<String, List<Double>> getTimes() {
        return times;
    }
    
	/**
	 * @return the terminal2Times
	 */
	//public Map<String, List<Double>> getTerminalFromHeader2Times() {
	//	return terminalFromHeader2Times;
	//}

	//@Override
	//public String toString() {
	//	return "RouteInfo [line=" + line + ", route=" + route + ", terminalFromHeader2Times=" + terminalFromHeader2Times + "]";
	//}
	@Override
	public String toString() {
        return "RouteInfo [line=" + line + ", route=" + route + ", times=" + times + ", routeType=" + routeType + "]";
    }
	

	public List<RouteStopInfo> getRouteStopInfos() {
		return routeStopInfos;
	}

	public void setRouteStopInfos(List<RouteStopInfo> routeStopInfos) {
		this.routeStopInfos = routeStopInfos;
	}

}
