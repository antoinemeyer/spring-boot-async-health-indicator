



# Spring Boot Async Health Indicator

Async Health Indicator for [spring-boot-actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html) >=2.2.0 gives [Health Indicator](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/actuate/health/HealthIndicator.html) the ability to get refreshed asynchronously on a background ThreadPoolExecutor using the annotation `@AsyncHealth`.

##### When annotating a `HealthIndicator` with `@AsyncHealth`:

The `health()` method is invoked on application startup and with the configured `refreshRate` (in second) between the termination of one execution and the commencement of the next.
The `/health` endpoint will not invoke the `health()` method but return the last `Health` calculated asynchronously.

The duration of the `health()` method is monitored. If it exceeds the configured `timeout`, any subsequent calls to `/health` will return this HealthIndicator as `DOWN` until the next `health()` method completion.

The following details are added to the pre-existing `Health` details:

  - `lastChecked` (`LocalDateTime`): Defines when that last `Health` started execution.
  - `lastDuration` (Duration in ms): Defines how long it took to calculate that last `Health`.
  - `reason` (enum): Added only if the Health Indicator is marked`DOWN`: `Exception` or `Timeout`.

## Advantages over synchronous Indicators

  - Expensive Health Indicators that do not have to be calculated every time `/health` is called can run on their own schedule.
  - The call to `/health` is now super fast as it returns pre-calculated `Health`.
  - Multiple Health Indicators can run in parallel.
  - Timeouts can be controlled per Health Indicator.

## Usage

This module is auto-configured.

  -  Include dependency from maven central:
```
<dependency>
  <groupId>com.teketik</groupId>
  <artifactId>async-health-indicator</artifactId>
  <version>boot2-v1.1</version>
</dependency>
```
  - Annotate any `HealthIndicator` with `@AsyncHealth(refreshRate = $REFRESH_RATE, timeout = $TIMEOUT)` 

`$REFRESH_RATE` = Fixed delay in seconds between the termination of the `health()` execution and the commencement of the next (default 1).
`$TIMEOUT`= The maximum time in seconds that the `health()` execution can take before being considered `DOWN` (default 10).

## Regarding Timeout

When a `health()` method duration exceeds the configured `timeout`, the thread running it is `interrupted`with the hope that the method will fail with an exception (causing it to be `DOWN`) and free up the thread. 
Unfortunately, most I/O calls are not interruptible and the thread may continue to execute the method until it times out (according to the libraries and configuration used).
If that happens, you will observe the `timeout` error message printed for each `/health` hit until that method times out like:
```
ERROR AsyncHealthIndicator   : HealthIndicator[name=myIndicator] is taking too long to execute [duration=2121ms][timeout=2s]
ERROR AsyncHealthIndicator   : HealthIndicator[name=myIndicator] is taking too long to execute [duration=3189ms][timeout=2s]
```

It is therefore recommended to ensure that your  `health()` methods can time out naturally within an acceptable window (matching the configured `timeout`)

## ThreadPool Configuration

 | Property | Description | Default |
 | -------- | ----------- | ------- |
 | `management.health.async.pool.max-size` | Number of maximum threads calculating the `health()` methods. (Note that this max will likely be reached on application startup when all indicators are starting up but will likely size down when different durations allow threads to be reused more efficiently). | 10 |
 | `management.health.async.pool.keep-alive` | Maximum time that excess idle threads will wait for new tasks before terminating.| 10 |


## Logging


  - On application startup, all cached health indicators are logged under logger `com.teketik.spring.health.AsyncHealthIndicatorAutoConfiguration` as `INFO`:

*Example*: `Initializing AsyncHealthIndicator[name=myIndicator][refreshRate=3s][timeout=2s]`

  - All `@AsyncHealth` annotated `HealthIndicator`s have details logged under logger `com.teketik.spring.health.AsyncHealthIndicator` as `DEBUG`:

*Example*: `HealthIndicator[name=myIndicator][duration=147ms][status=UP {detailKey=detailValue}]`

## Limitations

Only implementations of `HealthIndicator` are currently supported. `Composites` are not. (Let me know if you need that!).

## Notes

  - `HealthIndicator`  will return  `Status.UNKNOWN`  if the `/health` endpoint is called before the first  `HealthIndicator.health()`  check is completed. (likely to occur on application startup).

 
