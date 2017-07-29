package com.networknt.eventuate.common;

public class Int128 {

  private long hi;
  private long lo;

  public Int128(long hi, long lo) {
    this.hi = hi;
    this.lo = lo;
  }

  public String asString() {
    return String.format("%016x-%016x", hi, lo);
  }

  @Override
  public String toString() {
    return "Int128{" + asString() + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Int128 int128 = (Int128) o;

    return hi == int128.hi && lo == int128.lo;

  }

  @Override
  public int hashCode() {
    int result = (int) (hi ^ (hi >>> 32));
    result = 31 * result + (int) (lo ^ (lo >>> 32));
    return result;
  }

  public static Int128 fromString(String str) {
    String[] s = str.split("-");
    if (s.length != 2)
      throw new IllegalArgumentException("Should have length of 2: " + str);
    return new Int128(Long.parseUnsignedLong(s[0], 16), Long.parseUnsignedLong(s[1], 16));
  }

  public int compareTo(Int128 other) {
    int x = Long.compare(hi, other.hi);
    return x == 0 ? Long.compare(lo, other.lo) : x;
  }

  public long getHi() {
    return hi;
  }

  public long getLo() {
    return lo;
  }
}
