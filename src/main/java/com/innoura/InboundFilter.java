package com.innoura;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InboundFilter
        implements Filter
{

    private static final InheritableThreadLocal<Map<String, String>> headersHolder = new InheritableThreadLocal<>();
    private static final InheritableThreadLocal<String> requestIdHolder = new InheritableThreadLocal<>();
    private static final Map<String, RequestContext> requestContextMap = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(InboundFilter.class);

    private static class RequestContext
    {
        final Map<String, String> headers;
        final CountDownLatch latch;

        RequestContext(Map<String, String> headers)
        {
            this.headers = headers;
            this.latch = new CountDownLatch(1); // One async task per request
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {

        String requestId = UUID.randomUUID().toString();
        requestIdHolder.set(requestId);

        Map<String, String> headersMap;
        if (request instanceof HttpServletRequest httpRequest) {
            headersMap = new HashMap<>();
            Collections.list(httpRequest.getHeaderNames()).forEach(
                    headerName -> headersMap.put(headerName, httpRequest.getHeader(headerName))
            );
            headersHolder.set(headersMap);
            requestContextMap.put(requestId, new RequestContext(headersMap));
        }
        else {
            headersMap = null;
        }

        try {
            chain.doFilter(request, response);
        }
        finally {
            if (headersMap != null) {
                RequestContext context = requestContextMap.get(requestId);
                try {
                    context.latch.await(); // Wait for async task to complete
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                cleanup(requestId);
            }
            else {
                cleanup(requestId); // No headers, clean up immediately
            }
        }
    }

    private void cleanup(String requestId)
    {
        headersHolder.remove();
        requestIdHolder.remove();
        requestContextMap.remove(requestId);
    }

    public static Map<String, String> getHeaders()
    {
        return headersHolder.get();
    }

    public static String getRequestId()
    {
        return requestIdHolder.get();
    }

    public static Map<String, String> getHeadersByRequestId(String requestId)
    {
        RequestContext context = requestContextMap.get(requestId);
        return context != null ? context.headers : null;
    }

    public static void signalCompletion(String requestId)
    {
        RequestContext context = requestContextMap.get(requestId);
        if (context != null) {
            context.latch.countDown();
        }
    }
}
