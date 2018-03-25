package com.networknt.eventuate.cdc.service;

import com.networknt.rpc.Handler;
import com.networknt.rpc.router.ServiceHandler;
import com.networknt.utility.NioUtils;
import io.undertow.server.HttpServerExchange;

import java.nio.ByteBuffer;

/**
 * CdcService handler to output health check info.
 *
 * @author Steve Hu
 */
@ServiceHandler(id="lightapi.net/eventuate/cdc/0.0.1")
public class CdcService implements Handler {
    @Override
    public ByteBuffer handle(HttpServerExchange exchange, Object input)  {
        return NioUtils.toByteBuffer("{\"message\":\"OK!\"}");
    }
}
