package com.networknt.eventute.cdc.polling;


import com.networknt.eventuate.cdc.polling.EventPollingDataProvider;
import com.networknt.eventuate.cdc.polling.PollingDao;

import com.networknt.eventuate.server.common.PublishedEvent;
import com.networknt.service.SingletonServiceFactory;
import org.h2.tools.RunScript;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;


/**
 * Junit test class for MessageProducerJdbcImpl.
 * use H2 test database for data source
 */
public class PollingDaoTest {

    public static DataSource ds;

    static {
        ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
       try (Connection connection = ds.getConnection()) {
            // Runscript doesn't work need to execute batch here.
            String schemaResourceName = "/eventuate_sourcing_ddl.sql";
            InputStream in = PollingDaoTest.class.getResourceAsStream(schemaResourceName);

            if (in == null) {
                throw new RuntimeException("Failed to load resource: " + schemaResourceName);
            }
            InputStreamReader reader = new InputStreamReader(in);
            RunScript.execute(connection, reader);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private EventPollingDataProvider  pollingDataProvider= (EventPollingDataProvider) SingletonServiceFactory.getBean(EventPollingDataProvider.class);
    PollingDao pollingDao = new PollingDao(pollingDataProvider, ds, 5,5, 100);


    @BeforeClass
    public static void setUp() {

    }

    @Test
    public void testDao() {
        List<PublishedEvent> result=  pollingDao.findEventsToPublish();
        assertTrue(result.size()>0);

    }

    @Test
    public void testDao2() {

        List<PublishedEvent> events = new ArrayList<>();
        PublishedEvent event1 = new PublishedEvent();
        event1.setId("111");
        PublishedEvent event2 = new PublishedEvent();
        event2.setId("222");
        events.add(event1);
        events.add(event2);
        pollingDao.markEventsAsPublished(events);


    }
}
