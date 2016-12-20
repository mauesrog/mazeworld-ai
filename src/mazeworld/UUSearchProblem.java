package mazeworld;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import datastructures.*;

/**
 * Controller to attempt to solve the Missionaries and Cannibals problem  
 * @author Mauricio Esquivel Rogel, stub by Professor Devin Balkcom
 * @date 09/19/16
 *
 */
public abstract class UUSearchProblem {
/******************************** CONSTANTS ***********************************/
	// PROTECTED
	protected static final int INIT_MOVE = 0;
	protected static final int MOVE_NORTH = 1;
	protected static final int MOVE_SOUTH = 2;
	protected static final int MOVE_EAST = 3;
	protected static final int MOVE_WEST = 4;
	protected static final int[] POSSIBLE_DIRECTIONS = 
			new int []{ MOVE_EAST, MOVE_SOUTH, MOVE_WEST, MOVE_NORTH };
		
	// PRIVATE
		//
	
/*************************** INSTANCE VARIABLES *******************************/
	// used to store performance information about search runs;
	//  these should be updated during the process of searches;
	// see methods later in this class to update these values
	// PROTECTED
	protected int nodesExplored;
	protected int maxMemory;
	protected Duration runningTime;
	protected String searchName;
	protected ArrayList<UUSearchNode> startNode;
	protected ArrayList<ArrayList<UUSearchNode>> beliefStates;
	
