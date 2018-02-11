package OrganicNN;

import TrainSet.TrainSet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Uses the Neuron class to create an organic Deep Neural Network (DNN),
 * a "structureless" neural network defined by neuron axons instead of layers.
 *
 * @author JAForewit
 * @version 1.0, 02/08/2018
 * @see "README.md"
 */
public class OrganicNN {
    private static final Logger LOGGER = Logger.getLogger( OrganicNN.class.getName() );
    private final double MIN_BIAS = -0.7;
    private final double MAX_BIAS = 0.7;
    private final double MIN_WEIGHT = -1.0;
    private final double MAX_WEIGHT = 1.0;
    private int inputCount;     // number of input neurons
    private int outputCount;    // number of output neurons
    private int neuronCount;    // total number of neurons
    private Neuron neurons[];

    /**
     * Creates a neural network defined by the number of input neurons, output neurons,
     * hidden neurons, and the axon connections between them. That information is
     * loaded from a text file.
     *
     * @param filename text file that defines the network
     * @see "README.md"
     */
    public OrganicNN(String filename) {
        // load structure file
        try(BufferedReader reader = new BufferedReader(new FileReader(filename))) {

            // read network critical definitions
            int[] nextLine = Arrays.stream(reader.readLine().split("\\s")).mapToInt(Integer::parseInt).toArray();
            inputCount = nextLine[0];
            outputCount = nextLine[2];
            neuronCount = inputCount + outputCount + nextLine[1];

            // initialize neurons with a random bias
            neurons = new Neuron[neuronCount];
            double randBias;
            for (int i = 0; i < neuronCount; i++) {
                randBias = (Math.random() * (MAX_BIAS - MIN_BIAS)) + MIN_BIAS;
                neurons[i] = new Neuron(randBias);
            }

            // create axons and assign random weights
            double randWeight;
            for (int i = 0; i < neuronCount - outputCount; i++) {
                nextLine = Arrays.stream(reader.readLine().split("\\s")).mapToInt(Integer::parseInt).toArray();
                for (int index : nextLine) {
                    randWeight = (Math.random() * (MAX_WEIGHT - MIN_WEIGHT)) + MIN_WEIGHT;
                    neurons[i].addOutputAxon(neurons[index], randWeight);
                }
            }

            // verifying the file has ended
            if (reader.readLine() != null) throw new IOException();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "There was a problem reading the structure file.");
            e.printStackTrace();
        }
    }

    /**
     * Returns the number of input neurons
     *
     * @return number of input neurons
     */
    public int getInputCount() { return inputCount; }

    /**
     * Returns the number of output neurons
     *
     * @return number of output neurons
     */
    public int getOutputCount() { return outputCount; }

    /**
     * Feeds input values into the neural network and returns the outputs.
     *
     * @param inputs values for each input neuron
     * @return the outputs of each output neuron
     */
    public double[] calculateOutput(double[] inputs) {
        if (inputs.length != inputCount) {
            LOGGER.log(Level.SEVERE, "Passed an invalid input size to calculateOutputs()."
                    + " Expected inputs[" + inputCount + "].");
            return null;
        }
        double[] outputs = new double[outputCount];
        feedForward(inputs);
        for (int i=0; i<outputCount; i++) outputs[i] = neurons[neuronCount-i-1].getOutput();
        return outputs;
    }

    /**
     * Trains the neural network by performing multiple iterations of the feed forward
     * and backpropagation algorithms.
     *
     * @param inputs values for each input neuron
     * @param targets target values for the output neurons
     * @param rate the learning rate (eta)
     * @param iterations the number of times the network is trained backpropagated
     */
    public void train(double[] inputs, double[] targets, double rate, int iterations) {
        if (inputs.length != inputCount || targets.length != outputCount) {
            LOGGER.log(Level.SEVERE, "Passed an invalid input and target size to train()."
                    + " Expected inputs[" + inputCount + "], targets[" + outputCount + "].");
            return;
        }
        for (int i=0; i < iterations; i++) backpropagate(inputs,targets,rate);
    }

    /**
     * Trains the neural network in batches using the TrainSet class to hold the input
     * and target training data.
     *
     * @param set the TrainSet object which holds all input and target data for training
     * @param loops the number of times each batch will perform backpropagation
     * @param batchSize the size of a random subset (batch) of the training data that will be
     *                  processed together.
     * @param rate learning rate (eta)
     * @see TrainSet
     */
    public void train(TrainSet set, int loops, int batchSize, double rate) {
        if (set.getINPUT_SIZE() != inputCount || set.getTARGET_SIZE() != outputCount) {
            LOGGER.log(Level.SEVERE, "Passed a TrainSet with an invalid input and target size to train()."
                    + " Expected inputs[" + inputCount + "], targets[" + outputCount + "].");
            return;
        }
        for (int i = 0; i < loops; i++) {
            TrainSet batch = set.extractBatch(batchSize);
            for (int j=0; j < batch.size(); j++) {
                this.backpropagate(batch.getInput(j), batch.getTarget(j), rate);
            }
        }
    }

    /**
     * Calculates the mean squared error of the network given inputs and target outputs.
     * The MSE is the cost function of the network.
     *
     * @param inputs values for each input neuron
     * @param targets target values for the output neurons
     * @return the mean squared error
     */
    public double MSE (double[] inputs, double[] targets) {
        if (inputs.length != inputCount || targets.length != outputCount) {
            LOGGER.log(Level.SEVERE, "Passed an invalid input and target size to MSE()."
                    + " Expected: inputs[" + inputCount + "] targets[" + outputCount + "].");
            return Double.NaN;
        }
        double sum = 0;
        for (int i=0; i<outputCount; i++)
            sum += (targets[i] - calculateOutput(inputs)[i]) * (targets[i] - calculateOutput(inputs)[i]);
        return sum / (2d * outputCount);
    }

    /**
     * Provides the network with values for the input neurons and feeds them through
     * the axons. This updates each neuron's output, ultimately updating the output
     * neuron's values.
     *
     * @param inputs values for each input neuron
     * @see "README.md"
     */
    private void feedForward(double[] inputs) {
        for (int i=0; i<inputCount; i++)
            neurons[i].feedForward(inputs[i]);
    }

    /**
     * Calculates the error for each output neuron based on the target value, then
     * "backpropagates" that error throughout the network, updating the weights of
     * the axons and biases of the neurons to minimize the error. Backpropagation
     * is the backbone of how the network learns a set of training data.
     *
     * @param inputs values for each input neuron
     * @param targets target values for the output neurons
     * @param rate the learning rate (eta)
     */
    private void backpropagate(double[] inputs, double[] targets, double rate) {
        feedForward(inputs);
        for (int i=0; i<outputCount; i++) neurons[neuronCount-i-1].backpropagate(targets[i], rate);
    }
}
