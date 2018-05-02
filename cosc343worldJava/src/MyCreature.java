import cosc343.assig2.Creature;

import java.util.Random;

/**
 * The MyCreate extends the cosc343 assignment 2 Creature.  Here you implement
 * creatures chromosome and the agent function that maps creature percepts to
 * actions.
 *
 * @author
 * @version 1.0
 * @since 2017-04-05
 */
public class MyCreature extends Creature {

    // Random number generator
    Random rand = new Random();

    int [] chromosome = new int [9];
    double fitness;
    double probability;
    boolean survivedOneRound;

    /* Empty constructor - might be a good idea here to put the code that
     initialises the chromosome to some random state

     Input: numPercept - number of percepts that creature will be receiving
            numAction - number of action output vector that creature will need
                        to produce on every turn
    */
    public MyCreature(int numPercepts, int numActions) {
        fitness = 0;
        probability = 0;
        survivedOneRound = false;
        for (int i = 0; i < chromosome.length; i++) {
            chromosome[i] = rand.nextInt(numActions);
        }
    }

    /* This function must be overridden by MyCreature, because it implements
       the AgentFunction which controls creature behavoiur.  This behaviour
       should be governed by a model (that you need to come up with) that is
       parameterise by the chromosome.

       Input: percepts - an array of percepts
              numPercepts - the size of the array of percepts depend on the percept
                            chosen
              numExpectedAction - this number tells you what the expected size
                                  of the returned array of percepts should bes
       Returns: an array of actions
    */
    @Override
    public float[] AgentFunction(int[] percepts, int numPercepts, int numExpectedActions) {

        // This is where your chromosome gives rise to the model that maps
        // percepts to actions.  This function governs your creature's behaviour.
        // You need to figure out what model you want to use, and how you're going
        // to encode its parameters in a chromosome.

        // At the moment, the actions are chosen completely at random, ignoring
        // the percepts.  You need to replace this code.

        float actions[] = new float[numExpectedActions];
        boolean monsterNear = false;

        for (int i = 0; i < numPercepts; i++) {
            if (i < 9) { // monster location
                if (percepts[i] == 1) {
                    monsterNear = true;
                    if(chromosome[i] <= 8) {
                        actions[chromosome[i]] += 3; // run away
                    }
                }
            } else if (i >= 9 && i < 18) { //other creature location
                if (percepts[i] == 1) {
                    if(chromosome[i-9] <= 8) {
                        actions[chromosome[i-9]] += 1; // run away
                    }
                }
            } else { //food location
                if (percepts[i] == 2) { // food is red
                    if(chromosome[i-18] <= 8 ){ // if can move in 8 directions
                        actions[8-chromosome[i-18]] += 3; // move closer
                    }else{ // else eat food or pick a random movement
                        if(chromosome[i-18] == 9){ // if eat food
                            if(monsterNear){
                                actions[chromosome[i-18]] += 3;
                            }else {
                                actions[chromosome[i-18]] += 4;
                            }
                            actions[chromosome[i-18]] += 3;
                        }else{ // random movement
                            actions[chromosome[i-18]] += 2;
                        }
                    }
                } else if (percepts[i] == 1) { // food is green
                    if(chromosome[i-18] <= 8 ){ // if can move in 8 directions
                        actions[8-chromosome[i-18]] += 1;
                    }else{ // else eat food or pick a random movement
                        if(chromosome[i-18] == 9){ // if eat food
                            if(!monsterNear) {
                                actions[chromosome[i - 18]] += 3;
                            }else{
                                actions[chromosome[i - 18]] += 1;
                            }
                            //actions[chromosome[i - 18]] += 2;
                        }else{ // random movement
                            actions[chromosome[i-18]] += 1;
                        }
                    }
                }
            }
        }

        int actionsTotal = 0;
        for(int i=0; i<numExpectedActions; i++){
            actionsTotal += actions[i];
        }
        if(actionsTotal == 0){
            actions[rand.nextInt(9)] += 1;
        }

        return actions;
    }

}