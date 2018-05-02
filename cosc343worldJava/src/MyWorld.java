import cosc343.assig2.World;
import cosc343.assig2.Creature;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.*;


/**
 * The MyWorld extends the cosc343 assignment 2 World.  Here you can set
 * some variables that control the simulations and override functions that
 * generate populations of creatures that the World requires for its
 * simulations.
 *
 * @author
 * @version 1.0
 * @since 2017-04-05
 */
public class MyWorld extends World {

    /* Here you can specify the number of turns in each simulation
     * and the number of generations that the genetic algorithm will
     * execute.
    */
    private final int _numTurns = 100;
    private final int _numGenerations = 500;
    private double badGenerationMutChance = 0.2;
    private double modelAverageLifeTime = 0;
    private int generationCount = 0;
    static DefaultCategoryDataset dataset = new DefaultCategoryDataset( );

    /* Constructor.

       Input: griSize - the size of the world
              windowWidth - the width (in pixels) of the visualisation window
              windowHeight - the height (in pixels) of the visualisation window
              repeatableMode - if set to true, every simulation in each
                               generation will start from the same state
              perceptFormat - format of the percepts to use: choice of 1, 2, or 3
    */
    public MyWorld(int gridSize, int windowWidth, int windowHeight, boolean repeatableMode, int perceptFormat) {
        // Initialise the parent class - don't remove this
        super(gridSize, windowWidth, windowHeight, repeatableMode, perceptFormat);

        // Set the number of turns and generations
        this.setNumTurns(_numTurns);
        this.setNumGenerations(_numGenerations);


    }

    /* The main function for the MyWorld application

    */
    public static void main(String[] args) {
        // Here you can specify the grid size, window size and whether torun
        // in repeatable mode or not
        int gridSize = 24;
        int windowWidth = 1200;
        int windowHeight = 600;
        boolean repeatableMode = false;

      /* Here you can specify percept format to use - there are three to
         chose from: 1, 2, 3.  Refer to the Assignment2 instructions for
         explanation of the three percept formats.
      */
        int perceptFormat = 2;

        // Instantiate MyWorld object.  The rest of the application is driven
        // from the window that will be displayed.
        MyWorld sim = new MyWorld(gridSize, windowWidth, windowHeight, repeatableMode, perceptFormat);
    }


    /* The MyWorld class must override this function, which is
       used to fetch a population of creatures at the beginning of the
       first simulation.  This is the place where you need to  generate
       a set of creatures with random behaviours.

       Input: numCreatures - this variable will tell you how many creatures
                             the world is expecting

       Returns: An array of MyCreature objects - the World will expect numCreatures
                elements in that array
    */
    @Override
    public MyCreature[] firstGeneration(int numCreatures) {

        int numPercepts = this.expectedNumberofPercepts();
        int numActions = this.expectedNumberofActions();

        // This is just an example code.  You may replace this code with
        // your own that initialises an array of size numCreatures and creates
        // a population of your creatures
        MyCreature[] population = new MyCreature[numCreatures];
        for (int i = 0; i < numCreatures; i++) {
            population[i] = new MyCreature(numPercepts, numActions);
        }
        return population;
    }

