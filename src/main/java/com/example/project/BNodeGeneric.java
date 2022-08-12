import java.util.Vector;
package com.example.project;

public class BNodeGeneric<T extends<Comparable<T>>{

    Vector<T> keys; // keys of nodes
    int MinDeg; // Minimum degree of B-tree node
    Vector<BNodeGeneric> children; // Child node
    boolean isLeaf; // True when leaf node

    // Constructor
    public BNodeGeneric(int deg,boolean isLeaf){
        this.MinDeg = deg;
        this.isLeaf = isLeaf;
        this.keys = new Vector<T>(2*this.MinDeg-1);
        this.children = new BNodeGeneric<T>(2*this.MinDeg);
    }

    // Find the first location index equal to or greater than key
    public int findKey(int key){

        int idx = 0;
        // The conditions for exiting the loop are: 1.idx == num, i.e. scan all of them once
        // 2. IDX < num, i.e. key found or greater than key
        while (idx < num && keys.elementAt(idx) < key)
            ++idx;
        return idx;
    }


    public void remove(int key){

        int idx = findKey(key);
        if (idx < num && keys.elementAt(idx) == key){ // Find key
            if (isLeaf) // key in leaf node
                removeFromLeaf(idx);
            else // key is not in the leaf node
                removeFromNonLeaf(idx);
        }
        else{
            if (isLeaf){ // If the node is a leaf node, then the node is not in the B tree
                System.out.printf("The key %d is does not exist in the tree\n",key);
                return;
            }

            // Otherwise, the key to be deleted exists in the subtree with the node as the root

            // This flag indicates whether the key exists in the subtree whose root is the last child of the node
            // When idx is equal to num, the whole node is compared, and flag is true
            boolean flag = idx == num; 
            
            if (children.elementAt(idx).num < MinDeg) // When the child node of the node is not full, fill it first
                fill(idx);


            //If the last child node has been merged, it must have been merged with the previous child node, so we recurse on the (idx-1) child node.
            // Otherwise, we recurse to the (idx) child node, which now has at least the keys of the minimum degree
            if (flag && idx > num)
                children.elementAt(idx-1).remove(key);
            else
                children.elementAt(idx).remove(key);
        }
    }

    public void removeFromLeaf(int idx){

        // Shift from idx
        for (int i = idx +1;i < num;++i)
            keys.insertElementAt(keys.elementAt(i),i-1);
        num --;
    }

    public void removeFromNonLeaf(int idx){

        int key = keys.elementAt(idx);

        // If the subtree before key (children.elementAt(idx)) has at least t keys
        // Then find the precursor 'pred' of key in the subtree with children.elementAt(idx) as the root
        // Replace key with 'pred', recursively delete pred in children.elementAt(idx)
        if (children.elementAt(idx).num >= MinDeg){
            int pred = getPred(idx);
            keys.insertElementAt(pred,idx);
            children.elementAt(idx).remove(pred);
        }
        // If children.elementAt(idx) has fewer keys than MinDeg, check children.elementAt(idx+1)
        // If children.elementAt(idx+1) has at least MinDeg keys, in the subtree whose root is children.elementAt(idx+1)
        // Find the key's successor 'succ' and recursively delete succ in children.elementAt(idx+1)
        else if (children.elementAt(idx+1).num >= MinDeg){
            int succ = getSucc(idx);
            keys.insertElementAt(succ, idx)
            children.elementAt(idx+1).remove(succ);
        }
        else{
            // If the number of keys of children.elementAt(idx) and children.elementAt(idx+1) is less than MinDeg
            // Then key and children.elementAt(idx+1) are combined into children.elementAt(idx)
            // Now children.elementAt(idx) contains the 2t-1 key
            // Release children.elementAt(idx+1), recursively delete the key in children.elementAt(idx)
            merge(idx);
            children.elementAt(idx).remove(key);
        }
    }

    public int getPred(int idx){ // The predecessor node is the node that always finds the rightmost node from the left subtree

        // Move to the rightmost node until you reach the leaf node
        BNodeGeneric cur = children.elementAt(idx);
        while (!cur.isLeaf)
            cur = cur.children.elementAt(cur.num);
        return cur.keys.elementAt(cur.num-1);
    }

    public int getSucc(int idx){ // Subsequent nodes are found from the right subtree all the way to the left

        // Continue to move the leftmost node from children.elementAt(idx+1) until it reaches the leaf node
        BNodeGeneric cur = children.elementAt(idx+1);
        while (!cur.isLeaf)
            cur = cur.children.firstElement();
        return cur.firstElement();
    }

    // Fill children.elementAt(idx) with less than MinDeg keys
    public void fill(int idx){

        // If the previous child node has multiple MinDeg-1 keys, borrow from them
        if (idx != 0 && children.elementAt(idx-1).num >= MinDeg)
            borrowFromPrev(idx);
        // The latter sub node has multiple MinDeg-1 keys, from which to borrow
        else if (idx != num && children.elementAt(idx+1).num >= MinDeg)
            borrowFromNext(idx);
        else{
            // Merge children.elementAt(idx) and its brothers
            // If children.elementAt(idx) is the last child node
            // Then merge it with the previous child node or merge it with its next sibling
            if (idx != num)
                merge(idx);
            else
                merge(idx-1);
        }
    }

