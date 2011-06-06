package com.ontotext.jape.automaton;

/**
 * This class implements closed hash of elements of type Object. Provides two
 * methods that could be predefined: equal and getHashCode. It is very
 * convenient not to predefine the standard java methods equals and hashCode,
 * but to predefine equal and getHashCode.
 * 
 * @author petar.mitankin
 * 
 */
public class ClosedHashOfObjects {
	protected int[] hash;
	protected Object[] objects;
	protected int objectsStored;

	protected ClosedHashOfObjects() {
		hash = new int[255];

		for (int i = 0; i < hash.length; i++) {
			hash[i] = Constants.NO;
		}
		objects = new Object[256];
	}

	protected int put(Object o) {
		int i;

		for (i = getHashCode(o); hash[i] != Constants.NO; i = (i + Constants.hashStep)
				% hash.length) {
			if (equal(objects[hash[i]], o)) {
				return (hash[i]);
			}
		}
		hash[i] = objectsStored;
		if (objectsStored == objects.length) {
			Object[] newObjects = new Object[2 * objects.length];
			for (i = 0; i < objectsStored; i++) {
				newObjects[i] = objects[i];
			}
			objects = newObjects;
		}
		objects[objectsStored] = o;
		objectsStored++;
		if (10 * objectsStored > 9 * hash.length) {
			hash = new int[2 * hash.length + 1];
			for (i = 0; i < hash.length; i++) {
				hash[i] = Constants.NO;
			}
			for (int j = 0; j < objectsStored; j++) {
				for (i = getHashCode(objects[j]); hash[i] != Constants.NO; i = (i + Constants.hashStep)
						% hash.length)
					;
				hash[i] = j;
			}
		}
		return (objectsStored - 1);
	}

	protected int contains(Object o) {
		for (int i = getHashCode(o); hash[i] != Constants.NO; i = (i + Constants.hashStep)
				% hash.length) {
			if (equal(objects[hash[i]], o)) {
				return (hash[i]);
			}
		}
		return Constants.NO;
	}

	protected int getHashCode(Object o) {
		return o.hashCode() % objectsStored;
	}

	protected boolean equal(Object o1, Object o2) {
		return o1.equals(o2);
	}
}
