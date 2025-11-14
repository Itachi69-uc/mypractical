import java.util.*;
class WorstFit {
  public static void main(String[] a) {
    Scanner s=new Scanner(System.in);
    System.out.print("Enter no. of blocks: ");
    int nb=s.nextInt(),b[]=new int[nb];
    System.out.println("Enter block sizes:");
    for(int i=0;i<nb;i++)b[i]=s.nextInt();
    System.out.print("Enter no. of processes: ");
    int np=s.nextInt(),p[]=new int[np],alloc[]=new int[np];
    System.out.println("Enter process sizes:");
    for(int i=0;i<np;i++)p[i]=s.nextInt();
    Arrays.fill(alloc,-1);
    for(int i=0;i<np;i++){
      int worst=-1;
      for(int j=0;j<nb;j++)
        if(b[j]>=p[i]&&(worst==-1||b[j]>b[worst]))worst=j;
      if(worst!=-1){alloc[i]=worst;b[worst]-=p[i];}
    }
    System.out.println("\nP\tSize\tBlock");
    for(int i=0;i<np;i++)
      System.out.println("P"+(i+1)+"\t"+p[i]+"\t"+(alloc[i]==-1?"Not Allocated":alloc[i]+1));
  }
}
