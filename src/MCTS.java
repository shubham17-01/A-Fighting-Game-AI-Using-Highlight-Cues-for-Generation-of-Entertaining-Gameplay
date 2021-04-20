import gameInterface.AIInterface;
import simulator.Simulator;
import structs.FrameData;
import structs.GameData;
import structs.Key;
import structs.CharacterData;
import structs.MotionData;

import enumerate.Action;
import enumerate.State;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Vector;

import commandcenter.CommandCenter;

public class MCTS implements AIInterface {

	public static final double learning_rate = 0.5;
	public static final double discount_rate = 0.9;
	private Simulator simulator;
	public static LinkedList<Act> listAction = new LinkedList<Act>();
	private Key inputKey;
	private boolean player;
	private FrameData frameData;
	private CommandCenter cc;
	private GameData gameData;
	File file;
	private FrameData simulatorAheadFrameData;
	private LinkedList<Action> myActions;
	private LinkedList<Action> oppActions;
	private CharacterData myCharacter;
	private CharacterData oppCharacter;
	private static final int FRAME_AHEAD = 14;
	private Vector<MotionData> myMotion;
	private Vector<MotionData> oppMotion;
	private Action[] actionAir;
	private Action[] actionGround;
	private Action spSkill;
	private Node rootNode;
	private int myOriginalHp;
	private int oppOriginalHp;
	private Action prevAction;
	private int prevScore;
	private String prevKey;
	public static Key oppKey;


