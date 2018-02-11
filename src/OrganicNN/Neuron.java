package OrganicNN;

import java.util.HashMap;

/**
 * This Neuron class is used to create an organic Deep Neural Network (DNN),
 * a "structureless" neural network defined by neuron axons instead of layers.
 * Individual neurons are agnostic to the network but they can push
 * backpropagation or the feed forward algorithm by only tracking the neurons
 * connected to their axons.
 *
 * @author JAForewit
 * @version 1.0, 02/08/2018
 * @see "README.md"
 */
public class Neuron {
    private int inputCounter;
    private int outputCounter;
    private double bias;
    private double error;
    private double output;
    private HashMap<Neuron, Double> inputAxons; // Key: neuron, Value: weight
    private HashMap<Neuron, Double> outputAxons; // Key: neuron, Value: weight

    /**
     * Initializes the neuron with a bias, sets the initial error and initializes
     * an empty list of input and output axons.
     *
     * @param bias the neuron's bias (usually randomized) used in the backpropagation
     *             algorithm
     * @see "README.md"
     */
    public Neuron(double bias) {
        inputCounter = 0;
        outputCounter = 0;
        this.bias = bias;
        error = 0d;
        output = sigmoid(this.bias);
        inputAxons = new HashMap<>();
        outputAxons = new HashMap<>();
    }


    /**
     * Removes all axons in the event that the Neuron is removed after the network
     * has been created. This function also removes this neuron from the axons of
     * the neurons it is connected to.
     */
    public void removeAxons() {
        for (Neuron n : inputAxons.keySet()) removeInputAxon(n);
        for (Neuron n : outputAxons.keySet()) removeOutputAxon(n);
    }

    /**
     * Returns the neuron's current calculated error.
     *
     * @return the current calculated error
     */
    public double getError() { return error; }

    /**
     * Returns the current output value of the neuron.
     *
     * @return the current output
     */
    public double getOutput() { return output; }

    /**
     * Adds an axon connected to an input neuron
     *
     * @param n input neuron
     * @param weight axon's weight (which will get changed through backpropagation)
     */
    public void addInputAxon(Neuron n, double weight) {
        if (inputAxons.containsKey(n)) return;
        inputAxons.put(n,weight);
        n.addOutputAxon(this, weight);
    }

    /**
     * Adds an axon connected to an ouput neuron
     *
     * @param n output neuron
     * @param weight axon's weight (which will get changed through backpropagation)
     */
    public void addOutputAxon(Neuron n, double weight) {
        if (outputAxons.containsKey(n)) return;
        outputAxons.put(n,weight);
        n.addInputAxon(this, weight);
    }

    /**
     * Removes an axon connected to an input neuron
     *
     * @param n input neuron
     */
    public void removeInputAxon(Neuron n) {
        if (inputAxons.containsKey(n)) {
            inputAxons.remove(n);
            n.removeOutputAxon(this);
        }
    }

    /**
     * Removes an axon connected to an output neuron
     *
     * @param n output neuron
     */
    public void removeOutputAxon(Neuron n) {
        if (outputAxons.containsKey(n)) {
            outputAxons.remove(n);
            n.removeInputAxon(this);
        }
    }

    /**
     * Updates the weight of an axon connected to an input neuron
     *
     * @param n input neuron
     * @param weight new weight value
     */
    public void updateInputAxon(Neuron n, double weight) {
        if (inputAxons.get(n) == weight) return;
        inputAxons.replace(n, weight);
        n.updateOutputAxon(this, weight);
    }

    /**
     * Updates the weight of an axon connected to an output neuron
     *
     * @param n output neuron
     * @param weight new weight value
     */
    public void updateOutputAxon(Neuron n, double weight) {
        if (outputAxons.get(n) == weight) return;
        outputAxons.replace(n, weight);
        n.updateInputAxon(this, weight);
    }

    /**
     * Called on an input neuron and begins the feed forward process where
     * the output of one neuron leads to the output of the next etc.
     *
     * @param value the input value
     * @see "README.md"
     */
    public void feedForward(double value) {
        output = value;

        // continue feeding forward
        for (Neuron n : outputAxons.keySet()) n.feedForward(this);
    }

    /**
     * Calculates the error the neuron based on the target value, then
     * "backpropagates" that error throughout its neighbors, updating the weights of
     * the axons and biases of the neurons to minimize the error. Backpropagation
     * is the backbone of how the network learns a set of training data.
     *
     * @param target the target value for this output neuron
     * @param rate learning rate (eta)
     * @see "README.md"
     */
    public void backpropagate(double target, double rate) {
        error = (output - target) * output * (1 - output);

        // continue backpropagation
        for (Neuron n : inputAxons.keySet()) n.backpropagate(this, rate);

        // update bias and reset output
        bias += -rate * error;
    }

    /**
     * Performs the feed forward algorithm as prompted by an input axon. It will
     * wait until every input axon has "reported in" and then feed forward its
     * output through its output axons.
     *
     * @param n input neuron (from an axon)
     */
    private void feedForward(Neuron n) {
        // reset output
        if (inputCounter == 0) output = sigmoid(bias);

        inputCounter++;
        output = sigmoid(logit(output) + inputAxons.get(n) * n.getOutput());

        // ensure all input axons have been fed forward
        if (inputCounter < inputAxons.size()) return;

        // continue feeding forward
        for (Neuron i : outputAxons.keySet()) i.feedForward(this);

        // reset counter
        inputCounter = 0;
    }

    /**
     * Performs the backpropagation algorithm as propmted by an output axon. It
     * will wait until every output has "reported in" and then backpropagate its
     * error through its input axons.
     *
     * @param n output neuron (from an axon)
     * @param rate learning rate (eta)
     */
    private void backpropagate(Neuron n, double rate) {
        // reset error
        if (outputCounter == 0) error = 0;

        outputCounter++;
        error += n.getError() * outputAxons.get(n);

        // ensure all output axons have backpropagated
        if (outputCounter < outputAxons.size()) return;

        error *= output * (1 - output);

        // update weights
        double newWeight;
        for (Neuron i : outputAxons.keySet()) {
            newWeight = outputAxons.get(i) - rate * output * i.getError();
            updateOutputAxon(i, newWeight);
        }

        // continue backpropagation
        for (Neuron i : inputAxons.keySet()) i.backpropagate(this, rate);

        // update bias
        bias += -rate * error;
        outputCounter = 0;
    }

    /**
     * The nueron activation function which maps all values to between 0 and 1. This
     * is used to calculate the output of any neuron.
     *
     * @param x any double value
     * @return sigmoid(x)
     */
    private double sigmoid(double x) { return 1d / (1 + Math.exp(-x)); }

    /**
     * The reverse of the sigmoid activation funciton. This is used to retroactively
     * calculate new outputs as the network feeds forward.
     *
     * @param x any double value
     * @return logit(x)
     */
    private double logit(double x) { return Math.log(x / (1 - x)); }
}
