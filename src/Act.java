import enumerate.Action;



//list of all actions that can be performed by the AI player
public class Act {

	private String act;
	private double score;
	private String key;

	Act(String k, String a, double s){
		key = k;
		act = a;
		score = s;
	}
	
	public String getKey(){
		return key;
	}

	public String getString(){
		return act;
	}

	public double getScore(){
		return score;
	}
	
	public void setScore(double s){
		score = s;
	}


	//contains all the actions and their variants
	public Action getAction(){

		switch(act){
		case "STAND_D_DB_BA" : return Action.STAND_D_DB_BA;
		case "BACK_STEP" : return Action.BACK_STEP;
		case "FORWARD_WALK" : return Action.FORWARD_WALK;
		case "DASH" : return Action.DASH;
		case "JUMP" : return Action.JUMP;
		case "FOR_JUMP" : return Action.FOR_JUMP;
		case "BACK_JUMP" : return Action.BACK_JUMP;
		case "STAND_GUARD" : return Action.STAND_GUARD;
		case "CROUCH_GUARD" : return Action.CROUCH_GUARD;
		case "THROW_A" : return Action.THROW_A;
		case "THROW_B" : return Action.THROW_B;
		case "STAND_A" : return Action.STAND_A;
		case "STAND_B" : return Action.STAND_B;
		case "CROUCH_A" : return Action.CROUCH_A;
		case "CROUCH_B" : return Action.CROUCH_B;
		case "STAND_FA" : return Action.STAND_FA;
		case "STAND_FB" : return Action.STAND_FB;
		case "CROUCH_FA" : return Action.CROUCH_FA;
		case "CROUCH_FB" : return Action.CROUCH_FB;
		case "STAND_D_DF_FA" : return Action.STAND_D_DF_FA;
		case "STAND_D_DF_FB" : return Action.STAND_D_DF_FB;
		case "STAND_F_D_DFA" : return Action.STAND_F_D_DFA;
		case "STAND_F_D_DFB" : return Action.STAND_F_D_DFB;
		case "STAND_D_DB_BB" : return Action.STAND_D_DB_BB;
		case "STAND_D_DF_FC" : return Action.STAND_D_DF_FC;
		case "AIR_GUARD" : return Action.AIR_GUARD;
		case "AIR_A" : return Action.AIR_A;
		case "AIR_B" : return Action.AIR_B;
		case "AIR_DA" : return Action.AIR_DA;
		case "AIR_DB" : return Action.AIR_DB;
		case "AIR_FA" : return Action.AIR_FA;
		case "AIR_FB" : return Action.AIR_FB;
		case "AIR_UA" : return Action.AIR_UA;
		case "AIR_UB" : return Action.AIR_UB;
		case "AIR_D_DF_FA" : return Action.AIR_D_DF_FA;
		case "AIR_D_DF_FB" : return Action.AIR_D_DF_FB;
		case "AIR_F_D_DFA" : return Action.AIR_F_D_DFA;
		case "AIR_F_D_DFB" : return Action.AIR_F_D_DFB;
		case "AIR_D_DB_BA" : return Action.AIR_D_DB_BA;
		case "AIR_D_DB_BB" : return Action.AIR_D_DB_BB;
		default : return Action.JUMP;
		}
	}
}