public class partition {
    private static int[] temp;
    public static int[] a = {5, 6, 9, 2, 4, 4, 5, 0, 1, 3, 7, 8, 2, 0, 1};
    public static void sort(int[] a){
        temp = new int[a.length];
        sort(a,0,a.length-1);
    }
    private static void sort(int[] a, int lo, int hi){
        if(hi <= lo){
            return;
        }
        int mid = lo + (hi-lo)/2;
        sort(a,lo,mid);
        sort(a,mid+1,hi);
        merge(a,lo,mid,hi);
    }
    public static void merge(int[] a, int lo, int mid, int hi){
        int i=lo, j=mid+1;
        for(int k=lo;k<=hi;k++){
            temp[k] = a[k];
        }
        for(int k=lo; k<=hi; k++){
            if(i >mid){
                a[k] = temp[j++];
            }else if(j > hi){
                a[k] = temp[i++];
            }else{
                a[k] = temp[i++];
            }
        }

    }
    public static void main(String[] args) {

        sort(a);
        for(int i=0; i<temp.length; i++){
            System.out.print(a[i] + " ");
        }
        System.out.println();

    }
}