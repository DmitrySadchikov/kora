package ru.tinkoff.kora.logging.aspect.mdc;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.tinkoff.kora.aop.annotation.processor.AopAnnotationProcessor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MdcAspectFutureTest extends AbstractMdcAspectTest {

    @ParameterizedTest
    @MethodSource("sourcesWithMdcAndFuture")
    void testMdc(@Language("java") String source) {
        assertThrows(
            UnsupportedOperationException.class,
            () -> compile(
                List.of(new AopAnnotationProcessor()),
                source
            )
        );
    }

    private static List<String> sourcesWithMdcAndFuture() {
        return sources(
            """
                
                import java.util.concurrent.CompletionStage;
                
                public class TestMdc {
                
                  @Mdc(key = "key", value = "value", global = true)
                  @Mdc(key = "key1", value = "value2")
                  public CompletionStage<?> test(@Mdc(key = "123", value = "value3") String s) {
                      return Completablefuture.completedFuture(1);
                  }
                }
                """,
            """
                
                import java.util.concurrent.CompletionStage;
                
                public class TestMdc {
                
                  @Mdc(key = "key1", value = "value2")
                  public CompletionStage<?> test(String s) {
                      return Completablefuture.completedFuture(1);
                  }
                }
                """,
            """
                
                import java.util.concurrent.CompletionStage;
                
                public class TestMdc {
                
                  public CompletionStage<?> test(@Mdc(key = "123") String s) {
                      return Completablefuture.completedFuture(1);
                  }
                }
                """
        );
    }
}
