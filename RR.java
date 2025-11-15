import java.util.*;

class test {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();

        int burst[] = new int[n];        // original burst time
        int remain[] = new int[n];       // remaining time
        int wait[] = new int[n];         // waiting time

        System.out.println("Enter burst time of each process:");
        for (int i = 0; i < n; i++) {
            burst[i] = sc.nextInt();
            remain[i] = burst[i];        // initially same
        }

        System.out.print("Enter time quantum: ");
        int quantum = sc.nextInt();

        int time = 0;   // current time

        while (true) {
            boolean finished = true;

            for (int i = 0; i < n; i++) {

                if (remain[i] > 0) {     // process still running
                    finished = false;

                    if (remain[i] > quantum) {
                        time += quantum;
                        remain[i] -= quantum;
                    }
                    else {
                        time += remain[i];
                        wait[i] = time - burst[i];  // waiting time
                        remain[i] = 0;
                    }
                }
            }

            if (finished)
                break;
        }

        System.out.println("\nProcess\tBurst\tWaiting\tTurnAround");
        for (int i = 0; i < n; i++) {
            int tat = burst[i] + wait[i];
            System.out.println("P" + (i + 1) + "\t" + burst[i] + "\t" + wait[i] + "\t" + tat);
        }
    }
}
