package com.samvbeckmann.obriareus.algorithms;

import com.samvbeckmann.obriareus.core.Arm;
import com.samvbeckmann.obriareus.core.ArmMemory;
import com.samvbeckmann.obriareus.core.Bandit;
import com.samvbeckmann.obriareus.utilities.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * l-split algorithm.
 *
 * @author Sam Beckmann, Nate Beckemeyer. Thanks to Anton Ridgeway for original implementations.
 */
public class LSplit implements com.samvbeckmann.obriareus.core.IAlgorithm
{
    private static final boolean debugLSplit = false;

    @Override
    public String getName()
    {
        return "l-split";
    }

    /**
     * The l-split algorithm retains <code>lValue^(iteration)</code> arms after each iteration.
     *
     * @param curBandit       The agent currently employing this algorithm.
     * @param inputParameters [0] --> lValue: Value that determines how quickly arms are dropped.
     *                        <code>lValue * 100%</code> will be dropped in the first iteration.
     */
    @Override
    public void run(Bandit curBandit, List<Double> inputParameters)
    {
        double lValue = inputParameters.get(0);
        // Initialize variables
        Arm[] arms = curBandit.getArms();
        ArmMemory[] memories = curBandit.getMemories();
        ArrayList<Integer> remainingArms = new ArrayList<Integer>();

        double budget = curBandit.getBudget();
        int numToPull = arms.length;
        int iterations = 0;

        // Add all the arms to the list to be pulled in the first iteration
        for (int i = 0; i < curBandit.getArms().length; i++)
            remainingArms.add(i);

        // Loop the runs throughout the algorithm
        while (remainingArms.size() > 0 && budget >= curBandit.getMinCost())
        {
            for (int i = 0; i < remainingArms.size(); i++)
            {
                if (arms[i].getCost() <= budget)
                {
                    curBandit.pull(remainingArms.get(i));
                    if (debugLSplit) System.out.println(Utilities.getPullResult(getName(), remainingArms.get(i),
                            arms[remainingArms.get(i)], memories[remainingArms.get(i)]));
                }

            }

            iterations++;

            // Clears the current list of arms, to be repopulated for the next trial
            remainingArms.clear();

            // Updates the number of arms to be pulled in the next iteration.
            if (numToPull > 1)
            {
                numToPull = (int) (arms.length * (Math.pow(1 - lValue, iterations)));
                if (numToPull < 1)
                    numToPull = 1;
                if (debugLSplit) System.out.println("[" + getName() + "] Number of Arms to pull on next iteration: "
                        + numToPull);
            }

            // Picks the best arms, which will be pulled on the next iteration
            List<Integer> feasibles = new ArrayList<Integer>(arms.length);
            Utilities.generateIndices(feasibles, arms.length);
            for (int i = 0; i < numToPull; i++)
            {
                if (!feasibles.isEmpty())
                {
                    int currentBest = Utilities.getBestFromFeasibles(curBandit, feasibles);
                    if (arms[currentBest].getCost() <= curBandit.getBudget())
                        remainingArms.add(currentBest);
                    else
                        i--;
                    feasibles.remove(new Integer(currentBest));
                }
            }
        }

        if (debugLSplit) System.out.println("[" + getName() + "] Budget Exhausted. Trial Complete");
    }
}
