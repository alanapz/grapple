package org.grapple.schema.instrumentation;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import graphql.ExecutionResult;
import graphql.execution.instrumentation.DeferredFieldInstrumentationContext;
import graphql.execution.instrumentation.ExecutionStrategyInstrumentationContext;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.parameters.InstrumentationDeferredFieldParameters;
import graphql.execution.instrumentation.parameters.InstrumentationExecuteOperationParameters;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionStrategyParameters;
import graphql.execution.instrumentation.parameters.InstrumentationFieldCompleteParameters;
import graphql.execution.instrumentation.parameters.InstrumentationFieldFetchParameters;
import graphql.execution.instrumentation.parameters.InstrumentationFieldParameters;
import graphql.execution.instrumentation.parameters.InstrumentationValidationParameters;
import graphql.language.Document;
import graphql.validation.ValidationError;

public final class DebugInstrumentation<T> implements Instrumentation {

    private final DebugInstrumentationCallback<T> callback;

    public DebugInstrumentation(DebugInstrumentationCallback<T> callback) {
        this.callback = requireNonNull(callback, "callback");
    }

    private <U> InstrumentationContext<U> buildContext(String name, Object params) {
        final T callbackToken = callback.started(name, params);
        return new InstrumentationContext<U>() {

            @Override
            public void onDispatched(CompletableFuture<U> completableFuture) {
                // Nothing to do here
            }

            @Override
            public void onCompleted(U t, Throwable throwable) {
                if (throwable != null) {
                    callback.error(name, params, throwable, callbackToken);
                    return;
                }
                callback.complete(name, params, throwable, callbackToken);
            }
        };
    }

    @Override
    public InstrumentationContext<ExecutionResult> beginExecution(InstrumentationExecutionParameters instrumentationExecutionParameters) {
        return buildContext("beginExecution", instrumentationExecutionParameters);
    }

    @Override
    public InstrumentationContext<Document> beginParse(InstrumentationExecutionParameters instrumentationExecutionParameters) {
        return buildContext("beginParse", instrumentationExecutionParameters);
    }

    @Override
    public InstrumentationContext<List<ValidationError>> beginValidation(InstrumentationValidationParameters instrumentationValidationParameters) {
        return buildContext("beginValidation", instrumentationValidationParameters);
    }

    @Override
    public InstrumentationContext<ExecutionResult> beginExecuteOperation(InstrumentationExecuteOperationParameters instrumentationExecuteOperationParameters) {
        return buildContext("beginExecuteOperation", instrumentationExecuteOperationParameters);
    }

    @Override
    public ExecutionStrategyInstrumentationContext beginExecutionStrategy(InstrumentationExecutionStrategyParameters instrumentationExecutionStrategyParameters) {
        return new ExecutionStrategyInstrumentationContext(){

            @Override
            public void onDispatched(CompletableFuture<ExecutionResult> completableFuture) {
                // Nothing to do here ...
            }

            @Override
            public void onCompleted(ExecutionResult executionResult, Throwable throwable) {
                // Nothing to do here ...
            }
        };
    }

    @Override
    public DeferredFieldInstrumentationContext beginDeferredField(InstrumentationDeferredFieldParameters instrumentationDeferredFieldParameters) {
        return new DeferredFieldInstrumentationContext(){

            @Override
            public void onDispatched(CompletableFuture<ExecutionResult> completableFuture) {
                // Nothing to do here ...
            }

            @Override
            public void onCompleted(ExecutionResult executionResult, Throwable throwable) {
                // Nothing to do here ...
            }
        };
    }

    @Override
    public InstrumentationContext<ExecutionResult> beginField(InstrumentationFieldParameters instrumentationFieldParameters) {
        return buildContext("beginField", instrumentationFieldParameters);
    }

    @Override
    public InstrumentationContext<Object> beginFieldFetch(InstrumentationFieldFetchParameters instrumentationFieldFetchParameters) {
        return buildContext("beginFieldFetch", instrumentationFieldFetchParameters);
    }

    @Override
    public InstrumentationContext<ExecutionResult> beginFieldComplete(InstrumentationFieldCompleteParameters parameters) {
        return buildContext("beginFieldComplete", parameters);
    }

    @Override
    public InstrumentationContext<ExecutionResult> beginFieldListComplete(InstrumentationFieldCompleteParameters parameters) {
        return buildContext("beginFieldListComplete", parameters);
    }
}
