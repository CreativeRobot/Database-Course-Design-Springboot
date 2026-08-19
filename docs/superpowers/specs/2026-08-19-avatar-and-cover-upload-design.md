# Avatar And Cover Upload Design

## Scope

The bookstore runs on one local machine or one application server. Uploaded images are stored on that server's file system; the database stores only public relative URLs.

## Storage And Data

- Book covers remain in `uploads/covers/<uuid>.<extension>` and their URLs remain in `book.cover_url`.
- User avatars are stored in `uploads/avatars/<userId>/<uuid>.<extension>` and their URLs are stored in `users.avatar_url`.
- Accepted images are JPEG, PNG, GIF, and WEBP. The existing 5 MB application limit applies to both upload routes.
- The existing `/uploads/**` read-only resource mapping exposes the stored files. Database backups must include the configured upload directory.

## API

`POST /api/user/me/avatar` accepts `multipart/form-data` with a single `file` part. The authenticated request attribute `userId` determines the storage directory and the profile to update. A client cannot select another user's directory. The response is the updated `UserProfileVo`, including `avatarUrl`.

`POST /api/admin/uploads/images` remains the administrator-only book-cover route. It returns `UploadFileVo`, which the edit form stores as `coverUrl` on save.

## Flutter Behavior

The profile page displays the current avatar when its `avatarUrl` is present and shows the current initials fallback for missing or failed images. Selecting a local image uploads it immediately; the avatar controls are disabled while the request is pending and the profile state is replaced with the returned profile on success.

The administrator book editor retains its existing file-picker upload flow, adds an inline preview of its entered or uploaded cover URL, and keeps the entered URL if an upload fails. Relative media URLs are resolved against the configured API base URL by one shared utility.

## Error Handling

The backend rejects empty files, images above the configured limit, unsupported content types, invalid user IDs, and failed file copies with existing `BusinessException` responses. The Flutter client keeps the prior avatar and reports the returned user-facing error on upload failure.

## Verification

- Backend unit tests verify avatar storage under the authenticated user's directory, public URL creation, and MIME/size rejection.
- Flutter model tests verify `avatarUrl` parsing; controller/widget tests verify profile replacement and fallback rendering where practical.
- Run Maven tests when a functioning Maven runtime is available. Run Flutter formatting, focused tests, and analysis through the no-space mapped drive because the checkout path contains spaces.
