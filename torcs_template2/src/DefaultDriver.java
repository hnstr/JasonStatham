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
import java.util.Arrays;
import java.util.List;

public class DefaultDriver extends AbstractDriver {

    private NeuralNet neuralNetwork;
    private List<double[]> sensor_data = new ArrayList<>();
    private List<double[]> track_data = new ArrayList<>();
    private boolean loaded = false;
    private boolean learning = !true;
    private boolean first = true;
    private DefaultDriverGenome genome = new DefaultDriverGenome();
    private double[] angles;
    SensorModel sensorModel;
    private double first_corr;

    public DefaultDriver(double[] individual) {

        angles = individual;

        initialize();
        neuralNetwork = new NeuralNet(20, 4, 1);
        if (!learning) {
            neuralNetwork = neuralNetwork.loadGenome();
        }

    }

    double getLapTime() {
        return sensorModel.getLastLapTime();
    }


    private void initialize() {
        this.enableExtras(new AutomatedClutch());
        this.enableExtras(new AutomatedGearbox());
        //this.enableExtras(new AutomatedRecovering());
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
        float[] angles_out = new float[19];

        for(int i = 0; i < 9; i++) {
            angles_out[8 - i] = -(float) angles[i];
            angles_out[10 + i] = (float) angles[i];
        }

        angles_out[9] = 0.0F;

        return angles_out;
    }

    @Override
    public Action defaultControl(Action action, SensorModel sensors) {
        if (action == null) {
            action = new Action();
        }
        sensorModel = sensors;
        double normalized = (sensors.getTrackPosition() + 0) / 1;
        double[] sens_arr = new double[]{
                normalized,
                sensors.getTrackEdgeSensors()[0] / 200,
                sensors.getTrackEdgeSensors()[1] / 200,
                sensors.getTrackEdgeSensors()[2] / 200,
                sensors.getTrackEdgeSensors()[3] / 200,
                sensors.getTrackEdgeSensors()[4] / 200,
                sensors.getTrackEdgeSensors()[5] / 200,
                sensors.getTrackEdgeSensors()[6] / 200,
                sensors.getTrackEdgeSensors()[7] / 200,
                sensors.getTrackEdgeSensors()[8] / 200,
                sensors.getTrackEdgeSensors()[9] / 200,
                sensors.getTrackEdgeSensors()[10] / 200,
                sensors.getTrackEdgeSensors()[11] / 200,
                sensors.getTrackEdgeSensors()[12] / 200,
                sensors.getTrackEdgeSensors()[13] / 200,
                sensors.getTrackEdgeSensors()[14] / 200,
                sensors.getTrackEdgeSensors()[15] / 200,
                sensors.getTrackEdgeSensors()[16] / 200,
                sensors.getTrackEdgeSensors()[17] / 200,
                sensors.getTrackEdgeSensors()[18] / 200
        };

        double moveTo = sensors.getTrackPosition();

        // learning lap
        if (sensors.getLaps() < 9 && learning) {
            sensor_data.add(sens_arr);
            track_data.add(new double[]{normalized,0.0});
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
            moveTo = -1.0F* (net_out[0] - 0.0F) ;
            if (first) {
                first = !first;
                first_corr = moveTo;
            }
            if (sensors.getLaps() > 1) {
                first = !first;
            }
//            System.out.println("Advice: " + moveTo);


            moveTo = moveTo - first_corr;
            if (moveTo < 0.01F && moveTo > -0.01F) {
                moveTo = 0.0;
            }

            if (sensors.getTrackPosition() > 0.0F) {
                if (moveTo > 0.0F) {
                    moveTo = -moveTo;
                }
            }
            if (sensors.getTrackPosition() < 0.0F) {
                if (moveTo < 0.0F) {
                    moveTo = -moveTo;
                }
            }

//            System.out.println("Advice: " + moveTo);

            // param 1
            float floaty = 0.75F;
            if (moveTo > floaty) {
                moveTo = floaty;
            } else if (moveTo < -floaty) {
                moveTo = -floaty;
            }

        }

//        System.out.println(sensors.getTrackPosition());
//        System.out.println(moveTo);

        // 5:26 sigmoid

        if (learning) {
            action.steering = DriversUtils.alignToTrackAxis(sensors, 0.5);
            action.steering += DriversUtils.moveTowardsTrackPosition(sensors, 0.5, -sensors.getTrackPosition());
        }else{
            // param 2
            action.steering = DriversUtils.alignToTrackAxis(sensors, 0.25);
            action.steering += DriversUtils.moveTowardsTrackPosition(sensors, 0.25, moveTo);
        }

        // param 3
        if (action.steering < 0.025F && action.steering > -0.025F) {
            action.steering = 0.0F;
        } else if (action.steering < 0.15F && action.steering > -0.15F) {
            action.steering = action.steering / 2.0F;
        }

        if (action.steering > 1.0F) {
            action.steering = 1.0F;
        }
        if (action.steering < -1.0F) {
            action.steering = -1.0F;
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
                if (learning && !loaded) {
                    targetSpeed = 230.0F;
                } else {
                    targetSpeed = 210.0F;
                }
                double opp0 = sensors.getOpponentSensors()[16];
                double opp1 = sensors.getOpponentSensors()[17];
                double opp2 = sensors.getOpponentSensors()[18];

//                System.out.println(opp0 + " " + opp1 + " " + opp2 + ", " + sensors.getDistanceRaced());
//                System.out.println(Arrays.toString(sensors.getOpponentSensors()));

                if (opp1 < 200 || opp0 < 200 || opp2 < 200) {
                    targetSpeed = 350.0F;
                }
                if ((opp0 < 25 || opp1 < 25 || opp2 < 25) && sensors.getDistanceRaced() > 400) {
                    targetSpeed = (float) sensors.getSpeed() + 1;
                }
                if ((opp0 < 10 || opp1 < 10 || opp2 < 10) && sensors.getDistanceRaced() > 800) {
                    targetSpeed = (float) sensors.getSpeed();
                }
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


//        System.out.println("--------------" + getDriverName() + "--------------");
//        System.out.println("Steering: " + action.steering);
//        System.out.println("Acceleration: " + action.accelerate);
//        System.out.println("Brake: " + action.brake);
//        System.out.println("Angle: " + sensors.getAngleToTrackAxis());
//        System.out.println("Distance: " + sensors.getTrackPosition());
//        System.out.println("-----------------------------------------------");
        return action;
    }
}