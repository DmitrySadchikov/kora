package ru.tinkoff.kora.logging.aspect.mdc;

import org.intellij.lang.annotations.Language;
import ru.tinkoff.kora.annotation.processor.common.AbstractAnnotationProcessorTest;

import java.util.List;

public abstract class AbstractMdcAspectTest extends AbstractAnnotationProcessorTest {

    @Override
    protected String commonImports() {
        return super.commonImports() + """
            import ru.tinkoff.kora.logging.common.annotation.Mdc;
            """;
    }

    protected static List<String> sources(@Language("java") String... sources) {
        return List.of(sources);
    }
}
