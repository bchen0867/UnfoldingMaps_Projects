package module6;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.marker.AbstractMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.geo.Location;
import parsing.ParseFeed;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

/** An applet that shows airports (and routes)
 * on a world map.  
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 * MOOC team
 *
 */
public class AirportMap extends PApplet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	UnfoldingMap map;
	private List<Marker> airportList;
	List<Marker> routeList;
	
	private Marker lastSelected;
	private CommonMarker lastClicked;
	
	public int spacing = 50;
	public int infoBoxWidth;

	public void setup() {
		
		// setting up PApplet size to be the size of the screen 
		// the height needs to exclude the height of the title bar and Widnows tool bar

		size(displayWidth, displayHeight - 150, OPENGL);
		
		// set the background color to be gray
		background(200, 200, 200);
		
		// setting up  map and default events
		
		infoBoxWidth = width/4;
		map = new UnfoldingMap(this, 2*spacing + infoBoxWidth, spacing, width - 3*spacing - infoBoxWidth, height - 2*spacing);
		map.zoomAndPanTo(9, new Location(32.881, -117.238)); // zoom to UCSD first for the comfort of eyes
		
		MapUtils.createDefaultEventDispatcher(this, map);
		
		
		
		// get features from airport data
		List<PointFeature> features = ParseFeed.parseAirports(this, "airports.dat");
		
		// list for markers, hashmap for quicker access when matching with routes
		airportList = new ArrayList<Marker>();
		HashMap<Integer, Location> airports = new HashMap<Integer, Location>();
		
		// create markers from features
		for(PointFeature feature : features) {
			AirportMarker m = new AirportMarker(feature);
			airportList.add(m);
			
			// put airport in hashmap with OpenFlights unique id for key
			airports.put(Integer.parseInt(feature.getId()), feature.getLocation());
		}
		

		
		// parse route data and load route data to a list of shape features
		List<ShapeFeature> routes = ParseFeed.parseRoutes(this, "routes.dat");
		routeList = new ArrayList<Marker>();
		for (ShapeFeature route : routes) {
			
			// get source and destination airportIds
			int source = Integer.parseInt((String)route.getProperty("source"));
			int dest = Integer.parseInt((String)route.getProperty("destination"));
			
			// get locations for airports on route
			if (airports.containsKey(source) && airports.containsKey(dest)) {
				route.addLocation(airports.get(source));
				route.addLocation(airports.get(dest));
			}
			
			// create markers from features
			RouteMarker sl = new RouteMarker(route);
			routeList.add(sl);
		}
		
		
		//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
		map.addMarkers(routeList);

		
		// Make a sorted copy of the airport according to its latitude
		airportList = airportList
			.stream()
			.map(marker -> ((AirportMarker) marker))
			.sorted(Comparator.comparingDouble(AirportMarker::getLatitude))
			.collect(Collectors.toList());
		map.addMarkers(airportList);
		
		// Show message on how to use the map
		String msg = "(Click the airport to see its detailed information.)";
		text(msg, spacing, spacing - 5);

	}
	
	public void draw() {
		map.draw();
		drawInfoBox();
		showRoutes();
		for (Marker m: airportList) {
			AirportMarker marker = (AirportMarker) m;
			if (!marker.isHidden()) {
				if (marker.isSelected()) {
					showAirportTitle(marker, map);
				}
				if (marker.getClicked()) {
					showAirportInfoBox(marker);
				}
			}
		}
	}
	
	
	
	
	
	
	/**___________Start of Helper Methods ___________*/
	// A getter method to get edges on the screen of the input map
	// returns an array of (x1, y1, x2, y2)
	// where (x1, y1) is the top left position,
	// and (x2, y2) is the bottom right one.
	public float[] mapEdges(UnfoldingMap map) {
		float[] mapEdges = new float[4];
		mapEdges[0] = map.getScreenPosition(map.getTopLeftBorder()).x;
		mapEdges[1] = map.getScreenPosition(map.getTopLeftBorder()).y;
		mapEdges[2] = mapEdges[0] + map.getWidth();
		mapEdges[3] = mapEdges[1] + map.getHeight();
		return mapEdges;
	}
	

	// input a marker and a map
	// returns true if the marker inside the map dimension
	// otherwise returns false
	private boolean isInsideMap(Marker m, UnfoldingMap map) {
		float x = ((AbstractMarker) m).getScreenPosition(map).x;
		float y = ((AbstractMarker) m).getScreenPosition(map).y;
		if (x >= mapEdges(map)[0] && x <= mapEdges(map)[2]
		&& y >= mapEdges(map)[1] && y <= mapEdges(map)[3]) {
			return true;
		}
		return false;
	}
	
	// input a list of marker and a map
	// returns to a sublist of airportList with coordinates are inside the map
	private List <Marker> getMarkersInsideMap() {
		return airportList
				.stream()
				.filter(m -> isInsideMap(m, map))
				.collect(Collectors.toList());
	}
	
	// input a list of marker and a map
	// returns to a sublist of markers inside the map
	// (not hidden and inside the map)
	private List <Marker> getDisplayedMarkers() {
		return getMarkersInsideMap()
				.stream()
				.filter(m -> !m.isHidden())
				.collect(Collectors.toList());
	}
	
	private void drawInfoBox() {
		pushStyle();
		noStroke();
		fill(255);
		rect(spacing, spacing, infoBoxWidth, map.getHeight(), spacing/5);
		popStyle();
		
		String displayZoomLevel = "Zoom Level: " + Integer.toString(map.getZoomLevel());
		textAndCover(displayZoomLevel, spacing, spacing);
		
		String hideInfo = String.format("%d out of %d airports displayed on the map", 
				getDisplayedMarkers().size(), getMarkersInsideMap().size());
		textAndCover(hideInfo, spacing, spacing + 20);

		return;
	}
	
	private void showAirportInfoBox(Marker m) {
		textAndCover("Airport Code: " + m.getProperty("code"), spacing, spacing + 60);
		textAndCover("Altitude: " + m.getProperty("altitude"), spacing, spacing + 80);
		textAndCover(((AirportMarker) m).getTitle(), spacing, spacing + 100);

	}
	private void textAndCover(String string, int x, int y) {
		pushStyle();
		int fontSize = 14;
		textSize(fontSize);
		noStroke();
		fill(255);
		rect(x, y, textWidth(string) + 50, 20, spacing/5);
		fill(0);
		text(string, x + 5, y + fontSize + 5);
		popStyle();
		return;
	}
	
	
	// a helper method let airport markers only shown after a certain interval
	private void limitAirportShown(List <Marker> airportList, int latInterval, int markerInterval)
	{	
		// reset all airport
		hideAll();
	
		// Make a temp list to store airport markers within certain latitude range
		List <Marker> temp = new ArrayList<>();
		List <Integer> indexList = new ArrayList<>();
		indexList.add(0);
		
		for (int lat = -90; lat < 90; lat = lat + latInterval) {
			temp.clear();
			for (int i = indexList.get(indexList.size()-1); i < airportList.size(); i ++ ) {
				
				if (((AirportMarker) airportList.get(i)).getLatitude() >= lat 
						&& ((AirportMarker) airportList.get(i)).getLatitude() < lat + latInterval ) {
					temp.add(airportList.get(i));
				} else {
					indexList.add(i);
					break;
				}

			}

		// loop over the indexList (the list of index of airport maker corresponding to the latitude interval)
		for (int i = 0; i < indexList.size() - 1; i ++) {
			int index = indexList.get(i);
			int listSize = indexList.get(i + 1) - indexList.get(i);
			
			// Only show every markerInterval airport Marker
			for (int j = 0; j < listSize; j = j + markerInterval) {
				airportList.get(index + j).setHidden(false);
				}
				}
			}	
		}
	
	
	
	// loop over and hide all route markers
	private void hideAllRM() {
		for(Marker marker : routeList) {
			marker.setHidden(true);
		}
	}
	
	// loop over and hide all airport markers
	private void hideAll() {
		for(Marker marker : getMarkersInsideMap()) {
			marker.setHidden(true);
		}
	}
	
	// loop over and unhide all airport markers
	private void unhideAll() {
		for(Marker marker : getMarkersInsideMap()) {
			marker.setHidden(false);
		}
	}
	
	/** Show the title of the earthquake if this marker is selected */
	private void showAirportTitle(AirportMarker m, UnfoldingMap map)
	{	
		
		float x = m.getScreenPosition(map).x;
		float y = m.getScreenPosition(map).y;
		String title = x +", " + y + m.getTitle();
		
		float rightEdge = mapEdges(map)[2];
		float bottomEdge = mapEdges(map)[3];

		// If the title box exceeds the right border of the map
		if ((x + textWidth(title) + 6) > rightEdge) {
			x = x - textWidth(title) - 6;
		}
		
		// if the title box exceeds the bottom border of the map
		if ((y + 15 + 18) > bottomEdge) {
			y = y - 18 - 15;
		}
		
		pushStyle();
		
		rectMode(PConstants.CORNER);
		stroke(110);
		fill(255,255,255);
		rect(x, y + 15, textWidth(title) + 6, 18, 5);
		textAlign(PConstants.LEFT, PConstants.TOP);
		fill(0);
		text(title, x + 3 , y +18);

		popStyle();
		
	}
	

	/** select the first airport marker is hovered by the mouse*/
	private void selectMarkerIfHover(List<Marker> markers)
	{
		// Abort if there's already a marker selected
		if (lastSelected != null) {
			return;
		}
		
		for (Marker m : markers) {
			CommonMarker marker = (CommonMarker) m;
	
			if (marker.isInside(map,  mouseX, mouseY)) {
				lastSelected = marker;
				marker.setSelected(true);
				return;
			}
		}
	}
	
	
