package co.uk.bhyland.maybe;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class Constant {
	
	private Constant() {}
	
	public static <A> F0<A> constantF0(final A a) {
		return new F0<A>(){
			@Override public A apply() {
				return a;
			}
		};
	}
	public static <A,B> F1<A,B> constantF1(final B b) {
		return new F1<A,B>(){
			@Override public B apply(A a) {
				return b;
			}
		};
	}
	
	public static <A> Iterator<A> singleElementIterator(final A a) {
		return new Iterator<A>() {
			private boolean unmoved = true;
			@Override public boolean hasNext() {
				return unmoved;
			}

			@Override public A next() {
				if(!unmoved) { throw new NoSuchElementException(); }
				unmoved = false;
				return a;
			}

			@Override public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	public static <A> Iterator<A> emptyIterator() {
		return new Iterator<A>() {
			@Override public boolean hasNext() {
				return false;
			}

			@Override public A next() {
				throw new NoSuchElementException();
			}

			@Override public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public static <A> F1<A, A> identity() {
		return new F1<A, A>() {
			@Override public A apply(A a) {
				return a;
			}
		};
	}
	
	public static <A, T extends Throwable> F0<A> throwing(final T t) {
		return new F0<A>() {
			@Override public A apply() {
				return Constant.<A, RuntimeException>castThrow(t);
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	private static <A, T extends Throwable> A castThrow(Throwable t) throws T {
		throw (T)t;
	}
}
