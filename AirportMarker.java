package module6;

import java.util.List;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import processing.core.PGraphics;

/** 
 * A class to represent AirportMarkers on a world map.
 *   
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 * MOOC team
 *
 */
public class AirportMarker extends CommonMarker
{
	public static List<SimpleLinesMarker> routes;
	
	public AirportMarker(Feature airport) {
		super(((PointFeature)airport).getLocation(), airport.getProperties());
		this.setId(airport.getId());
	
	}
	

	// a getter method to get latitude
	public float getLatitude() {
		return this.getLocation().getLat();
	}
	
	// a getter method to get the title
	public String getTitle() {
		String title = String.join(", ", (String) this.getProperty("name"), (String) this.getProperty("city"), (String) this.getProperty("country"));
		return title;
	}
	
	
	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
//		pg.fill(11);
		pg.ellipse(x, y, 5, 5);
		
		
	}

	@Override
	public void showTitle(PGraphics pg, float x, float y) {
		
		// save previous styling
		pg.pushStyle();
		 // show rectangle with title
		pg.fill(255);

		// show routes
	}
	
}
