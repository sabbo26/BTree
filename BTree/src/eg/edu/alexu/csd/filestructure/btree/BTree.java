package eg.edu.alexu.csd.filestructure.btree;

import javax.management.RuntimeErrorException;
import java.util.List;

public class BTree<K extends Comparable<K>,V> implements IBTree<K,V> {

    private IBTreeNode<K,V> root ;
    private int minDegree ;

    public BTree(int minDegree) {
        this.minDegree = minDegree ;
    }

    @Override
    public int getMinimumDegree() {
        return this.minDegree ;
    }

    @Override
    public IBTreeNode<K, V> getRoot() {
        return this.root ;
    }

    @Override
    public void insert(K key, V value) {
        if(key == null || value == null || search(key) != null )
            return ;
        if(this.root == null ){
            this.root = new BTreeNode<>();
            this.root.getKeys().add(key);
            this.root.getValues().add(value);
        }
        else{
            IBTreeNode<K,V> temp = this.root ;
            while(!temp.isLeaf()){
                List<K> tempKeys = temp.getKeys();
                if(key.compareTo(tempKeys.get(tempKeys.size()-1)) > 0 )
                    temp = temp.getChildren().get(temp.getChildren().size()-1);
                else{
                    for(int i = 0 ; i < tempKeys.size() ; i++){
                        if(key.compareTo(tempKeys.get(i)) < 0 ){
                            temp = temp.getChildren().get(i);
                            break ;
                        }
                    }
                }
            }
            entry(temp,key,value);
            if(isFull(temp))
                splitNode(temp);
        }
    }

    @Override
    public V search(K key) {
       if( key == null || this.root == null ) return null ;
       IBTreeNode<K,V> temp = this.root ;
       while(true){
           List<K> tempKeys = temp.getKeys();
            if( key.compareTo(tempKeys.get(tempKeys.size()-1)) > 0 ){
                if(temp.isLeaf())
                    return null ;
                temp = temp.getChildren().get(temp.getChildren().size()-1);
            }
            else{
                for( int i = 0 ; i < tempKeys.size();i++ ){
                    if(key.compareTo(tempKeys.get(i)) == 0 )
                        return temp.getValues().get(i);
                    else if (key.compareTo(tempKeys.get(i)) < 0 ){
                        if(temp.isLeaf())
                            return null ;
                        temp = temp.getChildren().get(i);
                        break;
                    }
                }
            }
       }
    }

    @Override
    public boolean delete(K key) {
        if(key == null ){
            throw new RuntimeErrorException(new Error("null isn't allowed"));
        }
        if (this.root == null || this.root.getKeys().size() == 0 )
            return false ;
        IBTreeNode<K,V> temp = this.root ;
        boolean isFound = false ;
        int deletionIndex = 0 ;
        while( !isFound ){
            List<K> tempKeys = temp.getKeys();
            if( key.compareTo(tempKeys.get(tempKeys.size()-1)) > 0 ){
                if(temp.isLeaf())
                    return false ;
                temp = temp.getChildren().get(temp.getChildren().size()-1);
            }
            else{
                for( int i = 0 ; i < tempKeys.size();i++ ){
                    if(key.compareTo(tempKeys.get(i)) == 0 ){
                        deletionIndex = i ;
                        isFound = true ;
                        break ;
                    }
                    else if (key.compareTo(tempKeys.get(i)) < 0 ){
                        if(temp.isLeaf())
                            return false ;
                        temp = temp.getChildren().get(i);
                        break;
                    }
                }
            }
        }
        if(temp.isLeaf()){
            if (temp == this.root && temp.getKeys().size() == 1){
                this.root = null ;
                return true ;
            }
            temp.getKeys().remove(deletionIndex);
            temp.getValues().remove(deletionIndex);
            checkMin(temp);
        }
        else{
            IBTreeNode<K,V> pre = findPredecessor(temp.getChildren().get(deletionIndex));
            IBTreeNode<K,V> suc = findSuccessor(temp.getChildren().get(deletionIndex+1));
            K pree = pre.getKeys().get(pre.getKeys().size()-1);
            K succ = suc.getKeys().get(0);
            V preeValue = pre.getValues().get(pre.getValues().size()-1);
            V succValue = suc.getValues().get(0);
            if(pre.getKeys().size() >= this.minDegree){
                temp.getKeys().set(deletionIndex , pree );
                temp.getValues().set(deletionIndex , preeValue);
                pre.getKeys().remove(pree);
                pre.getValues().remove(pre.getValues().size()-1);
            }
            else if (  suc.getKeys().size() >= this.minDegree  ){
                temp.getKeys().set(deletionIndex , succ );
                temp.getValues().set(deletionIndex , succValue);
                suc.getKeys().remove(succ);
                suc.getValues().remove(suc.getValues().size()-1);
            }
            else{
                if(temp.getChildren().contains(suc)){
                    for(int i = 0 ; i < suc.getKeys().size() ; i++ ){
                        pre.getKeys().add(suc.getKeys().get(i));
                        pre.getValues().add(suc.getValues().get(i));
                    }
                    temp.getKeys().remove(deletionIndex);
                    temp.getValues().remove(deletionIndex);
                    temp.getChildren().remove(suc);
                    checkMin(temp);
                }
                else{
                    IBTreeNode<K,V> preParent = getParent(pre) ;
                    IBTreeNode<K,V> preSibling = preParent.getChildren().get(preParent.getChildren().size()-1) ;
                    IBTreeNode<K,V> sucParent = getParent(suc) ;
                    IBTreeNode<K,V> sucSibling = sucParent.getChildren().get(1) ;
                    if(sucSibling.getKeys().size() >= this.minDegree){
                        temp.getKeys().set(deletionIndex , succ );
                        temp.getValues().set(deletionIndex , succValue );
                        suc.getKeys().remove(0);
                        suc.getValues().remove(0) ;
                        checkMin(suc);
                    }
                    else{
                        temp.getKeys().set(deletionIndex ,pree );
                        temp.getValues().set(deletionIndex , preeValue );
                        pre.getKeys().remove(pre.getKeys().size()-1);
                        pre.getValues().remove(pre.getValues().size()-1) ;
                        checkMin(pre);
                    }
                }
            }
        }
        return true ;
    }

