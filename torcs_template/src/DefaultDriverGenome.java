import cicontest.torcs.genome.IGenome;

import java.util.*;

public class DefaultDriverGenome implements IGenome {
    private static final long serialVersionUID = 6534186543165341653L;

    private static final int SENSORS_SIZE = 9;
    private static final double UNIFORM_RATE = 0.5;
    private static final double MUTATION_RATE = 0.2;
    private ArrayList<double[]> population = new ArrayList<>();
    private static final int POPULATION_SIZE = 10;
    private int fastest = 0;
    private int second = 0;
    private ArrayList<Integer> ranking = new ArrayList<>();

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

    /*
        Cut the 5 slowest individuals from the list.
     */
    public void selectFittest(ArrayList<Double> times) {

        // clear list for new use
        ranking.clear();

        int third = 0;
        int fourth = 0;
        int fifth = 0;

        // initialise index list for the five best individuals
        ranking.addAll(Arrays.asList(fastest, second, third, fourth, fifth));

        // select the five best times and their indices
        for (int i = 0; i < ranking.size(); i++) {
            int max = times.indexOf(Collections.max(times));
            ranking.set(i, max);
            times.set(max, 0.0);
        }

        // remove the worst five from the population
        for (int j = 0; j < population.size(); j++) {
            if (!ranking.contains(j)) {
                population.set(j, null);
            }
        }

    }

    /*
        Repopulate the list with pseudo random babies.
     */
    public void repopulate() {

        fastest = ranking.get(0);
        second = ranking.get(1);

        // do not mate with the first and last individual
        ranking.remove(0);
        ranking.remove(ranking.size() - 1);


        // make three babies with the fastest individual
        for (Integer aRanking : ranking) {
            population.add(mate(population.get(fastest), population.get(aRanking)));
        }

        ranking.remove(0);

        // make two babies with the second fastest individual
        for (Integer aRanking : ranking) {
            population.add(mate(population.get(second), population.get(aRanking)));
        }

        // remove old individuals from the population
        ArrayList<double[]> newPopulation = new ArrayList<>();

        for (int i = 0; i < population.size(); i++) {
            if (population.get(i) != null) {
                newPopulation.add(population.get(i));
            }
        }

        population = newPopulation;
    }

}