//	/** select the first route marker is clicked by the mouse*/
//	private void checkRouteForClick(List<Marker> markers)
//	{
//		// Abort if there's already a marker clicked
//		if (lastClicked != null) {
//			return;
//		}
//		
//		for (Marker m : markers) 
//		{
//			RouteMarker marker = (RouteMarker) m;
//			float w = marker.getStrokeWeight()/2;
//			List<PVector> vectors = new ArrayList<>(); 
//			vectors.add(new PVector (marker.getScreenPosition(map).x, marker.getScreenPosition(map).y - w));
//			vectors.add(new PVector (marker.getScreenPosition(map).x - w, marker.getScreenPosition(map).y));
//			vectors.add(new PVector (marker.getScreenPosition(map).x + w, marker.getScreenPosition(map).y));
//			vectors.add(new PVector (marker.getScreenPosition(map).x, marker.getScreenPosition(map).y + w));
//			
//			if (marker.isInside(mouseX, mouseY, vectors)) { // need to change the isInside method to the vector version 
//				lastClicked = marker;
//				marker.setClicked(true);
//				return;
//			}
//		}
//	}
	
	private void showRoutes() {
		hideAllRM();
		List<Integer>idList = getDisplayedMarkers()
				.stream()
				.map(m -> Integer.parseInt(m.getId()))
				.collect(Collectors.toList());
		
		routeList
		.stream()
		.map(m -> ((RouteMarker) m))
		.filter(m -> idList.contains(m.getSourceID()) && idList.contains(m.getDestID()))
		.forEach(m -> m.setHidden(false));;
	}
	
	/**___________End of Helper Methods ___________*/
	 
	
	
	
	/**___________Start of Event Methods ___________*/
	
	/** The event handler for mouse wheel
	 * It will restrict the amount of airport markers displaying.
	 * A max amount of 
	 */
	// Restrict  when the user zooms out to zoomlevel 5
	@Override
	public void mouseWheel() {

		if (map.getZoomLevel() >= 5) {
			unhideAll();
		} else if (map.getZoomLevel() < 5 &&  map.getZoomLevel() >= 3) {
			// max of 6 
			limitAirportShown(getMarkersInsideMap(), 10, 6);
		} else {
			// Limit max amount of airport displayed
			limitAirportShown(getMarkersInsideMap(), 10, 30);
		}
	}
	
	
	/** Event handler that gets called automatically when the 
	 * mouse moves.
	 */
	@Override
	public void mouseMoved()
	{
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		
		}
		selectMarkerIfHover(airportList);
		//loop();
	}
	
	
	/** The event handler for mouse clicks
	 * It will display an earthquake and its threat circle of cities
	 * Or if a city is clicked, it will display all the earthquakes 
	 * where the city is in the threat circle
	 */
	@Override
	public void mouseClicked()
	{	
		// reset lastClicked to make sure only one is set Clicked per time
		if (lastClicked != null) {
			lastClicked.setClicked(false);
			lastClicked = null;
		}

		// check airport marker for click with map's getFirstHitMarker method
		CommonMarker hitMarker = (CommonMarker) map.getFirstHitMarker(mouseX, mouseY);
		
	    if (hitMarker != null && airportList.contains(hitMarker)) {
	        // Select current marker 
	    	hitMarker.setClicked(true);
	        lastClicked = hitMarker;
	        System.out.println("Marker clicked and its in airport list");
			}
	}


	
	
	/**___________End of Event Methods ___________*/
	
	
// the end
}
