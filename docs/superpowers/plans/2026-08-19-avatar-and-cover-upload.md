# Avatar And Cover Upload Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add secure avatar uploads and complete the existing cover-upload user experience for the Spring and Flutter bookstore.

**Architecture:** Spring stores accepted images under the configured local `uploads` root and persists relative public URLs. A token-derived user ID selects the avatar directory and profile record. Flutter reuses the existing Dio multipart client and file picker, while a shared URL resolver allows every image widget to consume relative or absolute URLs.

**Tech Stack:** Spring Boot 4, JPA, MySQL 8, JUnit 5, Flutter, Riverpod, Dio, file_picker, cached_network_image.

**Spec:** `docs/superpowers/specs/2026-08-19-avatar-and-cover-upload-design.md`

## Global Constraints

- The application runs on one local machine or one server; use the existing local `uploads` directory and do not add object storage.
- Persist image URLs only; do not store image bytes in MySQL.
- Accept only JPEG, PNG, GIF, and WEBP through the existing 5 MB limit.
- Avatar ownership always comes from the authenticated request attribute, never from a client-provided user ID.
- Preserve existing untracked tests and avoid unrelated page refactors.

---

### Task 1: Add Avatar Persistence And Secure Backend Upload

**Files:**
- Modify: `src/main/java/com/example/demo/entity/User.java`
- Modify: `src/main/java/com/example/demo/vo/UserProfileVo.java`
- Modify: `src/main/java/com/example/demo/service/UserService.java`
- Modify: `src/main/java/com/example/demo/service/FileStorageService.java`
- Modify: `src/main/java/com/example/demo/controller/UserController.java`
- Modify: `sql/01_schema.sql`
- Create: `sql/02_add_user_avatar_url.sql`
- Test: `src/test/java/com/example/demo/service/FileStorageServiceTests.java`

**Interfaces:**
- Produces `UploadFileVo FileStorageService.storeAvatar(Long userId, MultipartFile file)`.
- Produces `UserProfileVo UserService.updateAvatar(Long userId, MultipartFile file)`.
- Produces `POST /api/user/me/avatar` with `file` multipart part and `Result<UserProfileVo>` response.

- [ ] **Step 1: Extend the existing failing storage test with invalid input cases**

```java
@Test
void rejectsUnsupportedAvatarContentType() {
    MockMultipartFile file = new MockMultipartFile(
        "file", "portrait.txt", "text/plain", new byte[]{1});
    assertThrows(BusinessException.class, () -> storage.storeAvatar(42L, file));
}
```

- [ ] **Step 2: Run the focused backend test to verify RED**

Run: `./mvnw test -Dtest=FileStorageServiceTests`

Expected: compilation failure because `storeAvatar` does not yet exist.

- [ ] **Step 3: Implement avatar storage**

```java
public UploadFileVo storeAvatar(Long userId, MultipartFile file) {
    if (userId == null || userId <= 0) {
        throw new BusinessException(HttpStatus.BAD_REQUEST, "用户信息不合法");
    }
    return storeImage(file, uploadRoot.resolve("avatars").resolve(userId.toString()),
        "/avatars/" + userId);
}
```

Extract the existing empty-file, size, MIME and `Files.copy` checks into one private `storeImage` method. `initialize` creates both `covers` and `avatars` directories. Keep UUID filenames and verify the normalized target remains below the selected directory.

- [ ] **Step 4: Add profile persistence and endpoint**

```java
@PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public Result<UserProfileVo> updateAvatar(
        @RequestAttribute("userId") Long userId,
        @RequestParam("file") MultipartFile file) {
    return Result.success(userService.updateAvatar(userId, file));
}
```

Add `@Column(length = 500) private String avatarUrl;` to `User`, add `avatarUrl` to `UserProfileVo`, copy it in `toProfileVo`, and implement `updateAvatar` as: load active user, store image, set URL, save user, return the converted profile.

- [ ] **Step 5: Update initial and existing-database schema**

```sql
-- sql/01_schema.sql, inside users
avatar_url VARCHAR(500) NULL,

-- sql/02_add_user_avatar_url.sql
ALTER TABLE users ADD COLUMN avatar_url VARCHAR(500) NULL;
```

- [ ] **Step 6: Run focused backend test to verify GREEN**

Run: `./mvnw test -Dtest=FileStorageServiceTests`

Expected: all `FileStorageServiceTests` pass. If Maven is unavailable, record the exact wrapper error and leave source changes intact.

### Task 2: Add Flutter Avatar Model, Upload And Presentation

**Files:**
- Modify: `lib/core/constants/api_paths.dart`
- Create: `lib/core/utils/media_url.dart`
- Modify: `lib/data/models/profile/user_profile.dart`
- Modify: `lib/features/profile/data/profile_repository.dart`
- Modify: `lib/features/profile/presentation/profile_controller.dart`
- Modify: `lib/features/profile/presentation/profile_page.dart`
- Test: `test/user_profile_avatar_test.dart`

