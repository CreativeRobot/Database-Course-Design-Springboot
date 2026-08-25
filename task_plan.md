# Avatar and Cover Upload Plan

## Goal
Add authenticated avatar upload with profile persistence, keep the existing cover upload compatible, and document both APIs.

## Phases
- [completed] Review existing storage, profile, schema, tests, and API documentation.
- [completed] Run the avatar storage regression tests and confirm the expected RED state.
- [completed] Implement avatar storage, persistence, endpoint, and database migration.
- [completed] Update API documentation for avatar and cover uploads.
- [completed] Run focused and full backend verification.

## Decisions
- Preserve `POST /api/admin/uploads/images` for book covers.
- Add `POST /api/user/me/avatar` with multipart field `file` for the authenticated user.
- Store avatars under `uploads/avatars/{userId}` and persist a public relative URL in `users.avatar_url`.

## Errors Encountered
| Error | Resolution |
| --- | --- |
| `python` command unavailable when running planning-session catchup | Continue with local source and Git inspection; Maven Wrapper is available for tests. |
| TDD guidance referenced a non-existent `writing-good-tests.md` path | Use the core TDD guidance and the existing focused tests. |
| PowerShell could not start `mvnw.cmd` and reported `Cannot index into a null array` | Retry once through `cmd /c` instead of invoking the batch wrapper directly. |
| PowerShell parsed the comma in `-Dtest=FileStorageServiceTests,UserServiceTests` as an argument separator | Pass the Maven property as one quoted argument to `cmd.exe`. |
| The quoted `cmd.exe` invocation still triggered the Wrapper's `Cannot index into a null array` failure | Wrapper startup is not usable in this environment; inspect the system Maven installation rather than retrying it. |
| System Maven used the unwritable default repository `C:\\.m2\\repository` | Run Maven with `-Dmaven.repo.local=.m2/repository`, using the repository cache already present in the project. |
| Maven dependency resolution was blocked by sandbox networking (`Permission denied: getsockopt`) | Request one escalated Maven run with the same project-local repository setting. |
| Offline focused test compiled all code but could not start Surefire because three plugin dependencies were not cached | Run the focused test online once to cache the missing Surefire dependencies. |
| Full suite failed to load the Spring context because Hibernate could not find `users.avatar_url` | `SHOW COLUMNS FROM users LIKE 'avatar_url'` confirmed the local development database predates this migration; apply `sql/02_add_user_avatar_url.sql`, then rerun the suite. |
| Non-escalated full suite passed business assertions but JUnit could not clean `%TEMP%` directories | The same focused tests pass when Maven runs with the required Windows permissions; rerun the full suite under the already approved Maven rule. |

## 2026-08-20 Recommendation Continuation

### Goal

Complete the approved backend portion of the homepage recommendation system on `recommend` before beginning the Flutter work.

### Phases

- [completed] Recover the approved hybrid-ranking design and preserve all existing recommendation changes.
- [completed] Verify recommendation ranking, API contract, cache invalidation, and JPA wiring with focused tests.
- [completed] Run clean full-suite verification and record the environment result.

### Verification Notes

- `RecommendationRankerTests`, `RecommendationServiceTests`, `OrderServiceTests`, and `ReviewServiceTests` pass with the system Maven and the project-local `.m2/repository` cache.
- `mvnw.cmd` remains unusable in this PowerShell sandbox (`Cannot start maven from wrapper`); use system Maven instead.
- A non-escalated full suite reaches the tests but reports only `FileStorageServiceTests` cleanup errors from denied access to `%TEMP%`, unrelated to the recommendation change.
- The requested elevated full-suite rerun was rejected by the approval service with HTTP 503. Do not retry or work around the denied elevation; verify all tests that do not require the blocked temporary-directory cleanup path.
- Final safe verification passed: `mvn -Dmaven.repo.local=.m2/repository -Dtest=!FileStorageServiceTests test` ran 34 tests with 0 failures and 0 errors. `git diff --check` is clean.

## 2026-08-25 Recommendation System Audit

### Goal
Review the existing backend collaborative-filtering/hybrid recommendation implementation for algorithmic correctness, scalability, data quality, cache behavior, API robustness, and test coverage. Analysis only; do not change business code.

### Phases
- [completed] Locate the existing recommendation implementation and prior validation notes.
- [in_progress] Inspect ranking algorithm, repository queries, data model, cache behavior, endpoint, and tests.
- [pending] Identify evidence-backed optimization opportunities and prioritize them.
- [pending] Produce a concise audit report; no code changes in this task.

### Audit Constraints
- The repository has unrelated tracked local modifications in security/config/test files; exclude them from recommendation findings.
- `rg.exe` cannot run in this sandbox (access denied); use PowerShell file discovery and `Select-String` instead.
- Git requires the one-command `-c safe.directory=...` override; do not change global Git configuration.

| Focused recommendation test reports cannot be created in the existing `target/surefire-reports` directory (access denied); Surefire reported `Tests run: 0`, so this is not valid test-pass evidence. | Rerun with a newly writable Surefire reports directory under `.tmp_junit`; do not interpret the previous Maven `BUILD SUCCESS` as test success. |
| The alternative pre-existing `.tmp_junit` directory is also not writable (access denied). | Treat both pre-existing generated-output directories as environment-owned; use a newly created directory at the writable project root for the next isolated Surefire-report attempt. |
| Redirected Surefire command failed before tests began because `cmd /c` split the unquoted report-directory path at its spaces (`Unknown lifecycle phase "game\\Code..."`). | The diagnostic directory was confirmed writable; invoke the system Maven command directly from PowerShell with the property passed as one argument. |

### Final Audit Status
- [completed] Locate the existing recommendation implementation and prior validation notes.
- [completed] Inspect ranking algorithm, repository queries, data model, cache behavior, endpoint, and tests.
- [completed] Identify evidence-backed optimization opportunities and prioritize them.
- [completed] Produce an analysis-only audit report; no backend business code was modified.

### Verification
- `mvn -o -Dmaven.repo.local=.m2/repository -Dtest=RecommendationRankerTests,RecommendationServiceTests test` compiled successfully, but it was not a valid test execution: Surefire could not write to the pre-existing `target/surefire-reports` directory and reported `Tests run: 0`.
- A clean report directory was writable, but Surefire continued to use the pre-existing target directory despite the attempted property override. No further retries were run because this is an environment-output-permission issue and does not block the static audit.
