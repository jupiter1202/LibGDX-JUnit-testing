package de.jupiter1202.test.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Marks a test that needs the global libGDX environment.
 *
 * <p>The resource lock prevents two such tests from changing the static {@code Gdx.*} fields at
 * the same time if JUnit parallel execution is enabled later.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(HeadlessGdxExtension.class)
@ResourceLock(value = HeadlessGdxExtension.GDX_STATIC_STATE, mode = ResourceAccessMode.READ_WRITE)
public @interface GdxTest {
}
