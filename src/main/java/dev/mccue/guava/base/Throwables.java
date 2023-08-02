/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package dev.mccue.guava.base;

import static dev.mccue.guava.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import dev.mccue.jsr305.CheckForNull;

/**
 * Static utility methods pertaining to instances of {@code Throwable}.
 *
 * <p>See the Guava User Guide entry on <a
 * href="https://github.com/google/guava/wiki/ThrowablesExplained">Throwables</a>.
 *
 * @author Kevin Bourrillion
 * @author Ben Yu
 * @since 1.0
 */

@ElementTypesAreNonnullByDefault
public final class Throwables {
  private Throwables() {}

  /**
   * Throws {@code throwable} if it is an instance of {@code declaredType}. Example usage:
   *
   * <pre>
   * for (Foo foo : foos) {
   *   try {
   *     foo.bar();
   *   } catch (BarException | RuntimeException | Error t) {
   *     failure = t;
   *   }
   * }
   * if (failure != null) {
   *   throwIfInstanceOf(failure, BarException.class);
   *   throwIfUnchecked(failure);
   *   throw new AssertionError(failure);
   * }
   * </pre>
   *
   * @since 20.0
   */
  // Class.cast, Class.isInstance
  public static <X extends Throwable> void throwIfInstanceOf(
      Throwable throwable, Class<X> declaredType) throws X {
    checkNotNull(throwable);
    if (declaredType.isInstance(throwable)) {
      throw declaredType.cast(throwable);
    }
  }

  /**
   * Propagates {@code throwable} exactly as-is, if and only if it is an instance of {@code
   * declaredType}. Example usage:
   *
   * <pre>
   * try {
   *   someMethodThatCouldThrowAnything();
   * } catch (IKnowWhatToDoWithThisException e) {
   *   handle(e);
   * } catch (Throwable t) {
   *   Throwables.propagateIfInstanceOf(t, IOException.class);
   *   Throwables.propagateIfInstanceOf(t, SQLException.class);
   *   throw Throwables.propagate(t);
   * }
   * </pre>
   *
   * @deprecated Use {@code #throwIfInstanceOf}, which has the same behavior but rejects {@code
   *     null}.
   */
  @Deprecated
  // throwIfInstanceOf
  public static <X extends Throwable> void propagateIfInstanceOf(
      @CheckForNull Throwable throwable, Class<X> declaredType) throws X {
    if (throwable != null) {
      throwIfInstanceOf(throwable, declaredType);
    }
  }

  /**
   * Throws {@code throwable} if it is a {@code RuntimeException} or {@code Error}. Example usage:
   *
   * <pre>
   * for (Foo foo : foos) {
   *   try {
   *     foo.bar();
   *   } catch (RuntimeException | Error t) {
   *     failure = t;
   *   }
   * }
   * if (failure != null) {
   *   throwIfUnchecked(failure);
   *   throw new AssertionError(failure);
   * }
   * </pre>
   *
   * @since 20.0
   */
  public static void throwIfUnchecked(Throwable throwable) {
    checkNotNull(throwable);
    if (throwable instanceof RuntimeException) {
      throw (RuntimeException) throwable;
    }
    if (throwable instanceof Error) {
      throw (Error) throwable;
    }
  }

  /**
   * Propagates {@code throwable} exactly as-is, if and only if it is an instance of {@code
   * RuntimeException} or {@code Error}. Example usage:
   *
   * <pre>
   * try {
   *   someMethodThatCouldThrowAnything();
   * } catch (IKnowWhatToDoWithThisException e) {
   *   handle(e);
   * } catch (Throwable t) {
   *   Throwables.propagateIfPossible(t);
   *   throw new RuntimeException("unexpected", t);
   * }
   * </pre>
   *
   * @deprecated Use {@code #throwIfUnchecked}, which has the same behavior but rejects {@code
   *     null}.
   */
  @Deprecated
  public static void propagateIfPossible(@CheckForNull Throwable throwable) {
    if (throwable != null) {
      throwIfUnchecked(throwable);
    }
  }

  /**
   * Propagates {@code throwable} exactly as-is, if and only if it is an instance of {@code
   * RuntimeException}, {@code Error}, or {@code declaredType}. Example usage:
   *
   * <pre>
   * try {
   *   someMethodThatCouldThrowAnything();
   * } catch (IKnowWhatToDoWithThisException e) {
   *   handle(e);
   * } catch (Throwable t) {
   *   Throwables.propagateIfPossible(t, OtherException.class);
   *   throw new RuntimeException("unexpected", t);
   * }
   * </pre>
   *
   * @param throwable the Throwable to possibly propagate
   * @param declaredType the single checked exception type declared by the calling method
   */
  // propagateIfInstanceOf
  public static <X extends Throwable> void propagateIfPossible(
      @CheckForNull Throwable throwable, Class<X> declaredType) throws X {
    propagateIfInstanceOf(throwable, declaredType);
    propagateIfPossible(throwable);
  }

