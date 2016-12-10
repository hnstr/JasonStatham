import org.neuroph.core.learning.LearningRule;
import org.neuroph.nnet.Adaline;
import org.neuroph.nnet.Perceptron;
import scr.SensorModel;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import java.io.*;
import java.util.Arrays;
import org.neuroph.util.TransferFunctionType;
import org.neuroph.nnet.learning.BackPropagation;

public class NeuralNet implements Serializable {

    private static final long serialVersionUID = -88L;
    MultiLayerPerceptron net;
    DataSet data;

    NeuralNet(int inputs, int hidden, int outputs) {
        net = new MultiLayerPerceptron(TransferFunctionType.LINEAR, inputs, hidden, outputs);
        data = new DataSet(inputs, outputs);
    }

    public void load(double[] sense, double[] result) {
        data.addRow(new DataSetRow(sense, result));
    }

    public void learn() {
        BackPropagation backPropagation = new BackPropagation();
        backPropagation.setMaxIterations(100);
        net.learn(data, backPropagation);
    }

    public double[] getOutput(double[] sense) {
        net.setInput(sense);
        net.calculate();
        double[] networkOutput = net.getOutput();

        return networkOutput;
    }

    //Store the state of this neural network
    public void storeGenome() {
        net.save("torcs_template/src/memory/mydriver.nnet");
    }

    // Load a neural network from memory
    public NeuralNet loadGenome() {

        net.createFromFile("torcs_template/src/memory/mydriver.nnet");
        return this;
    }

}
