package mazeworld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;

import mazeworld.UUSearchProblem.UUSearchNode;

/**
 * A Fibonacci heap ordered by priority according to a Comparator<E> object.
 * All nodes in the same level share a doubly-linked list, and all of them 
 *
 * @author Mauricio Esquivel Rogel; Template: Prof. Devin Balkcom
 * @date Fall Term 2016
 */
public class MazeworldProblem extends UUSearchProblem {
/******************************** CONSTANTS ***********************************/
	//
	
/*************************** INSTANCE VARIABLES *******************************/
	// PUBLIC
		//
	
	// PRIVATE	
	private int totalRobots, mazeWidth, mazeHeight, goalx, goaly;
	private int[][] mazeWalls;
	private int[][][] impossibleMovesTracker;
	private boolean blindRobots, pacmanPhysics;
	private RobotNode[] robots;
	private static JPanel mazePanel;
	private static JFrame mazeFrame;
	
/***************************** INNER CLASSES **********************************/
	/**
	 * As an implementation of UUSearchNode, provides the relevant data and 
	 * methods for each state/believe state
	 *
	 * @author Mauricio Esquivel Rogel
	 * @date Fall Term 2016
	 */
	private class RobotNode implements UUSearchNode {
	//----------------------INSTANCE VARIABLES--------------------------------//
		// PUBLIC
			//
		
		// PRIVATE
		private int[] state;		
		private int depth;
		private int id; 

	//--------------------------- CONSTRUCTOR --------------------------------//
		public RobotNode(int x, int y, int d, int i) {
			state = new int[2];
			this.state[0] = x;
			this.state[1] = y;
			
			depth = d;
			id = i;
		}
		
	//----------------------------- OVERRIDES --------------------------------//
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~ getSuccessors() ~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		@Override
		public ArrayList<UUSearchNode> getSuccessors() {
			ArrayList<UUSearchNode> successors = new ArrayList<UUSearchNode>();
			int[] successor = null;
			
			if (this.state[0] + 1 < mazeWidth) {
				successor = this.state.clone();
				successor[0] += 1;				
				
				if (isSafeMove(successor)){
					successors.add(new RobotNode(successor[0], successor[1],
							this.depth + 1, this.id));
				}
			}
			
			if (this.state[0] - 1 >= 0) {
				successor = this.state.clone();
				successor[0] -= 1;				
				
				if (isSafeMove(successor)){
					successors.add(new RobotNode(successor[0], successor[1],
							this.depth + 1, this.id));
				}
			}
			
			if (this.state[1] + 1 < mazeHeight) {
				successor = this.state.clone();
				successor[1] += 1;				
				
				if (isSafeMove(successor)){
					successors.add(new RobotNode(successor[0], successor[1],
							this.depth + 1, this.id));
				}
			}
			
			if (this.state[1] - 1 >= 0) {
				successor = this.state.clone();
				successor[1] -= 1;				
				
				if (isSafeMove(successor)){
					successors.add(new RobotNode(successor[0], successor[1],
							this.depth + 1, this.id));
				}
			}
			

			return successors;
		}
		
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ goalTest() ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		@Override
		public boolean goalTest() {
			return this.state[0] == goalx && this.state[1] == goaly;
		}		

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ equals() ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		@Override
		public boolean equals(Object other) {
			return Arrays.equals(state, ((RobotNode) other).state);
		}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~ hashCode() ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		@Override
		public int hashCode() {
			return state[0] * 100 + state[1] * 10 + depth;
		}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~ toString() ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		@Override
		public String toString() {
			// simple (m,c,b) format
			return "(" + this.state[0] + "," + this.state[1] + ")";
		}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~ getDepth() ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		@Override
		public int getDepth() {
			return depth;
		}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~ calculateKey() ~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		@Override
		public int calculateKey() {
			return depth + Math.abs(goalx - this.state[0]) +
					Math.abs(goaly - this.state[1]);
		}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~ updateStateNode() ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		@Override
		public int[] updateStateNode(final int moveId, int []totalOffsets) {
			Iterator<UUSearchNode> beliefStatesIterator = 
					beliefStates.get(this.id).iterator();
			UUSearchNode currBeliefState = null;
			int x, y, offsetX = 0, offsetY = 0;
			
			switch(moveId) {
				case 1:
					offsetY++;
					break;
				case 2:
					offsetY--;
					break;
				case 3:
					offsetX++;
					break;
				case 4:
					offsetX--;
					break;
			}
			
			totalOffsets[0] += offsetX;
			totalOffsets[1] += offsetY;
			
			while (beliefStatesIterator.hasNext()) {
				currBeliefState = beliefStatesIterator.next();
				x = ((RobotNode) currBeliefState).state[0] + totalOffsets[0];
				y = ((RobotNode) currBeliefState).state[1] + totalOffsets[1];
				
				if (-1 <= x && x < mazeWidth + 1 &&
						-1 <= y && y < mazeHeight + 1) {
					if (isSafeMove(new int[]{ x, y })) {
						beliefStatesIterator.remove();
					}
				} else {
					beliefStatesIterator.remove();
				}
			}
			
			if (moveId < 3) {
				if (totalOffsets[1] < 0) {
					totalOffsets[1]++;
				} else if (totalOffsets[1] > 0) {
					totalOffsets[1]--;
				}
			} else {
				if (totalOffsets[0] < 0) {
					totalOffsets[0]++;
				} else if (totalOffsets[0] > 0) {
					totalOffsets[0]--;
				}
			}
			
			beliefStatesIterator = beliefStates.get(this.id).iterator();
			
			
			while (beliefStatesIterator.hasNext()) {
				currBeliefState = beliefStatesIterator.next();
				x = ((RobotNode) currBeliefState).state[0] + totalOffsets[0];
				y = ((RobotNode) currBeliefState).state[1] + totalOffsets[1];
				
				if (!isSafeMove(new int[]{ x, y })) {
					beliefStatesIterator.remove();
				}
			}

			return totalOffsets;
		}
		
