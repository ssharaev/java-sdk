## Welcome! Super happy to have you here.

A few things.

First, we have [a code of
conduct](https://github.com/open-feature/.github/blob/main/CODE_OF_CONDUCT.md). Don't
be a jerk.

We're not keen on vendor-specific stuff in this library, but if there are changes that need to happen in the spec to enable vendor-specific stuff in user code or other extension points, check out [the spec](https://github.com/open-feature/spec).

Any contributions you make are expected to be tested with unit tests. You can validate these work with `mvn test`.
Further, it is recommended to verify code styling and static code analysis with `mvn verify -P !deploy`.
Regardless, the automation itself will run them for you when you open a PR.

> [!TIP]
> For easier usage maven wrapper is available. Example usage: `./mvnw verify`

Your code is supposed to work with Java 8+.

If you think we might be out of date with the spec, you can check that by invoking `python spec_finder.py` in the root of the repository. This will validate we have tests defined for all of the specification entries we know about.

If you're adding tests to cover something in the spec, use the `@Specification` annotation like you see throughout the test suites.

## End-to-End Tests

The continuous integration runs a set of [gherkin e2e tests](https://github.com/open-feature/test-harness/blob/main/features/evaluation.feature) using `InMemoryProvider`.

to run alone:
```
mvn test -P e2e
```

## Benchmarking

There is a small JMH benchmark suite for testing allocations that can be run with:

```sh
mvn -P benchmark test-compile jmh:benchmark -Djmh.f=1  -Djmh.prof='dev.openfeature.sdk.benchmark.AllocationProfiler'
```

If you are concerned about the repercussions of a change on memory usage, run this an compare the results to the committed. `benchmark.txt` file.

## Releasing

See [releasing](./docs/release.md).

Thanks and looking forward to your issues and pull requests.
