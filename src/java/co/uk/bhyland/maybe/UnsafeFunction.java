package co.uk.bhyland.maybe;

/* 0-argument function that may throw exceptions.
 * aka Callable. */
public interface UnsafeFunction<A, T extends Throwable> {
	A apply() throws T;
}