	//~~~~~~~~~~~~~~~~~~~~~~~~~ beliefStateReached() ~~~~~~~~~~~~~~~~~~~~~~~~~//		
		@Override
		public boolean beliefStateReached() {
			return beliefStates.get(this.id).size() == 1;
		}
		
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~ updateStartNode() ~~~~~~~~~~~~~~~~~~~~~~~~~~//		
		@Override
		public void updateStartNode(UUSearchNode node) {
			startNode.set(this.id, node);
		}
		
	//~~~~~~~~~~~~~~~~~~~~~~ updateStartNodeToCurrent() ~~~~~~~~~~~~~~~~~~~~~~//		
		@Override
		public void updateStartNodeToCurrent() {
			startNode.set(this.id, new RobotNode(this.state[0], this.state[1],
					this.depth, this.id));
		}
		
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~ getObstacles() ~~~~~~~~~~~~~~~~~~~~~~~~~~~~//		
	@Override
	public boolean[] getObstacles(int[] offset) {
		boolean [] walls = new boolean[4];
		int dx = 0, dy = 0;
		
		for (int i = 0; i < 4; i++) {
			switch (POSSIBLE_DIRECTIONS[i]) {
				case MOVE_NORTH:
					dy = 1;
					break;
					
				case MOVE_SOUTH:
					dy = -1;
					break;
					
				case MOVE_EAST:
					dx = 1;
					break;
					
				case MOVE_WEST:
					dx = -1;
					break;
			}
			walls[i] = !isSafeMove(new int[]{ this.state[0] + offset[0] + dx,
					this.state[1] + offset[1] + dy});
			
			dx = 0;
			dy = 0;
		}

		return walls;
	}
		
	//------------------------- PRIVATE METHODS ------------------------------//
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~ isSafeMode() ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		/*
		 * Checks if a potential state successor is valid i.e. makes sense 
		 * according to the rules set for this Cartesian plan
		 * @param successor - potential state successor
		 * @return     true - potential state successor possible
		 * 		      false - potential state successor impossible
		 */
		private boolean isSafeMove(int[] successor) {
			return 0 <= successor[0] && successor[0] < mazeWidth &&
					0 <= successor[1] && successor[1] < mazeHeight &&
					mazeWalls[successor[0]][successor[1]] == 1;
		}
	}
	
	/**
	 * Draws the Cartesian plane for the maze of this particular problem with
	 * the walls/obstacles black and empty paths white 
	 *
	 * @author Mauricio Esquivel Rogel
	 * @date Fall Term 2016
	 */
	private class Maze extends JComponent {
	//----------------------------- CONSTANTS --------------------------------//
		// PUBLIC
			//
		
