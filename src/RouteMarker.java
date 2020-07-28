package module6;

import java.util.List;

import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import processing.core.PGraphics;
import processing.core.PVector;

public class RouteMarker extends SimpleLinesMarker
{
	// Records whether this marker has been clicked (most recently)
	protected boolean clicked = false;
	
	public RouteMarker(ShapeFeature route) {
		super(route.getLocations(), route.getProperties());
		this.setHidden(true); // set the default to hidden
		this.setStrokeWeight(3);
		this.setStrokeColor(140);
//		this.highlightColor = 50;
	}
	public int getStrokeWeight() {
		return this.strokeWeight;
	}	
	
	// Getter method for the OpenFlights ID of the source airport for the route
	public int getSourceID() {
		return Integer.parseInt((String)this.getProperty("source"));
	}
	
	// Getter method for the OpenFlights ID of the destination airport for the route
	public int getDestID() {
		return Integer.parseInt((String)this.getProperty("destination"));
	}
	
	// Getter method for clicked field
	public boolean getClicked() {
		return clicked;
	}
	
	// Setter method for clicked field
	public void setClicked(boolean state) {
		clicked = state;
	}
	
	
	public void draw(PGraphics pg, float x, float y) {
		if (clicked) {
			System.out.println("I'm clicked -  by routemarker");
			this.setStrokeColor(250);
		}
		
	}
	
	public boolean checkForClick(int mouseX, int mouseY, List<PVector> vectors) {
		return this.isInside(mouseX, mouseY, vectors);
	}
}
