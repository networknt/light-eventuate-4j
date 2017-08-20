package com.networknt.eventuate.client.restclient;

import com.networknt.client.Http2Client;
import com.networknt.eventuate.common.*;
import com.networknt.eventuate.common.impl.*;
import io.undertow.client.ClientConnection;
import io.undertow.client.ClientRequest;
import io.undertow.client.ClientResponse;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.IoUtils;
import org.xnio.OptionMap;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;


/**
 * RestFul Client used for process Event Sourcing Aggregate Crud process
 * @author gavin
 */
public class EventuateRESTClient implements AggregateCrud{

    public static final String  SAVE = "save";
    public static final String  FIND = "find";

    protected Logger logger = LoggerFactory.getLogger(getClass());
    private Http2Client client;
    private URI url;
    private String correlationId;
    private String traceabilityId;
    private String authToken;

    public EventuateRESTClient(String authToken, URI url) throws Exception {
        new EventuateRESTClient(authToken, url, null, null);
    }

    public EventuateRESTClient(String authToken, URI url, String correlationId, String traceabilityId) {
        client = Http2Client.getInstance();
        this.authToken = authToken;
        this.url = url;
        this.correlationId = correlationId;
        this.traceabilityId = traceabilityId;
       // HttpGet httpGet = new HttpGet(url);
      //  client.populateHeader(httpGet, authToken, correlationId,traceabilityId );
    }

    private void populateHeader() {
        // HttpGet httpGet = new HttpGet(url);
        //  client.populateHeader(httpGet, authToken, correlationId,traceabilityId );

    }

    private <T> CompletableFuture<T> withRetry(Supplier<CompletableFuture<T>> asyncRequest) {
        CompletableFuture<T> result = new CompletableFuture<>();
        attemptOperation(asyncRequest, result);
        return result;
    }

