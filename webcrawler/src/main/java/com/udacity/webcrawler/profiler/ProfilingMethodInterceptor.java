package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.lang.IllegalArgumentException;

import java.time.Instant;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final Object objectInstance;
  private final ProfilingState state;

  // TODO: You will need to add more instance fields and constructor arguments to this class.
   ProfilingMethodInterceptor(Clock clock, Object delegate, ProfilingState profileState) {
    this.clock = Objects.requireNonNull(clock);
    this.objectInstance = delegate;
    this.state = profileState;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // TODO: This method interceptor should inspect the called method to see if it is a profiled
    //       method. For profiled methods, the interceptor should record the start time, then
    //       invoke the method using the object that is being profiled. Finally, for profiled
    //       methods, the interceptor should record how long the method call took, using the
    //       ProfilingState methods.

        //Checks if the annotation present on this class is of the profiled type. If so, we can proceed to the other logic
        Throwable ThrownExceptionError = null;
        Object result = new Object();
        Instant StartTime = clock.instant();
        try {
        result = method.invoke(objectInstance, args);
        return result;
        }
        catch (InvocationTargetException e) {
          throw e.getCause();
        }
        catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
        finally {
        if (method.isAnnotationPresent(Profiled.class)) {
        Instant EndTime = clock.instant();
        Duration elapsedTime = Duration.between(StartTime, EndTime);
        state.record(objectInstance.getClass(), method, elapsedTime);
        }
      }
    
  }
}
