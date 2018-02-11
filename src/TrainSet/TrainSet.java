
package TrainSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Datatype used to store inputs and targets for training a neural network.
 *
 * @author Luecx
 * @author JAForewit
 * @version 1.1 02/08/2018
 */
public class TrainSet {
    private final int INPUT_SIZE;
    private final int TARGET_SIZE;
    //double[][] <- index1: 0 = input, 1 = output || index2: index of element
    private ArrayList<double[][]> data = new ArrayList<>();

    /**
     * Sets the sizes for input and target (output) training data.
     *
     * @param INPUT_SIZE number of input neurons
     * @param OUTPUT_SIZE number of output neurons
     */
    public TrainSet(int INPUT_SIZE, int OUTPUT_SIZE) {
        this.INPUT_SIZE = INPUT_SIZE;
        this.TARGET_SIZE = OUTPUT_SIZE;
    }

    /**
     * Adds a set of input and target data to the training set.
     *
     * @param inputs input neuron values
     * @param targets target output neuron values
     */
    public void addData(double[] inputs, double[] targets) {
        if(inputs.length != INPUT_SIZE || targets.length != TARGET_SIZE) return;
        data.add(new double[][]{inputs, targets});
    }

    /**
     * Generates a random subset of the training data.
     *
     * @param size size of the subset
     * @return a new TrainSet randomly selected from the training data
     */
    public TrainSet extractBatch(int size) {
        if(size > 0 && size <= this.size()) {
            TrainSet set = new TrainSet(INPUT_SIZE, TARGET_SIZE);

            Integer[] ids = new Integer[size];
            Random r = new Random();
            for (int i = 0; i < size; i++) ids[i] = r.nextInt(this.size());

            for(Integer i:ids) {
                set.addData(this.getInput(i),this.getTarget(i));
            }
            return set;
        }else return this;
    }

    /**
     * Returns a String of the training data in a clear format
     *
     * @return a String of the training data
     */
    public String toString() {
        String s = "TrainSet ["+INPUT_SIZE+ " ; "+ TARGET_SIZE +"]\n";
        int index = 0;
        for(double[][] r:data) {
            s += index +":   "+Arrays.toString(r[0]) +"  >-||-<  "+Arrays.toString(r[1]) +"\n";
            index++;
        }
        return s;
    }

    /**
     * Returns the number of training sets (sets of inputs and targets) that
     * are in the training data.
     *
     * @return size of the training data
     */
    public int size() {
        return data.size();
    }

    /**
     * Returns the input data at a given index of the training data
     *
     * @param index index for the requested data
     * @return input array
     */
    public double[] getInput(int index) {
        if(index >= 0 && index < size())
            return data.get(index)[0];
        else return null;
    }

    /**
     * Returns the target data at a given index of the training data
     *
     * @param index index for the requested data
     * @return target array
     */
    public double[] getTarget(int index) {
        if(index >= 0 && index < size())
            return data.get(index)[1];
        else return null;
    }

    /**
     * Returns the size of the input data (number of input neurons)
     *
     * @return size of input data
     */
    public int getINPUT_SIZE() {
        return INPUT_SIZE;
    }

    /**
     * Returns the size of the target data (number of output neurosn
     *
     * @return size of target data
     */
    public int getTARGET_SIZE() {
        return TARGET_SIZE;
    }
}