    // Borrow a key from children.elementAt(idx-1) and insert it into children.elementAt(idx)
    public void borrowFromPrev(int idx){

        BNodeGeneric child = children.elementAt(idx);
        BNodeGeneric sibling = children.elementAt(idx-1);

        // The last key from children.elementAt(idx-1) overflows to the parent node
        // The key.elementAt(idx-1) underflow from the parent node is inserted as the first key in children.elementAt(idx)
        // Therefore, sibling decreases by one and children increases by one
        for (int i = child.num-1; i >= 0; --i) // children.elementAt(idx) move forward
            child.keys.insertElementAt(child.keys.elementAt(i),i+1);

        if (!child.isLeaf){ // Move children.elementAt(idx) forward when they are not leaf nodes
            for (int i = child.num; i >= 0; --i)
                child.children.insertElementAt(child.children.elementAt(i),i+1);
        }

        // Set the first key of the child node to the keys of the current node .elementAt(idx-1)
        child.insertElementAt(keys.elementAt(idx-1),0);
        if (!child.isLeaf) // Take the last child of sibling as the first child of children.elementAt(idx)
            child.insertElementAt(sibling.children.elementAt(sibling.num),0)

        // Move the last key of sibling up to the last key of the current node
        keys.insertElementAt(sibling.keys.elementAt(sibling.num-1),idx-1)
        child.num += 1;
        sibling.num -= 1;
    }

    // Symmetric with borowfromprev
    public void borrowFromNext(int idx){

        BNodeGeneric child = children.elementAt(idx);
        BNodeGeneric sibling = children.elementAt(idx+1);

        child.insertElementAt(keys.elementAt(idx),child.num);

        if (!child.isLeaf)
            child.chilfren.insertElementAt(sibling.children.firstElement(),child.num+1)

        keys.insertElementAt(sibling.firstElement(),idx);

        for (int i = 1; i < sibling.num; ++i)
            sibling.insertElementAt(sibling.keys.elementAt(i),i-1);

        if (!sibling.isLeaf){
            for (int i= 1; i <= sibling.num;++i)
                sibling.children.insertElementAt(sibling.children.elementAt(i),i-1);
        }
        child.num += 1;
        sibling.num -= 1;
    }

    // Merge childre.elementAt(idx+1) into childre.elementAt(idx)
    public void merge(int idx){

        BNodeGeneric child = children.elementAt(idx);
        BNodeGeneric sibling = children.elementAt(idx+1);

        // Insert the last key of the current node into the MinDeg-1 position of the child node
        child.keys.insertElementAt(keys.elementAt(idx), MinDeg-1);

        // keys: children.elementAt(idx+1) copy to children.elementAt(idx)
        for (int i =0 ; i< sibling.num; ++i)
            child.keys.insertElementAt(sibling.keys.elementAt(i),i+MinDeg);

        // children: children.elementAt(idx+1) copy to children.elementAt(idx)
        if (!child.isLeaf){
            for (int i = 0;i <= sibling.num; ++i)
                child.children.insertElementAt(sibling.children.elementAt(i),i+MinDeg);
        }

        // Move keys forward, not gap caused by moving keys.elementAt(idx) to children.elementAt(idx)
        for (int i = idx+1; i<num; ++i)
            keys.insertElementAt(keys.elementAt(i),i-1);
        // Move the corresponding child node forward
        for (int i = idx+2;i<=num;++i)
            children.insertElementAt(children.elementAt(i),i-1);

        child.num += sibling.num + 1;
        num--;
    }


    public void insertNotFull(int key){

        int i = num -1; // Initialize i as the rightmost index

        if (isLeaf){ // When it is a leaf node
            // Find the location where the new key should be inserted
            while (i >= 0 && keys.elementAt(i) > key){
                keys.insertElementAt(keys.elementAt(i),i+1); // keys backward shift
                i--;
            }
            keys.insertElementAt(key,i+1);
            num = num +1;
        }
        else{
            // Find the child node location that should be inserted
            while (i >= 0 && keys.elementAt(i) > key)
                i--;
            if (children.elementAt(i+1).num == 2*MinDeg - 1){ // When the child node is full
                splitChild(i+1,children.elementAt(i+1));
                // After splitting, the key in the middle of the child node moves up, and the child node splits into two
                if (keys.elementAt(i+1) < key)
                    i++;
            }
            children.elementAt(i+1).insertNotFull(key);
        }
    }


    public void splitChild(int i ,BNodeGeneric y){

        // First, create a node to hold the keys of MinDeg-1 of y
        BNodeGeneric z = new BNodeGeneric(y.MinDeg,y.isLeaf);
        z.num = MinDeg - 1;

        // Pass the properties of y to z
        for (int j = 0; j < MinDeg-1; j++)
            z.insertElementAt(y.keys.elementAt(j+MinDeg),j)
        if (!y.isLeaf){
            for (int j = 0; j < MinDeg; j++)
                z.children.insertElementAt(y.children.elementAt(j+MinDeg),j);
        }
        y.num = MinDeg-1;

        // Insert a new child into the child
        for (int j = num; j >= i+1; j--)
            children.insertElementAt(children.elementAt(j),j+1);
        children.insertElementAt(z,i+1);

        // Move a key in y to this node
        for (int j = num-1;j >= i;j--)
            keys.insertElementAt(keys.elementAt(j),j+1);
        keys.insertElementAt(y.keys.elementAt(MinDeg-1),i);

        num = num + 1;
    }


    public void traverse(){
        int i;
        for (i = 0; i< num; i++){
            if (!isLeaf)
                children.elementAt(i).traverse();
            System.out.printf(" %d",keys.elementAt(i));
        }

        if (!isLeaf){
            children.elementAt(i).traverse();
        }
    }


    public BNodeGeneric search(int key){
        int i = 0;
        while (i < num && key > keys.elementAt(i))
            i++;

        if (keys.elementAt(i) == key)
            return this;
        if (isLeaf)
            return null;
        return children.elementAt(i).search(key);
    }
}