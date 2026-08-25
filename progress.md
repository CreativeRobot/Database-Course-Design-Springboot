# Progress

## 2026-08-19

- Confirmed the requested design and permission to work in the current checkout.
- Inspected existing cover upload, user profile flow, static-resource configuration, and API documentation.
- Created persistent task notes before implementation.
- The first focused-test invocation did not reach Maven because PowerShell failed while launching `mvnw.cmd`; trying the batch wrapper through `cmd /c` next.
- The direct `cmd /c` attempt required quoting the comma-separated test selector; no Maven process has run yet.
- Wrapper startup was attempted through three distinct invocation forms and remains unavailable; switching to system Maven discovery.
- System Maven and JDK 21 are installed, but Maven must be pointed at the project-local `.m2/repository` cache because its default local repository is unwritable.
- Maven reached project resolution but sandbox networking blocked the missing Spring Boot parent download; requesting one scoped dependency-resolution test run.
- Focused tests reached test compilation and failed as expected because the avatar APIs and model accessors did not exist.
- Implemented authenticated avatar upload, persisted `users.avatar_url`, added the one-time migration, and documented both avatar and cover uploads.
- Focused test compilation is green; the offline test runner needs three missing Surefire dependencies before it can execute tests.
- Focused avatar regression tests passed: 3 tests, 0 failures, 0 errors. Running the full backend suite next.
- Full-suite root cause confirmed: local MySQL `bookstore.users` has no `avatar_url` column. Applying the new additive migration before rerunning the suite.
- After applying the migration, the non-escalated full suite reached all tests but Windows denied JUnit cleanup under `%TEMP%`; rerunning under the approved Maven permissions.
- Final verification passed: full backend suite ran 25 tests with 0 failures and 0 errors after applying `sql/02_add_user_avatar_url.sql` to the local development database.

## 2026-08-20 Recommendation Continuation

- Recovered the approved homepage recommendation design and implementation plan from the paired Flutter repository.
- Confirmed the backend and frontend are both on `recommend`; preserved all existing uncommitted recommendation files.
- Reviewing the current backend implementation and tests before adding regression coverage for cache and API-contract edge cases.
- Added and observed a RED regression test showing the cache incorrectly reused a `limit=1` result for `limit=2`.
- Changed the cache key to include both user ID and requested limit; user invalidation removes every cached limit for that user.
- Focused recommendation, order, and review tests pass (20 tests total). Full non-escalated Maven verification reached Spring/JPA startup and all recommendation tests, but two existing avatar file-storage tests fail while JUnit cleans denied `%TEMP%` directories.
- The elevated rerun request was rejected by the external approval service with HTTP 503. Continuing with the safe non-elevated suite that excludes only the known temporary-directory-bound file storage tests.
- Independent review found and the implementation now fixes scoring multiplication, false personalized explanations when no eligible candidate matches, and pre-commit cache invalidation. Review-status changes also invalidate recommendations after commit.
- Final verification passed for all 34 non-file-storage backend tests, including the Spring/JPA context and all recommendation, order, and review tests. `git diff --check` is clean.

## 2026-08-23 Order Inventory Concurrency and Service Split

- Extracted stock return and completed-order sales-count updates from `OrderService` into `InventoryService`, while keeping controller/API method signatures unchanged.
- Updated cancellation, expiration cancellation, and confirm-receipt flows to delegate inventory work through the new service; updated Mockito tests to verify delegation.
- Added a MySQL Testcontainers 2.0.5 repository integration test that starts two independent transactions against one-stock inventory and asserts exactly one conditional decrement succeeds and final stock is zero.
- `OrderServiceTests`: 11 tests passed. The real MySQL test compiled but was skipped because Docker is not installed/running in this environment.
- Final rerun on 2026-08-23: 48 non-environment backend tests passed, 1 Docker-gated MySQL concurrency test skipped because Docker is unavailable; git diff --check is clean.

## 2026-08-25 Recommendation Audit
- Initialized an analysis-only audit plan without touching backend business logic.
- Located existing recommendation services/tests from the project tree and recovered previous verification notes.
- Environment limitation: `rg.exe` cannot launch (access denied); switched to PowerShell `Get-ChildItem`/`Select-String`.
- Completed static inspection of ranker, service, controller, repository queries, entity lazy mappings, invalidation call sites, schema, and unit-test names.
- Saved evidence-backed audit observations to findings.md before verification.
- Focused Maven verification started, but it is inconclusive: Surefire could not write files below the existing `target/surefire-reports` (access denied), then reported `Tests run: 0`. The build success is compilation/lifecycle success only, not test-pass evidence. Next attempt will redirect Surefire reports to a known writable project subdirectory.
- Root-cause evidence: both `target/surefire-reports` and existing `.tmp_junit` are environment-owned/unwritable under the current sandbox identity. This is a filesystem-permission issue, not a recommendation-test failure. A clean project-root report directory will be used for the one remaining isolated verification attempt.
- The clean report directory is writable. The first redirected test command did not run Maven tests because `cmd /c` split an unquoted path containing spaces; Maven treated the path suffix as a lifecycle phase. The next command will pass each Maven property as a PowerShell argument to isolate command parsing from test execution.
- Focused test verification remains inconclusive: Maven compiled with exit code 0, but Surefire ran zero tests because its existing target report directory is unwritable. This result must not be described as a passed test suite.
- Completed the analysis-only recommendation audit. No backend business-code files were edited.
