package tcc;

import ail.mas.DefaultEnvironment;
import ail.syntax.Action;
import ail.syntax.Unifier;
import ail.syntax.NumberTerm;
import ail.syntax.NumberTermImpl;
import ail.syntax.Predicate;
import ail.util.AILexception;
import ajpf.util.AJPFLogger;
import ail.syntax.Term;
import java.util.Random;

import com.sun.xml.internal.ws.assembler.jaxws.MustUnderstandTubeFactory;

public class SimpleEnv extends DefaultEnvironment{
	
	// AV's initial position
	private int car_x = 2;
	private int car_y = 0;
	
	// intersection position
	private int cross_x = 2;
	private int cross_y = 2;
	
	// stop sign position
	private int sign_x = 2;
	private int sign_y = 1;
	
	// stoplight info
	private int light_x = 2;
	private int light_y = -1;
	private boolean light_open = false;
	
	// variable used to simulate passage of time
	// each agent has one move per turn
	// checking ahead is not considered as a move
	private int turn = 0;
	
	Random rand = new Random(); 
	
	// predicates that indicate ambient conditions to the agent
	Predicate clear_path = new Predicate("ahead_is_clear");
	Predicate no_clear_path = new Predicate("ahead_is_not_clear");
	Predicate red_light = new Predicate("red_stop_light");
	
	public Unifier executeAction(String agName, Action act) throws AILexception {
		Unifier u = new Unifier();
		
		if(act.getFunctor().equals("drive")) {
			int new_car_x = car_x;
			int new_car_y = car_y;
			
			String direction = act.getTerm(0).getFunctor();
			
			switch(direction) {

			case "north": 
				// Move up in the Y axis
				new_car_y++; 
				break;
			case "south": 
				// Move down in the Y axis
				new_car_y--;
				break;
			case "east": 
				// Move right in the X axis
				new_car_x++;
				break;
			case "west": 
				// Move left in the X axis
				new_car_x--;
				break;
			default: 
				System.err.println("DON'T MOVE");
		
			}
				
			Predicate old_position = new Predicate("at");
			old_position.addTerm(new NumberTermImpl(car_x));
			old_position.addTerm(new NumberTermImpl(car_y));
			
			Predicate at = new Predicate("at");
			at.addTerm(new NumberTermImpl(new_car_x));
			at.addTerm(new NumberTermImpl(new_car_y));
			
			System.err.println("Turn " + turn + ": MOVING " + new_car_x + " " + new_car_y);
			
			// Update position of the agent in the enviroment
			car_x = new_car_x;
			car_y = new_car_y;
			
			removePercept(agName, old_position); //remove old position
			addPercept(agName, at); //inform new position to the agent
			
			removePercept(agName, clear_path);
			
			turn++;
			System.err.println("\n");
		}
		else if(act.getFunctor().equals("check_ahead")) {
			
			if(car_y + 1 == sign_y) {
				System.err.println("Turn " + turn + ": STOP SIGN AHEAD");
				
				Predicate stop = new Predicate("must_stop");
				stop.addTerm(new NumberTermImpl(sign_x));
				stop.addTerm(new NumberTermImpl(sign_y));
				
				addPercept(agName, stop);
			}
			
			if(car_y + 1 == light_y && !light_open) {
				addPercept(agName, red_light);
				
				System.err.println("Turn " + turn + ": RED STOPLIGHT AHEAD");
			}
			
			if(car_y + 1 == 2) {
				if (rand.nextInt(2) == 1) {
					System.err.println("Turn " + turn + ": PEDESTRIAN CROSSING");
					
					addPercept(agName, no_clear_path);
				}
				else {
					System.err.println("Turn " + turn + ": NO PEDESTRIAN AT CROSSING");
					
					addPercept(agName, clear_path);
				}
			}
			else {
				System.err.println("Turn " + turn + ": PATH IS CLEAR");
				
				addPercept(agName, clear_path);
			}
		}
		else if(act.getFunctor().equals("wait")) {
			String reason = act.getTerm(0).getFunctor();
			
			System.err.println("Turn " + turn + ": WAITING FOR " + reason.toUpperCase());
			
			removePercept(agName, no_clear_path);
			removePercept(agName, clear_path);
			removePercept(agName, red_light);
			turn++;
			System.err.println("\n");
		}
		
		if(turn > 10) {light_open = true;}
		
		super.executeAction(agName, act);
		
		return u;
	}
}