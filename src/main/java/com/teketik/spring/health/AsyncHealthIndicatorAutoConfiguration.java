
package com.teketik.spring.health;

import com.teketik.utils.SchedulingThreadPoolExecutor;

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

import java.util.ArrayList;
import java.util.List;

@EnableConfigurationProperties(AsyncHealthIndicatorConfiguration.class)
@ConditionalOnClass(HealthContributorRegistry.class)
@ConditionalOnBean(HealthContributorRegistry.class)
@AutoConfigureAfter(HealthEndpointAutoConfiguration.class)
//TODO find a more efficient way to tap into the contributor map?
class AsyncHealthIndicatorAutoConfiguration implements InitializingBean, DisposableBean {

    //TODO
    //in case of interutpion expcetion, say in healthcheck it likely timed out!

    //TODO add a note that many many rest calls cannot be interrupted.
    //the timeout will say it is timed out but the rest call may still be ongoing and the next check will start after this call has finished.
    // therefore PLEASE ENSURE THAT YOUR CALLS have timeouts!!


    //add in readme a section on how it works
    //add an example with a mermaid schema.


    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private HealthContributorRegistry healthContributorRegistry;

    @Autowired
    private AsyncHealthIndicatorConfiguration asyncHealthIndicatorConfiguration;

    private SchedulingThreadPoolExecutor schedulingThreadPoolExecutor;

    @Override
    public void afterPropertiesSet() throws Exception {
        final List<AsyncHealthIndicator> asyncHealthIndicators = new ArrayList<>();
        for (NamedContributor<?> namedContributor : healthContributorRegistry) {
            final String contributorName = namedContributor.getName();
            final Object indicatorAsObject = namedContributor.getContributor();
            final AsyncHealth annotation = AnnotationUtils.findAnnotation(indicatorAsObject.getClass(), AsyncHealth.class);
            if (annotation != null) {
                if (indicatorAsObject instanceof HealthIndicator) {
                    final HealthIndicator indicator = (HealthIndicator) indicatorAsObject;
                    healthContributorRegistry.unregisterContributor(contributorName);
                    final AsyncHealthIndicator asyncHealthIndicator = new AsyncHealthIndicator(
                        indicator,
                        contributorName,
                        annotation.refreshRate(),
                        annotation.timeout()
                    );
                    healthContributorRegistry.registerContributor(contributorName, asyncHealthIndicator);
                    asyncHealthIndicators.add(asyncHealthIndicator);
                    logger.info("Initializing " + asyncHealthIndicator);
                } else {
                    logger.warn(contributorName + " is annotated with " + AsyncHealth.class + " but is not a " + HealthIndicator.class + "!");
                }
            }
        }
        if (!asyncHealthIndicators.isEmpty()) {
            final int maxSize = asyncHealthIndicatorConfiguration.getMaxSize();
            final int keepAliveInSeconds = asyncHealthIndicatorConfiguration.getKeepAlive();
            logger.info("Initializing " + asyncHealthIndicators.size() + " asynchronous health indicators in pool [maxSize=" + maxSize + "][keepAlive=" + keepAliveInSeconds + "s]");
            schedulingThreadPoolExecutor = new SchedulingThreadPoolExecutor(maxSize, keepAliveInSeconds);
            for (AsyncHealthIndicator asyncHealthIndicator : asyncHealthIndicators) {
                schedulingThreadPoolExecutor.run(asyncHealthIndicator);
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        if (schedulingThreadPoolExecutor != null) {
            schedulingThreadPoolExecutor.shutdown();
        }
    }

}
