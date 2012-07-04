import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Given a collection of elements of type T, gives, one by one, every
 * permutation of it. What is a permutation? It is a choice of first element,
 * followed by a permutation of the rest of the elements.
 * 
 * Use the method hasNext to check if there are more permutations to be given.
 * Use the method next to get the next permutation. If next is used when hasNext
 * returns false, it will give a permutation already given before.
 * 
 * Notice that it will assume each element of the collection distingth, no
 * matter what equals says. So with a collection of exactly 4 equal elements it
 * will give 24 equal permutations.
 * 
 * @author erik
 * 
 * @param <T>
 *            The type of the elements that should be permutated.
 */
/*
 * This idea behind this implementation is as follows. We have the elements in
 * some "canonical" order, here the order they are in the collection we get.
 * Then each permutation can be described as a list of integers, where the first
 * integer gives which element from the canonicial ordering should be the first
 * element. The second integer gives where in the canonical ordering of the
 * remaining elements we can find the second element of the permutation, and so
 * on.
 * 
 * In this implementation the list of integers [1, 1, 1] would mean that the
 * first element in the permutation is the first element in the original
 * collection. The second element in the permutation is the first of the
 * remaining elements, so therefore the second element, and so on.
 * 
 * The construction of a permutation goes through two faces. The first is to
 * iterate the list of integers that represent the permutation, and the second
 * phase is to reconstruct the actual permutation. The second can maybe be
 * optimized a bit, as it is O(n^2), because of the removal of a specific index
 * in an ArrayList.
 */
public class PermutationIterator<T> implements Iterator<Collection<T>> {

	/**
	 * Some crap to test it
	 */
	public static void main(String[] args) {
		ArrayList<String> testList = new ArrayList<String>();
		testList.add("1");
		testList.add("2");
		testList.add("3");
		testList.add("4");
		testList.add("5");
		testList.add("6");
		testList.add("7");
		testList.add("8");
		testList.add("9");
		testList.add("10");
		testList.add("11");
		testList.add("12");

		PermutationIterator<String> permGen = new PermutationIterator<String>(
				testList);
		int numbers = 0;
		while (permGen.hasNext()) {
			System.out.println("nr " + ++numbers + ": " + permGen.next());
			// permGen.next();
		}
		System.out.println("done");
	}

	private ArrayList<T> elems = null;
	private int[] currentPermutation;
	// To indicate whether we have delivered the first permutation (and are
	// possibly on our second round).
	private boolean deliveredFirstOnce = false;

	public PermutationIterator(Collection<T> elements) {
		this.elems = new ArrayList<T>(elements);
		currentPermutation = new int[elems.size()];
	}

	/*
	 * This method will iterate the integer-representation of the permutation by
	 * one.
	 */
	private void iterateCurrentPermutation() {
		int possibleElementsAtIndex = 1;
		for (int i = currentPermutation.length - 1; i >= 0; i--) {
			currentPermutation[i] = (currentPermutation[i] + 1)
					% possibleElementsAtIndex;
			// When we have exchausted all possible elements at this position we
			// are done.
			if (currentPermutation[i] != 0) {
				break;
			}
			possibleElementsAtIndex++;
		}
	}

	private ArrayList<T> generatePermutation() {
		ArrayList<T> copyOfelems = new ArrayList<T>(elems);
		ArrayList<T> returnPermutation = new ArrayList<T>(elems.size());
		for (int i = 0; i < currentPermutation.length; i++) {
			returnPermutation.add(copyOfelems.remove(currentPermutation[i]));
		}
		return returnPermutation;
	}

	public boolean hasNext() {
		for (int i = 0; i < currentPermutation.length; i++) {
			if (currentPermutation[i] != 0) {
				return true;
			}
		}
		// Only false if we have delivered the first permutation at least once.
		return false || !deliveredFirstOnce;
	}

	public ArrayList<T> next() {
		deliveredFirstOnce = true;
		iterateCurrentPermutation();
		return generatePermutation();
	}

	@Override
	public void remove() {
		throw new IllegalArgumentException();
	}

}
