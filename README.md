# Java-Threaded-AlteredRoundRobin
A simulation of a modified round-robin(includes SRTF scheduling) in java

# Simulating Round Robin Process Scheduling Simulating Round Robin Process Scheduling

## Requirements
Implement the simulation of a process scheduler that is responsible for scheduling a given list of
processes. The scheduler is running on a machine with one CPU. The scheduling policy is a type of non‐preemptive round‐robin scheduling and works as follows:  

- Scheduler works in a cyclic manner, i.e. it gives the CPU to a process for a quantum of time and then get the CPU back.  
- The quantum for each process is equal to 10 percent of the remaining execution time of the process.  
- Each process comes with its own arrival time and burst time.  
- Each time, the scheduler gives the CPU to a process (say P1) that has the shortest remaining processing time, but this should not starve other processes in the queue, and which are ready to start. These processes should be allocated to the CPU before it is given back to P1, i.e. include some fairness for long jobs already in the queue.  
- In the case that two or more processes have equal remaining time for completion, the scheduler gives priority to the older process (i.e. process that has been in the system for longer time). 

## Input
Each line of the input file containsinformation related to one process. The first column
is the arrival time (ready time) of the process and the second column shows the required
execution time for the process.  

Sample “input.txt”:
 1 5
 2 3
 3 1

## Output
Set of strings indicating events in the program including:  
- Start and End of each process
- Start and End of each time slice
- Waiting time for each process

**Note: This sample output is only an example, but doesn't represent an accurate output for our current job scheduler**

Sample “output.txt”:

Time 1, Process 1, Started

Time 1, Process 1, Resumed

Time 2, Process 1, Paused

Time 2, Process 2, Started

Time 2, Process 2, Resumed

Time 3, Process 2, Paused

Time 3, Process 3, Started

Time 3, Process 3, Resumed

Time 4, Process 3, Paused

Time 4, Process 3, Finished

Time 4, Process 2, Resumed

Time 5, Process 2, Paused

Time 5, Process 2, Resumed

Time 6, Process 2, Paused

Time 6, Process 2, Finished

Time 6, Process 1, Resumed

Time 7, Process 1, Paused

Time 7, Process 1, Resumed

Time 8, Process 1, Paused

Time 8, Process 1, Resumed

Time 9, Process 1, Paused

Time 9, Process 1, Resumed

Time 10, Process 1, Paused

Time 10, Process 1, Finished

Waiting Times:
Process 1: 4
Process 2: 2
Process 3: 2 