	//closing the match after round ends
	@Override
	public void close() {
		// TODO Auto-generated method stub

		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			for (int i = 0; i < listAction.size(); i++) {
				pw.println(listAction.get(i).getKey() + " " + listAction.get(i).getString() + " " + listAction.get(i).getScore());
			}
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//choosinf character
	@Override
	public String getCharacter() {
		// TODO Auto-generated method stub
		return null;
	}

	//passing information about the frames
	@Override
	public void getInformation(FrameData fd) {
		// TODO Auto-generated method stub
		frameData = fd;
		cc.setFrameData(frameData, player);
	}


	//starts the game
	@Override
	public int initialize(GameData gd, boolean playerNumber) {
		// TODO Auto-generated method stub
		player = playerNumber;
		gameData = gd;
		inputKey = new Key();
		frameData = new FrameData();
		cc = new CommandCenter();

		file = new File("data/aiData/MCTSAI/data.txt");

		try {
			Scanner br = new Scanner(new FileReader(file));
			while (br.hasNext()) {
				String key = br.next();
				String act = br.next();
				double score = br.nextDouble();
				listAction.add(new Act(key, act, score));
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.myActions = new LinkedList<Action>();
		this.oppActions = new LinkedList<Action>();

		simulator = gameData.getSimulator();

		myOriginalHp = 0;

		oppOriginalHp = 0;

		prevScore = 0;

		prevAction = Action.JUMP;

		oppKey = new Key();
		oppKey.empty();

		actionAir =
				new Action[] {Action.AIR_GUARD, Action.AIR_A, Action.AIR_B, Action.AIR_DA, Action.AIR_DB,
						Action.AIR_FA, Action.AIR_FB, Action.AIR_UA, Action.AIR_UB, Action.AIR_D_DF_FA,
						Action.AIR_D_DF_FB, Action.AIR_F_D_DFA, Action.AIR_F_D_DFB, Action.AIR_D_DB_BA,
						Action.AIR_D_DB_BB};
		actionGround =
				new Action[] {Action.STAND_D_DB_BA, Action.BACK_STEP, Action.FORWARD_WALK, Action.DASH,
						Action.JUMP, Action.FOR_JUMP, Action.BACK_JUMP, Action.STAND_GUARD,
						Action.CROUCH_GUARD, Action.THROW_A, Action.THROW_B, Action.STAND_A, Action.STAND_B,
						Action.CROUCH_A, Action.CROUCH_B, Action.STAND_FA, Action.STAND_FB, Action.CROUCH_FA,
						Action.CROUCH_FB, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB, Action.STAND_F_D_DFA,
						Action.STAND_F_D_DFB, Action.STAND_D_DB_BB};
		spSkill = Action.STAND_D_DF_FC;

		myMotion = player ? gameData.getPlayerOneMotion() : gameData.getPlayerTwoMotion();
		oppMotion = player ? gameData.getPlayerTwoMotion() : gameData.getPlayerOneMotion();

		return 0;
	}

	//values of the input keys for the AI player
	@Override
	public Key input() {
		// TODO Auto-generated method stub
		return inputKey;
	}

	//carried out during the game
	@Override
	public void processing() {
		// TODO Auto-generated method stub
		if(!frameData.getEmptyFlag() && frameData.getRemainingTime() > 0){
			oppKey = frameData.getKeyData().getOpponentKey(!player);
			if (cc.getskillFlag()) {
				inputKey = cc.getSkillKey();
			} else {
				inputKey.empty(); 
				cc.skillCancel();

				double diffScore = getScore(frameData) - prevScore;

				String tempKey = keyToString(oppKey);

				// Preparation
				simulatorAheadFrameData = simulator.simulate(frameData, player, null, null, FRAME_AHEAD);

				myCharacter = player ? simulatorAheadFrameData.getP1() : simulatorAheadFrameData.getP2();
				oppCharacter = player ? simulatorAheadFrameData.getP2() : simulatorAheadFrameData.getP1();

				myActions = setAction(myCharacter, myMotion);
				oppActions = setAction(oppCharacter, oppMotion);
				
				// MCTS algorithm
				rootNode =
						new Node(simulatorAheadFrameData, null, myActions, oppActions, gameData, player,
								cc);
				rootNode.createNode();

				Action bestAction = rootNode.execute();
				
				processListAction(tempKey, prevAction, diffScore, bestAction, prevKey);

				cc.commandCall(bestAction.name());

				prevAction = bestAction;
				prevScore = getScore(frameData);
				prevKey = tempKey;
			}
		}
	}


	//selecting actions to be performed
	public LinkedList<Action> setAction (CharacterData c, Vector<MotionData> m){
		LinkedList<Action> res = new LinkedList<Action>();

		int energy = c.getEnergy();

		if (c.getState() == State.AIR) {
			for (int i = 0; i < actionAir.length; i++) {
				if (Math.abs(m.elementAt(Action.valueOf(actionAir[i].name()).ordinal())
						.getAttackStartAddEnergy()) <= energy) {
					res.add(actionAir[i]);
				}
			}
		} else {
			if (Math.abs(m.elementAt(Action.valueOf(spSkill.name()).ordinal())
					.getAttackStartAddEnergy()) <= energy) {
				res.add(spSkill);
			}

			for (int i = 0; i < actionGround.length; i++) {
				if (Math.abs(m.elementAt(Action.valueOf(actionGround[i].name()).ordinal())
						.getAttackStartAddEnergy()) <= energy) {
					res.add(actionGround[i]);
				}
			}
		}
		return res;
	}


	//calculating and displaying the score of the player
	public int getScore(FrameData fd) {
		return player ? (fd.getP1().hp - myOriginalHp) - (fd.getP2().hp - oppOriginalHp) : (fd
				.getP2().hp - myOriginalHp) - (fd.getP1().hp - oppOriginalHp);
	}


	//carrying out the actions
	public void processListAction(String ok, Action action, double score, Action actionNow, String prevKey){
		boolean found = false;
		boolean foundForward = false;
		for (int i = 0; i < listAction.size(); i++) {
			if (listAction.get(i).getString() == action.name()){
				if (listAction.get(i).getKey().equals(prevKey)){
					double forwardScore = 0;
					for (int j = 0; j < listAction.size(); j++){
						if (listAction.get(i).getString() == actionNow.name()){
							if (listAction.get(i).getKey().equals(ok)){
								forwardScore = listAction.get(j).getScore();
								foundForward = true;
							}
						}
					}
					if (!foundForward){
						listAction.get(i).setScore(listAction.get(i).getScore() + learning_rate * (score + discount_rate * forwardScore - listAction.get(i).getScore()));
					}
					found = true;
				}
			}
		}
		if (!found){
			listAction.add(new Act(ok, action.name(), score));
		}
	}



	public static String keyToString(Key k){
		String res = "";
		if ((k.A == false) && (k.L == false) && (k.D == false) && (k.R == false) && (k.A == false) && (k.B == false) && (k.C == false)) res += "N";
		else {
			if (k.U) res += "U";
			else if (k.L) res += "L";
			else if (k.D) res += "D";
			else if (k.R) res += "R";

			if (k.A) res += "A";
			else if (k.B) res += "B";
			else if (k.C) res += "C";
		}

		return res;
	}
}
