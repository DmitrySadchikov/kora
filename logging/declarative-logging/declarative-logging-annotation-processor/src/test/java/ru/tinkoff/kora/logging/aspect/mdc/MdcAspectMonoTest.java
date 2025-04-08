package ru.tinkoff.kora.logging.aspect.mdc;

import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.annotation.processor.common.AbstractAnnotationProcessorTest;
import ru.tinkoff.kora.aop.annotation.processor.AopAnnotationProcessor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MdcAspectMonoTest extends AbstractMdcAspectTest {

    private final MDCContextHolder contextHolder = new MDCContextHolder();

    @Test
    void testMdc() throws Exception {
        var aopProxy = compile(
            List.of(new AopAnnotationProcessor()),
            """
                
                import ru.tinkoff.kora.logging.aspect.mdc.MDCContextHolder;
                import ru.tinkoff.kora.logging.common.MDC;
                import reactor.core.publisher.Mono;
                
                public class TestMonoMdc {
                
                  private final MDCContextHolder mdcContextHolder;
                
                  public TestMonoMdc(MDCContextHolder mdcContextHolder) {
                      this.mdcContextHolder = mdcContextHolder;
                  }
                
                  @Mdc(key = "key", value = "value")
                  @Mdc(key = "key1", value = "value2")
                  public Mono<?> test(@Mdc(key = "123", value = "value3") String s) {
                      return Mono.fromRunnable(() -> mdcContextHolder.set(MDC.get().values()));
                  }
                }
                """
        );

        aopProxy.assertSuccess();

        var generatedClass = aopProxy.loadClass("$TestMonoMdc__AopProxy");
        var constructor = generatedClass.getConstructors()[0];
        var params = new Object[constructor.getParameterCount()];
        params[0] = contextHolder;
        final AbstractAnnotationProcessorTest.TestObject testObject = new AbstractAnnotationProcessorTest.TestObject(generatedClass, constructor.newInstance(params));

        testObject.invoke("test", "test");

        final Map<String, String> context = contextHolder.get()
            .entrySet()
            .stream()
            .map(entry -> Map.entry(entry.getKey(), entry.getValue().writeToString()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertEquals(Map.of("key", "\"value\"", "key1", "\"value2\"", "123", "\"test\""), context);
    }

    @Test
    void testGlobalMdcShouldThrowException() {
        final RuntimeException runtimeException = assertThrows(
            RuntimeException.class,
            () -> compile(
                List.of(new AopAnnotationProcessor()),
                """
                    
                    import ru.tinkoff.kora.logging.aspect.mdc.MDCContextHolder;
                    import ru.tinkoff.kora.logging.common.MDC;
                    import reactor.core.publisher.Mono;
                    
                    public class TestFluxMdc {
                    
                      private final MDCContextHolder mdcContextHolder;
                    
                      public TestFluxMdc(MDCContextHolder mdcContextHolder) {
                          this.mdcContextHolder = mdcContextHolder;
                      }
                    
                      @Mdc(key = "key", value = "value", global = true)
                      public Mono<?> test(String s) {
                          return Mono.fromRunnable(() -> mdcContextHolder.set(MDC.get().values()));
                      }
                    }
                    """
            )
        );

        assertInstanceOf(
            UnsupportedOperationException.class,
            runtimeException
        );
    }

    @Test
    void testGlobalMdcArgumentShouldThrowException() {
        final RuntimeException runtimeException = assertThrows(
            RuntimeException.class,
            () -> compile(
                List.of(new AopAnnotationProcessor()),
                """
                    
                    import ru.tinkoff.kora.logging.aspect.mdc.MDCContextHolder;
                    import ru.tinkoff.kora.logging.common.MDC;
                    import reactor.core.publisher.Mono;
                    
                    public class TestFluxMdc {
                    
                      private final MDCContextHolder mdcContextHolder;
                    
                      public TestFluxMdc(MDCContextHolder mdcContextHolder) {
                          this.mdcContextHolder = mdcContextHolder;
                      }
                    
                      public Mono<?> test(@Mdc(key = "key", global = true) String s) {
                          return Mono.fromRunnable(() -> mdcContextHolder.set(MDC.get().values()));
                      }
                    }
                    """
            )
        );

        assertInstanceOf(
            UnsupportedOperationException.class,
            runtimeException
        );
    }
}
