# Spec: <Feature / Vertical Slice Name>

## Status
Draft

## Goal
<One or two sentences describing the user-visible or project-risk outcome.>

## Context
<Why this matters now. Link to docs/plan.md phase, architecture decision, or current risk.>

## Non-Goals
- <What this spec deliberately does not solve.>
- <What should not be built yet.>

## User / Developer Flow
1. <First observable step.>
2. <Next step.>
3. <Final demonstrable outcome.>

## Requirements
- <Concrete behavior the implementation must provide.>
- <Another concrete behavior.>
- <Error/loading/empty state requirement if relevant.>

## Data / State
<What data exists, where it lives, and what is source-of-truth vs derived/cache data.>

## UX Notes
<Only the necessary interaction and display expectations. Avoid polish unless this phase needs it.>

## Technical Notes
<Important implementation constraints, existing files, model contracts, threading, platform limits.>

## Metrics
- <Latency, memory, tokens/sec, retrieval quality, or other measurement needed.>

## Acceptance Criteria
- <Builds/runs condition.>
- <Demo condition.>
- <Correctness condition.>
- <Metrics recorded if relevant.>

## Risks
- <Main thing that could invalidate the approach.>
- <Known technical uncertainty.>

## Open Questions
- <Question that must be answered before or during implementation.>

## Verification
- [ ] `./gradlew ktlintCheck`
- [ ] `./gradlew detekt`
- [ ] `./gradlew test`
- [ ] `./gradlew assembleDebug` if native/Gradle/app wiring changed
- [ ] Physical device smoke test if behavior depends on model/device runtime
