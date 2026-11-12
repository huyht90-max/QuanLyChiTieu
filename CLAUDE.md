# Ghi chú cho Claude

Ứng dụng Android quản lý chi tiêu cá nhân "Chi Tiêu" — Kotlin + Jetpack Compose + Material 3, dữ liệu SQLite cục bộ. Giao diện và giao tiếp với người dùng bằng **tiếng Việt**.

## Cấu trúc
- `app/src/main/java/vn/quanlychitieu/app/` — `data/` (SQLite, model, cài đặt), `ui/screens/` (5 màn hình), `ui/components/` (thẻ, biểu đồ Canvas), `ui/theme/`.
- Không dùng Room/KSP: `SQLiteOpenHelper` + `StateFlow`. Đổi schema thì tăng version trong `DbHelper` và viết `onUpgrade` — **không được làm mất dữ liệu người dùng**.
- `applicationId` là `vn.quanlychitieu.app` — không đổi (đổi là thành app khác, mất dữ liệu).

## Phát hành bản mới (người dùng cài APK từ GitHub Releases)
1. Sửa code, kiểm tra build: `./gradlew assembleDebug`.
2. Tăng **cả** `versionCode` (+1) và `versionName` trong `app/build.gradle.kts`.
3. Commit, push lên `main`.
4. Tạo và push tag trùng `versionName`: `git tag v1.0.3 && git push origin v1.0.3`.
5. GitHub Actions (`.github/workflows/android.yml`) tự build, ký bằng khóa từ Secrets, kiểm tra vân tay chứng chỉ và tạo Release kèm `ChiTieu-<version>.apk`. Theo dõi: `gh run list`, `gh run watch <id>`.
6. Gửi người dùng link: https://github.com/huyht90-max/QuanLyChiTieu/releases/latest

Nếu không đẩy được tag (vd: phiên Claude trên cloud bị chặn đẩy tag) hoặc build tag bị lỗi: sửa trên `main`, rồi chạy tay workflow trên `main` với ô `tag` (vd `gh workflow run android.yml --ref main -f tag=v1.0.3`). Workflow build từ `main`, kiểm tra `versionName` khớp tag, và tự tạo tag nếu chưa có (không cần xóa tag cũ).

## Khóa ký — quan trọng
- Bản phát hành phải ký bằng khóa có SHA-256 `4d915e75e54226f8325bf0ed3a2bd1ed2890aa94bc2f87bc3686a771cbf4221c`, nếu không người dùng không cài đè được.
- Khóa chỉ nằm trong GitHub Secrets (`SIGNING_KEYSTORE_BASE64`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`) và bản sao lưu trên máy người dùng. **Không bao giờ** commit file `.jks`, `keystore.properties` hay mật khẩu.
- Không sửa `EXPECTED_CERT_SHA256` trong workflow để "cho qua" lỗi chữ ký.
