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
	public abstract <B> B fold(final F1<A,B> ifJust, final F0<B> ifNothing);
	
	public boolean isDefined() {
		return fold(Constant.<A,Boolean>constantF1(true), Constant.constantF0(false));
	}
	public boolean isEmpty() {
		return !isDefined();
	}
	
	// It's important to provide an orElse which is strict in its argument as the
	// boilerplate involved in using the alternative can be very troubling.
	public A orElse(final A a) {
		return orElse(Constant.constantF0(a));
	}
	public A orElse(final F0<A> a) {
		return fold(Constant.<A>identity(), a);
	}
	
	// orNull and fromNullable are desirable to provide a good interface between
	// Maybe-handling code and null-handling code.	
	public A orNull() {
		return fold(Constant.<A>identity(), Constant.<A>constantF0(null));
	}

	public static <A> Maybe<A> fromNullable(final A a) {
		if(a == null) {
			return nothing();
		}
		else {
			return just(a);
		}
	}
	
	// orElseThrow is desirable to provide a good interface between Maybe-handling code
	// and exception-handling code. As before, we don't mind the strict argument as we
	// wouldn't be using Maybe at all if we had performance concerns on that level.
    public <T extends Throwable> A orElseThrow(final T t) throws T {
    	// One way or another, this is going to have to be really evil.
    	// Since all exceptions are unchecked in the jvm, we can trick the compiler
    	// into allowing the fold by casting t to RuntimeException.
    	// Or, we can open up our Just in some way (e.g. an explicit subclass and cast).
    	// Let's play with the Throwable, since that seems more fun.
    	// See http://james-iry.blogspot.co.uk/2010/08/on-removing-java-checked-exceptions-by.html
    	return fold(Constant.<A>identity(), Constant.<A, T>throwing(t));
	}
	
	// as above, this iterates over one element in the Just case and
	// zero elements in the Nothing case.
	@Override public Iterator<A> iterator() {
		final F1<A,Iterator<A>> ifJust = new F1<A,Iterator<A>>(){
			public Iterator<A> apply(final A a) {
				return Constant.singleElementIterator(a);
			}
		};
		final F0<Iterator<A>> ifNothing = new F0<Iterator<A>>(){
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
	// Once we've made this decision, and remembering we also decided to
	// implement everything in terms of fold, we don't really need to specify
	// the Nothing and Just subclasses explicitly.
	
	private static final Maybe<?> NOTHING = new Maybe<Object>() {
		@Override public <B> B fold(final F1<Object, B> ifJust, final F0<B> ifNothing) {
			return ifNothing.apply();
		}
	};

	@SuppressWarnings("unchecked")
	public static <A> Maybe<A> nothing() {
		return (Maybe<A>)NOTHING;
	}
	
	// It should be impossible to subvert Maybe by creating a Just which contains null 
	public static <A> Maybe<A> just(final A a) {
		if(a == null) { throw new NullPointerException("Just may not contain null"); }
		return new Maybe<A>() {
			@Override public <B> B fold(F1<A, B> ifJust, F0<B> ifNothing) {
				return ifJust.apply(a);
			}
		};
	}
	
	// Note on equality: I'd rather pretend that equals(Object) didn't exist,
	// but if you were going to implement equals (and hashcode) you would ensure
	// that all Nothings were equal and that Just(a) == Just(b) iff a == b.
}