		// PRIVATE
		private static final long serialVersionUID = 1L;
		
	//----------------------- INSTANCE VARIABLES -----------------------------//
		// PUBLIC
			//
		
		// PRIVATE
		private int ratio;    // ratio of real width/height to coordinate-wise
							  		// width/height
		private int borders;  // width of borders
		
	//--------------------------- CONSTRUCTOR --------------------------------//	
		private Maze(Dimension rs) {		      
			ratio = rs.width > rs.height ? rs.width / mazeWidth :
					rs.height / mazeHeight;
			borders = 40;
		}
		
	//----------------------------- OVERRIDES --------------------------------//
	//~~~~~~~~~~~~~~~~~~~~~~~~~~ paintComponent() ~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		@Override
		public void paintComponent(Graphics g) {
			// draw robots in red
			g.setColor(Color.RED);
			for (int i = 0; i < robots.length; i++) {
				drawBot(robots[i], g);
			}
			
			// draw the maze's Cartesian plane vertical lines in black
			g.setColor(Color.BLACK);
			for (int w = 0; w < mazeWidth + 1; w++) {
				drawTransformedLine(w, 0, w, mazeHeight, g);
				
				if (w < mazeWidth) {
					for (int h = 0; h < mazeHeight; h++) {
						// draw obstacles in black as well
						if (mazeWalls[w][h] == -1) {
							drawObstacle(w, h + 1, g);
						}
					}
				}
			}
			
			// draw the maze's Cartesian plane horizontal lines in black as well
			for (int h = 0; h < mazeHeight + 1; h++) {
				drawTransformedLine(0, h, mazeWidth, h, g);
			}
		}
		
	//------------------------- PRIVATE METHODS ------------------------------//
	//~~~~~~~~~~~~~~~~~~~~~~ drawTransformedLine() ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		/*
		 * Draws an individual line from one coordinate to another, mapping
		 * coordinates to actual screen coordinates. X-coordinates are
		 * calculated by simply multiplying the original coordinate by the ratio
		 * and then adding the border width. Y-coordinates are calculated 
		 * in the same, only that the product is subtracted from the
		 * maze height times the ratio in order to invert the screen's
		 * y-axis to match that of a real Cartesian plane
		 * @param x1 - x-coordinate of first point
		 * @param y1 - y-coordinate of first point
		 * @param x2 - x-coordinate of second point
		 * @param y2 - y-coordinate of second point
		 */
		private void drawTransformedLine(float x1, float y1, float x2, float y2,
				Graphics g) {
			g.drawLine(Math.round(x1 * this.ratio) + this.borders,
					Math.round(mazeHeight * this.ratio - y1 *
							this.ratio + this.borders),
					Math.round(x2 * this.ratio) + this.borders,
					Math.round(mazeHeight * this.ratio - y2 *
							this.ratio + this.borders));
		}
		
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ drawObstacle() ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		private void drawObstacle(int x1, int y1, Graphics g) {
			g.fillRect(x1 * this.ratio + this.borders,
					mazeHeight * this.ratio - y1 * this.ratio + this.borders,
					this.ratio, this.ratio);
		}
		
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ drawBot() ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//	
		private void drawBot(RobotNode bot, Graphics g) {
			g.fillOval(this.ratio * bot.state[0] + 40 + this.ratio / 4,
					this.ratio * (mazeHeight - 1) - this.ratio * bot.state[1] +
							40 + this.ratio / 4,
					this.ratio / 2, this.ratio / 2);
		}
	}
	
	/**
	 * A Fibonacci heap ordered by priority according to a Comparator<E> object.
	 * All nodes in the same level share a doubly-linked list, and all of them 
	 *
	 * @author Implementation: Mauricio Esquivel Rogel
	 * @date Fall Term 2016
	 */
	private class MazeSolution extends JComponent {
	//----------------------------- CONSTANTS --------------------------------//
		// PUBLIC
			//
			
		// PRIVATE
		private static final long serialVersionUID = 1L;
		
	//----------------------INSTANCE VARIABLES--------------------------------//
		// PUBLIC
			//
			
