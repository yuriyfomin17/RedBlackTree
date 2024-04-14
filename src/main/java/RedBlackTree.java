import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

/**
 * <ol>
 *     <li>A node is either RED or BLACK</li>
 *     <li>The root of the tree is always black</li>
 *     <li>All leaves are null and they are black</li>
 *     <li>All paths from a node to its NULL endings descendants contain the same
 *     number of black node</li>
 *     <li>the height of the red black tree is at most 2 * log(n + 1)</li>
 * </ol>
 */
public class RedBlackTree<V extends Comparable<? super V>> implements Tree<V> {

    private int size = 0;
    private Node<V> root;


    private void rotateRight(@NonNull Node<V> node) {
        Node<V> leftChild = node.getLeft();
        Node<V> parent = node.getParent();
        node.setLeft(null);
        Node<V> leftRightChild = leftChild == null ? null : leftChild.getRight();
        if (parent != null && parent.getRight() == node){
            parent.setRight(leftChild);
        }

        if (parent != null && parent.getLeft() == node){
            parent.setLeft(leftChild);
        }

        if (leftChild != null) {
            leftChild.setRight(node);
            leftChild.setParent(node.getParent());
        }
        if (node == this.root){
            this.root = leftChild;
        }
        node.setParent(leftChild);
        node.setLeft(leftRightChild);
        if (leftRightChild != null) {
            leftRightChild.setParent(node);
        }
    }

    private void rotateLeft(@NonNull Node<V> node) {
        Node<V> rightChild = node.getRight();
        Node<V> parent = node.getParent();
        node.setRight(null);
        Node<V> rightLeftChild = rightChild == null ? null : rightChild.getLeft();
        if (parent != null && parent.getRight() == node){
            parent.setRight(rightChild);
        }
        if (parent != null && parent.getLeft() == node){
            parent.setLeft(rightChild);
        }
        if (rightChild != null) {
            rightChild.setLeft(node);
            rightChild.setParent(node.getParent());
        }
        if (node == this.root){
            this.root = rightChild;
        }
        node.setParent(rightChild);
        node.setRight(rightLeftChild);
        if (rightLeftChild != null) {
            rightLeftChild.setParent(node);
        }
    }


    @Override
    public void put(V value) {
        Node<V> newNode = putHelper(this.root, value);
        if (newNode != null) {
            this.size += 1;
            repair(newNode);
        }
        this.root.redBlackTreeColor = RedBlackTreeColor.BLACK;
    }
    private void repair(Node<V> node){
        if (node == null){
            return;
        }
        if (node.getParent() == null){
            return;
        }
        if (node.getParent().redBlackTreeColor == RedBlackTreeColor.BLACK){
            return;
        }
        if (node.getParent().getParent() == null){
            return;
        }
        Node<V> grandParent = node.getParent().getParent();
        Node<V> parent = node.getParent();
        Node<V> uncle = getUncle(parent);
        // case 1 newNode uncle is red -> recolor
        boolean uncleColorIsRed = uncle != null &&  uncle.redBlackTreeColor == RedBlackTreeColor.RED;
        if (uncleColorIsRed){
            parent.setRedBlackTreeColor(RedBlackTreeColor.BLACK);
            grandParent.setRedBlackTreeColor(RedBlackTreeColor.RED);
            uncle.setRedBlackTreeColor(RedBlackTreeColor.BLACK);
            repair(node.getGrandParent());
            return;
        }
        // case 2 uncle is BLACK and right triangle is formed -> rotate to right parent
        boolean uncleColorIsBlack = uncle == null || uncle.redBlackTreeColor == RedBlackTreeColor.BLACK;
        if (uncleColorIsBlack && childAndParentFormTriangle(parent, node) && parent.getLeft() == node){
            rotateRight(parent);
            repair(parent);
            return;
        }
        // case 2 uncle is BLACK and left triangle is formed -> rotate to left parent
        if (uncleColorIsBlack && childAndParentFormTriangle(parent, node) && parent.getRight() == node){
            rotateLeft(parent);
            repair(parent);
            return;
        }
        // case 3 uncle is BLACK and flat line is formed to the right -> rotate grandparent to right and recolor parent and grandparent
        if (uncleColorIsBlack && childAndParentFormFlatLine(parent, node) && parent.getLeft() == node){
            rotateRight(grandParent);
            grandParent.setRedBlackTreeColor(RedBlackTreeColor.RED);
            parent.setRedBlackTreeColor(RedBlackTreeColor.BLACK);
            repair(node.getParent());
            return;
        }

        // case 3 uncle is BLACK and flat line is formed to the left -> rotate grandparent to right and recolor parent and grandparent
        if (uncleColorIsBlack && childAndParentFormFlatLine(parent, node) && parent.getRight() == node){
            rotateLeft(grandParent);
            grandParent.setRedBlackTreeColor(RedBlackTreeColor.RED);
            parent.setRedBlackTreeColor(RedBlackTreeColor.BLACK);
            repair(node.getParent());
            return;
        }
        repair(node.getParent());
    }
    private boolean childAndParentFormFlatLine(Node<V> parent, Node<V> child){
        if (parent == null){
            return false;
        }
        if (child == null){
            return false;
        }
        if (parent.getParent() == null){
            return false;
        }
        Node<V> grandParent = parent.getParent();
        if (grandParent.getRight() == parent && parent.getRight() == child){
            return true;
        }
        return grandParent.getLeft() == parent && parent.getLeft() == child;
    }
    private boolean childAndParentFormTriangle(Node<V> parent, Node<V> child){
        if (parent == null){
            return false;
        }
        if (child == null){
            return false;
        }
        if (parent.getParent() == null){
            return false;
        }
        Node<V> grandParent = parent.getParent();
        if (parent.getLeft() == child && grandParent.getRight() == parent){
            return true;
        }
        return parent.getRight() == child && grandParent.getLeft() == parent;
    }