    private void checkMin (IBTreeNode<K,V> node){
        if(node.getKeys().size() >= this.minDegree-1 || node == this.root)
            return;
        IBTreeNode<K,V> parent = getParent(node);
        int index = parent.getChildren().indexOf(node);
        if(index != 0 && parent.getChildren().get(index-1).getKeys().size() >= this.minDegree){
            IBTreeNode<K,V> leftSibling =  parent.getChildren().get(index-1) ;
            entry(node,parent.getKeys().get(index-1),parent.getValues().get(index-1));
            parent.getKeys().set(index-1 , leftSibling.getKeys().get(leftSibling.getKeys().size()-1));
            parent.getValues().set(index-1 , leftSibling.getValues().get(leftSibling.getValues().size()-1));
            leftSibling.getKeys().remove(leftSibling.getKeys().size()-1);
            leftSibling.getValues().remove(leftSibling.getValues().size()-1);
            if(!leftSibling.isLeaf()){
                node.getChildren().add(0,leftSibling.getChildren().get(leftSibling.getChildren().size()-1));
                leftSibling.getChildren().remove(leftSibling.getChildren().size()-1);
            }
        }
        else if(index != parent.getChildren().size()-1 && parent.getChildren().get(index+1).getKeys().size() >= this.minDegree){
            IBTreeNode<K,V> rightSibling =  parent.getChildren().get(index+1) ;
            entry(node,parent.getKeys().get(index),parent.getValues().get(index));
            parent.getKeys().set(index , rightSibling.getKeys().get(0));
            parent.getValues().set(index , rightSibling.getValues().get(0));
            rightSibling.getKeys().remove(0);
            rightSibling.getValues().remove(0);
            if(!rightSibling.isLeaf()){
                node.getChildren().add(rightSibling.getChildren().get(0));
                rightSibling.getChildren().remove(0);
            }
        }
        else{
            if(index != 0 ){
                IBTreeNode<K,V> leftSibling =  parent.getChildren().get(index-1) ;
                leftSibling.getKeys().add(parent.getKeys().get(index-1));
                leftSibling.getValues().add(parent.getValues().get(index-1));
                for(int i = 0 ; i < node.getKeys().size() ; i++ ){
                    leftSibling.getKeys().add(node.getKeys().get(i));
                    leftSibling.getValues().add(node.getValues().get(i));
                    if(!leftSibling.isLeaf())
                        leftSibling.getChildren().add(node.getChildren().get(i));
                }
                if(!leftSibling.isLeaf())
                    leftSibling.getChildren().add(node.getChildren().get(node.getChildren().size()-1));
                parent.getKeys().remove(index-1);
                parent.getValues().remove(index-1);
                parent.getChildren().remove(index);
                if(parent == this.root && parent.getKeys().size() == 0){
                    this.root = leftSibling ;
                    return;
                }
            }
            else{
                IBTreeNode<K,V> rightSibling =  parent.getChildren().get(index+1) ;
                node.getKeys().add(parent.getKeys().get(index));
                node.getValues().add(parent.getValues().get(index));
                for(int i = 0 ; i < rightSibling.getKeys().size() ; i++ ){
                    node.getKeys().add(rightSibling.getKeys().get(i));
                    node.getValues().add(rightSibling.getValues().get(i));
                    if(!rightSibling.isLeaf())
                        node.getChildren().add(rightSibling.getChildren().get(i));
                }
                if(!rightSibling.isLeaf())
                    node.getChildren().add(rightSibling.getChildren().get(rightSibling.getChildren().size()-1));
                parent.getKeys().remove(index);
                parent.getValues().remove(index);
                parent.getChildren().remove(index+1);
                if(parent == this.root && parent.getKeys().size() == 0){
                    this.root = node ;
                    return;
                }
            }
            checkMin(parent);
        }
    }

