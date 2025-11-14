import java.util.*;
class RR {
    public static void main(String[] a) {
        Scanner s=new Scanner(System.in);
        System.out.print("Enter no. of processes: ");
        int n=s.nextInt();
        int bt[]=new int[n],rt[]=new int[n],wt[]=new int[n],tat[]=new int[n];
        System.out.print("Enter burst times: ");
        for(int i=0;i<n;i++){bt[i]=s.nextInt();rt[i]=bt[i];}
        System.out.print("Enter time quantum: ");
        int tq=s.nextInt(),t=0;
        while(true){
            boolean done=true;
            for(int i=0;i<n;i++)
                if(rt[i]>0){done=false;
                    if(rt[i]>tq){rt[i]-=tq;t+=tq;}
                    else{t+=rt[i];wt[i]=t-bt[i];rt[i]=0;}
                }
            if(done)break;
        }
        System.out.println("\nP\tBT\tWT\tTAT");
        for(int i=0;i<n;i++){
            tat[i]=bt[i]+wt[i];
            System.out.println("P"+(i+1)+"\t"+bt[i]+"\t"+wt[i]+"\t"+tat[i]);
        }
    }
}
