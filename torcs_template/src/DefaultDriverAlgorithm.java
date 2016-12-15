import java.io.File;

import cicontest.algorithm.abstracts.AbstractAlgorithm;
import cicontest.algorithm.abstracts.AbstractRace;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.controller.Driver;
import cicontest.torcs.controller.Human;
import race.TorcsConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
public class DefaultDriverAlgorithm extends AbstractAlgorithm {

    private static final long serialVersionUID = 654963126362653L;

    DefaultDriverGenome[] drivers = new DefaultDriverGenome[1];
    int[] results = new int[1];

    public Class<? extends Driver> getDriverClass() {
        return DefaultDriver.class;
    }

    public void run(boolean continue_from_checkpoint) {
//        if (!continue_from_checkpoint) {
//            //init NN
//            DefaultDriverGenome genome = new DefaultDriverGenome();
////            drivers[0] = genome;
//
//            for (int i = 0; i < 10; i++) {
//                drivers.add(genome.getPopulation().get(i));
//            }
//
//            //Start a race
//            DefaultRace race = new DefaultRace();
//            race.setTrack("aalborg", "road");
//            race.laps = 1;
//
//            //for speedup set withGUI to false
//            results = race.runRace(drivers, false);
//
//            // Save genome/nn
////            DriversUtils.storeGenome(drivers[0]);
//        }
//        // create a checkpoint this allows you to continue this run later
//        DriversUtils.createCheckpoint(this);
//        //DriversUtils.clearCheckpoint();
    }

    public static void main(String[] args) {

        //Set path to torcs.properties
        TorcsConfiguration.getInstance().initialize(new File("torcs_template/torcs.properties"));
        /*
		 *
		 * Start without arguments to run the algorithm
		 * Start with -continue to continue a previous run
		 * Start with -show to show the best found
		 * Start with -show-race to show a race with 10 copies of the best found
		 * Start with -human to race against the best found
		 *
		 */
//        DefaultDriverAlgorithm algorithm = new DefaultDriverAlgorithm();
//        DriversUtils.registerMemory(algorithm.getDriverClass());
//        if (args.length > 0 && args[0].equals("-show")) {
//            new DefaultRace().showBest();
//        } else if (args.length > 0 && args[0].equals("-show-race")) {
//            new DefaultRace().showBestRace();
//        } else if (args.length > 0 && args[0].equals("-human")) {
//            new DefaultRace().raceBest();
//        } else if (args.length > 0 && args[0].equals("-continue")) {
//            if (DriversUtils.hasCheckpoint()) {
//                DriversUtils.loadCheckpoint().run(true);
//            } else {
//                algorithm.run();
//            }
//        } else {
//            algorithm.run();
//        }

//        algorithm.run();

        DefaultDriverGenome driverGenome = new DefaultDriverGenome();
        ArrayList<double[]> population = driverGenome.getPopulation();

//        DefaultDriver driver = new DefaultDriver(population.get(0));

        DefaultDriverAlgorithm algorithm = new DefaultDriverAlgorithm();

        ArrayList<Double> times = new ArrayList<>();

        while (true) {
            population = driverGenome.getPopulation();

            for (int j = 0; j < 10; j++) {
                System.out.println(Arrays.toString(population.get(j)));
            }

            for (int i = 0; i < 10; i++) {
                DefaultDriver driver = new DefaultDriver(population.get(i));
                double laptime = algorithm.getLaptime(driver);
                times.add(laptime);
            }
            driverGenome.selectFittest(times);
            driverGenome.repopulate();
        }

    }

    /*
        Return laptime after 1 lap for a given driver.
     */
    public double getLaptime(DefaultDriver driver) {

        // initialise array for driver
        DefaultDriver[] drivers = new DefaultDriver[1];
        drivers[0] = driver;

        // race specifications
        DefaultRace race = new DefaultRace();
        race.setTrack("forza", "road");
        race.laps = 1;

        // navigate to the right folder to start torcs
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "cd \"E:\\Documents\\IdeaProjects\\JasonStatham\\torcs-win\\Torcs\" && start wtorcs -r quickrace.xml");
            builder.redirectErrorStream(true);
            builder.start();

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        // run a race
        results = race.runRace(drivers, false, true);

        System.out.println(drivers[0].getLapTime());

        return drivers[0].getLapTime();
    }

}