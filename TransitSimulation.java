import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulates a queuing theory process meant to replicate the events that take
 * place during a series of bus stop interactions.
 *
 * @author Blake Whitman
 *
 */
public final class TransitInformation {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private TransitInformation() {
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        Scanner in = new Scanner(System.in);

        /*
         * The following constants are used throughout the simulation:
         */
        final int capacity = 40; // size of each bus
        final int simLength = 1440; // length of simulation in minutes
        final int numBuses = 4; // total number of buses in circulation
        final int numStops = 5; // total number of bus stops
        final int timePerBus = 20; // time for a bus to go from stop i-->i+1

        /*
         * These values fluctuate as the simulation proceeds:
         */
        int departing = 0; // departing value to be filled during each iteration
        int time = 1; // initial time value in minutes
        int timeArrived = 0; // time each waiting patron arrives in line
        int maxDeparting = 0; // max departing value filled during simulation
        int minDeparting = 0; // min departing value filled during simulation
        int eachTime = 0;

        /*
         * These arrays track the total number of passengers that wait at each
         * stop throughout the simulation, along with the total time they
         * waited. These totals are then used to compile the average time waited
         * per stop.
         */
        int[] numWaiting = new int[numStops];
        int[] timeWaiting = new int[numStops];

        /*
         * Creates an array of Queues meant to keep track of the arrival and
         * departure of passengers in line at stops 1-numStops. Each queue is
         * initialized as a new LinkedList.
         */
        @SuppressWarnings("unchecked")
        Queue<Integer>[] allQueues = new Queue[numStops];
        for (int count = 0; count < numStops; count++) {
            allQueues[count] = new LinkedList<>();
        }

        /*
         * Metric used to determine the allotted intervals between buses. This
         * allows bus inventory to be evenly distributed throughout the routes.
         */
        int distanceBetween = (timePerBus * numStops) / (numBuses);

        /*
         * Initializes a "waiting array" with the initial amount of people
         * waiting at each stop 1-numStops. The values are determined with a
         * random integer [0, 20].
         */
        int[] waiting = new int[numStops];
        for (int i = 0; i < waiting.length; i++) {
            waiting[i] = 0;
        }
        /*
         * Initializes a "passengers array" with the initial amount of people
         * waiting on each bus 1-numBuses. The values are determined with a
         * random integer [0, capacity].
         */
        int[] passengers = new int[numBuses];
        for (int j = 0; j < passengers.length; j++) {
            passengers[j] = 0;
        }

        /*
         * Initializes the divisor of the fraction that computes the number of
         * passengers exiting a bus. For instance, if a bus stops at Stop 1,
         * then the minimum number of passengers that exit that bus will be
         * (passengers[stop1] / minimumDeparting[stop1]), which is 6 in this
         * case.
         */
        int[] minimumDeparting = new int[numStops];
        minimumDeparting[0] = 6;
        minimumDeparting[1] = 6;
        minimumDeparting[2] = 2;
        minimumDeparting[3] = 2;
        minimumDeparting[4] = 3;

        /*
         * Creates two arrays to track the number of times a bus has passed a
         * particular spot, as well as iterate through the different stops.
         */
        int[] n = new int[numBuses];
        int[] iterator = new int[numBuses];
        for (int a = 0; a < numBuses; a++) {
            iterator[a] = 1;
        }

        String[] stopNames = new String[numStops];
        stopNames[0] = "St. John Arena";
        stopNames[1] = "Knowlton Hall";
        stopNames[2] = "the RPAC";
        stopNames[3] = "Thompson Library";
        stopNames[4] = "the Student Union";

        /*
         * Sets initial arrival rate for each stop 1, ... , numStops between the
         * range of ([arriveLow, arriveHigh], inclusive.
         */
        int[] arriveLow = new int[numStops];
        int[] arriveHigh = new int[numStops];
        arriveLow[0] = 7;
        arriveHigh[0] = 11;
        arriveLow[1] = 12;
        arriveHigh[1] = 14;
        arriveLow[2] = 3; // allow for user input here based on desired range
        arriveHigh[2] = 7;
        arriveLow[3] = 4;
        arriveHigh[3] = 10;
        arriveLow[4] = 8;
        arriveHigh[4] = 12;

        /*
         * Computes the initial arrival time for each stop using a random time
         * between the range of [arriveLow, arriveHigh], inclusive.
         */
        int[] timePerArrival = new int[numStops];
        for (int a = 0; a < timePerArrival.length; a++) {
            timePerArrival[a] = ThreadLocalRandom.current()
                    .nextInt(arriveLow[a], arriveHigh[a] + 1);
        }

        /*
         * Used to print out all wait times to an external file.
         */
        ArrayList<Integer> allTimes = new ArrayList<>();

        @SuppressWarnings("unchecked")
        ArrayList<Integer>[] arrayOfList = new ArrayList[numStops];
        for (int b = 0; b < numStops; b++) {
            arrayOfList[b] = new ArrayList<>();
        }

        /*
         * Outputs header for data table.
         */
        System.out.print("Time \t");
        for (int k = 1; k <= numStops; k++) {
            System.out.print("Stop " + k + "\t");
        }

        for (int m = 1; m <= numBuses; m++) {
            System.out.print("Bus " + m + "\t");
        }
        System.out.print("\n");

