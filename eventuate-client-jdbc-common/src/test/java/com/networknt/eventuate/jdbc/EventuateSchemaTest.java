package com.networknt.eventuate.jdbc;

import org.junit.Assert;
import org.junit.Test;

public class EventuateSchemaTest {

  @Test
  public void testDefaultConstructor() {
    EventuateSchema eventuateSchema = new EventuateSchema();

    Assert.assertTrue(eventuateSchema.isDefault());
    Assert.assertFalse(eventuateSchema.isEmpty());
    Assert.assertEquals(EventuateSchema.DEFAULT_SCHEMA, eventuateSchema.getEventuateDatabaseSchema());
    Assert.assertEquals(EventuateSchema.DEFAULT_SCHEMA + ".test", eventuateSchema.qualifyTable("test"));
  }

  @Test
  public void testDefaultSchema() {
    EventuateSchema eventuateSchema = new EventuateSchema(EventuateSchema.DEFAULT_SCHEMA);

    Assert.assertTrue(eventuateSchema.isDefault());
    Assert.assertFalse(eventuateSchema.isEmpty());
    Assert.assertEquals(EventuateSchema.DEFAULT_SCHEMA, eventuateSchema.getEventuateDatabaseSchema());
    Assert.assertEquals(EventuateSchema.DEFAULT_SCHEMA + ".test", eventuateSchema.qualifyTable("test"));
  }

  @Test
  public void testEmptySchema() {
    EventuateSchema eventuateSchema = new EventuateSchema(EventuateSchema.EMPTY_SCHEMA);

    Assert.assertFalse(eventuateSchema.isDefault());
    Assert.assertTrue(eventuateSchema.isEmpty());
    Assert.assertEquals(EventuateSchema.EMPTY_SCHEMA, eventuateSchema.getEventuateDatabaseSchema());
    Assert.assertEquals("test", eventuateSchema.qualifyTable("test"));
  }

  @Test
  public void testCustomSchema() {
    EventuateSchema eventuateSchema = new EventuateSchema("custom");

    Assert.assertFalse(eventuateSchema.isDefault());
    Assert.assertFalse(eventuateSchema.isEmpty());
    Assert.assertEquals("custom", eventuateSchema.getEventuateDatabaseSchema());
    Assert.assertEquals("custom.test", eventuateSchema.qualifyTable("test"));
  }
}
