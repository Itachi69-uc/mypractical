import java.util.*;
class Priority {
    public static void main(String[] a) {
        Scanner s=new Scanner(System.in);
        System.out.print("Enter no. of processes: ");
        int n=s.nextInt();
        int bt[]=new int[n],p[]=new int[n],wt[]=new int[n],tat[]=new int[n];
        for(int i=0;i<n;i++){
            System.out.print("Enter Burst time & Priority for P"+(i+1)+": ");
            bt[i]=s.nextInt(); p[i]=s.nextInt();
        }
        for(int i=0;i<n-1;i++) // sort by priority
            for(int j=i+1;j<n;j++)
                if(p[j]<p[i]){int t=p[i];p[i]=p[j];p[j]=t;t=bt[i];bt[i]=bt[j];bt[j]=t;}
        wt[0]=0;
        for(int i=1;i<n;i++) wt[i]=wt[i-1]+bt[i-1];
        for(int i=0;i<n;i++) tat[i]=bt[i]+wt[i];
        System.out.println("\nP\tBT\tPR\tWT\tTAT");
        for(int i=0;i<n;i++)
            System.out.println("P"+(i+1)+"\t"+bt[i]+"\t"+p[i]+"\t"+wt[i]+"\t"+tat[i]);
    }
}
