

# Spring Boot Async Health Indicator

Async Health Indicator for [spring-boot-actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html) >=2.2.0 gives [Health Indicator](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/actuate/health/HealthIndicator.html) the ability to get refreshed on a background [ScheduledThreadPoolExecutor](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ScheduledThreadPoolExecutor.html) using the annotation `@AsyncHealth`.

##### When annotating a `HealthIndicator` with `@AsyncHealth`:

TODO add stuff regarding timeout

The `health()` method is invoked on application startup and with the given delay (in second) between the termination of one execution and the commencement of the next.

The last `Health` calculated will be returned when calling the `/health` endpoint.

The following details are added to the `Health`:

  - `lastChecked` (`LocalDateTime`): Defines when the health was calculated.
  - `lastDuration` (Duration in ms): Defines how long it took to calculate that last `Health`.

## Advantages over synchronous Indicators

  - Expensive Health Indicators that do not have to be calculated every time `/health` is called can run on their own schedule.
  - The call to `/health` is now super fast as it returns pre-calculated `Health`.
  - Multiple Health Indicators can run in parallel.

## Usage

This module is auto-configured.

  -  Include dependency from maven central:
```
<dependency>
  <groupId>com.teketik</groupId>
  <artifactId>async-health-indicator</artifactId>
  <version>boot2-v1.0</version>
</dependency>
```
  - Annotate any `HealthIndicator` with `@AsyncHealth($REFRESH_RATE)` 

`$REFRESH_RATE` = Fixed delay in seconds between the termination of the `health()` execution and the commencement of the next (default 1)


## ScheduledThreadPool Configuration

TODO dynamic pool size ?

 | Property | Description | Default |
 | -------- | ----------- | ------- |
 | `management.health.async.maximum-pool-size` | Number of maximum threads calculating the `health()` methods. (Note that this max will likely be reached on application startup when all indicators are starting up but will likely size down when different durations allow threads to be reused more efficiently). | 10 |


## Logging


TODO update with trace debug and shit

  - On application startup, all cached health indicators are logged under logger `com.teketik.spring.health.AsyncHealthIndicatorAutoConfiguration` as `INFO`:

*Example*: `Using a AsyncHealthIndicator for myIndicator with [refreshRate=2s]`

  - All `@AsyncHealth` annotated `HealthIndicator`s have details logged under logger `com.teketik.spring.health.AsyncHealthIndicator` as `TRACE`:

*Example*: `myIndicator computed in 247ms is UP {detailKey=detailValue}`

 
## Limitations

Only implementations of `HealthIndicator` are currently supported. `Composites` are not. (Let me know if you need that!).

## Notes

  - `HealthIndicator`  will return  `Status.UNKNOWN`  if the `/health` endpoint is called before the first  `HealthIndicator.health()`  check is completed. (likely to occur on application startup).
