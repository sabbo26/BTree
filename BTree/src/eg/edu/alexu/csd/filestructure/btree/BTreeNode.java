package eg.edu.alexu.csd.filestructure.btree;

import java.util.*;

public class BTreeNode<K extends Comparable<K> , V> implements IBTreeNode<K,V> {

    private List<K> keys ;
    private List<V> values ;
    private List<IBTreeNode<K,V>> children ;

    public BTreeNode() {
        this.keys = new LinkedList<>();
        this.values = new LinkedList<>();
        this.children = new LinkedList<>();
    }

    @Override
    public int getNumOfKeys() {
        return keys.size();
    }

    @Override
    public void setNumOfKeys(int numOfKeys) {

    }

    @Override
    public boolean isLeaf() {
        return children.size() == 0 ;
    }

    @Override
    public void setLeaf(boolean isLeaf) {

    }

    @Override
    public List<K> getKeys() {
        return this.keys ;
    }

    @Override
    public void setKeys(List<K> keys) {
        this.keys = keys ;
    }
    @Override
    public List<V> getValues() {
        return this.values ;
    }

    @Override
    public void setValues(List<V> values) {
        this.values = values ;
    }

    @Override
    public List<IBTreeNode<K, V>> getChildren() {
        return this.children ;
    }

    @Override
    public void setChildren(List<IBTreeNode<K, V>> children) {
        this.children = children ;
    }
}