		// PRIVATE
		private int ratio;
		private List<UUSearchNode> solution;
		private int borders;
		
	//--------------------------- CONSTRUCTOR --------------------------------//
		private MazeSolution(Dimension rs, List<UUSearchNode> s) {		      
			ratio = rs.width > rs.height ? rs.width / mazeWidth :
					rs.height / mazeHeight;
			borders = 40;
			solution = s;
		}
		
	//----------------------------- OVERRIDES --------------------------------//
	//~~~~~~~~~~~~~~~~~~~~~~~~~~ paintComponent() ~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		public void paintComponent(Graphics g) {
			RobotNode curr = null, next = null;
			
			g.clearRect(0, 0, getWidth(), getHeight());
			g.setFont(g.getFont().deriveFont(18.0f));
			g.drawString(searchName, 800, 100);
			g.setFont(g.getFont().deriveFont(16.0f));
			g.drawString("Nodes Explored: " + nodesExplored, 760, 250);
			g.drawString("Maximum Memory Usage: " + maxMemory, 760, 275);
			g.drawString("Solution Path Length: " + this.solution.size(), 760,
					300);
			g.drawString("Running time: " + runningTime.toMillis() /
					1000.0 + "s", 760, 325);
			
			for (int i = 0; i < this.solution.size(); i++) {
				if (i < this.solution.size() - 1) {
					curr = (RobotNode) this.solution.get(i);
					next = (RobotNode) this.solution.get(i + 1);
					
					if (searchName == "Breadth-First Search") 
						g.setColor(Color.YELLOW);
					else if (searchName == "A* Search") 
						g.setColor(Color.BLUE);
					else
						g.setColor(Color.BLUE);
					
					drawTransformedLine(curr.state[0], curr.state[1],
							next.state[0], next.state[1], g);
					
					g.setColor(Color.YELLOW);
				} else {
					g.setColor(Color.GREEN);
					drawBot(next, g);
					g.setColor(Color.YELLOW);
				}
			}
		}
		
	//------------------------- PRIVATE METHODS ------------------------------//
	//~~~~~~~~~~~~~~~~~~~~~~ drawTransformedLine() ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		private void drawTransformedLine(float x1, float y1, float x2, 
				float y2, Graphics g) {
			g.drawLine(Math.round(x1 * this.ratio) + 40 + this.ratio / 2,
					Math.round(mazeHeight * this.ratio - (y1 + 1) *
							this.ratio + this.ratio / 2 + this.borders),
					Math.round(x2 * this.ratio + 40 + this.ratio / 2),
					Math.round(mazeHeight * this.ratio - (y2 + 1) 
							* this.ratio + this.ratio / 2 + this.borders));
		}
		
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~ drawBot() ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		private void drawBot(RobotNode bot, Graphics g) {
			g.fillOval(this.ratio * bot.state[0] + 40 + this.ratio / 4,
					this.ratio * (mazeHeight - 1) - this.ratio * bot.state[1] + 
							40 + this.ratio / 4, this.ratio / 2,
					this.ratio / 2);
		}
	}

/****************************** CONSTRUCTOR ***********************************///-----------------------------------------------//
	public MazeworldProblem(int nrobots, int w, int h, int gx, 
			int gy, boolean b, boolean pP) {
		totalRobots = nrobots;
		mazeWidth = w;
		mazeHeight = h;
		goalx = gx;
		goaly = gy;
		blindRobots = b;
		pacmanPhysics = pP;
		robots = new RobotNode[totalRobots];
		startNode = new ArrayList<UUSearchNode>();
		
		if (this.blindRobots)
			beliefStates = new ArrayList<ArrayList<UUSearchNode>>();
		
		mazeWalls = loadMaze();
		mazePanel = new JPanel();
		
		if (mazeFrame == null) {
			mazeFrame = new JFrame();
			mazeFrame.setBackground(Color.WHITE);
		}
		
		if (mazePanel == null) {
			mazePanel = new JPanel();
			mazePanel.setBackground(Color.WHITE);
		}
		
		mazeFrame.setTitle(w + "x" + h + (this.pacmanPhysics ? 
				" Pacman-Physics" : "") + " Maze: " + nrobots + " " +
				(this.blindRobots ? "blind" : "non-blind") + " robot" +
						(nrobots == 1 ? "" : "s"));
		
		for (int i = 0; i < totalRobots; i++) {
			this.robots[i] = new RobotNode(0, i, 0, i);
			
			if (blindRobots) { beliefStates.add(predict(INIT_MOVE, i)); }
			
			this.startNode.add(this.robots[i]);
		} 
		
		startMaze();
	}
	