  /**
   * Propagates {@code throwable} exactly as-is, if and only if it is an instance of {@code
   * RuntimeException}, {@code Error}, {@code declaredType1}, or {@code declaredType2}. In the
   * unlikely case that you have three or more declared checked exception types, you can handle them
   * all by invoking these methods repeatedly. See usage example in {@code
   * #propagateIfPossible(Throwable, Class)}.
   *
   * @param throwable the Throwable to possibly propagate
   * @param declaredType1 any checked exception type declared by the calling method
   * @param declaredType2 any other checked exception type declared by the calling method
   */
  // propagateIfInstanceOf
  public static <X1 extends Throwable, X2 extends Throwable> void propagateIfPossible(
      @CheckForNull Throwable throwable, Class<X1> declaredType1, Class<X2> declaredType2)
      throws X1, X2 {
    checkNotNull(declaredType2);
    propagateIfInstanceOf(throwable, declaredType1);
    propagateIfPossible(throwable, declaredType2);
  }

  /**
   * Propagates {@code throwable} as-is if it is an instance of {@code RuntimeException} or {@code
   * Error}, or else as a last resort, wraps it in a {@code RuntimeException} and then propagates.
   *
   * <p>This method always throws an exception. The {@code RuntimeException} return type allows
   * client code to signal to the compiler that statements after the call are unreachable. Example
   * usage:
   *
   * <pre>
   * T doSomething() {
   *   try {
   *     return someMethodThatCouldThrowAnything();
   *   } catch (IKnowWhatToDoWithThisException e) {
   *     return handle(e);
   *   } catch (Throwable t) {
   *     throw Throwables.propagate(t);
   *   }
   * }
   * </pre>
   *
   * @param throwable the Throwable to propagate
   * @return nothing will ever be returned; this return type is only for your convenience, as
   *     illustrated in the example above
   * @deprecated Use {@code throw e} or {@code throw new RuntimeException(e)} directly, or use a
   *     combination of {@code #throwIfUnchecked} and {@code throw new RuntimeException(e)}. For
   *     background on the deprecation, read <a href="https://goo.gl/Ivn2kc">Why we deprecated
   *     {@code Throwables.propagate}</a>.
   */
  @CanIgnoreReturnValue
  @Deprecated
  public static RuntimeException propagate(Throwable throwable) {
    throwIfUnchecked(throwable);
    throw new RuntimeException(throwable);
  }

  /**
   * Returns the innermost cause of {@code throwable}. The first throwable in a chain provides
   * context from when the error or exception was initially detected. Example usage:
   *
   * <pre>
   * assertEquals("Unable to assign a customer id", Throwables.getRootCause(e).getMessage());
   * </pre>
   *
   * @throws IllegalArgumentException if there is a loop in the causal chain
   */
  public static Throwable getRootCause(Throwable throwable) {
    // Keep a second pointer that slowly walks the causal chain. If the fast pointer ever catches
    // the slower pointer, then there's a loop.
    Throwable slowPointer = throwable;
    boolean advanceSlowPointer = false;

    Throwable cause;
    while ((cause = throwable.getCause()) != null) {
      throwable = cause;

      if (throwable == slowPointer) {
        throw new IllegalArgumentException("Loop in causal chain detected.", throwable);
      }
      if (advanceSlowPointer) {
        slowPointer = slowPointer.getCause();
      }
      advanceSlowPointer = !advanceSlowPointer; // only advance every other iteration
    }
    return throwable;
  }

