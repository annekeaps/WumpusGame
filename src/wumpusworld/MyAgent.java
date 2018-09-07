package wumpusworld;

import java.util.Arrays;
import java.util.Random;

/**
 * Contains starting code for creating your own Wumpus World agent.
 * Currently the agent only make a random decision each turn.
 * 
 * @author Johan Hagelbäck
 */
public class MyAgent implements Agent
{
    private World w;
    private int rnd;
    private String current_state;
    private int direction;
    //public QTable q;
    QTable q = new QTable();
    
    // col1 : state, col2: action, col3: q value, col4: N (how many (state,action) combination has been used)
    //String[][] Q = new String[80][4];
       
    /**
     * Creates a new instance of your solver agent.
     * 
     * @param world Current world state 
     */
    public MyAgent(World world)
    {
        w = world;   
    }
    
    public MyAgent(World world, String [][] qTable) {
        w = world;
        q.Q = qTable;
    }
    
    public MyAgent(World world,  QTable qTable) {
        
    }
            
    /**
     * Asks your solver agent to execute an action.
     */

    @Override
    public void doAction()
    {
        //percept = new String[percept.length];
        
        //qTable();
        // percept {breeze, stench, pit, wumpus, glitter, gold}
        String[] percept = {World.UNKNOWN, World.UNKNOWN, World.UNKNOWN, World.UNKNOWN, World.UNKNOWN, World.UNKNOWN};
        
        //Location of the player
        int cX = w.getPlayerX(); System.out.println("x: "+cX);
        int cY = w.getPlayerY(); System.out.println("y: "+cY);        
        
        //Basic action:
        //Grab Gold if we can.
        if (w.hasGlitter(cX, cY))
        {
            percept[4] = World.GLITTER;
            percept[5] = World.GOLD;
            w.doAction(World.A_GRAB);
            System.out.println("Has Gold!");
            //return;
        }
        
        //Basic action:
        //We are in a pit. Climb up.
        if (w.isInPit())
        {
            w.doAction(World.A_CLIMB);
            System.out.println("I'm climbing up!");
            //return;
        }
        
        //There is a wumpus in this state.
        if (w.hasWumpus(cX, cY))
        {
            System.out.print("Has wumpus!");
            percept[3] = World.WUMPUS;        
        }
        
        //Test the environment
        if (w.hasBreeze(cX, cY))
        {
            percept[0] = World.BREEZE;
            System.out.println("I am in a Breeze");
        }
        if (w.hasStench(cX, cY))
        {
            percept[1] = World.STENCH;
            System.out.println("I am in a Stench");
        }
        if (w.hasPit(cX, cY))
        {
            percept[2] = World.PIT;
            System.out.println("I am in a Pit");
        }
        if (w.getDirection() == World.DIR_RIGHT)
        {
            direction = World.DIR_RIGHT;
            System.out.println("I am facing Right");
        }
        if (w.getDirection() == World.DIR_LEFT)
        {
            direction = World.DIR_LEFT;
            System.out.println("I am facing Left");
        }
        if (w.getDirection() == World.DIR_UP)
        {
            direction = World.DIR_UP;
            System.out.println("I am facing Up");
        }
        if (w.getDirection() == World.DIR_DOWN)
        {
            direction = World.DIR_DOWN;
            System.out.println("I am facing Down");
        }
        
        //decide next move
        String act = qLearningAgent2(percept, cX, cY, direction);
        if ("LL".equals(act)) {                  // if the action is to turn 180 degrees
            w.doAction(World.A_TURN_LEFT);
            w.doAction(World.A_TURN_LEFT);
            w.doAction(World.A_MOVE);
        } else if (act.equals(World.A_MOVE)) {            // if the action is only to move
            w.doAction(act);
        } else if ((act.substring(act.length()-1,act.length()).equals(World.A_SHOOT))) {
            System.out.println("I'M SHOOTING THE WUMPUS! WOHOO!!");
            if (act.equals("LL"+World.A_SHOOT)) {
                System.out.println("left left shoot!");
                w.doAction(World.A_TURN_LEFT);
                w.doAction(World.A_TURN_LEFT);
                w.doAction(World.A_SHOOT);
                w.doAction(World.A_MOVE);
            } else if (act.equals(World.A_MOVE+World.A_SHOOT)) {
                System.out.println("shoot move!");
                w.doAction(World.A_SHOOT);
                w.doAction(World.A_MOVE);
            } else {
                System.out.println("anything then shoot and move!");
                w.doAction(act.substring(0,1));
                System.out.println(act.substring(0,1));
                w.doAction(World.A_SHOOT);
                w.doAction(World.A_MOVE);
            }
        } else {                            // if the action is to turn left or right
            w.doAction(act);
            w.doAction(World.A_MOVE);
        }        
    }    
    
     /**
     * Generates a random instruction for the Agent.
     * @return 
     */
    /*public int decideRandomMove()
    {
      return (int)(Math.random() * 4);
    }*/
    
