/**
    @Author(s) Gabriel Karras-40036341, Tianyang Jin-40045932
    @Date 03/10/2020
    @Title Simulating a Hybrid Round-Robin and SRTF Process Scheduler with Threads

    @Architecture
        -Semaphore: A Java class imported from a previous project created by Serguei A. Mokhov
                    Implements semaphores with wait and signal

        -Main: Implements hybrid round robin scheduler and runs main program process.
               Includes static classes Scheduler and Process

        -Scheduler:

        -Process:

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

public class Main {

    /* Global Data Field */
    static int turn=-1; // Indicates the turn of process

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
    public static void main(String[] args){

        /* Reading input.txt */

        /* Initialize processes */
        Process p1 = new Process(10,1,0);
        Process p2 = new Process(9,2,1);
        Process p3 = new Process(4,10,2);

        // Array of processes
        Process[] ps = {p1,p2,p3};

        /* Initialize Scheduler and run every thread for each process */
        Scheduler thread = new Scheduler(ps);
        for(int i = 0; i < ps.length; i++){
            ps[i].start(); // Start threaded process
        }
        thread.start(); // Start scheduler

        /* Write to output.txt */
    }

    /* Scheduler Class */
    static class Scheduler extends Thread{

        /* Member Data Field */
        Process[] ps; // Array of processes(emulates Ready queue)
        double timer = 0; // Time slice

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

            System.out.println("scheduler start");

            /* Verify the arrival time of all processes */
            double beginning = ps[0].arrival; // Start with arrival time of first process
            for(int i = 1; i < ps.length; i++){
                if(ps[i].arrival < beginning) beginning = ps[i].arrival;// Earliest arrival time will initiate scheduler
            }

            timer = beginning; // Initiate Scheduler timer with earliest arrival time
            System.out.println("Timer is: " + timer);

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
                System.out.println("timer is:" + timer);

                /* Verify if all processes are terminated */
                boolean end = false; // Are all processes terminated? By default said to false.
                for(int i = 0; i < ps.length; i++){
                    end = end||ps[i].running;
                }
                /* If the scheduler is done then stop */
                if(!end) {
                    System.out.println("Scheduler finished");
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
        public double burst; // CPU burst time of the process(as well as the remaining time)
        public double arrival; // Arrival time of process
        public boolean running = true; // Status of process(running or waiting)

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
            System.out.println("process[" + ID + "] start");
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

                burst = 0.9 * burst; // Decrease remaining time by q(%10)
                System.out.println("process[" + ID + "] remaining is: " + burst);
                /* Exiting critical section */
                mutex2.Signal();

                /*
                * Verify if process is terminated
                * Threshold/Cut-off remaining time is 0.1
                * Any remaining burst time for a process that is less than cut-off is considered terminated
                * */
                if (burst < 0.1) {
                    System.out.println("process[" + ID + "] finished");
                    mutex2.Signal(); // Signal other processes
                    running = false; // Status is terminated
                    break;
                }
            }
        }
    }
}
