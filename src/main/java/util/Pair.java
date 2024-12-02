package util;

public class Pair<K, V> {
    public K first;
    public V second;

    public Pair(K k, V v){
        first = k;
        second = v;
    }
    public Pair(){

    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(!(obj instanceof Pair<?, ?> pair)) return false;
        if(!pair.first.equals(this.first)) return false;
        if(!pair.second.equals(this.second)) return false;
        return true;
    }

    @Override
    public int hashCode(){
        return 37 * first.hashCode() + 17 * second.hashCode();
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
