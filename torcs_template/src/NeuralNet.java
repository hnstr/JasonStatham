import scr.SensorModel;
import org.neuroph.nnet.Adaline;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import java.io.*;

public class NeuralNet implements Serializable {

    private static final long serialVersionUID = -88L;
    Adaline net;
    DataSet data;

    NeuralNet(int inputs, int hidden, int outputs) {
        net = new Adaline(inputs);
        data = new DataSet(inputs, outputs);
    }

    public void load(double[] sense, double track) {
        data.addRow(new DataSetRow(sense, new double[]{track}));
    }

    public void learn() {
        net.learn(data);
    }

    public double[] getOutput(double[] sense) {
        net.setInput(sense);
        net.calculate();
        return net.getOutput();
    }

    //Store the state of this neural network
    public void storeGenome() {
        ObjectOutputStream out = null;
        try {
            //create the memory folder manually
            out = new ObjectOutputStream(new FileOutputStream("memory/mydriver.mem"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.writeObject(this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load a neural network from memory
    public NeuralNet loadGenome() {

        // Read from disk using FileInputStream
        FileInputStream f_in = null;
        try {
            f_in = new FileInputStream("memory/mydriver.mem");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Read object using ObjectInputStream
        ObjectInputStream obj_in = null;
        try {
            obj_in = new ObjectInputStream(f_in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Read an object
        try {
            if (obj_in != null) {
                return (NeuralNet) obj_in.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
