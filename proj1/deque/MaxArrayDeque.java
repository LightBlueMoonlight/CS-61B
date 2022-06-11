package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    Comparator<T> compar ;

    public MaxArrayDeque(Comparator<T> c){
        compar = c;
    }

    public T max(){
//        if(this.isEmpty() || items == null){
//            return null;
//        }
        int maxDex = 0;
        for (int i =0; i <this.size();i++){
            if(compar.compare(this.get(i),this.get(maxDex)) > 0){
                maxDex = i;
            }
        }
        return this.get(maxDex);
    }

    public T max(Comparator<T> c){

//        if(this.isEmpty() || items == null){
//            return null;
//        }
        int maxDex = 0;
        T returnItem = this.get(maxDex);
        for (int i =0; i <this.size();i++){
            if(c.compare(this.get(i),this.get(maxDex)) > 0){
                maxDex = i;
            }
        }
        return this.get(maxDex);
    }


}
