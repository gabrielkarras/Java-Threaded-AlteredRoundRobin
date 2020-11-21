package test;

public class aaaa {
    static int turn=-1;
    static Semaphore s1=new Semaphore(0);

    //these two semaphore is used to do the mutual exclusion
    static Semaphore mutex1=new Semaphore(0);
    static Semaphore mutex2=new Semaphore(1);

    public static void main(String[] args) throws InterruptedException {
        Process p1=new Process(10,1,0);
        Process p2=new Process(9,2,1);
        Process p3=new Process(4,10,2);
        Process ps[]={p1,p2,p3};
        scheduler thread=new scheduler(ps);
        for(int i=0;i<ps.length;i++){
            ps[i].start();
        }
        thread.start();
    }

    static class scheduler extends Thread{
        Process ps[];
        double timer=0;
        scheduler(Process ps[]){
            this.ps=ps;
        }
        public void run(){
            //check the arrival time and take the minimum number of the arrival time
            System.out.println("scheduler start");
            double beginning=ps[0].arrival;
            for(int i=1;i<ps.length;i++){
                if(ps[i].arrival<beginning) beginning=ps[i].arrival;
            }
            timer=beginning;
            System.out.println("Timer is: "+timer);

            //schedule the process
            while(true) {
                //System.out.println("scheduler start");
                mutex2.Wait();
                //turn=-1;System.out.println("111111111111111");
                int thisturn=0;
                double min = 10000000;

                //this for loop is used to check the minimum burst time
                for (int i = 0; i < ps.length; i++) {
                    if(ps[i].running==true&&timer>=ps[i].arrival) {
                        //this if statement is used to check if two processes have the same burst time and give the CPU to the older process

                        if (ps[i].a <= min) {
                                if(ps[i].a==min&&ps[i].arrival>ps[thisturn].arrival) {
                                }
                            else{
                                thisturn = i;
                                min = ps[i].a;
                            }
                        }
                    }
                }

                turn=thisturn;

                timer+=ps[turn].a*0.1; //increment of timer

                System.out.println("timer is:" +timer);

                //check if all processes are finished
                boolean end=false;
                for(int i=0;i<ps.length;i++){
                    end=end||ps[i].running;
                }
                if(end==false) {
                    System.out.println("Scheduler finished");
                    break;
                }
                mutex1.Signal();
            }
        }
    }

     static class Process extends Thread{
        int ID;
        public double a; // this is the burst time
        public double arrival;
        public boolean running=true;
        Process(double a,double arrival, int ID){
            this.a=a;
            this.ID=ID;
            this.arrival=arrival;
        }
        public void run() {
            System.out.println("process["+ID+"] start");
            while (true) {

                //used to check which process should be executed
                while (turn != ID) {
                    try {
                        sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mutex1.Wait();

                a = 0.9 * a;//decrease the burst time

                System.out.println("process[" + ID + "] remaining is: " + a);
                mutex2.Signal(); //System.out.println("signal");

                //check if the process is finished
                if (a < 0.1) {
                    System.out.println("process["+ID+ "] finished");
                    mutex2.Signal();
                    running = false;
                    break;
                }
            }
        }
    }
}