  /**
   * Gets a {@code Throwable} cause chain as a list. The first entry in the list will be {@code
   * throwable} followed by its cause hierarchy. Note that this is a snapshot of the cause chain and
   * will not reflect any subsequent changes to the cause chain.
   *
   * <p>Here's an example of how it can be used to find specific types of exceptions in the cause
   * chain:
   *
   * <pre>
   * Iterables.filter(Throwables.getCausalChain(e), IOException.class));
   * </pre>
   *
   * @param throwable the non-null {@code Throwable} to extract causes from
   * @return an unmodifiable list containing the cause chain starting with {@code throwable}
   * @throws IllegalArgumentException if there is a loop in the causal chain
   */
  public static List<Throwable> getCausalChain(Throwable throwable) {
    checkNotNull(throwable);
    List<Throwable> causes = new ArrayList<>(4);
    causes.add(throwable);

    // Keep a second pointer that slowly walks the causal chain. If the fast pointer ever catches
    // the slower pointer, then there's a loop.
    Throwable slowPointer = throwable;
    boolean advanceSlowPointer = false;

    Throwable cause;
    while ((cause = throwable.getCause()) != null) {
      throwable = cause;
      causes.add(throwable);

      if (throwable == slowPointer) {
        throw new IllegalArgumentException("Loop in causal chain detected.", throwable);
      }
      if (advanceSlowPointer) {
        slowPointer = slowPointer.getCause();
      }
      advanceSlowPointer = !advanceSlowPointer; // only advance every other iteration
    }
    return Collections.unmodifiableList(causes);
  }

  /**
   * Returns {@code throwable}'s cause, cast to {@code expectedCauseType}.
   *
   * <p>Prefer this method instead of manually casting an exception's cause. For example, {@code
   * (IOException) e.getCause()} throws a {@code ClassCastException} that discards the original
   * exception {@code e} if the cause is not an {@code IOException}, but {@code
   * Throwables.getCauseAs(e, IOException.class)} keeps {@code e} as the {@code
   * ClassCastException}'s cause.
   *
   * @throws ClassCastException if the cause cannot be cast to the expected type. The {@code
   *     ClassCastException}'s cause is {@code throwable}.
   * @since 22.0
   */
  // Class.cast(Object)
  @CheckForNull
  public static <X extends Throwable> X getCauseAs(
      Throwable throwable, Class<X> expectedCauseType) {
    try {
      return expectedCauseType.cast(throwable.getCause());
    } catch (ClassCastException e) {
      e.initCause(throwable);
      throw e;
    }
  }

  /**
   * Returns a string containing the result of {@code Throwable#toString() toString()}, followed by
   * the full, recursive stack trace of {@code throwable}. Note that you probably should not be
   * parsing the resulting string; if you need programmatic access to the stack frames, you can call
   * {@code Throwable#getStackTrace()}.
   */
  // java.io.PrintWriter, java.io.StringWriter
  public static String getStackTraceAsString(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    throwable.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }

  /**
   * Returns the stack trace of {@code throwable}, possibly providing slower iteration over the full
   * trace but faster iteration over parts of the trace. Here, "slower" and "faster" are defined in
   * comparison to the normal way to access the stack trace, {@code Throwable#getStackTrace()
   * throwable.getStackTrace()}. Note, however, that this method's special implementation is not
   * available for all platforms and configurations. If that implementation is unavailable, this
   * method falls back to {@code getStackTrace}. Callers that require the special implementation can
   * check its availability with {@code #lazyStackTraceIsLazy()}.
   *
   * <p>The expected (but not guaranteed) performance of the special implementation differs from
   * {@code getStackTrace} in one main way: The {@code lazyStackTrace} call itself returns quickly
   * by delaying the per-stack-frame work until each element is accessed. Roughly speaking:
   *
   * <ul>
   *   <li>{@code getStackTrace} takes {@code stackSize} time to return but then negligible time to
   *       retrieve each element of the returned list.
   *   <li>{@code lazyStackTrace} takes negligible time to return but then {@code 1/stackSize} time
   *       to retrieve each element of the returned list (probably slightly more than {@code
   *       1/stackSize}).
   * </ul>
   *
   * <p>Note: The special implementation does not respect calls to {@code Throwable#setStackTrace
   * throwable.setStackTrace}. Instead, it always reflects the original stack trace from the
   * exception's creation.
   *
   * @since 19.0
   * @deprecated This method is equivalent to {@code Throwable#getStackTrace()} on JDK versions past
   *     JDK 8 and on all Android versions. Use {@code Throwable#getStackTrace()} directly, or where
   *     possible use the {@code java.lang.StackWalker.walk} method introduced in JDK 9.
   */
  @Deprecated
  // lazyStackTraceIsLazy, jlaStackTrace

  public static List<StackTraceElement> lazyStackTrace(Throwable throwable) {
    return unmodifiableList(asList(throwable.getStackTrace()));
  }

  /**
   * Returns whether {@code #lazyStackTrace} will use the special implementation described in its
   * documentation.
   *
   * @since 19.0
   * @deprecated This method always returns false on JDK versions past JDK 8 and on all Android
   *     versions.
   */
  @Deprecated
  public static boolean lazyStackTraceIsLazy() {
    return false;
  }
}
