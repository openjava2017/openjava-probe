package org.openjava.probe.client;

public class Main {
    public static void main(String[] args) {
        System.out.println(System.getenv());
        System.out.println(System.getProperties());
        int[] ar = new int[1];
        int[] ab = new int[1];
        System.out.println(ar.getClass() == ab.getClass());
        E a = E.a;
        System.out.println(a.getClass().isPrimitive());
    }

    private static void testParam(Object o) {

    }

    static enum E {
        a, b
    }
}
