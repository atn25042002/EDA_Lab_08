package com.example.project;

public class BTreeGeneric<E extends Comparable<? super E>> {
    BNodeGeneric<T> root;
    int MinDeg, height;

    public boolean add(E value) {
        if(root.countValidKeys()!= root.keys.capacity()){
            root.keys.add(value);
            return true;
        }
        for(E val: root.keys){
            if(val.compareTo(value)> 0){
                root.search(val).add(E);
                height++;
                return true;
            }
        }
        return false;
    }

    public E remove(E value) {
        root.remove(E);
    }

    public void clear() {
        root= new BNodeGeneric(MinDeg, true);
    }

    public boolean search(E value) {
        return root.search(E)!=null;
    }

    public int size() {
        return height;
    }
}