/******************************* PUBLIC METHODS *******************************/
//------------------------------ drawSolution() ------------------------------//
	public void drawSolution(List<UUSearchNode> solution, 
			Callable<Boolean> callback, boolean last) {
		Dimension realMazeSize = this.mazeHeight > this.mazeWidth ?
				new Dimension(620 / this.mazeHeight * this.mazeWidth, 620) :
				new Dimension(620, 620 / this.mazeWidth * this.mazeHeight);
				
		MazeSolution mSolution = new MazeSolution(realMazeSize, solution);
				
		mSolution.setOpaque(false);
		
	    mazePanel.add(mSolution);
	    mazeFrame.pack();
	    
	    mazePanel.repaint();
	    mazePanel.revalidate();
		
	    mazeFrame.repaint();
	    mazeFrame.revalidate();
		
		
		SwingUtilities.invokeLater(new Runnable(){
	        @Override
	        public void run(){
	            try {
	            	if (!last) {
	            		Thread.sleep(5000);
	            		
	            		if (searchName == "A* Search") {
		            		mazeFrame.remove(mazePanel);
		            		mazePanel.removeAll();
		            		
		            		mazePanel.repaint();
		            	    mazePanel.revalidate();
		            		
		            	    mazeFrame.repaint();
		            	    mazeFrame.revalidate();
		            		
		            		SwingUtilities.invokeLater(new Runnable(){
		            	        @Override
		            	        public void run(){
		            	        	try {
										callback.call();
									} catch (Exception e) {
										e.printStackTrace();
									}
		            	        }
		            		});
		            	}
	            		else { callback.call(); } 
	            	}
				} catch (Exception e) {
					e.printStackTrace();
				}
	        }
	    });
	}
	
/**************************** PRIVATE METHODS *********************************/
//----------------------------- loadMaze() -----------------------------------//
	private int[][] loadMaze() {
		boolean white = false;
		int[][] maze = new int[this.mazeWidth][this.mazeHeight];
		Random random = new Random();
		
		if (this.mazeHeight == 100 && this.mazeHeight == this.mazeWidth) {
			int offset = 18;
			
			for (int h = 0; h < this.mazeHeight; h++) {
				for (int w = 0; w < this.mazeWidth; w++) {
					white = false;
					
					if (w == this.mazeWidth - 1) {
						white = true;
					} else {
						if (h < 10 || h > this.mazeHeight - 10) {
							if (w == h || w == h + 1) {
								white = true;
							}
						} else {
							if (w < this.mazeWidth - offset) {
								switch (h) {
									case 10: ;
										if (w >= 10) {
											white = true;
										}
										break;
										
									default: ;
										if (h % 2 == 0 && w < this.mazeWidth - offset) {
											white = true;	
										} else {
											if ((h - 11) / 2 % 2 == 0) {
												if (w == this.mazeWidth - offset - 1)
													white = true;
											} else {
												if (w == 0) {
													white = true;
												}
											}
										}
								}
							} else {
								if (h == 90 && w <= this.mazeWidth - 9) {
									white = true;
								}
							}
						}
					}
					
					maze[w][h] = white ? 1 : -1;
				}
			}
		} else {
			int midpointWidth = Math.floorDiv(this.mazeWidth, 2);
			int lowerQuartileWidth = Math.floorDiv(midpointWidth, 2);
			int upperQuartileWidth = midpointWidth + lowerQuartileWidth;
			
			for (int h = 0; h < this.mazeHeight; h++) {
				for (int w = 0; w < this.mazeWidth; w++) {
					if (h == 0) {
						if (w <= lowerQuartileWidth || w >= upperQuartileWidth)
							maze[w][h] = 1;
					} else if (h < this.mazeHeight - 1) {
						if (w == lowerQuartileWidth || w == upperQuartileWidth)
							maze[w][h] = 1;
					} else {
						if (w <= upperQuartileWidth)
							maze[w][h] = 1;
					}
				}
			}
		}
		
		for (int w = 0; w < this.mazeWidth; w++) {
			for (int h = 0; h < this.mazeHeight; h++) {
				if (maze[w][h] != 1) {
					maze[w][h] = (random.nextBoolean()) ? 1 : -1;
				}
			}
		}
		
		return maze;
	}
	
