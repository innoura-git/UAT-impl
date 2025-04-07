package com.innoura;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Component
public class OutboundFilter implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(OutboundFilter.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
            ClientHttpRequestExecution execution) throws IOException {
        Map<String, String> headers = InboundFilter.getHeaders();
        String requestId = InboundFilter.getRequestId();

        if (headers == null && requestId != null) {
            headers = InboundFilter.getHeadersByRequestId(requestId);
            if (headers != null) {
                //logger.info("Retrieved headers by request ID: {}", requestId);
            } else {
                //logger.warn("No headers found for request ID: {}", requestId);
            }
        }

        if (headers == null) {
            logger.info("Headers not found, attempting to capture from main thread context");
            headers = captureHeadersFromMainThread();
            requestId = InboundFilter.getRequestId(); // Update requestId if captured
        }

        if (headers != null) {
            logger.info("Applying headers: {}", headers);
            headers.forEach(request.getHeaders()::set);


        } else {
            logger.warn("No headers available to forward for this request");
        }
        request.getHeaders().set("X-Request-ID", requestId);
        ClientHttpResponse response = execution.execute(request, body);

        if (requestId != null) {
            InboundFilter.signalCompletion(requestId);
        }
       // logger.info("downStream call : requestID = {} , x-user-id = {} , x-custom-header = {} , Custom-Header = {} ",request.getHeaders().get("X-Request-ID"), request.getHeaders().get("x-user-id"), request.getHeaders().get("x-custom-header"), request.getHeaders().get("Custom-Header"));
        return response;
    }

    private Map<String, String> captureHeadersFromMainThread() {
        FutureTask<Map<String, String>> futureTask = new FutureTask<>(() -> {
            String requestId = InboundFilter.getRequestId();
            if (requestId != null) {
                return InboundFilter.getHeadersByRequestId(requestId);
            }
            return InboundFilter.getHeaders();
        });

        if (InboundFilter.getHeaders() != null) {
            return InboundFilter.getHeaders();
        }

        new Thread(futureTask).start();

        try {
            return futureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }
}
