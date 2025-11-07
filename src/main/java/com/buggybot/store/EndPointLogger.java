package com.buggybot.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.Set;

@Component
public class EndPointLogger {

    private static final Logger logger = LoggerFactory.getLogger(EndPointLogger.class);

    private final RequestMappingHandlerMapping handlerMapping;

    public EndPointLogger(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logEndpoints() {
        try {
            if (handlerMapping == null) {
                logger.warn("RequestMappingHandlerMapping not available — skipping endpoint log.");
                return;
            }

            logger.info("=== Registered API Endpoints ===");
            Map<RequestMappingInfo, HandlerMethod> map = handlerMapping.getHandlerMethods();

            map.forEach((info, method) -> {
                try {
                    // defensive null checks for conditions
                    Set<String> patterns = info.getPatternsCondition() != null
                            ? info.getPatternsCondition().getPatterns()
                            : Set.of();
                    Set<RequestMethod> httpMethods = info.getMethodsCondition() != null
                            ? info.getMethodsCondition().getMethods()
                            : Set.of();

                    // only log your app routes
                    String pkg = method.getBeanType() != null ? method.getBeanType().getPackageName() : "";
                    if (pkg.startsWith("com.buggybot")) {
                        String methods = httpMethods.isEmpty() ? "[ANY]" : httpMethods.toString();
                        String paths = patterns.isEmpty() ? "[]" : patterns.toString();
                        String handler = (method.getBeanType() != null ? method.getBeanType().getSimpleName() : "Unknown")
                                + "#" + (method.getMethod() != null ? method.getMethod().getName() : "unknownMethod");

                        logger.info("{} {} -> {}", methods, paths, handler);
                    }
                } catch (Throwable inner) {
                    // protect the loop from unexpected mapping shapes
                    logger.warn("Failed to log one mapping — continuing.", inner);
                }
            });

            logger.info("=== End of API Endpoints ===");
        } catch (Throwable t) {
            // absolutely never allow endpoint logging to fail startup
            logger.error("EndpointLogger encountered an error while logging endpoints — skipping endpoint logging.", t);
        }
    }
}