    private boolean isFull (IBTreeNode<K,V> node ){
        return node.getKeys().size() > 2 * this.minDegree -1 ;
    }

    private void entry (IBTreeNode<K,V> node , K key , V value ){
        List<K> keys = node.getKeys() ;
        for(int i = 0 ; i < keys.size() ; i++){
            if(key.compareTo(keys.get(i)) < 0 ){
                keys.add(i,key);
                node.getValues().add(i,value);
                return;
            }
        }
        keys.add(key);
        node.getValues().add(value);
    }

    private  void splitNode ( IBTreeNode<K,V> node){
        IBTreeNode<K,V> n1 = new BTreeNode<>();
        IBTreeNode<K,V> n2 = new BTreeNode<>();
        for(int i = 0 ; i < node.getKeys().size() ; i++ ){
            if( i < this.getMinimumDegree() ){
                n1.getKeys().add(node.getKeys().get(i));
                n1.getValues().add(node.getValues().get(i));
                if(!node.isLeaf())
                    n1.getChildren().add(node.getChildren().get(i));
            }
            else if ( i == minDegree ){
                if(!node.isLeaf()){
                    n1.getChildren().add(node.getChildren().get(i));
                }
                continue;
            }
            else{
                n2.getKeys().add(node.getKeys().get(i));
                n2.getValues().add(node.getValues().get(i));
                if(!node.isLeaf())
                    n2.getChildren().add(node.getChildren().get(i));
            }
        }
        if(!node.isLeaf()){
            n2.getChildren().add(node.getChildren().get(node.getChildren().size()-1));
        }
        if(node == this.root){
            IBTreeNode<K,V> end = new BTreeNode<>();
            this.root = end ;
            end.getKeys().add(node.getKeys().get(this.minDegree));
            end.getValues().add(node.getValues().get(this.minDegree));
            end.getChildren().add(n1);
            end.getChildren().add(n2);
            return;
        }
        else{
            IBTreeNode<K,V> parent = getParent(node);
            int h = parent.getChildren().indexOf(node);
            parent.getChildren().remove(h);
            parent.getChildren().add(h,n2);
            parent.getChildren().add(h,n1);
            entry(parent,node.getKeys().get(this.minDegree),node.getValues().get(this.minDegree));
            if(isFull(parent)){
                splitNode(parent);
            }
        }
    }

    private IBTreeNode<K,V> getParent ( IBTreeNode<K,V> node){
        if( node == this.root )
            return null ;
        IBTreeNode<K,V> temp = this.root ;
        while( !temp.isLeaf()  ){
            if(temp.getChildren().contains(node)){
                return temp ;
            }
            else{
                if(node.getKeys().get(0).compareTo(temp.getKeys().get(temp.getKeys().size()-1)) > 0 )
                    temp = temp.getChildren().get(temp.getChildren().size()-1);
                else{
                    for(int i = 0 ; i < temp.getKeys().size() ; i++ ){
                        if(node.getKeys().get(0).compareTo(temp.getKeys().get(i)) < 0 ){
                            temp = temp.getChildren().get(i);
                            break ;
                        }
                    }
                }
            }
        }
        return null ;
    }

    private IBTreeNode<K, V> findPredecessor (IBTreeNode<K,V> node){
        while(!node.isLeaf())
            node = node.getChildren().get(node.getChildren().size()-1);
        return node ;
    }

    private IBTreeNode<K,V> findSuccessor (IBTreeNode<K,V> node ){
        while(!node.isLeaf())
            node = node.getChildren().get(0);
        return node ;
    }
}