    /**
     * Q Learning Method.
     * @param percept
     * @param cX
     * @param cY
     * @param direction
     * @return 
     */
    public String qLearningAgent2(String[] percept, int cX, int cY, int direction){
        /*Get and name the current state based on current position
            13 14 15 16
            9  10 11 12
            5  6  7  8
            1  2  3  4 
        */        
        current_state = nameState(cX,cY); System.out.println("current state: "+current_state);
        int reward = rewardValue(percept); System.out.println("reward: "+reward);
        
        //update Q value if there is wumpus
        if (w.hasWumpus(cX, cY)) {
            for (int id=0; id<4; id++) {
                q.Q[((Integer.parseInt(current_state)-1)*4)+id][2] = Integer.toString(reward);
                //System.out.print("wumpus: "+q.Q[((Integer.parseInt(current_state)-1)*4)+id][2]+ " ");
            }
            //System.out.println();
            return "wumpus";
        } else {                
            // Determine possible state to move to
            //next_state = [0]up, [1]right, [2]down, [3]left
            String [] next_state = possibleMove(cX, cY);
            int temp = 0, idx = 0, max = 0;
            double alpha = 0.5, gamma = 0.5;
            boolean first = true;
            //q_idx : index of Q max
            String [] q_idx = {};
            
            System.out.print("next state: ");
            for(int j=0; j<4;j++){
                System.out.print(next_state[j]+" ");
            }
            
            // Fill in Q values for various possible state-action series
            System.out.print("\nq: ");
            for (idx=0; idx<4; idx++) {                
                if (next_state[idx]!=null) {
                    temp = Integer.parseInt(q.Q[((Integer.parseInt(current_state)-1)*4)+idx][2]);                
                    //find Q max for current_state
                    if (first == true){
                        max = temp;       
                        //create new array
                        q_idx = new String [1];
                        q_idx[0] = Integer.toString(idx);
                        first = false;
                    } else {
                        if (temp > max) {
                            max = temp;
                            //remove and create new array
                            q_idx = new String [1];
                            q_idx[0] = Integer.toString(idx);
                        } else if(temp == max){
                            //append array
                            q_idx = Arrays.copyOf(q_idx, q_idx.length+1);
                            q_idx[q_idx.length-1] = Integer.toString(idx);
                        }
                    }
                    
                    //Update Q Value (in the third column)
                    temp += alpha * (reward + (gamma * maxQ(next_state[idx])) - temp);
                    q.Q[((Integer.parseInt(current_state)-1)*4)+idx][2] = Integer.toString(temp);
                }
                System.out.print(q.Q[((Integer.parseInt(current_state)-1)*4)+idx][2]+" ");
            }
            
            // Determine the next action            
            String next_action = null;
            
            System.out.print("\nq_idx: ");
            for(int j=0; j<q_idx.length;j++){
                System.out.print(q_idx[j]);
            }
            System.out.println();
            
            // select an action
            if (q_idx.length > 1) {
                // exploration
                System.out.println("IT'S EXPLORATION!");                
                idx = new Random().nextInt(q_idx.length);   // random the steps first           
                
                if (w.hasStench(cX, cY)) { // if has stench
                    System.out.println("HAS STENCH!");
                    temp = Integer.parseInt(q.Q[((Integer.parseInt(current_state)-1)*4)+Integer.parseInt(q_idx[idx])][2]);
                    System.out.println("q wumpus: "+temp);
                    if (temp < -100) { //thresholding if there is gold and wumpus and/or pit at the same cell
                        System.out.println("act wumpus: "+getAct(q_idx[idx], direction)+World.A_SHOOT);
                        next_action = getAct(q_idx[idx], direction) + World.A_SHOOT;                        
                    } 
                    else if (temp > -100) {
                        System.out.println("act no wumpus: "+getAct(q_idx[idx], direction));
                        next_action = getAct(q_idx[idx], direction);
                    }
                } else { // if it doesn't has stench then just do 
                    System.out.println("act: " + getAct(q_idx[idx], direction)); 
                    next_action = getAct(q_idx[idx], direction);
                }
                
            } else {
                // exploitation
                System.out.println("IT'S EXPLOITATION!");
                
                if (w.hasStench(cX, cY)) {
                    System.out.println("HAS STENCH!");
                    temp = Integer.parseInt(q.Q[((Integer.parseInt(current_state)-1)*4)+Integer.parseInt(q_idx[0])][2]);
                    System.out.println("q wumpus: "+temp);
                    if (temp < -100) { //thresholding if there is gold and wumpus and/or pit at the same cell
                        System.out.println("act wumpus: "+getAct(q_idx[0], direction)+World.A_SHOOT);
                        next_action = getAct(q_idx[0], direction) + World.A_SHOOT;                        
                    } 
                    else if (temp > -100) {
                        System.out.println("act no wumpus: "+getAct(q_idx[0], direction));
                        next_action = getAct(q_idx[0], direction);
                    }                    
                } else { // if it doesn't has stench then just do
                    System.out.println("act: " + getAct(q_idx[0], direction));
                    next_action = getAct(q_idx[0], direction);
                }                
            }
            System.out.println("next action: "+next_action);
            return next_action;
        }
    }
           
