package wumpusworld;

import java.util.Vector;
/**
 * Starting class for the Wumpus World program. The program
 * has three options: 1) Run a GUI where the Wumpus World can be
 * solved step by step manually or by an agent, or 2) run
 * a simulation with random worlds over a number of games,
 * or 3) run a simulation over the worlds read from a map file.
 * 
 * @author Johan Hagelbäck
 */
public class WumpusWorld {
    
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        WumpusWorld ww = new WumpusWorld();
    }
    
    /**
     * Starts the program.
     * 
     */
    public WumpusWorld()
    {
        String option = Config.getOption();
        
        if (option.equalsIgnoreCase("gui"))
        {
            showGUI();
        }
        if (option.equalsIgnoreCase("sim"))
        {
            runSimulator();
        }
        if (option.equalsIgnoreCase("simdb"))
        {
            runSimulatorDB();
        }
    }
    
    /**
     * Starts the program in GUI mode.
     */
    private void showGUI()
    {
        GUI g = new GUI();
    }
    
    /**
     * Starts the program in simulator mode with
     * maps read from a data file.
     */
    private void runSimulatorDB()
    {
        MapReader mr = new MapReader();
        Vector<WorldMap> maps = mr.readMaps();
        
        double totScore = 0;
        for (int i = 0; i < maps.size(); i++)
        {
            World w = maps.get(i).generateWorld();
            totScore += (double)runSimulation(w);
        }
        totScore = totScore / (double)maps.size();
        System.out.println("Average score: " + totScore);
    }
    
    /**
     * Starts the program in simulator mode
     * with random maps.
     */
    private void runSimulator()
    {
        double totScore = 0;
        for (int i = 0; i < 10; i++)
        {
            WorldMap w = MapGenerator.getRandomMap(i);
            totScore += (double)runSimulation(w.generateWorld());
        }
        totScore = totScore / (double)10;
        System.out.println("Average score: " + totScore);
    }
    
    // this is for obtaining the Q value so that the Q is not reseting to 0 after new game
    String[][] myQTab = new String[64][4];
    QTable myqtab = new QTable();
    
    /**
     * Runs the solver agent for the specified Wumpus
     * World.
     * 
     * @param w Wumpus World
     * @return Achieved score
     */
    private int runSimulation(World w)
    {
        int score = 0;
        int actions = 0;
        this.myQTab = myqtab.Q;
        Agent a = new MyAgent(w, this.myQTab);
        while (!w.gameOver())
        {
            a.doAction();
            this.myQTab = myqtab.Q;

            actions++;
        }
        if (w.gameOver()) {
            a.doAction();
            this.myQTab = myqtab.Q;
        }
        score = w.getScore();
        System.out.println("Simulation ended after " + actions + " actions. Score " + score);

        return score;
    }
}
