package com.networknt.eventuate.jdbc.common;

import com.networknt.eventuate.common.Int128;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IdGeneratorImplTest {

  @Test
  public void shouldGenerateId() {
    IdGeneratorImpl idGen = new IdGeneratorImpl();
    Int128 id = idGen.genId();
    assertNotNull(id);
  }

  @Test
  public void shouldGenerateMonotonicId() {
    IdGeneratorImpl idGen = new IdGeneratorImpl();
    Int128 id1 = idGen.genId();
    Int128 id2 = idGen.genId();
    assertTrue(id1.compareTo(id2) < 0);
  }

  @Test
  public void shouldGenerateLotsOfIds() throws InterruptedException {
    IdGeneratorImpl idGen = new IdGeneratorImpl();
    IntStream.range(1, 1000000).forEach(x -> idGen.genId());
    TimeUnit.SECONDS.sleep(1);
    IntStream.range(1, 1000000).forEach(x -> idGen.genId());
  }

}