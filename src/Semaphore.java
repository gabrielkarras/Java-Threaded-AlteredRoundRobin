package test;
//author Serguei A. Mokhov, mokhov@cs.concordia.ca; Inspired by previous code by Prof. D. Probst
public class Semaphore
{
    private int iValue;
    public Semaphore(int piValue)
    {
        this.iValue = piValue;
    }

    public Semaphore()
    {
        this(0);
    }
    public synchronized boolean isLocked()
    {
        return (this.iValue <= 0);
    }
    public synchronized void Wait()
    {
        try
        {
            while(this.iValue <= 0)
            {
                wait();
            }


            this.iValue--;
        }
        catch(InterruptedException e)
        {
            System.out.println
                    (
                            "Semaphore::Wait() - caught InterruptedException: " +
                                    e.getMessage()
                    );

            e.printStackTrace();
        }
    }
    public synchronized void Signal()
    {
        ++this.iValue;
        notify();
    }
    public synchronized void P()
    {
        this.Wait();
    }
    public synchronized void V()
    {
        this.Signal();
    }
}

// EOF