    public String [] possibleMove(int cX, int cY){
        // Determine possible state to move to
        //next_state = [0]up, [1]right, [2]down, [3]left
        String [] next_state = new String[4];

        //giving next state's number just for the possible next state
        //giving 'null' value for impossible next state
        if ((cY+1)<=4) { next_state[0] = nameState(cX,cY+1); }
        if ((cX+1)<=4) { next_state[1] = nameState(cX+1,cY); }
        if ((cY-1)>0) { next_state[2] = nameState(cX,cY-1); }
        if ((cX-1)>0) { next_state[3] = nameState(cX-1,cY); }
        
        return next_state;
    }
    
    public String getAct(String idx, int direction){
        //action for state (turn right, turn left, move, or (LL) turn 180 degrees)
        String action = null;
                
        switch (idx) {            
            case "0":
                if (direction == World.DIR_UP) { action = World.A_MOVE; }
                else if (direction == World.DIR_RIGHT) { action = World.A_TURN_LEFT; }
                else if (direction == World.DIR_DOWN) { action = "LL"; }
                else if (direction == World.DIR_LEFT) { action = World.A_TURN_RIGHT; }
                break;
            case "1":
                if (direction == World.DIR_UP) { action = World.A_TURN_RIGHT; }
                else if (direction == World.DIR_RIGHT) { action = World.A_MOVE; }
                else if (direction == World.DIR_DOWN) { action = World.A_TURN_LEFT; }
                else if (direction == World.DIR_LEFT) { action = "LL"; }
                break;
            case "2":
                if (direction == World.DIR_UP) { action = "LL"; }
                else if (direction == World.DIR_RIGHT) { action = World.A_TURN_RIGHT; }
                else if (direction == World.DIR_DOWN) { action = World.A_MOVE; }
                else if (direction == World.DIR_LEFT) { action = World.A_TURN_LEFT; }
                break;
            case "3":
                if (direction == World.DIR_UP) { action = World.A_TURN_LEFT; }
                else if (direction == World.DIR_RIGHT) { action = "LL"; }
                else if (direction == World.DIR_DOWN) { action = World.A_TURN_RIGHT; }
                else if (direction == World.DIR_LEFT) { action = World.A_MOVE; }
                break;
        }
        return action;
    }
    
    public String nameState(int x, int y) {
        /*Get and Name the current state based on current position
            13 14 15 16
            9  10 11 12
            5  6  7  8
            1  2  3  4 
        */ 
        String state = Integer.toString(x+((y-1)*4));
        return state;
    }
        
    public String getX(String state){        
        int x = Integer.parseInt(state) % 4;
        if(x == 0) {
            x = Integer.parseInt(state)/4;
        }
        return Integer.toString(x);
    }
    
    public String getY(String state){
        int y = (Integer.parseInt(state) / 4) + 1;
        return Integer.toString(y);
    }
    
    /**
     * Find maximum of Q in a state
     * @param state
     * @return 
     */
    public int maxQ(String state){
        String [] next_state = possibleMove(Integer.parseInt(getX(state)), Integer.parseInt(getY(state)));
                
        //System.out.println("\nns: "+state);
        int temp, max = 0;
        boolean first = true;
        /*
        System.out.print("next state: ");
        for (int i=0; i<4; i++){
            System.out.print(next_state[i]);
        }
                
        System.out.print("\nmax: ");
        */
        for (int i=0; i<4; i++){            
            if (next_state[i] != null){
                //System.out.print("\nq ns: ");
                if (first) {
                    max = Integer.parseInt(q.Q[(Integer.parseInt(state)-1)*4+i][2]);
                    //System.out.print(max+" ");
                    first = false;
                } else {
                    temp = Integer.parseInt(q.Q[((Integer.parseInt(state)-1)*4)+i][2]); 
                    if (temp > max) {max = temp; }
                }
            }
        }          
        //System.out.println("\nmaximum: "+max);        
        return max;
    }
    
    /**
     * R value
     * @param percept
     * @return 
     */
    public int rewardValue(String [] percept) {
        int rval = 0;        
        System.out.print("percept: ");
        for(int i=0; i<percept.length; i++){
            System.out.print(percept[i]);
            switch(percept[i]) {
                case World.BREEZE   : rval += -1; break;
                case World.STENCH   : rval += -1; break;
                case World.PIT      : rval += -1000; break;
                case World.WUMPUS   : rval += -1000; break;
                case World.GLITTER  : rval += +1; break;
                case World.GOLD     : rval += +1000; break;
                case World.UNKNOWN  : rval += 0;
            }
        }
        System.out.println();
        return rval;
    }

}