//----------------------------- startMaze() ----------------------------------//
	private void startMaze() {
		Dimension realMazeSize = this.mazeHeight > this.mazeWidth ?
				new Dimension(620 / this.mazeHeight * this.mazeWidth, 620) :
				new Dimension(620, 620 / this.mazeWidth * this.mazeHeight);
				
		Dimension realSize = new Dimension(realMazeSize.width + 80 + 400,
				realMazeSize.height + 80);
		
		mazeFrame.add(mazePanel, BorderLayout.CENTER);
		
		mazeFrame.setPreferredSize(realSize);
		mazeFrame.setBounds(0, 0, realSize.width, realSize.height);
		
		mazeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mazeFrame.setLocationRelativeTo(null);
	    
		mazePanel.setPreferredSize(realMazeSize);
		
		Maze mazePanelCompontent = new Maze(realMazeSize);
		
		LayoutManager overlay = new OverlayLayout(mazePanel);
		mazePanel.setLayout(overlay);
		
		mazePanel.add(mazePanelCompontent);
		
		mazeFrame.pack();
		
		mazeFrame.setVisible(true);
		mazePanel.setVisible(true);
	}
	
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ predict() ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
	/*
	 * Checks if a potential state successor is valid i.e. makes sense 
	 * according to the rules set for this Cartesian plan
	 * @param successor - potential state successor
	 * @return     true - potential state successor possible
	 * 		      false - potential state successor impossible
	 */
	private ArrayList<UUSearchNode> predict(final int action, int id) {
		ArrayList<UUSearchNode> beliefStates = new ArrayList<UUSearchNode>();
		this.impossibleMovesTracker = 
				new int[4][this.mazeWidth][this.mazeHeight];
		
		if (action == INIT_MOVE) {
			for (int w = 0; w < mazeWidth; w++) {
				for (int h = 0; h < mazeHeight; h++) {
					if (mazeWalls[w][h] == 1)
						beliefStates.add(new RobotNode(w, h, 0, id));
					else {
						if (w + 1 < mazeWidth && mazeWalls[w+1][h] == 1)
							this.impossibleMovesTracker[MOVE_WEST - 1][w+1][h]
									= 1;
						if (w - 1 >= 0 && mazeWalls[w-1][h] == 1)
							this.impossibleMovesTracker[MOVE_EAST - 1][w-1][h]
									= 1;
						if (h + 1 < mazeHeight && mazeWalls[w][h+1] == 1)
							this.impossibleMovesTracker[MOVE_SOUTH - 1][w][h+1]
									= 1;
						if (h - 1 >= 0 && mazeWalls[w][h-1] == 1)
							this.impossibleMovesTracker[MOVE_NORTH - 1][w][h-1]
									= 1;
					}
				}
			}
		}
		
		return beliefStates;
	}
	
//~~~~~~~~~~~~~~~~~~~~~~~~~~ printImpossibleMoves() ~~~~~~~~~~~~~~~~~~~~~~~~~~//
		/*
		 * Checks if a potential state successor is valid i.e. makes sense 
		 * according to the rules set for this Cartesian plan
		 * @param successor - potential state successor
		 * @return     true - potential state successor possible
		 * 		      false - potential state successor impossible
		 */
	public void printImpossibleMoves() {
		String impossibleMoves;
		
		for (int move = 0; move < 4; move++) {
			impossibleMoves = "";
			
			switch(move) {
				case 0:
					System.out.println("NORTH");
					break;
				
				case 1:
					System.out.println("\nSOUTH");
					break;
				
				case 2:
					System.out.println("\nEAST");
					break;
					
				default:
					System.out.println("\nWEST");
			}
			
			for (int wi = 0; wi < mazeWidth; wi++) {
				for (int he = 0; he < mazeHeight; he++) {
					if (impossibleMovesTracker[move][wi][he] == 1) {
						impossibleMoves += "("+wi+","+he+")";
					}
						
				}
			}
			
			System.out.println(impossibleMoves);
		}
	}
}