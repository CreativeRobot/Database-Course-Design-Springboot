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
