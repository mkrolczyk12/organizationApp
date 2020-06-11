package io.github.organizationApp.aspect;

import io.github.organizationApp.LogConfigurationProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;


@Aspect
@Component
class LogicAspect {
    private static final Logger logger = LoggerFactory.getLogger(LogicAspect.class);
    private final LogConfigurationProperties config;

    private final String PROCESS_SERVICE_POINTCUT = "execution(* io.github.organizationApp.expensesProcess.ProcessService.*(..))";
    // TODO
    private final String CATEGORYTYPE_SERVICE_POINTCUT = "execution(* io.github.organizationApp.expensesCategoryType.CategoryTypeService.*.*(..))";
    //TODO
    private final Timer processTimer;
    // TODO
    private final Timer categoryTypeTimer;

    LogicAspect(final LogConfigurationProperties config, final MeterRegistry registry) {
        this.config = config;
        //TODO
        this.processTimer = registry.timer("------NONE-------");
        // TODO
        this.categoryTypeTimer = registry.timer("------NONE-------");
    }

    @Around(value = PROCESS_SERVICE_POINTCUT)
    Object aroundProcessServiceFunctions(ProceedingJoinPoint jp) {

        if (config.getShow().isAllowAroundEachClassServiceFunctionTrackExecuteTime()) {
            logger.info("PROCESS_SERVICE_POINTCUT: [METHOD] -> {}", jp.getSignature().toShortString());

            Object[] signatureArgs = jp.getArgs();
            for (Object signatureArg : signatureArgs) {
                logger.info("PROCESS_SERVICE_POINTCUT: [ARGS] -> {}: {}", signatureArg.getClass().getSimpleName(),
                        signatureArg.toString());
            }
            Instant startTime = Instant.now();
            Object obj = null;
            try {
                obj = jp.proceed();
            } catch (Throwable e) {
                if (e instanceof RuntimeException) {
                    return (RuntimeException) e;
                }
                return new RuntimeException(e);
            }
            Instant endTime = Instant.now();

            logger.info("PROCESS_SERVICE_POINTCUT: [METRICS] -> {}, time: {} {} ", jp.getSignature().toShortString(),
                    Duration.between(startTime, endTime).getNano(), "nanosec.");
            return obj;
        } else {
            try {
                return jp.proceed();
            } catch (Throwable e) {
                if (e instanceof RuntimeException) {
                    return e;
                }
                return new RuntimeException(e);
            }
        }
    }

    // TODO -> aroundCategoryTypeServiceFunctions()
    // TODO -> aroundMonthServiceFunctions()
    // TODO -> aroundYearServiceFunctions()
}