	// PRIVATE
	private Instant initialTime;
	
/***************************** INNER INTERFACES *******************************/
	protected interface UUSearchNode extends KeyableObject {
		public ArrayList<UUSearchNode> getSuccessors();
		public boolean goalTest();
		public int getDepth();
		public int[] updateStateNode(final int moveId, int []totalOffsets);
		public void updateStartNode(UUSearchNode node);
		public void updateStartNodeToCurrent();
		public boolean beliefStateReached();
		public boolean[] getObstacles(int[] offset);
	}
	
/***************************** INNER CLASSES **********************************/
	private class PathCostComparator implements Comparator<UUSearchNode>{
	    @Override
	    public int compare(UUSearchNode x, UUSearchNode y)
	    {
	    	int xPathCost = x.calculateKey(), yPathCost = y.calculateKey();
	    	
	    	if (xPathCost > yPathCost) { return 1; }
	    	if (xPathCost < yPathCost) { return -1; }
	    	
	        return 0;
	    }
	}
	
/******************************* PUBLIC METHODS *******************************/
//--------------------------- breadthFirstSearch() ---------------------------//
	public List<UUSearchNode> breadthFirstSearch(){
		resetStats("Breadth-First Search");
		
		// to hold specific nodes
		UUSearchNode currentNode = this.startNode.get(0), successor = null;
		
		// if the startNode=goalNode there's no need to even begin the search
		if (currentNode.goalTest()) {
			runningTime = Duration.between(initialTime, Instant.now());
			return Arrays.asList(currentNode);
		}
		
		// all the nodes to be explored next 
		Queue<UUSearchNode> frontier = new LinkedList<UUSearchNode>(Arrays.asList(currentNode));
		
		// to avoid visiting nodes twice
		HashMap<UUSearchNode, UUSearchNode> visited = new HashMap<UUSearchNode, UUSearchNode>();
		
		// holds all the successors of a certain node
		ArrayList<UUSearchNode> successors = null;
		
		// the startNode has been visited already by definition, so save it, and update the memory/node counts 
		visited.put(currentNode, null);
		incrementNodeCount();
		updateMemory(frontier.size() + visited.size()); // space complexity is determined by the sizes of the frontier
														// and the visited hashmap
		
		// do an infinite loop until a solution is found or there are no nodes left unexplored
		while (true) {
			// check that the frontier is not empty and get the next item in the queue
			if ((currentNode = frontier.poll()) == null) { return null; }
			
			successors = currentNode.getSuccessors();			
			
			// for each unvisited node, return the node if it is the goal node or add it to the frontier to check its
			// successors
			for (int i = 0; i < successors.size(); i++) {
				successor = successors.get(i);
				
				// check that node hasn't already been visited
				if (!visited.containsKey(successor)) {
					
					// save the node as visited with a reference to its parent and add one more to the total
					// number of nodes visited
					visited.put(successor, currentNode);
					incrementNodeCount();
					
					// if the goal node has been found, update memory count and get the full path of the optimal
					// solution
					if (successor.goalTest()) {
						updateMemory(frontier.size() + visited.size());
						return backchain(successor, visited); 
					}
					
					// it wasn't the goal node, but add it to the frontier to check its successors and update the memory
					// count
					frontier.add(successor);			
					updateMemory(frontier.size() + visited.size());
				}
			}
		}
	}
	
//------------------------------- aStarSearch() ------------------------------//
	public List<UUSearchNode> aStarSearch(){
		resetStats("A* Search");
		
		if (this.beliefStates != null && !deriveStartNodes()) { return null; }
		
		FibonacciHeapNode<UUSearchNode> currentNode = 
				new FibonacciHeapNode<UUSearchNode>(this.startNode.get(0)),
				successor = null, original = null;
		
		if (currentNode.getValue().goalTest()) {
			runningTime = Duration.between(initialTime, Instant.now());
			return Arrays.asList(currentNode.getValue());
		}
		
		PriorityFibonacciHeap<UUSearchNode> frontier = new PriorityFibonacciHeap<UUSearchNode>(new PathCostComparator());
		
		// to avoid visiting nodes twice
		HashMap<FibonacciHeapNode<UUSearchNode>, FibonacciHeapNode<UUSearchNode>.FibonacciHeapNodeReferenceHandler> visited = new HashMap<FibonacciHeapNode<UUSearchNode>, FibonacciHeapNode<UUSearchNode>.FibonacciHeapNodeReferenceHandler>();
		
		// holds all the successors of a certain node
		ArrayList<UUSearchNode> successors = null;
		
		frontier.insert(currentNode);
		
		// the startNode has been visited already by definition, so save it, and update the memory/node counts
		visited.put(currentNode, currentNode.new FibonacciHeapNodeReferenceHandler(null, currentNode));
		incrementNodeCount();
		updateMemory(frontier.size() + visited.size()); // space complexity is determined by the sizes of the frontier
														// and the visited hashmap
		
		// do an infinite loop until a solution is found or there are no nodes left unexplored
		while (true) {
			// check that the frontier is not empty and get the next item in the queue
			if ((currentNode = frontier.poll()) == null) {
				runningTime = Duration.between(initialTime, Instant.now());
				return null;
			}
			
			visited.get(currentNode).originalElement = null;
			
			successors = currentNode.getValue().getSuccessors();			
			
			// for each unvisited node, return the node if it is the goal node or add it to the frontier to check its
			// successors
			for (int i = 0; i < successors.size(); i++) {
				successor = new FibonacciHeapNode<UUSearchNode>(successors.get(i));
				
				// check that node hasn't already been visited
				if (!visited.containsKey(successor)) {
					visited.put(successor, successor.new FibonacciHeapNodeReferenceHandler(currentNode, successor));
					// if the goal node has been found, update memory count and get the full path of the optimal
					// solution
					if (successor.getValue().goalTest()) {
						updateMemory(frontier.size() + visited.size());
						return backchainFibonacci(successor, visited); 
					}
					
					frontier.insert(successor);
					// save the node as visited with a reference to its parent and add one more to the total
					// number of nodes visited
					incrementNodeCount();
					
					// it wasn't the goal node, but add it to the frontier to check its successors and update the memory
					// count
					updateMemory(frontier.size() + visited.size());
				} 
				else {
					original = visited.get(successor).originalElement;
					
					if (original != null) {
						frontier.decreaseKey(original, successor.getValue().calculateKey());
					}
				}
			}
		}
	}

/**************************** PROTECTED METHODS *******************************/
//------------------------------ resetStats() --------------------------------//
	protected void resetStats(String name) {
		searchName = name;
		initialTime = Instant.now();
		nodesExplored = 0;
		maxMemory = 0;
	}

//------------------------------- printStats() -------------------------------//
	protected void printStats() {
		System.out.println("Nodes explored during last search:  " + nodesExplored);
		System.out.println("Maximum memory usage during last search " + maxMemory);
		System.out.println("Running time: " + runningTime.toMillis() / 1000.0 + "s");
	}

//------------------------- printLastUsedSearchName() ------------------------//
	protected void printLastUsedSearchName() {
		System.out.println(searchName);
	}

//----------------------------- updateMemory() -------------------------------//
	protected void updateMemory(int currentMemory) {
		maxMemory = Math.max(currentMemory, maxMemory);
	}

//-------------------------- incrementNodeCount() ----------------------------//
	protected void incrementNodeCount() {
		nodesExplored++;
	}

/***************************** PRIVATE METHODS ********************************/
//------------------------------- backchain() --------------------------------//
	private List<UUSearchNode> backchain(UUSearchNode node,
			HashMap<UUSearchNode, UUSearchNode> visited) {
		List<UUSearchNode> backchainPath = new LinkedList<UUSearchNode>();  
		
		// add goal node to the optimal solution path and get its parent
		backchainPath.add(node);
		UUSearchNode currentNode = visited.get(node);
		
		// navigate from all nodes to their parents until null is found i.e. that start node
		while (currentNode != null) {
			backchainPath.add(currentNode);
			currentNode = visited.get(currentNode);
		}
		
		// return optimal solution
		Collections.reverse(backchainPath);
		
		runningTime = Duration.between(initialTime, Instant.now());
		
		return backchainPath;
	}
	
//----------------------------- backchainFibonacci() -------------------------//
	private List<UUSearchNode> backchainFibonacci(FibonacciHeapNode<UUSearchNode> node,
			HashMap<FibonacciHeapNode<UUSearchNode>, FibonacciHeapNode<UUSearchNode>.FibonacciHeapNodeReferenceHandler> visited) {
		List<UUSearchNode> backchainPath = new LinkedList<UUSearchNode>(); 
		FibonacciHeapNode<UUSearchNode> currentNode;
		
		// add goal node to the optimal solution path and get its parent
		backchainPath.add(node.getValue());
		currentNode = visited.get(node).predecessor;
		
		// navigate from all nodes to their parents until null is found i.e. that start node
		while (currentNode != null) {
			backchainPath.add(currentNode.getValue());
			currentNode = visited.get(currentNode).predecessor;
		}
		
		// return optimal solution
		Collections.reverse(backchainPath);
		
		runningTime = Duration.between(initialTime, Instant.now());
		
		return backchainPath;
	}
	
