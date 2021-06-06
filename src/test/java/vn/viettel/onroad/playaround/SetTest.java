package vn.viettel.onroad.playaround;

import java.util.*;

public class SetTest {
    static class A implements Comparable<A> {
        Integer value;

        public A(int value) {
            this.value = value;
        }

        @Override
        public int compareTo(A o) {
            return this.value.compareTo(o.value);
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }

    public static void main(String[] args) {
//        TreeSet<Integer> a = new TreeSet<>(Arrays.asList(1, 2, 3, 4, 4, 5, 5, -23, -20, 100));
//        System.out.print(a.last() + " " + a.first());

        TreeSet<A> b = new TreeSet<>();
        b.add(new A(-10));
        b.add(new A(-5));
        b.add(new A(2));
        b.add(new A(1));
        b.add(new A(100));
        b.add(new A(23));
        b.add(new A(-434));
        b.add(new A(10));
        System.out.println(b.last() + " " + b.first());
    }
}
