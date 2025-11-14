import java.util.*;
class SJF {
    public static void main(String[] a) {
        Scanner s=new Scanner(System.in);
        System.out.print("Enter no. of processes: ");
        int n=s.nextInt();
        int at[]=new int[n],bt[]=new int[n],rt[]=new int[n],wt[]=new int[n],tat[]=new int[n];
        for(int i=0;i<n;i++){
            System.out.print("Enter Arrival & Burst time for P"+(i+1)+": ");
            at[i]=s.nextInt(); bt[i]=s.nextInt(); rt[i]=bt[i];
        }
        int done=0,t=0;
        while(done<n){
            int m=-1,min=999;
            for(int i=0;i<n;i++)
                if(at[i]<=t&&rt[i]>0&&rt[i]<min){min=rt[i];m=i;}
            if(m==-1){t++;continue;}
            rt[m]--; t++;
            if(rt[m]==0){done++;wt[m]=t-at[m]-bt[m];if(wt[m]<0)wt[m]=0;}
        }
        System.out.println("\nP\tAT\tBT\tWT\tTAT");
        for(int i=0;i<n;i++){
            tat[i]=bt[i]+wt[i];
            System.out.println("P"+(i+1)+"\t"+at[i]+"\t"+bt[i]+"\t"+wt[i]+"\t"+tat[i]);
        }
    }
}
