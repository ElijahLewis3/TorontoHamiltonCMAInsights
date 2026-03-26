package com.mfhe.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Order(2)
public class RateLimitFilter implements Filter {

    private static final int MAX_REQUESTS_PER_WINDOW = 60;
    private static final long WINDOW_MS = 60_000;

    private static final int ADMIN_MAX_REQUESTS = 3;
    private static final long ADMIN_WINDOW_MS = 300_000;

    private final Map<String, ClientBucket> buckets = new ConcurrentHashMap<>();
    private final Map<String, ClientBucket> adminBuckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String clientIp = req.getRemoteAddr();
        String path = req.getRequestURI();

        if (path.startsWith("/api/admin")) {
            if (!isAllowed(clientIp, adminBuckets, ADMIN_MAX_REQUESTS, ADMIN_WINDOW_MS)) {
                res.setStatus(429);
                res.setContentType("application/json");
                res.getWriter().write("{\"status\":\"Rate limit exceeded. Try again later.\"}");
                return;
            }
        } else if (path.startsWith("/api/")) {
            if (!isAllowed(clientIp, buckets, MAX_REQUESTS_PER_WINDOW, WINDOW_MS)) {
                res.setStatus(429);
                res.setContentType("application/json");
                res.getWriter().write("{\"status\":\"Rate limit exceeded. Try again later.\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isAllowed(String key, Map<String, ClientBucket> store,
                              int maxRequests, long windowMs) {
        long now = System.currentTimeMillis();
        ClientBucket bucket = store.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStart.get() > windowMs) {
                return new ClientBucket(now);
            }
            return existing;
        });

        return bucket.count.incrementAndGet() <= maxRequests;
    }

    private static class ClientBucket {
        final AtomicLong windowStart;
        final AtomicLong count;

        ClientBucket(long start) {
            this.windowStart = new AtomicLong(start);
            this.count = new AtomicLong(0);
        }
    }
}
