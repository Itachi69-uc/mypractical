import java.util.*;

class RR {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();

        int burstTime[] = new int[n];
        int remainingTime[] = new int[n];
        int waitingTime[] = new int[n];

        System.out.print("Enter burst times: ");
        for (int i = 0; i < n; i++) {
            burstTime[i] = sc.nextInt();
            remainingTime[i] = burstTime[i];
        }

        System.out.print("Enter time quantum: ");
        int quantum = sc.nextInt();

        int currentTime = 0;

        while (true) {
            boolean allFinished = true;

            for (int i = 0; i < n; i++) {

                if (remainingTime[i] > 0) {
                    allFinished = false;

                    int execute = Math.min(remainingTime[i], quantum);
                    remainingTime[i] -= execute;
                    currentTime += execute;

                    if (remainingTime[i] == 0) {
                        waitingTime[i] = currentTime - burstTime[i];
                    }
                }
            }

            if (allFinished) break;
        }

        System.out.println("\nProcess\tBurst\tWaiting\tTurnAround");
        for (int i = 0; i < n; i++) {
            int turnAround = burstTime[i] + waitingTime[i];
            System.out.println("P" + (i+1) + "\t" +
                               burstTime[i] + "\t" +
                               waitingTime[i] + "\t" +
                               turnAround);
        }
    }
}
