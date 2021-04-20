import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;

import jdk.tools.jmod.Main;
import simulator.Simulator;
import structs.CharacterData;
import structs.FrameData;
import structs.GameData;

import commandcenter.CommandCenter;

import enumerate.Action;


public class Node {


	public static final int EXEC_TIME = 165 * 100000;
	public static final int MCTS_TREE_DEPTH = 2;
	public static final int CREATE_NODE_THRESHOULD = 10;
	public static final int SIMULATION_TIME = 60;
	private Random rnd;
	private Node parent;
	private Node[] children;
	private int depth;
	private double val;
	private double visit;
	private LinkedList<Action> myActions;
	private LinkedList<Action> oppActions;
	private Simulator simulator;
	private LinkedList<Action> selectedMyActions;
	private int myOriginalHp;
	private int oppOriginalHp;
	private FrameData frameData;
	private boolean playerNumber;
	private CommandCenter commandCenter;
	private GameData gameData;
	Deque<Action> mAction;
	Deque<Action> oppAction;


	//choosing the actions to be played in each node
	public Node(FrameData frameData, Node parent, LinkedList<Action> myActions,
			LinkedList<Action> oppActions, GameData gameData, boolean playerNumber,
			CommandCenter commandCenter, LinkedList<Action> selectedMyActions) {
		this(frameData, parent, myActions, oppActions, gameData, playerNumber, commandCenter);

		this.selectedMyActions = selectedMyActions;
	}


	//defining the nodes
	public Node(FrameData frameData, Node parent, LinkedList<Action> myActions,
			LinkedList<Action> oppActions, GameData gameData, boolean playerNumber,
			CommandCenter commandCenter) {
		this.frameData = frameData;
		this.parent = parent;
		this.myActions = myActions;
		this.oppActions = oppActions;
		this.gameData = gameData;
		this.simulator = new Simulator(gameData);
		this.playerNumber = playerNumber;
		this.commandCenter = commandCenter;

		this.selectedMyActions = new LinkedList<Action>();

		this.rnd = new Random();
		this.mAction = new LinkedList<Action>();
		this.oppAction = new LinkedList<Action>();

		CharacterData myCharacter = playerNumber ? frameData.getP1() : frameData.getP2();
		CharacterData oppCharacter = playerNumber ? frameData.getP2() : frameData.getP1();
		myOriginalHp = myCharacter.getHp();
		oppOriginalHp = oppCharacter.getHp();

		if (this.parent != null) {
			this.depth = this.parent.depth + 1;
		} else {
			this.depth = 0;
		}
	}


	//exploitation phase
	public Action execute(){
		long start = System.nanoTime();
		while(System.nanoTime() - start <= EXEC_TIME){
			djikstra();
		}

		return getBestVisitAction();
	}


	//exploration phase
	public double playout() {
		mAction.clear();
		oppAction.clear();

		for (int i = 0; i < selectedMyActions.size(); i++) {
			mAction.add(selectedMyActions.get(i));
		}

		for (int i = 0; i < 5 - selectedMyActions.size(); i++) {
			mAction.add(myActions.get(rnd.nextInt(myActions.size())));
		}

		for (int i = 0; i < 5; i++) {
			oppAction.add(oppActions.get(rnd.nextInt(oppActions.size())));
		}

		FrameData nFrameData =
				simulator.simulate(frameData, playerNumber, mAction, oppAction, SIMULATION_TIME); // Perform simulation

		return getScore(nFrameData);
	}

	

	//monte carlo tree search method
	public double MCTS(){
		Node selectedNode = null;

		double bestVal = -9999;

		for (Node child : this.children) {
			if (child.visit == 0){
				child.val = 0;
				for (Act a : Main.listAction){
					for (Action act : child.selectedMyActions){
						if (a.getAction() == act && a.getKey().equals(Main.keyToString(Main.oppKey))){
							child.val += a.getScore();
						}
					}
				}
			}

			if (bestVal < child.val) {
				selectedNode = child;
				bestVal = child.val;
			}
		}

		double score = 0;
		if (selectedNode.visit == 0) {
			score = selectedNode.playout();
		} else {
			if (selectedNode.children == null) {
				if (selectedNode.depth < MCTS_TREE_DEPTH) {
					if (CREATE_NODE_THRESHOULD <= selectedNode.visit) {
						selectedNode.createNode();
						score = selectedNode.djikstra();
					} else {
						score = selectedNode.playout();
					}
				} else {
					score = selectedNode.playout();
				}
			} else {
				if (selectedNode.depth < MCTS_TREE_DEPTH) {
					score = selectedNode.djikstra();
				} else {
					selectedNode.playout();
				}
			}

		}

		selectedNode.visit++;
		selectedNode.val += score;

		if (depth == 0) {
			visit++;
		}

		return score;
	}

	//node creation
	public void createNode() {

		this.children = new Node[myActions.size()];

		for (int i = 0; i < children.length; i++) {

			LinkedList<Action> my = new LinkedList<Action>();
			for (Action act : selectedMyActions) {
				my.add(act);
			}

			my.add(myActions.get(i));

			children[i] =
					new Node(frameData, this, myActions, oppActions, gameData, playerNumber, commandCenter, my);
		}	  
	}


	//finding out the best possible action at the end of exploration phase
	public Action getBestVisitAction() {

		int selected = -1;
		double bestGames = -9999;

		for (int i = 0; i < children.length; i++) {

			if (bestGames < children[i].val) {
				bestGames = children[i].val;
				selected = i;
			}
		}

		return this.myActions.get(selected);
	}

	//calcutlation of the score from mcts
	public int getScore(FrameData fd) {
		return playerNumber ? (fd.getP1().hp - myOriginalHp) - (fd.getP2().hp - oppOriginalHp) : (fd.getP2().hp - myOriginalHp) - (fd.getP1().hp - oppOriginalHp);
	}


}
