package no.uib.ii.algo.st8.interval;

/**
 * Interval with integer endpoints, always ordered left <= right, possibly
 * equal. Two intervals are equal if their endpoints are.
 * 
 * This class is immutable and final.
 * 
 * @author Pål Grønås Drange
 * 
 */
public class Interval implements Comparable<Interval> {
  private final int left;
  private final int right;

  public Interval(int a, int b) {
    if (a < b) {
      this.left = a;
      this.right = b;
    } else {
      this.left = b;
      this.right = a;
    }
  }

  public int getLeft() {
    return left;
  }

  public int getRight() {
    return right;
  }

  public Interval shifted(int shift) {
    return new Interval(left + shift, right + shift);
  }

  public Interval shiftLeft(int shift) {
    return new Interval(left + shift, right);
  }

  public Interval shiftRight(int shift) {
    return new Interval(left, right + shift);
  }

  public boolean overlaps(Interval o) {
    return o.left <= right && o.right >= left;
  }

  public int other(int x) {
    if (x == this.left) {
      return this.right;
    } else {
      return this.left;
    }
  }

  public int length() {
    return right - left;
  }

  /**
   * Sort by leftmost ascending and rightmost descending (hence long interval
   * first)
   */
  @Override
  public int compareTo(Interval o) {
    if (left < o.left) {
      return -1;
    } else if (left == o.left) {
      if (right > o.right) {
        return -1;
      } else {
        return right == o.right ? 0 : -1;
      }
    } else {
      return 1;
    }
  }

  @Override
  public int hashCode() {
    return left * 97 + right;
  }

  public boolean equals(Object o) {
    if (o == null || (!(o instanceof Interval)))
      return false;
    Interval i = (Interval) o;
    return i.left == left && i.right == right;
  }

  public String toString() {
    return "(" + left + "-" + right + ")";
  }
}
