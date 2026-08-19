# Findings

- Existing cover upload endpoint: `POST /api/admin/uploads/images`, protected by the admin interceptor.
- `FileStorageService` currently validates image MIME types and size, generates UUID filenames, and stores files in `uploads/covers`.
- Static resources are exposed from `/uploads/**`.
- User profiles currently have no avatar field or endpoint.
- Existing untracked regression tests define `FileStorageService.storeAvatar(Long, MultipartFile)` and avatar URLs under `/uploads/avatars/{userId}/`.
- RED verification confirmed that `storeAvatar`, `updateAvatar`, and profile/avatar accessors were missing before implementation.
- Avatar storage and covers now share one image-validation and write path. The static `/uploads/**` handler already exposes both directories.
