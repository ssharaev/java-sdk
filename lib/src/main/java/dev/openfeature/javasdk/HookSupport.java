package dev.openfeature.javasdk;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

@RequiredArgsConstructor
class HookSupport {

    private final Logger log;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void errorHooks(FlagValueType flagValueType, HookContext hookCtx, Exception e, List<Hook> hooks, Map<String, Object> hints) {
        executeHooks(flagValueType, hooks, "error", hook -> hook.error(hookCtx, e, hints));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void afterAllHooks(FlagValueType flagValueType, HookContext hookCtx, List<Hook> hooks, Map<String, Object> hints) {
        executeHooks(flagValueType, hooks, "finally", hook -> hook.finallyAfter(hookCtx, hints));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void afterHooks(FlagValueType flagValueType, HookContext hookContext, FlagEvaluationDetails details, List<Hook> hooks, Map<String, Object> hints) {
        executeHooksUnchecked(flagValueType, hooks, hook -> hook.after(hookContext, details, hints));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> void executeHooks(
        FlagValueType flagValueType, List<Hook> hooks,
        String hookMethod,
        Consumer<Hook<T>> hookCode
    ) {
        hooks
            .stream()
            .filter(hook -> hook.supportsFlagValueType() == flagValueType)
            .forEach(hook -> executeChecked(hook, hookCode, hookMethod));
    }

    @SuppressWarnings("rawtypes")
    private <T> void executeHooksUnchecked(
        FlagValueType flagValueType, List<Hook> hooks,
        Consumer<Hook<T>> hookCode
    ) {
        hooks
            .stream()
            .filter(hook -> hook.supportsFlagValueType() == flagValueType)
            .forEach(hookCode::accept);
    }

    private <T> void executeChecked(Hook<T> hook, Consumer<Hook<T>> hookCode, String hookMethod) {
        try {
            hookCode.accept(hook);
        } catch (Exception exception) {
            log.error("Exception when running {} hooks {}", hookMethod, hook.getClass(), exception);
        }
    }

    @SuppressWarnings("rawtypes")
    public EvaluationContext beforeHooks(FlagValueType flagValueType, HookContext hookCtx, List<Hook> hooks, Map<String, Object> hints) {
        var result = callBeforeHooks(flagValueType, hookCtx, hooks, hints);
        return EvaluationContext.merge(hookCtx.getCtx(), collectContexts(result));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Stream<EvaluationContext> callBeforeHooks(FlagValueType flagValueType, HookContext hookCtx, List<Hook> hooks, Map<String, Object> hints) {
        // These traverse backwards from normal.
        return Lists
            .reverse(hooks)
            .stream()
            .filter(hook -> hook.supportsFlagValueType() == flagValueType)
            .map(hook -> hook.before(hookCtx, hints))
            .filter(Objects::nonNull)
            .flatMap(Optional::stream);
    }

    //for whatever reason, an error `error: incompatible types: invalid method reference` is thrown on compilation with javac
    //when the reduce call is appended directly as stream call chain above. moving it to its own method works however...
    private EvaluationContext collectContexts(Stream<EvaluationContext> result) {
        return result
            .reduce(new EvaluationContext(), EvaluationContext::merge, EvaluationContext::merge);
    }
}
