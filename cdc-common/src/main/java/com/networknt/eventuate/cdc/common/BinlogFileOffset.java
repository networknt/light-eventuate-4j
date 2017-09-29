package com.networknt.eventuate.cdc.common;

public class BinlogFileOffset {

  private String binlogFilename;
  private long offset;

  public BinlogFileOffset() {
  }

  public BinlogFileOffset(String binlogFilename, long offset) {
    this.binlogFilename = binlogFilename;
    this.offset = offset;
  }

  public String getBinlogFilename() {
    return binlogFilename;
  }

  public void setBinlogFilename(String binlogFilename) {
    this.binlogFilename = binlogFilename;
  }

  public long getOffset() {
    return offset;
  }

  public void setOffset(long offset) {
    this.offset = offset;
  }

  public boolean isSameOrAfter(BinlogFileOffset binlogFileOffset) {
    if(this.equals(binlogFileOffset))
      return true;
    if(this.getBinlogFilename().equals(binlogFileOffset.getBinlogFilename())) {
      if(this.getOffset()>binlogFileOffset.getOffset()) {
        return true;
      }
    } else {
      if(this.getBinlogFilename().compareTo(binlogFileOffset.getBinlogFilename())>0) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BinlogFileOffset that = (BinlogFileOffset) o;

    if (offset != that.offset) return false;
    return binlogFilename != null ? binlogFilename.equals(that.binlogFilename) : that.binlogFilename == null;
  }

  @Override
  public int hashCode() {
    int result = binlogFilename != null ? binlogFilename.hashCode() : 0;
    result = 31 * result + (int) (offset ^ (offset >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "BinlogFileOffset{" +
            "binlogFilename='" + binlogFilename + '\'' +
            ", offset=" + offset +
            '}';
  }
}