    private <T> void attemptOperation(Supplier<CompletableFuture<T>> asyncRequest, CompletableFuture<T> result) {
        CompletableFuture<T> f = asyncRequest.get();
        f.handleAsync((val, throwable) -> {
            if (throwable != null) {
                if (throwable instanceof EventuateServiceUnavailableException) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                        attemptOperation(asyncRequest, result);
                    } catch (Exception e ) {
                        result.completeExceptionally(throwable);
                    }
                } else
                    result.completeExceptionally(throwable);
            }
            else
                result.complete(val);
            return null;
        });
    }
    @Override
    public CompletableFuture<EntityIdVersionAndEventIds> save(String aggregateType, List<EventTypeAndData> events, Optional<AggregateCrudSaveOptions> options) {
        return withRetry(() -> {
            if (logger.isDebugEnabled())
                logger.debug("save: " + aggregateType + ", events" + events);

            CompletableFuture<EntityIdVersionAndEventIds> cf = new CompletableFuture<>();

            CreateEntityRequest request = new CreateEntityRequest(aggregateType, events);
            options.flatMap(AggregateCrudSaveOptions::getEntityId).ifPresent(request::setEntityId);

            String json = JSonMapper.toJson(request);

            final AtomicReference<ClientResponse> reference = new AtomicReference<>();
            final CountDownLatch latch = new CountDownLatch(1);
            final ClientConnection connection;
            try {
                connection = client.connect(url, Http2Client.WORKER, Http2Client.SSL, Http2Client.POOL, OptionMap.EMPTY).get();
                try {
                    connection.getIoThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            final ClientRequest request = new ClientRequest().setMethod(Methods.POST);
                            request.getRequestHeaders().put(Headers.HOST, "localhost");
                            request.getRequestHeaders().put(Headers.CONTENT_TYPE, "application/json");
                            request.getRequestHeaders().put(Headers.TRANSFER_ENCODING, "chunked");
                            connection.sendRequest(request, client.createClientCallback(reference, latch, json));
                        }
                    });

                    latch.await(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    logger.error("IOException: ", e);
                    cf.completeExceptionally(e);
                } finally {
                    IoUtils.safeClose(connection);
                }
                int statusCode = reference.get().getResponseCode();
                String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
                if (statusCode >= 200 && statusCode < 300) {
                    CreateEntityResponse r = JSonMapper.fromJson(body, CreateEntityResponse.class);
                    cf.complete(new EntityIdVersionAndEventIds(r.getEntityId(), r.getEntityVersion(), r.getEventIds()));
                    logger.debug("responseBody = " + body);
                } else {
                    cf.completeExceptionally(new RuntimeException("Unexpected response status: " + statusCode + " body: " + body));
                }
            } catch (Exception e) {
                logger.error("Exception:", e);
                cf.completeExceptionally(e);
            }
            return cf;
        });
    }

    @Override
    public <T extends Aggregate<T>> CompletableFuture<LoadedEvents> find(String aggregateType, String entityId, Optional<AggregateCrudFindOptions> findOptions) {
        return withRetry(() -> {
            CompletableFuture<LoadedEvents> cf = new CompletableFuture<>();
            String path =  "/" + aggregateType + "/" + entityId + makeGetQueryString(findOptions.flatMap(AggregateCrudFindOptions::getTriggeringEvent));

            final AtomicReference<ClientResponse> reference = new AtomicReference<>();
            final CountDownLatch latch = new CountDownLatch(1);
            final ClientConnection connection;
            try {
                connection = client.connect(url, Http2Client.WORKER, Http2Client.SSL, Http2Client.POOL, OptionMap.EMPTY).get();
                try {
                    connection.getIoThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            final ClientRequest request = new ClientRequest().setMethod(Methods.GET).setPath(path);
                            request.getRequestHeaders().put(Headers.HOST, "localhost");
                            request.getRequestHeaders().put(Headers.CONTENT_TYPE, "application/json");
                            connection.sendRequest(request, client.createClientCallback(reference, latch));
                        }
                    });

                    latch.await(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    logger.error("IOException: ", e);
                    cf.completeExceptionally(e);
                } finally {
                    IoUtils.safeClose(connection);
                }
                int statusCode = reference.get().getResponseCode();
                String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
                if (statusCode >= 200 && statusCode < 300) {

                    GetEntityResponse r = JSonMapper.fromJson(body, GetEntityResponse.class);
                    if (r.getEvents().isEmpty())
                        cf.completeExceptionally(new EntityNotFoundException());
                    else {
                        Optional<SerializedSnapshotWithVersion> snapshot = Optional.empty();  // TODO - retrieve snapshot
                        cf.complete(new LoadedEvents(snapshot, r.getEvents()));
                    }
                    logger.debug("responseBody = " + body);
                } else {
                    cf.completeExceptionally(new RuntimeException("Unexpected response status: " + statusCode + " body: " + body));
                }
            } catch (Exception e) {
                logger.error("Exception:", e);
                cf.completeExceptionally(e);
            }
            return cf;
        });
    }

    private String makeGetQueryString(Optional<EventContext> triggeringEvent) {
        return triggeringEvent.flatMap(te -> Optional.of(te.getEventToken())).map(eventToken -> "?triggeringEventToken=" + eventToken).orElse("");
    }

    @Override
    public CompletableFuture<EntityIdVersionAndEventIds> update(EntityIdAndType entityIdAndType, Int128 entityVersion, List<EventTypeAndData> events, Optional<AggregateCrudUpdateOptions> updateOptions) {
        return withRetry(() -> {
            if (logger.isDebugEnabled())
                logger.debug("update: " + entityIdAndType.getEntityType() + ", " + entityIdAndType.getEntityId() + ", " + ", events" + events + ", " + updateOptions);

            CompletableFuture<EntityIdVersionAndEventIds> cf = new CompletableFuture<>();

            // TODO post snapshot if there is one
            UpdateEntityRequest request = new UpdateEntityRequest(events, entityVersion,
                    updateOptions.flatMap(AggregateCrudUpdateOptions::getTriggeringEvent).map(EventContext::getEventToken).orElse(null));

            String json = JSonMapper.toJson(request);
            String path = "/" + entityIdAndType.getEntityType() + "/" + entityIdAndType.getEntityId();

            final AtomicReference<ClientResponse> reference = new AtomicReference<>();
            final CountDownLatch latch = new CountDownLatch(1);
            final ClientConnection connection;
            try {
                connection = client.connect(url, Http2Client.WORKER, Http2Client.SSL, Http2Client.POOL, OptionMap.EMPTY).get();
                try {
                    connection.getIoThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            final ClientRequest request = new ClientRequest().setMethod(Methods.POST).setPath(path);
                            request.getRequestHeaders().put(Headers.HOST, "localhost");
                            request.getRequestHeaders().put(Headers.CONTENT_TYPE, "application/json");
                            request.getRequestHeaders().put(Headers.TRANSFER_ENCODING, "chunked");
                            connection.sendRequest(request, client.createClientCallback(reference, latch, json));
                        }
                    });

                    latch.await(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    logger.error("IOException: ", e);
                    cf.completeExceptionally(e);
                } finally {
                    IoUtils.safeClose(connection);
                }
                int statusCode = reference.get().getResponseCode();
                String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
                if (statusCode >= 200 && statusCode < 300) {
                    CreateEntityResponse r = JSonMapper.fromJson(body, CreateEntityResponse.class);
                    cf.complete(new EntityIdVersionAndEventIds(r.getEntityId(), r.getEntityVersion(), r.getEventIds()));
                    logger.debug("responseBody = " + body);
                } else {
                    cf.completeExceptionally(new RuntimeException("Unexpected response status: " + statusCode + " body: " + body));
                }
            } catch (Exception e) {
                logger.error("Exception:", e);
                cf.completeExceptionally(e);
            }
            return cf;
        });
    }
}
