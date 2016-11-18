import cicontest.algorithm.abstracts.AbstractDriver;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.controller.extras.ABS;
import cicontest.torcs.controller.extras.AutomatedClutch;
import cicontest.torcs.controller.extras.AutomatedGearbox;
import cicontest.torcs.controller.extras.AutomatedRecovering;
import cicontest.torcs.genome.IGenome;
import scr.Action;
import scr.SensorModel;
import scr.SimpleDriver;

import java.sql.DriverManager;

public class DefaultDriver extends AbstractDriver {

    private NeuralNetwork neuralNetwork;

    public DefaultDriver() {
        initialize();
        neuralNetwork = new NeuralNetwork(12, 8, 2);
//        neuralNetwork = neuralNetwork.loadGenome();
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
        double output = neuralNetwork.getOutput(sensors);
        return 1;
    }

    @Override
    public double getSteering(SensorModel sensors) {
        Double output = neuralNetwork.getOutput(sensors);
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

        action.steering = DriversUtils.alignToTrackAxis(sensors, 0.5);
        action.steering += DriversUtils.moveTowardsTrackPosition(sensors, 0.5, -sensors.getTrackPosition());

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
                    targetSpeed = 100.0F;
                } else if (targetSpeed > 215.0F) {
                    targetSpeed = 350.0F;
                }
            } else {
                // 230 => no penalty, 234 => fastest time with penalty
                targetSpeed = 230.0F;
            }

            action.accelerate = (float)(2.0D / (1.0D + Math.exp(sensors.getSpeed() - (double)targetSpeed)) - 1.0D);
        } else {
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