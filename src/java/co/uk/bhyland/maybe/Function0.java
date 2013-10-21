package co.uk.bhyland.maybe;

/* 0-argument function that may not throw exceptions.
 * aka Supplier, Producer etc. */
public interface Function0<A> {
	A apply();
}