    /* The MyWorld class must override this function, which is
       used to fetch the next generation of the creatures.  This World will
       proivde you with the old_generation of creatures, from which you can
       extract information relating to how they did in the previous simulation...
       and use them as parents for the new generation.

       Input: old_population_btc - the generation of old creatures before type casting.
                                The World doesn't know about MyCreature type, only
                                its parent type Creature, so you will have to
                                typecast to MyCreatures.  These creatures
                                have been simulated over and their state
                                can be queried to compute their fitness
              numCreatures - the number of elements in the old_population_btc
                             array


    Returns: An array of MyCreature objects - the World will expect numCreatures
             elements in that array.  This is the new population that will be
             use for the next simulation.
    */
    @Override
    public MyCreature[] nextGeneration(Creature[] old_population_btc, int numCreatures) {
        // Typcast old_population of Creatures to array of MyCreatures
        MyCreature[] old_population = (MyCreature[]) old_population_btc;
        // Create a new array for the new population
        MyCreature[] new_population = new MyCreature[numCreatures];

        // Here is how you can get information about old creatures and how
        // well they did in the simulation
        float avgLifeTime = 0f;
        int nSurvivors = 0;
        int count = 0;
        double totalFitness = 0;
        for (MyCreature creature : old_population) {
            // The energy of the creature.  This is zero if creature starved to
            // death, non-negative oterhwise.  If this number is zero, but the
            // creature is dead, then this number gives the enrgy of the creature
            // at the time of death.
            int energy = creature.getEnergy();

            // This querry can tell you if the creature died during simulation
            // or not.
            boolean dead = creature.isDead();

            if (dead) {
                // If the creature died during simulation, you can determine
                // its time of death (in turns)
                int timeOfDeath = creature.timeOfDeath();
                avgLifeTime += (float) timeOfDeath;
                if (timeOfDeath > (_numTurns/2)) {
                    creature.fitness = (energy + timeOfDeath);
                } else {
                    creature.fitness = timeOfDeath*0.5;
                }
            } else {
                nSurvivors += 1;
                avgLifeTime += (float) _numTurns;

                if(creature.survivedOneRound){
                    creature.fitness = (energy + _numTurns)*3;
                    new_population[count] = creature;
                    new_population[count].survivedOneRound = true;
                    count++;
                }else{
                    creature.fitness = (energy + _numTurns)*2;
                    double elitismChance = Math.random();
                    if(nSurvivors <= 2){
                        new_population[count] = creature;
                        new_population[count].survivedOneRound = true;
                        count++;
                    }else if(elitismChance < 0.3){
                        new_population[count] = creature;
                        new_population[count].survivedOneRound = true;
                        count++;
                    }
                }
            }
            totalFitness += creature.fitness;
        }
        avgLifeTime /= (float) numCreatures;

        for (MyCreature creature : old_population) {
            creature.probability = (creature.fitness / totalFitness) * 100;
        }

        // Increase mutation chance if this generation is bad
        double mutationChance;
        if(nSurvivors == 0 && avgLifeTime<45){
            mutationChance = badGenerationMutChance;
            if(badGenerationMutChance <= 0.5){
                badGenerationMutChance += 0.01;
            }
        }else{
            mutationChance = 0.05;
        }

        // produce children
        while(count <= numCreatures-2){
            int [] parentA = new int [9];
            int [] parentB = new int [9];
            int [] child = new int [9];

            double offset = 0;
            Random rand = new Random();
            double randomDouble = rand.nextDouble() * 101;
            for (MyCreature creature : old_population) {
                offset += creature.probability;
                if (randomDouble < offset) {
                    //System.out.println("picked: " + creature.fitness);
                    parentA = creature.chromosome.clone();
                    break;
                }
            }

            offset = 0;
            randomDouble = rand.nextDouble() * 101;
            for (MyCreature creature : old_population) {
                offset += creature.probability;
                if (randomDouble < offset) {
                    //System.out.println("picked: " + creature.fitness);
                    parentB = creature.chromosome.clone();
                    break;
                }
            }

            for(int i=0; i<child.length; i++){
                double uniformCrossOver = Math.random();
                if(uniformCrossOver < 0.5){
                    child[i] = parentA[i];
                }else{
                    child[i] = parentB[i];
                }
            }

            double mutationFactor = Math.random();
            int randomGene = rand.nextInt(child.length);
            if (mutationFactor <= mutationChance){
                child[randomGene] = rand.nextInt(11);
            }

            /*System.out.print("child: ");
            for(int i=0; i<child.length; i++){
                System.out.print(child[i] + " ");
            }
            System.out.print("\n");*/
            new_population[count] = new MyCreature(27, 11);
            new_population[count].chromosome = child.clone();
            count++;
        }


        // Right now the information is used to print some stats...but you should
        // use this information to access creatures fitness.  It's up to you how
        // you define your fitness function.  You should add a print out or
        // some visual display of average fitness over generations.
        System.out.println("Simulation stats:");
        System.out.println("  Survivors    : " + nSurvivors + " out of " + numCreatures);
        System.out.println("  Avg life time: " + avgLifeTime + " turns");
        System.out.println("  Avg fitness: " + totalFitness/numCreatures);


        // Having some way of measuring the fitness, you should implement a proper
        // parent selection method here and create a set of new creatures.  You need
        // to create numCreatures of the new creatures.  If you'd like to have
        // some elitism, you can use old creatures in the next generation.  This
        // example code uses all the creatures from the old generation in the
        // new generation.
        for (int i = count; i < numCreatures; i++) {
            //new_population[i] = old_population[i];
            new_population[i] = new MyCreature(27, 11); //some random creatures
        }

        modelAverageLifeTime += avgLifeTime;
        generationCount++;

        // Column keys don't show up properly because there's not enough space, sorry about that
        //dataset.addValue( avgLifeTime , "fitness" , Integer.toString(generationCount));
        dataset.addValue(totalFitness/numCreatures , "fitness" , Integer.toString(generationCount));

        if(generationCount == _numGenerations){
            System.out.println("\nModel Average Life Time: " + modelAverageLifeTime/_numGenerations);
            LineChart_AWT chart = new LineChart_AWT(
                    "Fitness Vs Generations" ,
                    "average fitness vs generations");

            chart.pack( );
            RefineryUtilities.centerFrameOnScreen( chart );
            chart.setVisible( true );
        }
        // Return new population of cratures.
        return new_population;
    }

    private static class LineChart_AWT extends ApplicationFrame {

        public LineChart_AWT( String applicationTitle , String chartTitle ) {
            super(applicationTitle);
            JFreeChart lineChart = ChartFactory.createLineChart(
                    chartTitle,
                    "Generations","Average fitness",
                    createDataset(),
                    PlotOrientation.VERTICAL,
                    true,true,false);

            ChartPanel chartPanel = new ChartPanel( lineChart );
            chartPanel.setPreferredSize( new java.awt.Dimension( 1000 , 600 ) );
            setContentPane( chartPanel );
        }

        private DefaultCategoryDataset createDataset(){
            return dataset;
        }

    }

}