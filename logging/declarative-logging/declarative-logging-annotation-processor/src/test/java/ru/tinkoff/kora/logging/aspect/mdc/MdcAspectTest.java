package ru.tinkoff.kora.logging.aspect.mdc;

import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.aop.annotation.processor.AopAnnotationProcessor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MdcAspectTest extends AbstractMdcAspectTest {

    // TODO Написать тесты на void методы.
    //  Написать тесты на global параметры
    //  Возможно, больше комбинаций различных параметров

    private final MDCContextHolder contextHolder = new MDCContextHolder();

    @Test
    void testMdc() throws Exception {
        var aopProxy = compile(
            List.of(new AopAnnotationProcessor()),
            """
                import ru.tinkoff.kora.logging.aspect.mdc.MDCContextHolder;
                import ru.tinkoff.kora.logging.common.MDC;
                
                public class TestMdc {
                
                  private final MDCContextHolder mdcContextHolder;
                
                  public TestMdc(MDCContextHolder mdcContextHolder) {
                      this.mdcContextHolder = mdcContextHolder;
                  }
                
                  @Mdc(key = "key", value = "value")
                  @Mdc(key = "key1", value = "value2")
                  public Integer test(@Mdc(key = "123") String s) {
                      mdcContextHolder.set(MDC.get().values());
                      return null;
                  }
                }
                """
        );

        aopProxy.assertSuccess();

        var generatedClass = aopProxy.loadClass("$TestMdc__AopProxy");
        var constructor = generatedClass.getConstructors()[0];
        var params = new Object[constructor.getParameterCount()];
        params[0] = contextHolder;
        final TestObject testObject = new TestObject(generatedClass, constructor.newInstance(params));

        testObject.invoke("test", "test");

        final Map<String, String> context = contextHolder.get()
            .entrySet()
            .stream()
            .map(entry -> Map.entry(entry.getKey(), entry.getValue().writeToString()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertEquals(Map.of("key", "\"value\"", "key1", "\"value2\"", "123", "\"test\""), context);
    }

    @Test
    void testMdcWithCode() throws Exception {
        var aopProxy = compile(
            List.of(new AopAnnotationProcessor()),
            """
                import ru.tinkoff.kora.logging.aspect.mdc.MDCContextHolder;
                import ru.tinkoff.kora.logging.common.MDC;
                
                public class TestMdc {
                
                  private final MDCContextHolder mdcContextHolder;
                
                  public TestMdc(MDCContextHolder mdcContextHolder) {
                      this.mdcContextHolder = mdcContextHolder;
                  }
                
                  @Mdc(key = "key", value = "${java.util.UUID.randomUUID().toString()}")
                  public Integer test(String s) {
                      mdcContextHolder.set(MDC.get().values());
                      return null;
                  }
                }
                """
        );

        aopProxy.assertSuccess();

        var generatedClass = aopProxy.loadClass("$TestMdc__AopProxy");
        var constructor = generatedClass.getConstructors()[0];
        var params = new Object[constructor.getParameterCount()];
        params[0] = contextHolder;
        final TestObject testObject = new TestObject(generatedClass, constructor.newInstance(params));

        testObject.invoke("test", "test");

        final Map<String, String> context = contextHolder.get()
            .entrySet()
            .stream()
            .map(entry -> Map.entry(entry.getKey(), entry.getValue().writeToString()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        final String value = context.get("key");
        assertNotNull(value);
        assertDoesNotThrow(() -> UUID.fromString(value.substring(1, value.length() - 1)));
    }

    @Test
    void testVoidMethodWithMdc() throws Exception {
        var aopProxy = compile(
            List.of(new AopAnnotationProcessor()),
            """
                import ru.tinkoff.kora.logging.aspect.mdc.MDCContextHolder;
                import ru.tinkoff.kora.logging.common.MDC;
                
                public class TestMdc {
                
                  private final MDCContextHolder mdcContextHolder;
                
                  public TestMdc(MDCContextHolder mdcContextHolder) {
                      this.mdcContextHolder = mdcContextHolder;
                  }
                
                  @Mdc(key = "key", value = "value")
                  @Mdc(key = "key1", value = "value2")
                  public void test(@Mdc("123") String s) {
                      mdcContextHolder.set(MDC.get().values());
                  }
                }
                """
        );

        aopProxy.assertSuccess();

        var generatedClass = aopProxy.loadClass("$TestMdc__AopProxy");
        var constructor = generatedClass.getConstructors()[0];
        var params = new Object[constructor.getParameterCount()];
        params[0] = contextHolder;
        final TestObject testObject = new TestObject(generatedClass, constructor.newInstance(params));

        testObject.invoke("test", "test");

        final Map<String, String> context = contextHolder.get()
            .entrySet()
            .stream()
            .map(entry -> Map.entry(entry.getKey(), entry.getValue().writeToString()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertEquals(Map.of("key", "\"value\"", "key1", "\"value2\"", "123", "\"test\""), context);
    }
}
