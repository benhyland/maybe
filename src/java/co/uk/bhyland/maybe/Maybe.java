package co.uk.bhyland.maybe;

import java.util.Iterator;

// All comments below are only intended as advice on using Maybe in Java.

// Maybe should only be used in code which is not very performance sensitive.
// The complications in using it in low-garbage, no-boxing situations are probably
// not worth the effort at the moment.

// Maybe is parameterised on the value type.
// No specialisations should be provided to avoid boxing as we won't be using it in situations where we will care that boxing occurs.

// One of the major uses of Maybe is when performing a calculation which might fail, in multiple ways.
// For example, retrieving a value from a database - there could be a connection or credentials problem,
// or the value may not be present or parseable. In cases like this the application
// will often benefit from knowing which of the several possible problems actually occurred.
// If this is so, then Validation, defined as the sum type Either<SuccessValue, NonEmptyList<ErrorValue>>
// is probably a better choice.

// Maybe should implement Iterable - it is equivalent to a list of zero or one element.
public abstract class Maybe<A> implements Iterable<A> {

	// Maybe should be an abstract class with a private constructor
	// as this is probably the best way to prevent additional subtypes from being created.
	private Maybe() {}
	
	// Maybe should provide an api which includes methods with strict parameters, as well as
	// the more traditional non-strict functional-style interface.
	// Both of these are valid use cases in Java and the preferred option will depend on the team and situation.	
	// The api shown here is by way of example only, as there are rather too many helpful functions we could
	// implement (probably starting with bind, map, sequence).
	// The workload is much reduced in languages which can provide some higher order commonality (e.g. via typeclasses). 
	
	// Note that we deliberately do not provide any function along the following lines:
	// public abstract A get();
	// It would have no way to enforce that clients have checked that the Maybe is not Nothing.
	// In other words it would be a bugfactory. Best not to have it at all.
	// A safe equivalent is maybe.orElse(defaultValue)
	// An unsafe but explicit equivalent is maybe.orElseThrow(new Exception())
	
	// All interesting methods can be implemented in terms of fold.
	// We don't necessarily have to do this, but it is important to realise that we can.
	// The tradeoff is the usual subjective one of readability/purity.
	// The nice thing about this is that once you have exposed fold it is trivial to add new functionality in client code.
	public abstract <B> B fold(final Function1<A,B> ifJust, final Function0<B> ifNothing);
	
	public boolean isDefined() {
		return fold(Constant.<A,Boolean>constantFunction1(true), Constant.constantFunction0(false));
	}
	public boolean isEmpty() {
		return !isDefined();
	}
	
	// It's important to provide an orElse which is strict in its argument as the
	// boilerplate involved in using the alternative can be very troubling.
	public A orElse(final A a) {
		return orElse(Constant.constantFunction0(a));
	}
	public A orElse(final Function0<A> a) {
		return fold(Constant.<A>identity(), a);
	}
	
	// orNull and fromNullable are desirable to provide a good interface between
	// Maybe-handling code and null-handling code.
	public A orNull() {
		return fold(Constant.<A>identity(), Constant.<A>constantFunction0(null));
	}
	public static <A> Maybe<A> fromNullable(final A a) {
		if(a == null) {
			return nothing();
		}
		else {
			return just(a);
		}
	}
	
	// orElseThrow and the unsafe orElse overload are desirable to provide a good interface between
	// Maybe-handling code and exception-handling code.
	//
	// We don't care too much about object creation overhead since we are already using Maybe,
	// but filling out a stack trace is quite heavyweight so we provide a lazy overload.
	//
	// One way or another, the implementations are going to have to be a bit evil.
	// We could hack around with RuntimeExceptions if we want to reuse fold()
	// (see http://james-iry.blogspot.co.uk/2010/08/on-removing-java-checked-exceptions-by.html).
	// We could compromise the api by introducing a get().
	// We could use instanceof or something equivalent.
	// Here we choose to use a double check with a dead code path to avoid any of the above. YMMV.
	public <T extends Throwable> A orElseThrow(final T t) throws T {
		if(isDefined()) {
			return orNull(); // never null
		}
		else {
			throw t;
		}
	}
	public <T extends Throwable> A orElseThrow(final Function0<T> t) throws T {
		if(isDefined()) {
			return orNull(); // never null
		}
		else {
			throw t.apply();
		}
	}
	public <T extends Throwable> A orElse(final UnsafeFunction<A,T> defaultMayThrow) throws T {
		if(isDefined()) {
			return orNull(); // never null
		}
		else {
			return defaultMayThrow.apply();
		}
	}

	// as above, this iterates over one element in the Just case and
	// zero elements in the Nothing case.
	@Override public Iterator<A> iterator() {
		final Function1<A,Iterator<A>> ifJust = new Function1<A,Iterator<A>>(){
			public Iterator<A> apply(final A a) {
				return Constant.singleElementIterator(a);
			}
		};
		final Function0<Iterator<A>> ifNothing = new Function0<Iterator<A>>(){
			public Iterator<A> apply() {
				return Constant.emptyIterator();
			}
		};
		return fold(ifJust, ifNothing);
	}

	// It should be impossible for client code to tell whether
	// something is Just or Nothing from the type.
	// Pushing these calculations into a fold (directly or indirectly)
	// ensures that the client code avoids conditional branching.
	// So, nothing() and just() should return Maybe.
	// Since it may be useful for debuggers, we still provide explicit subclasses.
	private static final Maybe<?> NOTHING = new Nothing();

	@SuppressWarnings("unchecked")
	public static <A> Maybe<A> nothing() {
		return (Maybe<A>)NOTHING;
	}
	
	// It should be impossible to subvert Maybe by creating a Just which contains null 
	public static <A> Maybe<A> just(final A a) {
		if(a == null) { throw new NullPointerException("Just must not contain null"); }
		return new Just<A>(a);
	}
	
	private static final class Nothing extends Maybe<Object> {
		@Override public <B> B fold(final Function1<Object, B> ifJust, final Function0<B> ifNothing) {
			return ifNothing.apply();
		}
	}
	
	private static final class Just<A> extends Maybe<A> {
		final A a;
		public Just(final A a) {
			this.a = a;
		}
		@Override public <B> B fold(final Function1<A, B> ifJust, final Function0<B> ifNothing) {
			return ifJust.apply(a);
		}
	}
	
	// Note on equality: my personal preference is to avoid implementing equals(Object)
	// as it is unsafe and there is rarely only a single definition of equality.
	// However, were you to implement equals (and hashcode) you would ensure
	// that all Nothings were equal and that Just(a) == Just(b) iff a == b.
}