	private boolean deriveStartNodes() {
		UUSearchNode controller = this.startNode.get(0);
		int[] initPosition = new int[]{ 0, 0 },
					currentPosition = new int[]{ 0, 0 };
		boolean[] existingWalls = controller.getObstacles(initPosition);
		
		int direction = 0, targetMove = 1,
				alternateDirection = 3, relevantPosition = 0,
				currRelevantPosition = 0,
				resultingDirection = 0,
				offsetX = 0, offsetY = 0;
		
		if (!existingWalls[0])
			currentPosition[0]++;
		
		updateMemory(this.beliefStates.size());
		incrementNodeCount();
		
		while (!controller.beliefStateReached()) {	
			updateMemory(this.beliefStates.size());
			incrementNodeCount();
			
			targetMove = direction + 1 > 3 ? 0 : direction + 1;
			alternateDirection = direction - 1 < 0 ? 3 : direction - 1;
			relevantPosition = direction % 2 == 0 ? initPosition[1] :
				initPosition[0]; 
			currRelevantPosition = direction % 2 == 0 ? currentPosition[1] :
				currentPosition[0];
			offsetX = 0;
			offsetY = 0;

			existingWalls = controller.getObstacles(currentPosition);
			
			// if the turn to targetMove was successful, i.e. if
            // currRelevantPosition is no longer equal to relevantPosition, then
            // update the turner's direction to reflect this successful turn
            // and reset the position of the avatar when the turner changed
            // directions i.e. set initPosition to
            // currentPosition
            if (relevantPosition != currRelevantPosition) {
                initPosition = currentPosition;
                direction = targetMove;
    			controller.updateStateNode(resultingDirection, currentPosition);
    			resultingDirection = POSSIBLE_DIRECTIONS[(direction + 1 < 4) ?
    	                  direction + 1 : 0];
            }

            // if the turn to targetMove hasn't been accomplished yey, i.e if
            // currRelevantPosition is still equal to relevantPosition, decide where to
            // to keep on navigating...
            else {
                // if nothing is obstructing its way, have the turner stay
                // on its current direction in order to attempt a turn
                // towards targetMove at the next position
                if (!existingWalls[direction] &&
                    (direction != 3 || currentPosition[1] != 0)
                    && (direction != 2
                    || currentPosition[0] != 0)) {        	
                	resultingDirection = POSSIBLE_DIRECTIONS[direction];
                }

                // if the turner simply can't continue with its current
                // direction, turn to alternateDirection
                else {
                    // reset turner's init_position and direction, and
                    // make the avatar reflect that too
                    initPosition = currentPosition;
                    direction = alternateDirection;
                    controller.updateStateNode(resultingDirection, currentPosition);
                    resultingDirection =
                    		POSSIBLE_DIRECTIONS[alternateDirection];
                }
            }
            
            switch(resultingDirection) {
				case MOVE_NORTH:
					offsetY++;
					break;
				case MOVE_SOUTH:
					offsetY--;
					break;
				case MOVE_EAST:
					offsetX++;
					break;
				case MOVE_WEST:
					offsetX--;
					break;
			}
            
			currentPosition[0] += offsetX;
			currentPosition[1] += offsetY;
		}
		
		updateMemory(this.beliefStates.size());
		
		controller.updateStartNode(this.beliefStates.get(0).get(0));
		
		return true;
	}
}