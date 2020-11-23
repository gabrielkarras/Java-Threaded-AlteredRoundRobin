/**
    @Author(s) Gabriel Karras-40036341, Tianyang Jin-40045932
    @Date 03/10/2020
    @Title Simulating a Hybrid Round-Robin and SRTF Process Scheduler with Threads

    @Architecture
        -Semaphore: A Java class imported from a previous project created by Serguei A. Mokhov
                    Implements semaphores with wait and signal

        -Main: Implements hybrid round robin scheduler and runs main program process.
               Reads from input.txt, populates scheduler with processes and writes to output.txt
               Includes static classes Scheduler and Process

        -Scheduler: Implements the scheduler logic by searching for shortest remaining time and allocating time
                    time quantum q(10% of total remaining execution time)

        -Process: Implements the process logic by continually asking the scheduler for CPU resource until it reaches
                  the cut-off time(when remaining time < 0.1) then it is considered terminated

    @Description
    Implement the simulation of a process scheduler that is responsible for scheduling a given list of
    processes. The scheduler is running on a machine with one CPU. The scheduling policy is a
    type of non‐preemptive round‐robin scheduling and works as follows:

    ‐ Scheduler works in a cyclic manner, i.e. it gives the CPU to a process for a quantum of time and
    then get the CPU back.

    ‐ The quantum for each process is equal to 10 percent of the remaining execution time of the
    process.

    ‐ Each process comes with its own arrival time and burst time.

    ‐ Each time, the scheduler gives the CPU to a process (say P1) that has the shortest remaining
    processing time, but this should not starve other processes in the queue, and which are ready to
    start. These processes should be allocated to the CPU before it is given back to P1, i.e. include some
    fairness for long jobs already in the queue.

    ‐ In the case that two or more processes have equal remaining time for completion, the scheduler
    gives priority to the older process (i.e. process that has been in the system for longer time).

 */

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Scanner;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Main {

    /* Global Data Field */
    static int turn=-1; // Indicates the turn of process
    public static double timer = 0; // Time slice of system
    static StringBuilder outputFile = new StringBuilder(); // Will create text format for output file

    /* Global Semaphore Data Field */
    static Semaphore mutex1 = new Semaphore(0);
    static Semaphore mutex2 = new Semaphore(1);


    /**
     * Reads the rows from input.txt in which the first digit in each row represents the arrival time.
     * The second digit in each row represents the CPU burst time.
     *
     * Initialize every process in input.txt and store them in an array of processes.
     *
     * Initialize the scheduler.
     * Start every process thread and then start the scheduler thread.
     * Call in the scheduler(start scheduler thread)
     *
     * Write Scheduler result into output.txt
     *
     * @param args Default string argument for main
     */
    public static void main(String[] args) throws FileNotFoundException, InterruptedException {

        /* Reading input.txt */
        String f1 = "input.txt"; // File name
        double[] entries = ReadFile(f1); // Process Stats:Burst Time and Arrival Time(2 entries per process)

        //TODO Try to find a cleaner way of initializing processes

        /* Initialize processes */
        int number_of_processes = entries.length/2; // 2 entries per process->Total number of processes = #entries/2

        /* For each process i we grab entry 2*i for arrival time and 2*i + 1 for CPU burst time */
        Process[] ps = new Process[number_of_processes];
        for(int i = 0; i < ps.length; i++){
            double cputime = entries[2*i + 1]; // second entry of row
            double arrival = entries[2*i]; // first entry of row
            ps[i] = new Process(cputime, arrival, i); // Initialize process and add to queue
        }

        /* Initialize Scheduler and run every thread for each process */
        Scheduler thread = new Scheduler(ps);
        try {
            for (int i = 0; i < ps.length; i++) {
                ps[i].start(); // Start threaded process
            }
            thread.start(); // Start scheduler
        }
        catch(Exception e){
            e.getStackTrace();
        }

        /* Wait for all threads to terminate to proceed with writing */
        try {
            for (int i = 0; i < ps.length; i++) {
                ps[i].join(); // Start threaded process
            }
            thread.join(); // Start scheduler
        }
        catch(Exception e){
            e.getStackTrace();
        }

        /* Write to output.txt */
        try{
            FileWriter output = new FileWriter("./output.txt");
            output.write(outputFile.toString());
            output.close();
        }
        catch(Exception e){
            e.fillInStackTrace();
        }
    }

    /* Scheduler Class */
    static class Scheduler extends Thread{

        /* Member Data Field */
        Process[] ps; // Array of processes(emulates Ready queue)

        /**
         * Default constructor for Scheduler
         * @param ps Array of process which will be managed by scheduler
         */
        Scheduler(Process[] ps){
            this.ps = ps;
        }

        /**
         * Runs scheduler thread for hybrid round robin
         *
         * Initially searches for the earliest arrival time and initializes the scheduler time to the earliest
         * arrival time
         *
         * The scheduler then searches throughout the processes that have arrived or are currently waiting for the
         * smallest CPU burst time(smallest remaining execution time). It schedule the process with the smallest
         * remaining execution time. It continues until every process is terminated.
         */
        public void run(){

            System.out.println("Scheduler Start\n");
            outputFile.append("Scheduler Start\n");

            /* Verify the arrival time of all processes */
            double beginning = ps[0].arrival; // Start with arrival time of first process
            for(int i = 1; i < ps.length; i++){
                if(ps[i].arrival < beginning) beginning = ps[i].arrival;// Earliest arrival time will initiate scheduler
            }

            timer = beginning; // Initiate Scheduler timer with earliest arrival time
            System.out.println("First process arrives at " + timer + "\n");
            outputFile.append("\nFirst process arrives at " + timer + "\n\n");

            /* Schedule the processes */
            while(true) {

                /* Entering critical section */
                mutex2.Wait();
                int thisturn = 0; // Current turn of which process will run
                double min = 10000000; // Arbitrarily large number for search

                /* Search for smallest burst time within all processes */
                for (int i = 0; i < ps.length; i++) {
                    /* Verifies if the process has arrived and is ready to run */
                    if(ps[i].running==true && timer >= ps[i].arrival) {

                        /* If process burst time is the smaller than min*/
                        if (ps[i].burst <= min) {
                            /* Verifies if processes the same burst time. The scheduler will select the older process */
                            if(ps[i].burst==min && ps[i].arrival > ps[thisturn].arrival) {
                                //busy waiting
                            }
                            else {
                                thisturn = i; // Sets new turn to process with smallest remaining time
                                min = ps[i].burst; // Sets new minimum remaining time
                            }
                        }
                    }
                }

                turn = thisturn; // Sets process id with current smallest remaining time
                timer += ps[turn].burst*0.1; // Increment scheduler timer by q(10%)

                /* Verify if all processes are terminated */
                boolean end = false; // Are all processes terminated? By default said to false.
                for(int i = 0; i < ps.length; i++){
                    end = end||ps[i].running;
                }

                /* If the scheduler is done then stop */
                if(!end) {

                    System.out.println("\n---------------------------------------------------------------------------");
                    System.out.println("Scheduler finished");
                    outputFile.append("\n-------------------------------------------------------------------------\n");
                    outputFile.append("Scheduler finished\n");

                    for(int i=0;i<ps.length;i++){   //this for loop is used to print the waiting time for each process
                        System.out.println("The waiting time for process " + i + " is: "+ps[i].waiting);
                        outputFile.append("The waiting time for process " + i + " is: "+ps[i].waiting + "\n");
                    }
                    break;
                }

                /* Exiting critical section */
                mutex1.Signal();
            }
        }
    }

    /* Process Class */
     static class Process extends Thread{

         /* Member Data Field */
        int ID; // ID of process-will help with determining turn
        public double burst; // Remaining CPU burst time of the process
        public double arrival; // Arrival time of process
        public boolean running = true; // Status of process(running or waiting)
        public double totalbursttime;//The overall CPU burst time
        public double waiting;//Waiting time of the process
        public double finish;//The finish time of the process;

        /**
         * Default constructor for Process
         * @param cpuburst CPU burst time of process
         * @param arrival Arrival time of process
         * @param ID Process ID
         */
        Process(double cpuburst, double arrival, int ID){
            this.burst = cpuburst;
            this.ID = ID;
            this.arrival = arrival;
            this.totalbursttime=cpuburst;
        }

        /**
         * Runs process thread
         *
         * Processes will continually ask the scheduler if it is their turn to run
         * If it is not their turn, then they wait
         * If it is their turn, then they decrease their remaining time by q(10%)
         *
         * Processes are then considered terminated if their remaining time is less than 0.1
         */
        public void run() {

            /* Process will continually verify */
            while (true) {

                /* Verifies it is it the process's turn */
                while (turn != ID) {
                    try {
                        sleep(1); // wait
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                /* Entering critical section */
                mutex1.Wait();

                double start_timer = timer - 0.1*burst; // Will give us the %10 of burst to help with output formatting
                burst = 0.9 * burst; // Decrease remaining time by q(%10)


                /* Print content */
                System.out.println("Time " + start_timer + ", Process "+ID+", Started");
                System.out.println("Time " + start_timer + ", Process "+ID+", Resumed");
                System.out.println("Time " + timer + ", Process "+ID+", Paused");
                outputFile.append("Time " + start_timer + ", Process "+ID+", Started\n");
                outputFile.append("Time " + start_timer + ", Process "+ID+", Resumed\n");
                outputFile.append("Time " + timer + ", Process "+ID+", Paused\n");
                
                turn=-1;//reset turn

                /* Exiting critical section */

                /*
                * Verify if process is terminated
                * Threshold/Cut-off remaining time is 0.1
                * Any remaining burst time for a process that is less than cut-off is considered terminated
                * */
                if (burst < 0.1) {

                    System.out.println("Time " + timer + ", Process " + ID + ", Finished");
                    outputFile.append("Time " + timer + ", Process " + ID + ", Finished\n");

                    running = false; // Status is terminated
                    finish = timer;

                    double temp;
                    temp = finish - arrival - totalbursttime + burst;
                    waiting = round1(temp,1);  //set the scale of the waiting time

                    mutex2.Signal(); // Signal other processes
                    break;
                }

                mutex2.Signal(); // Signal other processes
            }
        }
    }

    /* Helper Methods*/

    /**
     * Accepts an input file in which the first digit represents the size of the array and
     * the following digits represent the states of the light bulb(0 or 1).
     * The method will then return an array populated by these light bulb states.
     *
     * @param fileName Name of the file and its file path. Must be located outside of src folder
     * @return array of entries(arrival time and CPU burst time altogether)
     * @throws FileNotFoundException Throws exception if system couldn't file the file
     */
    public static double[] ReadFile(String fileName) throws FileNotFoundException {

        StringBuilder inputText = new StringBuilder(); // Read from file and create a String

        Scanner in = new Scanner(new FileReader(fileName)); // Reader object
        /* If there's a line within the file then append that line into the string builder */
        while(in.hasNextLine()){
            inputText.append(in.nextLine() + " "); // Separate each line with a space
        }
        in.close(); // Close Reader

        String values = inputText.toString(); // Convert string builder to string
        String[] arrayValue = values.split(" "); // Separate each digit of the string

        /* Covert array of strings(w/digits) to array of integers for process initializing */
        double[] array = new double[arrayValue.length];
        for(int i = 0; i < arrayValue.length; i++){
            array[i] = Double.parseDouble(arrayValue[i]);
        }

        return array; // return array of integers
    }

    /**
     * Truncate double precision values
     * @param input The double precision value
     * @param scale The amount of decimal numbers(how many decimal points we wish to have)
     * @return Truncated value
     */
    public static double round1(double input, int scale){
        BigDecimal bigDecimal=new BigDecimal(input).setScale(scale, RoundingMode.HALF_EVEN);
        return bigDecimal.doubleValue();
    }
    
}