    private Node<V> getUncle(@NonNull Node<V> parent){
        Node<V> grandParent = parent.getParent();
        if (grandParent.getLeft() == parent){
            return grandParent.getRight();
        }
        if (grandParent.getRight() == parent){
            return grandParent.getLeft();
        }
        throw new IllegalStateException("Parent is not a child of grandparent");
    }

    private Node<V> putHelper(Node<V> node, V value) {
        if (this.root == null) {
            this.root = new Node<>(value, null);
            return this.root;
        }
        if (node == null) {
            return null;
        }
        int res = value.compareTo(node.getValue());
        if (res < 0 && node.getLeft() == null) {
            node.setLeft(new Node<>(value, node));
            return node.getLeft();
        }
        if (res > 0 && node.getRight() == null) {
            node.setRight(new Node<>(value, node));
            return node.getRight();
        }

        if (res < 0) {
            return putHelper(node.getLeft(), value);
        }
        if (res > 0) {
            return putHelper(node.getRight(), value);
        }
        return null;
    }

    @Override
    public Node<V> find(@NonNull V value) {
        return findNode(this.root, value);
    }

    private Node<V> findNode(Node<V> node, V value) {
        if (node == null) {
            return null;
        }
        int res = value.compareTo(node.getValue());
        if (res < 0) {
            return findNode(node.getLeft(), value);
        } else if (res > 0) {
            return findNode(node.getRight(), value);
        }
        return node;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Data
    public static class Node<V> {
        private V value;
        private RedBlackTreeColor redBlackTreeColor;
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private Node<V> parent;
        private Node<V> left;
        private Node<V> right;

        public Node(V value, Node<V> parent) {
            this.parent = parent;
            this.value = value;
            this.redBlackTreeColor = RedBlackTreeColor.RED;
        }

        private Node<V> getGrandParent(){
            if (this.parent == null){
                return null;
            }
            return this.getParent().getParent();
        }
    }
    enum RedBlackTreeColor {
        RED,
        BLACK
    }

    public static void main(String[] args) {
        createNinthTest();
    }
    private static void firstTest(){
        RedBlackTree<Integer> tree = new RedBlackTree<>();
        tree.put(15);
        tree.put(9);
        tree.put(25);
        tree.put(30);
        // Before fix
        //        15 (B)
        //       /  \
        //     9(R)  25(R)
        //            \
        //            30 (R)
        // After fix
        //          15 (B)
        //         /      \
        //        9(B)    25(B)
        //                 \
        //                 30 (R)
    }

    private static void secondTest(){
        // case 3 -> uncle is black and flat line is formed to right
        RedBlackTree<Integer> tree2 = new RedBlackTree<>();
        tree2.put(15);
        tree2.put(5);
        tree2.put(1);
        // Before fix
        //       15(B)
        //      /
        //     5(R)
        //    /
        //   1(R)

        // After fix
        //       5(B)
        //      /   \
        //     1(R)  15(R)
    }

    private static void thirdTest(){
        RedBlackTree<Integer> tree3 = createThirdTree();
        tree3.put(10);
        // Before fix
        //                   8 (B)
        //                  / \
        //              5(B)  15(R)
        //                    / \
        //                12(B)  19(B)
        //                / \      \
        //             9(R) 13(R)   23(R)
        //                \
        //                10(R)
        System.out.println("here");
        // After fix
        //                   12 (B)
        //                 /      \
        //              8(R)      15(R)
        //             /  \       /   \
        //           5(B)  9(B) 13(B)  19(B)
        //                 \            \
        //                 10(R)        23(R)
    }

    private static RedBlackTree<Integer> createThirdTree(){
        //                   8 (B)
        //                  / \
        //              5(B)  15(R)
        //                    / \
        //                12(B)  19(B)
        //                / \      \
        //             9(R) 13(R)   23(R)
        RedBlackTree<Integer> tree = new RedBlackTree<>();
        tree.root = new Node<>(8, null);
        tree.root.redBlackTreeColor = RedBlackTreeColor.BLACK;

        Node<Integer> node2 = new Node<>(5, tree.root);
        node2.redBlackTreeColor = RedBlackTreeColor.BLACK;
        tree.root.left = node2;

        Node<Integer> node3 = new Node<>(15, tree.root);
        node3.redBlackTreeColor = RedBlackTreeColor.RED;
        tree.root.right = node3;

        Node<Integer> node4 = new Node<>(12, node3);
        node4.redBlackTreeColor = RedBlackTreeColor.BLACK;
        node3.setLeft(node4);

        Node<Integer> node5 = new Node<>(19, node3);
        node5.redBlackTreeColor = RedBlackTreeColor.BLACK;
        node3.setRight(node5);

        Node<Integer> node6 = new Node<>(9, node4);
        node6.redBlackTreeColor = RedBlackTreeColor.RED;
        node4.setLeft(node6);

        Node<Integer> node7 = new Node<>(13, node4);
        node7.redBlackTreeColor = RedBlackTreeColor.RED;
        node4.setRight(node7);

        Node<Integer> node8 = new Node<>(23, node5);
        node8.redBlackTreeColor = RedBlackTreeColor.RED;
        node5.setRight(node8);
        return tree;
    }

    private static RedBlackTree<Integer> createFourthTest(){
        //                   17 (B)
        //                  / \
        //              9(B)  19(B)
        //                    / \
        //                18(R)  75(R)
        //                        \
        //                         81(R)
        RedBlackTree<Integer> tree = new RedBlackTree<>();
        tree.put(17);
        tree.put(9);
        tree.put(19);
        tree.put(18);
        tree.put(75);
        tree.put(81);

        // After fix
        //                   17 (B)
        //                  / \
        //              9(B)  19(B)
        //                    / \
        //                18(R)  75(R)
        //                        \
        //                         81(R)


        return tree;
    }

    private static RedBlackTree<Integer> createFifthTest(){
        // Before fix
        //                   17 (B)
        //                  / \
        //              9(B)  19(B)
        //                       \
        //                       24(R)
        //                        \
        //                         75(R)
        RedBlackTree<Integer> tree = new RedBlackTree<>();
        tree.put(17);
        tree.put(9);
        tree.put(19);
        tree.put(24);
        tree.put(75);

        // After fix
        //                   17 (B)
        //                  / \
        //              9(B)  24(B)
        //                    / \
        //                18(R)  75(R)

        return tree;
    }

    private static RedBlackTree<Integer> createSixthTest(){
        // Before fix
        //                   17 (B)
        //                  / \
        //              9(B)  18(B)
        //                       \
        //                       24(R)
        //                        \
        //                         75(R)
        RedBlackTree<Integer> tree = new RedBlackTree<>();
        tree.put(17);
        tree.put(9);
        tree.put(18);
        tree.put(24);
        tree.put(75);

        // After fix
        //                   17 (B)
        //                  / \
        //              9(B)  24(B)
        //                    / \
        //                18(R)  75(R)

        return tree;
    }

    private static RedBlackTree<Integer> createSeventhTest(){
        // Before fix
        //                   50 (B)
        //                 /   \
        //             25(B)    80(B)
        //                \       \
        //               35(R)    100(R)
        //                         \
        //                          120(R)
        RedBlackTree<Integer> tree = new RedBlackTree<>();
        tree.root = new Node<>(50, null);
        tree.root.redBlackTreeColor = RedBlackTreeColor.BLACK;

        Node<Integer> node2 = new Node<>(25, tree.root);
        node2.redBlackTreeColor = RedBlackTreeColor.BLACK;
        tree.root.left = node2;

        Node<Integer> node3 = new Node<>(80, tree.root);
        node3.redBlackTreeColor = RedBlackTreeColor.BLACK;
        tree.root.right = node3;

        Node<Integer> node4 = new Node<>(35, node2);
        node4.redBlackTreeColor = RedBlackTreeColor.RED;
        node2.setRight(node4);

        Node<Integer> node5 = new Node<>(100, node3);
        node5.redBlackTreeColor = RedBlackTreeColor.RED;
        node3.setRight(node5);

        tree.put(120);


        // After fix
        //                   50 (B)
        //                 /   \
        //            25(B)     100(B)
        //               \      /    \
        //              35(R)  80(R)     120(R)
        return tree;
    }

    private static RedBlackTree<Integer> createEighthTest(){
        RedBlackTree<Integer> tree = new RedBlackTree<>();
        tree.put(50);
        tree.put(25);
        tree.put(35);
        tree.put(80);
        tree.put(100);
        tree.put(120);
        // After fix
        //                   35 (B)
        //                 /   \
        //            25(B)     80(R)
        //                      /    \
        //                   50(B)   100(B)
        //                             \
        //                              120(R)
        return tree;

    }


    private static RedBlackTree<Integer> createNinthTest(){
        RedBlackTree<Integer> tree = new RedBlackTree<>();
        tree.put(50);
        tree.put(25);
        tree.put(35);
        tree.put(80);
        tree.put(100);
        tree.put(120);
        tree.put(11);
        tree.put(9);
        tree.put(1);
        // After fix
        //                   35 (B)
        //                 /       \
        //            11(R)         80(R)
        //           /   \          /   \
        //         9(B)  25(B)    50(B)   100(B)
        //         /                       \
        //       1(R)                     120(R)
        return tree;

    }

}
