package rewards.internal.aspects;

import org.springframework.stereotype.Component;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rewards.internal.monitor.Monitor;
import rewards.internal.monitor.MonitorFactory;

// 02: Use AOP to log a message before
// any repository's find...() method is invoked.
// - Add an appropriate annotation to this class to indicate this class is an aspect.
// - Also make it as a component.
// - Optionally place @Autowired annotation on the constructor
// where `MonitorFactory` dependency is being injected.
// (It is optional since there is only a single constructor in the class.)
@Aspect
@Component
public class LoggingAspect {

    public final static String BEFORE = "'Before'";

    public final static String AROUND = "'Around'";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MonitorFactory monitorFactory;


    public LoggingAspect(final MonitorFactory monitorFactory) {
        super();
        this.monitorFactory = monitorFactory;
    }


    // 03: Write Pointcut Expression
    // - Decide which advice type is most appropriate
    // - Write a pointcut expression that selects only find* methods on
    // our repository classes.
    @Before("execution(* rewards..*.*Repository.find*(..))")
    public void implLogging(final JoinPoint joinPoint) {
        // Do not modify this log message or the test will fail
        this.logger.info(BEFORE + " advice implementation - " + joinPoint.getTarget().getClass() + //
                "; Executing before " + joinPoint.getSignature().getName() + //
                "() method");
    }


    // 07: Use AOP to time update...() methods.
    //
    // - Mark this method as around advice.
    // - Write a pointcut expression to match on all update* methods
    // on Repository classes.
    @Around("execution(* rewards..*.*Repository.update*(..))")
    public Object monitor(final ProceedingJoinPoint repositoryMethod) throws Throwable {
        final String name = this.createJoinPointTraceName(repositoryMethod);
        final Monitor monitor = this.monitorFactory.start(name);
        try {
            // Invoke repository method ...

            // 08: Add the logic to proceed with the target method invocation.
            // - Be sure to return the target method's return value to the caller
            // and delete the line below.
            return repositoryMethod.proceed();

            // return new String("Delete this line after completing TODO-08");

        } finally {
            monitor.stop();
            // Do not modify this log message or the test will fail
            this.logger.info(AROUND + " advice implementation - " + monitor);
        }
    }

    private String createJoinPointTraceName(final JoinPoint joinPoint) {
        final Signature signature = joinPoint.getSignature();
        final StringBuilder sb = new StringBuilder();
        sb.append(signature.getDeclaringType().getSimpleName());
        sb.append('.').append(signature.getName());
        return sb.toString();
    }

}