        while (time <= simLength) { // simulates until the specified end time
            /*
             * Adds one customer to a line when the total time is divisible by
             * the calculated random arrival rate. Then queues this value to
             * keep track of how long that particular customer waited in line.
             * Finally, produces another random arrival rate for the next
             * customer and continues iteration.
             */
            for (int d = 0; d < numStops; d++) {
                if (time % timePerArrival[d] == 0) {
                    waiting[d] = waiting[d] + 1;
                    allQueues[d].add(time);
                    timePerArrival[d] = time + ThreadLocalRandom.current()
                            .nextInt(arriveLow[d], arriveHigh[d] + 1);
                }
            }

            for (int count = 0; count < numBuses; count++) {
                if (time % ((iterator[count] * timePerBus)
                        + (count * distanceBetween)) == 0) {

                    n[count] = n[count] + 1;

                    int stop = iterator[count];
                    if (stop > numStops) {
                        int r = stop % numStops;
                        if (r == 0) {
                            stop = numStops;
                        } else {
                            stop = r;
                        }
                    }
                    stop = stop - 1;

                    iterator[count]++;

                    maxDeparting = passengers[count];
                    minDeparting = passengers[count] / minimumDeparting[stop];

                    departing = ThreadLocalRandom.current()
                            .nextInt(minDeparting, maxDeparting + 1);
                    passengers[count] = passengers[count] - departing;
                    if (waiting[stop] <= capacity - passengers[count]) {
                        while (waiting[stop] != 0
                                && allQueues[stop].size() > 0) {

                            timeArrived = allQueues[stop].remove();
                            numWaiting[stop] = numWaiting[stop] + 1;
                            timeWaiting[stop] = timeWaiting[stop]
                                    + (count * distanceBetween
                                            + timePerBus * n[count])
                                    - timeArrived;
                            eachTime = (count * distanceBetween
                                    + timePerBus * n[count]) - timeArrived;
                            allTimes.add(eachTime);

                            arrayOfList[stop].add(eachTime);

                            waiting[stop]--;
                            passengers[count]++;
                        }
                    } else {
                        while (passengers[count] != capacity
                                && allQueues[stop].size() > 0) {
                            timeArrived = allQueues[stop].remove();
                            numWaiting[stop] = numWaiting[stop] + 1;
                            timeWaiting[stop] = timeWaiting[stop]
                                    + (count * distanceBetween
                                            + timePerBus * n[count])
                                    - timeArrived;
                            eachTime = (count * distanceBetween
                                    + timePerBus * n[count]) - timeArrived;
                            allTimes.add(eachTime);

                            arrayOfList[stop].add(eachTime);

                            waiting[stop]--;
                            passengers[count]++;
                        }
                    }
                }
            }

            /*
             * Prints out data table with the current number of passengers on
             * buses Pi, ... , PnumBuses and waiting at stops Wj, ... ,
             * WnumStops.
             */
            System.out.print(time + "\t");
            for (int p = 0; p < numStops; p++) {
                System.out.print(waiting[p] + "\t");
            }
            for (int b = 0; b < numBuses; b++) {
                System.out.print(passengers[b] + "\t");
            }
            System.out.print("\n");
            time = time + 1;
        }
        /*
         * Computes the average time in minutes that each customer had to wait
         * in lines 1-numStops.
         */
        double[] avg = new double[numStops];
        for (int r = 0; r < numStops; r++) {
            avg[r] = (double) timeWaiting[r] / numWaiting[r];
        }

        /*
         * Prints out the average time waited per customer in lines 1-numStops,
         * rounded to the nearest hundredth place.
         */
        for (int d = 0; d < numStops; d++) {
            System.out.printf(
                    "The average time waited at " + stopNames[d] + " is %.2f",
                    avg[d]);
            System.out.println(" minutes.");
        }

        /*
         * Outputs the time waited for each individual passenger into a .csv
         * file in order to analyze the data.
         */
        String csvAll = "all-data.csv";
        PrintWriter writer = new PrintWriter(csvAll);
        while (allTimes.size() != 0) {
            int oneTime = allTimes.remove(0);
            writer.println(oneTime);
        }

        String csv1 = "data1.csv";
        PrintWriter writer1 = new PrintWriter(csv1);
        while (arrayOfList[0].size() != 0) {
            int oneTime = arrayOfList[0].remove(0);
            writer1.println(oneTime);
        }

        String csv2 = "data2.csv";
        PrintWriter writer2 = new PrintWriter(csv2);
        while (arrayOfList[1].size() != 0) {
            int oneTime = arrayOfList[1].remove(0);
            writer2.println(oneTime);
        }

        String csv3 = "data3.csv";
        PrintWriter writer3 = new PrintWriter(csv3);
        while (arrayOfList[2].size() != 0) {
            int oneTime = arrayOfList[2].remove(0);
            writer3.println(oneTime);
        }

        String csv4 = "data4.csv";
        PrintWriter writer4 = new PrintWriter(csv4);
        while (arrayOfList[3].size() != 0) {
            int oneTime = arrayOfList[3].remove(0);
            writer4.println(oneTime);
        }

        String csv5 = "data5.csv";
        PrintWriter writer5 = new PrintWriter(csv5);
        while (arrayOfList[4].size() != 0) {
            int oneTime = arrayOfList[4].remove(0);
            writer5.println(oneTime);
        }

        in.close();
        writer.close();
        writer1.close();
        writer2.close();
        writer3.close();
        writer4.close();
        writer5.close();

    }
}
