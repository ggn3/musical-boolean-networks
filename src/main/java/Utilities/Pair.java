package Utilities;

/**
 * A basic data structure for encapsulating multiple objects in a single
 * parent object, mainly to allow multiple entities to be returned simultaneously by a method.
 * @param <A>
 * @param <B>
 */
public class Pair<A,B> {
    public final A a;
    public final B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }
}