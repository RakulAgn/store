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

import java.util.*;

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

            Map<RequestMappingInfo, HandlerMethod> map = handlerMapping.getHandlerMethods();

            // Group by controller simple name
            Map<String, List<String>> byController = new TreeMap<>(); // TreeMap -> sorted by controller name

            map.forEach((info, method) -> {
                try {
                    // defensive null checks
                    Set<String> patterns = info.getPatternsCondition() != null
                            ? info.getPatternsCondition().getPatterns()
                            : Set.of();
                    Set<RequestMethod> httpMethods = info.getMethodsCondition().getMethods();

                    // only log your app routes (filter by package)
                    Class<?> beanType = method.getBeanType();
                    String pkg = beanType.getPackage() != null
                            ? beanType.getPackageName()
                            : "";
                    if (!pkg.startsWith("com.buggybot")) {
                        return; // skip non-app controllers
                    }

                    String controllerName = beanType.getSimpleName();
                    String handler = method.getMethod().getName();

                    String methodsText = httpMethods.isEmpty() ? "[ANY]" : httpMethods.toString();
                    String pathsText = patterns.isEmpty() ? "[]" : patterns.toString();

                    String entry = String.format("  %s %s -> %s#%s",
                            methodsText, pathsText, controllerName, handler);

                    byController.computeIfAbsent(controllerName, k -> new ArrayList<>()).add(entry);
                } catch (Throwable inner) {
                    logger.warn("Failed to process one mapping — continuing.", inner);
                }
            });

            // Print grouped output
            logger.info("=== Registered API Endpoints (grouped by controller) ===");
            byController.forEach((controller, entries) -> {
                // sort entries to keep output stable
                Collections.sort(entries);
                logger.info("{}:", controller);
                entries.forEach(logger::info);
            });
            logger.info("=== End of API Endpoints ===");
        } catch (Throwable t) {
            logger.error("EndpointLogger encountered an error while logging endpoints — skipping endpoint logging.", t);
        }
    }
}
