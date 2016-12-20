package mazeworld;

import mazeworld.UUSearchProblem.UUSearchNode;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Attempts to solve the Missionaries and Cannibals problem using breadth-first search, memoizing depth-first search,
 * path-checking depth-first search, and iterative deepening path-checking depth-first search
 * @author Mauricio Esquivel Rogel, stub by Professor Devin Balkcom
 * @date 09/19/16
 *
 */
public class MazeworldDriver {
	public static final int MAXDEPTH = 5000;
	public static List<UUSearchNode> path = null;
	public static MazeworldProblem mcProblem = null;
	
	// TESTING TOGGLE 
	public static final boolean TESTING = true;   // ACTIVATE
//	public static final boolean TESTING = false;  // DEACTIVATE
	
	public static void main(String args[]) {
		if (TESTING) {
			MazeworldTest test = new MazeworldTest();
			test.runTests();
		}
		
		
		mcProblem = new MazeworldProblem(1, 100, 100, 99, 0, false, false);
		
		path = mcProblem.breadthFirstSearch();
		mcProblem.printLastUsedSearchName();
		System.out.println("Path length:  " + path.size() + " " + path);
		mcProblem.printStats();
		System.out.println("--------");
		
		mcProblem.drawSolution(path, new Callable<Boolean>(){
			public Boolean call() {
				path = mcProblem.aStarSearch();
				mcProblem.printLastUsedSearchName();
				System.out.println("Path length:  " + path.size() + 
						" " + path);
				mcProblem.printStats();
				System.out.println("--------");
				mcProblem.drawSolution(path, new Callable<Boolean>(){
					public Boolean call() {
						/*mcProblem = new MazeworldProblem(1, 100, 100, 99, 0, true,
								false);
						path = mcProblem.aStarSearch();
						mcProblem.printLastUsedSearchName();
						System.out.println("Path length:  " + path.size() + 
								" " + path);
						mcProblem.printStats();
						System.out.println("--------");
						mcProblem.drawSolution(path, new Callable<Boolean>(){
							public Boolean call() {
								return true;
							}
								
						}, true);
//						
*/						return true;
					}
				}, false);
				
				return true;
			}
		}, false);
	}
}