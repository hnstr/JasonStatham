import cicontest.torcs.genome.IGenome;

import java.util.*;

public class DefaultDriverGenome implements IGenome {
    private static final long serialVersionUID = 6534186543165341653L;

    private static final int SENSORS_SIZE = 9;
    private static final double UNIFORM_RATE = 0.5;
    private static final double MUTATION_RATE = 0.2;
    private ArrayList<double[]> population = new ArrayList<>();
    private static final int POPULATION_SIZE = 10;

    DefaultDriverGenome() {

        // initialize starting population
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(generateIndividual());
        }
    }

    /*
        Generate starting individual.
     */
    private double[] generateIndividual() {

        double[] individual = new double[SENSORS_SIZE];

        // always set sensor A to 1.0
        individual[0] = 1.0;

        Random random = new Random();

        // populate the array with random numbers
        for (int i = 1; i < SENSORS_SIZE; i++) {
            individual[i] = random.nextDouble() * 89 + 1;
        }

        Arrays.sort(individual);
//        System.out.println(Arrays.toString(individual));

        return individual;
    }

    /*
        Mate two individuals.
     */
    private double[] mate(double[] indiv1, double[] indiv2) {

        double[] child = new double[SENSORS_SIZE];
        child[0] = 1.0;

        // 50% chance of inheriting one parent's sensors
        for (int i = 1; i < SENSORS_SIZE; i++) {

            if (Math.random() <= UNIFORM_RATE) {
                child[i] = indiv1[i];
            } else {
                child[i] = indiv2[i];
            }

            // mutate the sensors, but make sure the smallest sensor is always 1.0
            if (Math.random() <= MUTATION_RATE) {
                if (Math.random() <= UNIFORM_RATE && child[i] > 2.0) {
                    child[i]--;
                } else {
                    child[i]++;
                }
            }
        }

        Arrays.sort(child);

        return child;
    }

    public ArrayList<double[]> getPopulation() {
        return population;
    }

//    /*
//        Cut the 5 slowest individuals from the list.
//     */
//    public void selectFittest(ArrayList<Double> times) {
//
//        // clear list for new use
//        ranking.clear();
//
//        int fastest = 0;
//        int second = 0;
//        int third = 0;
//        int fourth = 0;
//        int fifth = 0;
//
//        // initialise index list for the five best individuals
//        ranking.addAll(Arrays.asList(fastest, second, third, fourth, fifth));
//
//        // select the five best times and their indices
//        for (int i = 0; i < ranking.size(); i++) {
//            int max = times.indexOf(Collections.max(times));
//            ranking.set(i, max);
//            times.set(max, 0.0);
//        }
//
//        // remove the worst five from the population
//        for (int j = 0; j < population.size(); j++) {
//            if (!ranking.contains(j)) {
//                population.set(j, null);
//            }
//        }
//
//    }

    public void sort(ArrayList<Double> times) {
        boolean sorted = true;
        while (sorted) {
            sorted = false;

            for (int i = 0; i < times.size() - 1; i++) {
                int j = i + 1;
                if (times.get(i) > times.get(j)) {
                    double temp = times.get(i);
                    times.set(i, times.get(j));
                    times.set(j, temp);

                    double[] temp2 = population.get(i);
                    population.set(i, population.get(j));
                    population.set(j, temp2);
                    sorted = true;
                }
            }
        }
    }

    /*
        Repopulate the list with pseudo random babies.
     */
    public void repopulate() {
        ArrayList<Integer> ranking = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            population.remove(5);
        }

        // initialise index list for the five best individuals
        ranking.addAll(Arrays.asList(1, 2, 3));

        // make three babies with the fastest individual
        for (Integer aRanking : ranking) {
            population.add(mate(population.get(0), population.get(aRanking)));
        }

        ranking.remove(0);

        // make two babies with the second fastest individual
        for (Integer aRanking : ranking) {
            population.add(mate(population.get(1), population.get(aRanking)));
        }
    }

}

