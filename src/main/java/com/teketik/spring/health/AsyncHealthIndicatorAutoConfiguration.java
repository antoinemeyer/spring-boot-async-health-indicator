
package com.teketik.spring.health;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.NamedContributor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//https://stackoverflow.com/questions/59580882/make-spring-boot-actuator-heath-run-checks-in-parallel
// https://github.com/spring-projects/spring-boot/issues/2652
// https://github.com/spring-projects/spring-boot/issues/25459
@EnableConfigurationProperties(AsyncHealthIndicatorConfiguration.class)
@ConditionalOnClass(HealthContributorRegistry.class)
@ConditionalOnBean(HealthContributorRegistry.class)
@AutoConfigureAfter(HealthEndpointAutoConfiguration.class)
//TODO find a more efficient way to tap into the contributor map?
class AsyncHealthIndicatorAutoConfiguration implements InitializingBean, DisposableBean {

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private HealthContributorRegistry healthContributorRegistry;

    @Autowired
    private AsyncHealthIndicatorConfiguration asyncHealthIndicatorConfiguration;

    private ScheduledThreadPoolExecutor timeoutScheduledThreadPoolExecutor;

    @Override
    public void afterPropertiesSet() throws Exception {
        final int poolSize = asyncHealthIndicatorConfiguration.getPoolSize();
        final Map<AsyncHealthIndicator, Integer> asyncHealthIndicators = new HashMap<>();
        for (NamedContributor<?> namedContributor : healthContributorRegistry) {
            final String contributorName = namedContributor.getName();
            final Object indicatorAsObject = namedContributor.getContributor();
            final AsyncHealth annotation = AnnotationUtils.findAnnotation(indicatorAsObject.getClass(), AsyncHealth.class);
            if (annotation != null) {
                if (indicatorAsObject instanceof HealthIndicator) {
                    final HealthIndicator indicator = (HealthIndicator) indicatorAsObject;
                    int refreshRate = annotation.value();
                    healthContributorRegistry.unregisterContributor(contributorName);
                    final AsyncHealthIndicator cacheableHealthIndicator = new AsyncHealthIndicator(indicator, contributorName);
                    healthContributorRegistry.registerContributor(contributorName, cacheableHealthIndicator);
                    asyncHealthIndicators.put(cacheableHealthIndicator, refreshRate);
                    logger.info("Using a " + AsyncHealthIndicator.class.getSimpleName() + " for " + contributorName + " with [refreshRate=" + refreshRate + "s]");
                } else {
                    logger.warn(contributorName + " is annotated with " + AsyncHealth.class + " but is not a " + HealthIndicator.class + "!");
                }
            }
        }
        if (!asyncHealthIndicators.isEmpty()) {
            logger.info("Initializating " + asyncHealthIndicators.size() + " asynchronous health indicators with [poolSize=" + poolSize + "]");
            timeoutScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(poolSize);
            for (Entry<AsyncHealthIndicator, Integer> entry : asyncHealthIndicators.entrySet()) {
                timeoutScheduledThreadPoolExecutor.scheduleWithFixedDelay(entry.getKey(), 0, entry.getValue(), TimeUnit.SECONDS);
            }
        }
    }
    
    @Override
    public void destroy() throws Exception {
        if (timeoutScheduledThreadPoolExecutor != null) {
            timeoutScheduledThreadPoolExecutor.shutdown();
        }
    }

}