**Interfaces:**
- Produces `String? resolveMediaUrl(String baseUrl, String? path)`.
- Produces `Future<UserProfile> ProfileRepository.uploadAvatar({required List<int> bytes, required String filename})`.
- Produces `Future<bool> ProfileController.uploadAvatar({required List<int> bytes, required String filename})`.

- [ ] **Step 1: Run the existing model test to verify RED**

Run: `flutter test test/user_profile_avatar_test.dart`

Expected: compile failure because `UserProfile.avatarUrl` does not yet exist.

- [ ] **Step 2: Add the model, route and repository contract**

```dart
static const meAvatar = '$me/avatar';

Future<UserProfile> uploadAvatar({
  required List<int> bytes,
  required String filename,
}) async => (await _apiClient.postMultipart(
  ApiPaths.meAvatar,
  bytes: bytes,
  filename: filename,
  parser: UserProfile.fromJson,
)).data;
```

Add nullable `avatarUrl` to `UserProfile`, parse `json['avatarUrl'] as String?`, and preserve it in state updates.

- [ ] **Step 3: Add controller upload state and behavior**

```dart
Future<bool> uploadAvatar({required List<int> bytes, required String filename}) async {
  state = state.copyWith(submitting: true, clearError: true);
  try {
    final profile = await _repository.uploadAvatar(bytes: bytes, filename: filename);
    state = state.copyWith(profile: profile, submitting: false, clearError: true);
    return true;
  } on ApiException catch (error) {
    state = state.copyWith(submitting: false, errorMessage: await _messageFor(error));
    return false;
  }
}
```

Retain the generic failure branch used by other profile mutations.

- [ ] **Step 4: Render and select profile avatar**

Use `FilePicker.platform.pickFiles(type: FileType.image, withData: true)`. Pass selected bytes and filename to `uploadAvatar`. Make `_ProfileAvatar` accept `imageUrl`, display a `CachedNetworkImage` only when resolved URL exists, and retain the initials circle in its `errorWidget` and empty state. Put a small `IconButton(Icons.photo_camera_outlined)` over the editable primary avatar; disable it while `state.submitting`.

- [ ] **Step 5: Add shared media URL resolution**

```dart
String? resolveMediaUrl(String baseUrl, String? path) {
  if (path == null || path.trim().isEmpty) return null;
  if (Uri.tryParse(path)?.hasScheme ?? false) return path;
  return '${baseUrl.replaceFirst(RegExp(r'/$'), '')}/${path.replaceFirst(RegExp(r'^/'), '')}';
}
```

Use it for the new avatar and replace existing private cover URL helpers where direct imports do not create a dependency cycle.

- [ ] **Step 6: Run focused Flutter test to verify GREEN**

Run: `flutter test test/user_profile_avatar_test.dart`

Expected: the model test passes.

### Task 3: Complete The Existing Cover Editor Experience And Verify

**Files:**
- Modify: `lib/features/admin/presentation/admin_catalog_pages.dart`
- Test: `test/user_profile_avatar_test.dart`
- Modify: `task_plan.md`
- Modify: `findings.md`
- Modify: `progress.md`

**Interfaces:**
- Consumes `resolveMediaUrl(String baseUrl, String? path)`.
- Consumes the existing `AdminRepository.upload(List<int> bytes, String filename)`.

- [ ] **Step 1: Add cover preview next to the form field**

```dart
if (_fields['cover']!.text.trim().isNotEmpty)
  CommerceCover(url: _fields['cover']!.text, width: 86),
```

Refresh the state after successful upload so the preview changes immediately. Preserve the entered URL and show the existing `_error` message when upload fails.

- [ ] **Step 2: Format changed Dart files**

Run: `dart format lib/core/utils/media_url.dart lib/data/models/profile/user_profile.dart lib/features/profile/data/profile_repository.dart lib/features/profile/presentation/profile_controller.dart lib/features/profile/presentation/profile_page.dart lib/features/admin/presentation/admin_catalog_pages.dart test/user_profile_avatar_test.dart`

Expected: formatter exits with code 0.

- [ ] **Step 3: Run Flutter focused tests and analysis from no-space path**

Run: `subst X: "D:\no game\Code"` then `X:\Enviroment\fluter\flutter\bin\flutter.bat test test\user_profile_avatar_test.dart` and `X:\Enviroment\fluter\flutter\bin\flutter.bat analyze` from `X:\DatabaseHomework\BookStore_Flutter\flutter_application_bookstore`.

Expected: focused test passes. Report analyzer warnings separately from errors. Remove the mapping with `subst X: /d` only if this session created it.

- [ ] **Step 4: Inspect changes and update persistent work records**

Run: `git diff --check` in each repository and update the root planning files with completed phases and actual test results. Do not add generated `uploads`, `.dart_tool`, `build`, or `target` files to Git.
