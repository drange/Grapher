package no.uib.ii.algo.st8.interval;
import java.util.HashSet;

/**
 * Some statics methods for sets
 * 
 * @author Pål Grønås Drange
 * 
 */
public class SetUtils {
  public static HashSet<Integer> intersection(HashSet<Integer> a, HashSet<Integer> b) {
    HashSet<Integer> c = new HashSet<Integer>(Math.min(a.size(), b.size()));
    for (Integer i : a) {
      if (b.contains(i))
        c.add(i);
    }
    return c;
  }

  public static HashSet<Integer> setMinus(HashSet<Integer> set, int x) {
    if (set == null)
      return new HashSet<Integer>(5);
    if (!set.contains(x))
      return set;
    HashSet<Integer> c = new HashSet<Integer>(set.size());
    c.addAll(set);
    c.remove(x);
    return c;
  }

  public static HashSet<Integer> union(HashSet<Integer> set, int x) {
    if (set == null) {
      HashSet<Integer> xx = new HashSet<Integer>(5);
      xx.add(x);
      return xx;
    }
    if (set.contains(x))
      return set;
    HashSet<Integer> c = new HashSet<Integer>(set.size() + 1);
    c.addAll(set);
    c.add(x);
    return c;
  }

  public static boolean containsSetContaining(HashSet<HashSet<Integer>> setset, int elt) {
    for (HashSet<Integer> set : setset)
      if (set.contains(elt))
        return true;
    return false;
  }
}