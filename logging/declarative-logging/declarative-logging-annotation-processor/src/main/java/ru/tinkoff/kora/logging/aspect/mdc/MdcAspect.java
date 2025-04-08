package ru.tinkoff.kora.logging.aspect.mdc;

import com.squareup.javapoet.CodeBlock;
import ru.tinkoff.kora.annotation.processor.common.MethodUtils;
import ru.tinkoff.kora.aop.annotation.processor.KoraAspect;
import ru.tinkoff.kora.logging.common.annotation.Mdc;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.function.Predicate.not;
import static ru.tinkoff.kora.logging.aspect.mdc.MdcAspectClassNames.mdc;
import static ru.tinkoff.kora.logging.aspect.mdc.MdcAspectClassNames.mdcAnnotation;
import static ru.tinkoff.kora.logging.aspect.mdc.MdcAspectClassNames.mdcsAnnotation;

public class MdcAspect implements KoraAspect {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(mdcAnnotation.canonicalName(), mdcsAnnotation.canonicalName());
    }

    @Override
    public ApplyResult apply(ExecutableElement executableElement, String superCall, AspectContext aspectContext) {
        final Mdc[] methodAnnotations = executableElement.getAnnotationsByType(Mdc.class);

        final List<? extends VariableElement> parametersWithAnnotation = executableElement.getParameters()
            .stream()
            .filter(param -> param.getAnnotationsByType(Mdc.class).length != 0)
            .toList();

        if (methodAnnotations.length == 0 && parametersWithAnnotation.isEmpty()) {
            final CodeBlock code = CodeBlock.builder()
                .add(MethodUtils.isVoid(executableElement) ? "" : "return ")
                .addStatement(KoraAspect.callSuper(executableElement, superCall))
                .build();
            return new ApplyResult.MethodBody(code);
        }

        if (MethodUtils.isMono(executableElement) || MethodUtils.isFlux(executableElement)) {
            return reactiveBody(executableElement, superCall, methodAnnotations, parametersWithAnnotation);
        }
        if (MethodUtils.isFuture(executableElement)) {
            throw new UnsupportedOperationException("Future response type is not supported");
        }

        return blockingBody(executableElement, superCall, methodAnnotations, parametersWithAnnotation);
    }

    private ApplyResult.MethodBody reactiveBody(ExecutableElement executableElement, String superCall, Mdc[] methodAnnotations, List<? extends VariableElement> parametersWithAnnotation) {
        final CodeBlock.Builder fillMdcBuilder = CodeBlock.builder();
        final Set<String> methodKeys = fillMdcByMethodAnnotations(methodAnnotations, fillMdcBuilder, false);
        final Set<String> parametersKeys = fillMdcByParametersAnnotations(parametersWithAnnotation, fillMdcBuilder, false);
        final CodeBlock.Builder clearMdcBuilder = CodeBlock.builder();
        clearMdc(methodKeys, clearMdcBuilder);
        clearMdc(parametersKeys, clearMdcBuilder);

        final CodeBlock code = CodeBlock.builder()
            .add("return $L\n", KoraAspect.callSuper(executableElement, superCall))
            .indent()
            .add(".doOnSubscribe(_subscription -> {\n")
            .indent()
            .add(fillMdcBuilder.build())
            .unindent()
            .add("})\n")
            .add(".doFinally(_signalType -> {\n")
            .indent()
            .add(clearMdcBuilder.build())
            .unindent()
            .add("});\n")
            .build();

        return new ApplyResult.MethodBody(code);
    }

    private ApplyResult.MethodBody blockingBody(ExecutableElement executableElement,
                                                 String superCall,
                                                 Mdc[] methodAnnotations,
                                                 List<? extends VariableElement> parametersWithAnnotation) {
        final CodeBlock.Builder fillMdcBuilder = CodeBlock.builder();
        final Set<String> methodKeys = fillMdcByMethodAnnotations(methodAnnotations, fillMdcBuilder, true);
        final Set<String> parametersKeys = fillMdcByParametersAnnotations(parametersWithAnnotation, fillMdcBuilder, true);
        final CodeBlock.Builder clearMdcBuilder = CodeBlock.builder();
        clearMdc(methodKeys, clearMdcBuilder);
        clearMdc(parametersKeys, clearMdcBuilder);

        final CodeBlock code = CodeBlock.builder()
            .beginControlFlow("try")
            .add(fillMdcBuilder.build())
            .add(MethodUtils.isVoid(executableElement) ? "" : "return ")
            .addStatement(KoraAspect.callSuper(executableElement, superCall))
            .nextControlFlow("finally")
            .add(clearMdcBuilder.build())
            .endControlFlow()
            .build();

        return new ApplyResult.MethodBody(code);
    }

    private static Set<String> fillMdcByMethodAnnotations(Mdc[] methodAnnotations, CodeBlock.Builder b, boolean globalIsSupported) {
        final Set<String> keys = new HashSet<>();
        for (Mdc annotation : methodAnnotations) {
            final String key = annotation.key();
            final String value = annotation.value();
            if (!annotation.global()) {
                keys.add(key);
            } else if (!globalIsSupported) {
                throw new UnsupportedOperationException("Global MDC is not supported");
            }
            if (value.startsWith("${") && value.endsWith("}")) {
                b.addStatement("$T.put($S, $N)", mdc, key, value.substring(2, value.length() - 1));
            } else {
                b.addStatement("$T.put($S, $S)", mdc, key, value);
            }
        }
        return keys;
    }

    private Set<String> fillMdcByParametersAnnotations(List<? extends VariableElement> parametersWithAnnotation, CodeBlock.Builder b, boolean globalIsSupported) {
        final Set<String> keys = new HashSet<>();
        for (VariableElement parameter : parametersWithAnnotation) {
            final String parameterName = parameter.getSimpleName().toString();
            final Mdc[] annotations = parameter.getAnnotationsByType(Mdc.class);
            final Mdc first = annotations[0];
            final String key = Optional.ofNullable(first.key())
                .filter(not(String::isEmpty))
                .or(() -> Optional.ofNullable(first.value()))
                .filter(not(String::isEmpty))
                .orElse(parameterName);
            b.addStatement(
                "$T.put($S, $N)",
                mdc,
                key,
                parameterName
            );

            if (!first.global()) {
                keys.add(key);
            } else if (!globalIsSupported) {
                throw new UnsupportedOperationException("Global MDC is not supported");
            }
        }
        return keys;
    }

    private static void clearMdc(Set<String> keys, CodeBlock.Builder b) {
        for (String key : keys) {
            b.addStatement("$T.remove($S)", mdc, key);
        }
    }
}
