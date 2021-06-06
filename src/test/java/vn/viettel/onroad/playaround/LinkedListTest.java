package vn.viettel.onroad.playaround;

import java.util.*;

public class LinkedListTest {
    public static void main(String[] args) {
        LinkedList<Double> linkedList = new LinkedList<>(new LinkedList<>(Arrays.asList(1., 2., 3., 4., 5., 6., 7.)));

        ListIterator<Double> iter = linkedList.listIterator(linkedList.size());

        while (iter.hasPrevious()) {
            Double cur = iter.previous();
            System.out.println(cur);
        }
    }
}
