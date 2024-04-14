public interface Tree<V extends Comparable<? super V>> {

    void put(V value);

    RedBlackTree.Node<V> find(V value);


    int size();

    boolean isEmpty();

}