package co.uk.bhyland.maybe;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class Constant {
	
	private Constant() {}
	
	public static <A> Function0<A> constantFunction0(final A a) {
		return new Function0<A>(){
			@Override public A apply() {
				return a;
			}
		};
	}
	public static <A,B> Function1<A,B> constantFunction1(final B b) {
		return new Function1<A,B>(){
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

	public static <A> Function1<A, A> identity() {
		return new Function1<A, A>() {
			@Override public A apply(A a) {
				return a;
			}
		};
	}
	
	public static <A, T extends Throwable> UnsafeFunction<A, T> unsafeFunctionThrowing(final Function0<T> t) {
		return new UnsafeFunction<A,T>(){
			@Override public A apply() throws T {
				throw t.apply();
			}
		};
	}
}
