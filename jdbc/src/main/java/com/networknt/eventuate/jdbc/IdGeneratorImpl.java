package com.networknt.eventuate.jdbc;

import com.networknt.eventuate.common.Int128;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IdGeneratorImpl implements IdGenerator {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private static final long MAX_COUNTER = 1 << 16;

  private Long macAddress;
  private long currentPeriod = timeNow();
  private long counter = 0;


  public IdGeneratorImpl() {
    try {
      Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
      while (ifaces.hasMoreElements()) {
        NetworkInterface iface = ifaces.nextElement();
        if (iface.getHardwareAddress() != null) {
          macAddress = toLong(iface.getHardwareAddress());
          logger.debug("Mac address {}", macAddress);
          break;
        }
      }
      if (macAddress == null)
        throw new RuntimeException("Cannot find mac address");
    } catch (SocketException e) {
      throw new RuntimeException(e);
    }
  }

  private Long toLong(byte[] bytes) {
    long result = 0L;
    for (byte b : bytes) {
      result = (result << 8) + (b & 0xff);
    }
    return result;
  }

  private long timeNow() {
    return System.currentTimeMillis();
  }

  private Int128 makeId() {
    return new Int128(currentPeriod, (macAddress << 16) + counter);
  }

  public Int128 genIdInternal() {
    long now = timeNow();
    if (currentPeriod != now || counter == MAX_COUNTER) {
      long oldPeriod = this.currentPeriod;
      while ((this.currentPeriod = timeNow()) <= oldPeriod) {
        // Just do nothing
      }
      counter = 0;
    }
    Int128 id = makeId();
    counter = counter + 1;
    return id;
  }

  @Override
  public synchronized Int128 genId() {
    return genIdInternal();
  }
}
