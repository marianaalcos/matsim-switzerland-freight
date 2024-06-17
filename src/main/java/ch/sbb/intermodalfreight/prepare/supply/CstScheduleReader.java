package ch.sbb.intermodalfreight.prepare.supply;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * @author mdealmeida
 *
 */
public class CstScheduleReader {
    private static final Logger log = LogManager.getLogger(CstScheduleReader.class);
    
    private final List<RouteInfo> routeInfos;

	/**
	 * 
	 * Reads the schedule provided as xlsx file.
	 * 
	 * @param inputSchedulecstXLSX The schedule as xlsx file. For an example of how this file should look like, have a look into the test input directory.
	 * @param sheetName The xlsx sheet name, using the same as for the terminal schedule (Tabelle 1)
	 * @param hubs The hub information.
	 * @param arrivalDepartureOffsetFirstStop The first arrival departure offset time, i.e. how many seconds the vehicle should arrive before departing at the initial stop.
	 * 
	 */
	public CstScheduleReader(String inputSchedulecstXLSX, String sheetName, Map<String, Hub> hubs, double arrivalDepartureOffsetFirstStop) {
		routeInfos = new ArrayList<>();

		log.info("Reading CST transit schedule...");
		
		try {
			
			FileInputStream fis = new FileInputStream(new File(inputSchedulecstXLSX));
			Workbook wb;
			wb = WorkbookFactory.create(fis);
			
			Sheet sheet = wb.getSheet(sheetName);
					    
		    Map<Integer, String> column2Header = new HashMap<>();
		    
		    int rowCounter = 0;
		    for (Row row : sheet) {
		      	
		    	if (rowCounter == 0) {
		    		
		    		// store the headers (zug, strecke, richtung, hub names)
		    		String header = null;
		    		for (Cell cell : row) {
		    			if (cell.getCellType() == CellType.STRING) {
		    				header = cell.getRichStringCellValue().getString();
							column2Header.put(cell.getColumnIndex(), header);
							log.info(cell.getColumnIndex() + " -> " + header);
		    			} else if (cell.getCellType() == CellType.BLANK) {
		    				// merged cells -> use the previous header (merged cells are intermediate hubs with 3 cells)
							column2Header.put(cell.getColumnIndex(), header);
							log.info(cell.getColumnIndex() + " -> " + header);
		    			} else {
		    				throw new RuntimeException("Expecting only String Headers. Aborting...");
		    			}
		    		}
		    			    		
		    	} else if (rowCounter == 1) {
		    		// skip the second line (contains additional headers which we do not need)
		    		
		    	} else {
		    		
		    		String line = null;
			    	String route = null;
			    	Map<String,List<Double>> times = new HashMap<>();
			    			        
			        for (Cell cell : row) {
			        	
			        	if (cell.getCellType() == CellType.BLANK) {
			        		// skip cell
			        		continue;
			        	}
			        				        	
			        	String headerOfCurrentCell = column2Header.get(cell.getColumnIndex());
			        	headerOfCurrentCell = fixEncodingIssues(headerOfCurrentCell);
			        	
			        	if (headerOfCurrentCell.equals("Vehicle")) {
			        		if (cell.getCellType() == CellType.STRING) {
			        			line = cell.getRichStringCellValue().getString();
			        			line = fixEncodingIssues(line);
			        		} else {
			        			throw new RuntimeException("Expecting the Vehicle column as String format. Aborting...");
			        		}
			        	} else if (headerOfCurrentCell.equals("Strecke")) {
			        		if (cell.getCellType() == CellType.STRING) {
			        			route = cell.getRichStringCellValue().getString();
			        			route = fixEncodingIssues(route);
			        		} else {
			        			throw new RuntimeException("Expecting the Strecke column as String format. Aborting...");
			        		}
			        	} else if (headerOfCurrentCell.equals("Richtung")) {
			        		// not required
			        	
			        	} else {
			        		// this should be a hub name
			        		
			        		if (times.get(headerOfCurrentCell) == null) {
			        			times.put(headerOfCurrentCell, new ArrayList<>());
			        		}
			        				        				        		
			        		if (cell.getCellType() == CellType.NUMERIC) {
			        			Date date = cell.getDateCellValue();
			            		double time = date.getHours() * 3600. + date.getMinutes() * 60 + date.getSeconds();
			            		times.get(headerOfCurrentCell).add(time);
			            		
			        		} else if (cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.NUMERIC) {
			        			Date date = cell.getDateCellValue();
			            		double time = date.getHours() * 3600. + date.getMinutes() * 60 + date.getSeconds();
			            		times.get(headerOfCurrentCell).add(time);
			            		
			        		} else {
			        			throw new RuntimeException("Expecting the An/Ab column as Numeric or Formula/Numeric format. Aborting... cellType: " + cell.getCellType() + " / " + cell.getStringCellValue());
			        		}
			        	}
			        }
			        // add RouteType and set it to HUB
			        RouteInfo routeInfo = new RouteInfo(line, route, times, RouteInfo.RouteType.HUB);
			        if (line != null && route != null) routeInfos.add(routeInfo);
		    	}
		        rowCounter++;
		    }
		    
		    wb.close();
		    
		    log.info("CST Route data:");
		    for (RouteInfo routeInfo : routeInfos) {
		    	log.info(routeInfo.toString());
		    }
		    
		    log.info("Reading CST transit schedule... Done.");
		    
		    log.info("Processing CST transit schedule...");
		    process(hubs, arrivalDepartureOffsetFirstStop);
		    log.info("Processing CST transit schedule... Done.");
		    
		} catch (EncryptedDocumentException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	private String fixEncodingIssues(String route) {
		
		if (route.contains(" ")) {
			log.warn("Replace whitespace: " + route);
			route = route.replaceAll("\\s+","");
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

	private void process(Map<String, Hub> hubs, double arrivalDepartureOffsetFirstStop) {
		
		for (RouteInfo routeInfo : this.routeInfos) {
	    	log.info("Processing route info: " + routeInfo.toString());
	    	
	    	String transitRoute = routeInfo.getRoute();
	    	
			List<RouteStopInfo> routeStopInfos = new ArrayList<>();
			String[] hubArray = transitRoute.split("-");
			int hubCounter = 0;
	    	for (String hubShort : hubArray) {
	    		log.info(hubShort);
	    		Hub hub = getHubViaShortName(hubs, hubShort.trim());
	    		String hubFromHeader = hub.getHeader();
	    		
	    		double hubArrival;
				double hubDeparture;
				
	    		if (hubCounter == 0) {
	    			// first terminal
	    			if (routeInfo.getTimes().get(hubFromHeader) == null) {
	    				throw new RuntimeException("No times found for hub. Aborting... Hub: " + hub.getName() + " / Route: " + routeInfo);
	    			}
	    			
	    			if (routeInfo.getTimes().get(hubFromHeader).size() != 1) {
	    				throw new RuntimeException("Expecting only one time information. Aborting... Hub: " + hub.getName() + " / Route: " + routeInfo);
	    			}
	    			
	    			hubDeparture = routeInfo.getTimes().get(hubFromHeader).get(0);
	    			
	    			// we also want the vehicle to arrive some time before the departure, was NaN in the previous version...
	    			hubArrival = hubDeparture - arrivalDepartureOffsetFirstStop;
	    			// if (terminalArrival < 0.) terminalArrival = 0.;
	    			
	    		} else if (hubCounter == hubArray.length - 1) {
	    			// last terminal
	    			if (routeInfo.getTimes().get(hubFromHeader).size() != 1) {
	    				throw new RuntimeException("Expecting only one time information. Aborting..." + hub.getName());
	    			}
	    			hubArrival = routeInfo.getTimes().get(hubFromHeader).get(0);
	    			hubDeparture = Double.NaN;
	    			
	    		} else {
	    			// intermediate terminals
	    			double time1;
	    			double time2;
	    			if (routeInfo.getTimes().get(hubFromHeader).size() != 3) {
	    				log.warn("Expecting arrival, departure and haltezeit for intermediate stops. " + hub.getName() + " - " + transitRoute);
	    				log.warn("Assuming that the given time is the arrival time and assuming a fallback stop time of 30 minutes.");
	    				time1 = routeInfo.getTimes().get(hubFromHeader).get(0);
	    				time2 = time1 + 1800.;
	    			} else {
	    				time1 = routeInfo.getTimes().get(hubFromHeader).get(0);
		    			time2 = routeInfo.getTimes().get(hubFromHeader).get(2);
	    			}
	    			
	    			double haltezeit = routeInfo.getTimes().get(hubFromHeader).get(1);
	    			
	    			if (time1 - time2 == haltezeit ||
	    					time1 - time2 == haltezeit - 24 * 3600.) {
	    				
	    				hubArrival = time2;
	    				hubDeparture = time1;
	    				
	    			} else {
	    				hubArrival = time1;
	    				hubDeparture = time2;	
	    			}
	    		}
	    		
	    		RouteStopInfo stop = new RouteStopInfo(hub.getHubLink(), hub.getStop(), hubArrival, hubDeparture, routeInfo.getRouteType());
				routeStopInfos.add(stop);
				
				hubCounter++;
	    	}	 
	    	routeInfo.setRouteStopInfos(routeStopInfos);
	    }
	}
	
	private static Hub getHubViaShortName(Map<String, Hub> hubs, String hubShort) {
		for (Hub hub : hubs.values()) {
			if (hub.getShortName().equals(hubShort)){
				return hub;
			}
		}
		
		log.warn("Could not find a hub for " + hubShort);
		
		for (Hub hub : hubs.values()) {
			log.warn(hub.getShortName());
		}
		
		throw new NullPointerException("Could not find hub. Aborting..." + hubShort);
	}

	/**
	 * Returns the route information which was read from the xlsx schedule file.
	 * 
	 * @return
	 */
	public List<RouteInfo> getRouteInfos() {
		return this.routeInfos;
	}

}
