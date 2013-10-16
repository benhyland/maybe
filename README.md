maybe
=====

This is a quick implementation of Maybe, aka [Option](http://en.wikipedia.org/wiki/Option_type).

I sometime get involved in conversations about the desirability or otherwise of functional-style programming in Java.

Proper functional programming (in the sense that the entire program is pure) is inevitably crap in Java as the language lacks several features which are required to make it humane.

Assuming you are using Java for some other reason, the appropriate stance varies depending on your team, use case and will to live.
In particular different teams will have different ideas of what constitutes readable code.
Don't worry, everybody except you is wrong.

This code is intended mostly as a conversation piece with enough inline commentary to suggest what I think the best compromise will often be.
I'm using Maybe as it's just about the simplest type to implement that tends to spark these discussions; moreover it is often the first type that people come across when they first start learning in this area.

Everybody and their dog has written one of these so I won't link to other examples (although I'd be interested to hear if there is anything you've found particularly useful in real projects or for learning from).