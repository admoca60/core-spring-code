package rewards.internal.aspects;

import org.springframework.stereotype.Component;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rewards.internal.exception.RewardDataAccessException;


@Aspect
@Component
public class DBExceptionHandlingAspect {

    public static final String EMAIL_FAILURE_MSG = "Failed sending an email to Mister Smith : ";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    // -10 (Optional): Use AOP to log an exception.
    // (Steps 10, 11 and 12 are optional, skip them if you are short on time)
    //
    // - Configure this advice method to enable logging of
    // exceptions thrown by Repository class methods.
    // - Select the advice type that seems most appropriate.
    @AfterThrowing(pointcut = "execution(* rewards..*.*Repository.*(..))", throwing = "e")
    public void implExceptionHandling(final RewardDataAccessException e) {
        // Log a failure warning
        this.logger.warn(EMAIL_FAILURE_MSG + e + "\n");
    }

    // -11 (Optional): Annotate this class as a Spring-managed bean.
    // - Note that we enabled component scanning in an earlier step.

}
