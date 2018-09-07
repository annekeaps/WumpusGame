/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wumpusworld;

/**
 *
 * @author Angellica
 */
public class QTable {
    
    // col1 : state, col2: action, col3: q value, col4: N (how many (state,action) combination has been used)
    String[][] Q = new String[64][4];
    //action (table Q in column 2) = {up, right, down, left}    
    String [] action = {"up", "right", "down", "left"};
    int val = 0;
    int state = 1;
    
    public QTable() {
        for (int i=0; i<Q.length; i++){
            if (val == 3) {
                Q[i][1] = action[val];
                val = 0;
                state++;
            }
            else {
                //the second column of Q table is the action possibilities in each state
                Q[i][1] = action[val];
                val++;
            }
            /*the first column of Q table is the state (1 - 16)
                13 14 15 16
                9  10 11 12
                5  6  7  8
                1  2  3  4 
            */
            Q[i][0] = Integer.toString(state);
            Q[i][2] = "0";
            Q[i][3] = "0"; 
            
            //System.out.println(Q[i][1]);
        }
    }
}