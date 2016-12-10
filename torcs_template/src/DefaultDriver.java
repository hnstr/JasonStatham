import cicontest.algorithm.abstracts.AbstractDriver;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.controller.extras.ABS;
import cicontest.torcs.controller.extras.AutomatedClutch;
import cicontest.torcs.controller.extras.AutomatedGearbox;
import cicontest.torcs.controller.extras.AutomatedRecovering;
import cicontest.torcs.genome.IGenome;
import scr.Action;
import scr.SensorModel;

import java.util.ArrayList;
import java.util.List;

public class DefaultDriver extends AbstractDriver {

    private NeuralNet neuralNetwork;
    List<double[]> sensor_data = new ArrayList<>();
    List<double[]> track_data = new ArrayList<>();
    boolean loaded = false;
    boolean learning = !true;

    public DefaultDriver() {
        initialize();
        neuralNetwork = new NeuralNet(2, 4, 2);
        if (!learning) {
            neuralNetwork = neuralNetwork.loadGenome();
        }

    }

    private void initialize() {
        this.enableExtras(new AutomatedClutch());
        this.enableExtras(new AutomatedGearbox());
        this.enableExtras(new AutomatedRecovering());
        this.enableExtras(new ABS());
    }

    @Override
    public void loadGenome(IGenome genome) {
        if (genome instanceof DefaultDriverGenome) {
            DefaultDriverGenome myGenome = (DefaultDriverGenome) genome;
        } else {
            System.err.println("Invalid Genome assigned");
        }
    }

    @Override
    public double getAcceleration(SensorModel sensors) {
        double[] sensorArray = new double[4];
        //double[] output = neuralNetwork.getOutput(sensors);
        return 1;
    }

    @Override
    public double getSteering(SensorModel sensors) {
        //double[] output = neuralNetwork.getOutput(sensors);
        return 0.5;
    }

    @Override
    public String getDriverName() {
        return "Jason Statham";
    }

    @Override
    public Action controlWarmUp(SensorModel sensors) {
        Action action = new Action();
        return defaultControl(action, sensors);
    }

    @Override
    public Action controlQualification(SensorModel sensors) {
        Action action = new Action();
        return defaultControl(action, sensors);
    }

    @Override
    public Action controlRace(SensorModel sensors) {
        Action action = new Action();
        return defaultControl(action, sensors);
    }

    @Override
    public float[] initAngles() {
        float[] angles = new float[19];

        for(int i = 0; i < 19; ++i) {
            angles[i] = (float)(-90 + i * 10);
        }

        angles[8] = -1.0F;
        angles[10] = 1.0F;

        return angles;
    }

    @Override
    public Action defaultControl(Action action, SensorModel sensors) {
        if (action == null) {
            action = new Action();
        }

        double right = 0.0;
        if(sensors.getTrackPosition() > 0) {
            right = 1.0;
        }
        double[] sens_arr = new double[]{
                sensors.getTrackPosition(),
                right
//                sensors.getTrackEdgeSensors()[8],
//                sensors.getTrackEdgeSensors()[9],
//                sensors.getTrackEdgeSensors()[10]
        };

        double moveTo = sensors.getTrackPosition();

        // learning lap
        if (sensors.getLaps() < 1 && learning) {
            sensor_data.add(sens_arr);
            track_data.add(new double[]{moveTo, right});
        }
//        // learn
        else if (!loaded && learning) {
            System.out.println("Loading...");
            for (int i = 0; i < sensor_data.size(); i++) {
                neuralNetwork.load(sensor_data.get(i), track_data.get(i));
            }
            System.out.println("Learning...");
            neuralNetwork.learn();

            System.out.println("Storing...");
            neuralNetwork.storeGenome();
            loaded = !loaded;
        }
        // use learned data
        else {
            double[] net_out = neuralNetwork.getOutput(sens_arr);
            moveTo = net_out[0];
            //moveTo = sensors.getTrackPosition();
        }

        System.out.println(sensors.getTrackPosition());
        System.out.println(moveTo);

        if (learning) {
            action.steering = DriversUtils.alignToTrackAxis(sensors, 0.5);
            action.steering += DriversUtils.moveTowardsTrackPosition(sensors, 0.5, -sensors.getTrackPosition());
        }else{
            action.steering = DriversUtils.alignToTrackAxis(sensors, 0.05);
            action.steering += DriversUtils.moveTowardsTrackPosition(sensors, 0.1, -moveTo);
        }


        // target speed voor bochten. Zit hier maar niet aan..
        float targetSpeed = 210.0F;
        if(Math.abs(sensors.getTrackPosition()) < 1.0D) {
            float rxSensor = (float)sensors.getTrackEdgeSensors()[10];
            float sensorsensor = (float)sensors.getTrackEdgeSensors()[9];
            float sxSensor = (float)sensors.getTrackEdgeSensors()[8];

            if(sensorsensor <= 70.0F && (sensorsensor < rxSensor || sensorsensor < sxSensor)) {
                float h = sensorsensor * 0.08716F;
                float b;
                float sinAngle;

                if(rxSensor > sxSensor) {
                    b = rxSensor - sensorsensor * 0.99619F;
                } else {
                    b = sxSensor - sensorsensor * 0.99619F;
                }

                sinAngle = b * b / (h * h + b * b);
                targetSpeed = targetSpeed * (sensorsensor * sinAngle / 15.4F);
                if (targetSpeed < 100.0F) {
                    // minimum snelheid
                    targetSpeed = 100.0F;
                } else if (targetSpeed > 215.0F) {
                    // snelheid in bochten als hij merkt dat hij wel harder de bocht door kan.
                    targetSpeed = 350.0F;
                }
            } else {
                // standaard maximum snelheid
                // 230 => no penalty, 234 => fastest time with penalty
                targetSpeed = 230.0F;
            }

            action.accelerate = (float)(2.0D / (1.0D + Math.exp(sensors.getSpeed() - (double)targetSpeed)) - 1.0D);
        } else {
            // als de auto van track af raakt.
            action.accelerate = 0.4F;
        }

        if(action.accelerate > 0.01F) {
            action.brake = 0.0F;
        } else {
            action.brake = -action.accelerate;
            action.accelerate = 0.01F;
        }


        System.out.println("--------------" + getDriverName() + "--------------");
        System.out.println("Steering: " + action.steering);
        System.out.println("Acceleration: " + action.accelerate);
        System.out.println("Brake: " + action.brake);
        System.out.println("Angle: " + sensors.getAngleToTrackAxis());
        System.out.println("Distance: " + sensors.getTrackPosition());
        System.out.println("-----------------------------------------------");
        return action;
    }
}