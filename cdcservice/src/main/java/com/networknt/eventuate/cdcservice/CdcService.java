package com.networknt.eventuate.cdcservice;

import com.networknt.rpc.Handler;
import com.networknt.rpc.router.ServiceHandler;
import com.networknt.server.HandlerProvider;
import com.networknt.utility.NioUtils;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.nio.ByteBuffer;

/**
 * CdcService handler to output health check info.
 *
 * @author Steve Hu
 */
@ServiceHandler(id="lightapi.net/eventuate/cdcservice/0.0.1")
public class CdcService implements Handler {
    @Override
    public ByteBuffer handle(Object input)  {
        return NioUtils.toByteBuffer("{\"message\":\"OK!\"}");
    }
}
