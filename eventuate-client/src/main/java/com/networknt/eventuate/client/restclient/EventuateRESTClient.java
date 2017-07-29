package com.networknt.eventuate.client.restclient;

import com.networknt.eventuate.common.*;
import com.networknt.eventuate.common.impl.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.networknt.client.Client;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.concurrent.TimeUnit;


/**
 *  RestFul Client used for process Event Sourcing Aggregate Crud process
 * Created by gavin on 2017-04-16.
 */
public class EventuateRESTClient implements AggregateCrud{

    public static final String  SAVE = "save";
    public static final String  FIND = "find";

    protected Logger logger = LoggerFactory.getLogger(getClass());
    private Client client;
    private  URI url;
    private String correlationId;
    private String traceabilityId;
    private String authToken;

    public EventuateRESTClient(String authToken, URI url) throws Exception {
        new EventuateRESTClient(authToken, url, null, null);
    }

    public EventuateRESTClient(String authToken, URI url, String correlationId, String traceabilityId) {
        client = Client.getInstance();
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
            HttpPost httpPost = new HttpPost(url);

            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
               if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
            String responseBody;
            try {
                httpPost.setEntity(new StringEntity(json));
                httpPost.setHeader("Content-type", "application/json");
                client.populateHeader(httpPost, authToken, correlationId, traceabilityId );
                responseBody = client.getSyncClient().execute(httpPost, responseHandler);
                CreateEntityResponse r = JSonMapper.fromJson(responseBody, CreateEntityResponse.class);
                cf.complete(new EntityIdVersionAndEventIds(r.getEntityId(), r.getEntityVersion(), r.getEventIds()));
                logger.debug("message = " + responseBody);
            } catch (Exception e) {
                e.printStackTrace();
                cf.completeExceptionally(e);
            }


            return cf;
        });    }

    @Override
    public <T extends Aggregate<T>> CompletableFuture<LoadedEvents> find(String aggregateType, String entityId, Optional<AggregateCrudFindOptions> findOptions) {
        return withRetry(() -> {
            CompletableFuture<LoadedEvents> cf = new CompletableFuture<>();
            String path =  "/" + aggregateType + "/" + entityId + makeGetQueryString(findOptions.flatMap(AggregateCrudFindOptions::getTriggeringEvent));
            HttpGet httpGet = new HttpGet(path);
            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };

            String responseBody;
            try {
                httpGet.setHeader("Content-type", "application/json");
                client.populateHeader(httpGet, authToken, correlationId, traceabilityId );
                responseBody = client.getSyncClient().execute(httpGet, responseHandler);
                GetEntityResponse r = JSonMapper.fromJson(responseBody, GetEntityResponse.class);
                if (r.getEvents().isEmpty())
                    cf.completeExceptionally(new EntityNotFoundException());
                else {
                    Optional<SerializedSnapshotWithVersion> snapshot = Optional.empty();  // TODO - retrieve snapshot
                    cf.complete(new LoadedEvents(snapshot, r.getEvents()));
                }
                logger.debug("message = " + responseBody);
            } catch (Exception e) {
                e.printStackTrace();
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


            HttpPost httpPost = new HttpPost(path);

            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
            String responseBody;
            try {
                httpPost.setEntity(new StringEntity(json));
                httpPost.setHeader("Content-type", "application/json");
                client.populateHeader(httpPost, authToken, correlationId, traceabilityId );
                responseBody = client.getSyncClient().execute(httpPost, responseHandler);
                CreateEntityResponse r = JSonMapper.fromJson(responseBody, CreateEntityResponse.class);
                cf.complete(new EntityIdVersionAndEventIds(r.getEntityId(), r.getEntityVersion(), r.getEventIds()));
                logger.debug("message = " + responseBody);
            } catch (Exception e) {
                e.printStackTrace();
                cf.completeExceptionally(e);
            }

            return cf;
        });
    }
}
