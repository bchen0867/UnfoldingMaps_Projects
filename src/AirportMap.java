package module6;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.geo.Location;
import parsing.ParseFeed;
import processing.core.PApplet;

/** An applet that shows airports (and routes)
 * on a world map.  
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 * MOOC team
 *
 */
public class AirportMap extends PApplet {
	
	UnfoldingMap map;
	private List<Marker> airportList;
	List<Marker> routeList;
	
	public void setup() {
		// setting up PApplet
		size(1280,720, OPENGL);
		
		// set the background color to be gray
		background(200, 200, 200);
		
		// setting up map and default events
		int spacing = 50;
		map = new UnfoldingMap(this, spacing, spacing, width-2*spacing, height-2*spacing);
//		System.out.println("Soopky! Zoomed out to " + map.getZoomLevel());
		map.zoomAndPanTo(10, new Location(32.881, -117.238)); // zoom to UCSD first for the comfort of eyes
		

		
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// get features from airport data
		List<PointFeature> features = ParseFeed.parseAirports(this, "airports.dat");
		
		// list for markers, hashmap for quicker access when matching with routes
		airportList = new ArrayList<Marker>();
		HashMap<Integer, Location> airports = new HashMap<Integer, Location>();
		
		// create markers from features
		for(PointFeature feature : features) {
			AirportMarker m = new AirportMarker(feature);
	
			m.setRadius(5);
			airportList.add(m);
			
			// put airport in hashmap with OpenFlights unique id for key
			airports.put(Integer.parseInt(feature.getId()), feature.getLocation());
		
		}
		
		
		// parse route data
		List<ShapeFeature> routes = ParseFeed.parseRoutes(this, "routes.dat");
		routeList = new ArrayList<Marker>();
		for(ShapeFeature route : routes) {
			
			// get source and destination airportIds
			int source = Integer.parseInt((String)route.getProperty("source"));
			int dest = Integer.parseInt((String)route.getProperty("destination"));
			
			// get locations for airports on route
			if(airports.containsKey(source) && airports.containsKey(dest)) {
				route.addLocation(airports.get(source));
				route.addLocation(airports.get(dest));
			}
			
			SimpleLinesMarker sl = new SimpleLinesMarker(route.getLocations(), route.getProperties());
		
//			System.out.println(sl.getProperties());
			
			//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
			// routeList.add(sl);
		}
		
		
		
		//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
		// map.addMarkers(routeList);
		airportList = sortByLat(airportList);
		map.addMarkers(airportList);
//		printLatitude(airportList, 5); // test if the sort method works
	}
	
	public void draw() {
		map.draw();
		displayZoomLevel();
	}
	
	// Restrict the amount of airport markers displaying when the user zooms in or out the map
	public void mouseWheel() {
		if (map.getZoomLevel() >= 5) {
			return;
		} else if (map.getZoomLevel() < 5 &&  map.getZoomLevel() >= 3) {
			// Limit max amount of airport displayed
			limitAirportShown(10, 6);
//			System.out.println("Zoomed out to " + map.getZoomLevel());
		} else {
			// Limit max amount of airport displayed
			limitAirportShown(10, 30);
//			System.out.println("Soopky! Zoomed out to " + map.getZoomLevel());
		}
	}
	
	private void displayZoomLevel() {
		String displayZoomLevel = "Zoom Level: " + Integer.toString(map.getZoomLevel());
		fill(255);
		rect(40, 35, 150, 20);
		fill(0);
		text(displayZoomLevel, 50, 50);
		return;
	}
	// a helper method to print out latitude from list of marker
	private void printLatitude(List <Marker> airportList, int numToShow)
	{
		
		// fix the situation where numToShow is larger than the number of the airportMarkers
		if (numToShow > airportList.size()) {
			numToShow = airportList.size();
		}
		
		int i = 0;
		for (Marker m: airportList) {
			if (i < numToShow) {
				AirportMarker marker = (AirportMarker) m;
				System.out.println(marker.getLatitude());
				i ++;
			} else {
				return;
			}
		}
	}
	
	// a sorting method returns to the sorted list of airport markers
	private List<Marker> sortByLat(List<Marker> list){
		// make a new array of AirportMarker object and copy markers from airportList to it.
		List <AirportMarker> airports = new ArrayList<AirportMarker>();
		for (Marker m: airportList) {
			airports.add((AirportMarker)m);
		}
		
		// Sort the AirportMarker according to its altitudes
		Collections.sort(airports);
		
		// turn the list of airport marker back to list of maker
		List<Marker> sortedAirportMarker = new ArrayList<>();
		for (Marker m: airports) {
			sortedAirportMarker.add((Marker)m);
		}
		return sortedAirportMarker;
	}
	
	// a unit testing of airport marker's sorting
	private void limitAirportShown(int latInterval, int markerInterval)
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
				
//				System.out.println("the lat interval: " + lat + " - " + (lat+ latInterval));
//				System.out.println("index i: " + i);
				
				if (((AirportMarker) airportList.get(i)).getLatitude() >= lat 
						&& ((AirportMarker) airportList.get(i)).getLatitude() < lat + latInterval ) {
					temp.add(airportList.get(i));
				} else {
					indexList.add(i);
					break;
				}
//			System.out.println(indexList);
//			System.out.println(temp.size());
//			printLatitude(airportList, 5);
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
	
	
	// loop over and unhide all markers
	private void unhideAll() {
		for(Marker marker : airportList) {
			marker.setHidden(false);
		}
	}
	
	// loop over and hide all markers
	private void hideAll() {
		for(Marker marker : airportList) {
			marker.setHidden(true);
		}
	}
	// the end
}
