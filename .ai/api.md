# API đăng ký và đăng nhập

Tài liệu mô tả API theo code backend hiện tại. ID, thông tin người dùng, token và timestamp trong các ví dụ chỉ mang tính minh họa.

## Quy ước chung
- địa chỉ backend: 172.20.10.2:8080
- `{BASE_URL}` là địa chỉ backend của môi trường app kết nối.
- Request và response sử dụng JSON.
- Hai API đăng ký và đăng nhập không yêu cầu header `Authorization`.
- Response thành công trả trực tiếp các trường bên dưới, không bọc trong `data`.

Header cho cả hai API:

```http
Content-Type: application/json
```

## 1. Đăng ký

```http
POST {BASE_URL}/api/v1/auth/register
```

### Request body

```json
{
  "name": "Nguyễn Văn A",
  "username": "nguyenvana",
  "password": "12345678"
}
```

| Trường | Kiểu dữ liệu | Bắt buộc | Quy định |
| --- | --- | --- | --- |
| `name` | string | Có | Không được trống hoặc chỉ chứa khoảng trắng; tối đa 255 ký tự |
| `username` | string | Có | Không được trống hoặc chỉ chứa khoảng trắng; tối đa 255 ký tự; không trùng tài khoản đã tồn tại |
| `password` | string | Có | Không được trống hoặc chỉ chứa khoảng trắng; tối đa 72 byte UTF-8 |

App chỉ cần gửi ba trường trên. Backend tự sinh `id` và gán `role` là `USER`.

### Response thành công

**HTTP 201 Created**

```json
{
  "id": 1,
  "name": "Nguyễn Văn A",
  "username": "nguyenvana",
  "role": "USER"
}
```

Đăng ký thành công chưa trả token. App gọi API đăng nhập để lấy token.

### Các lỗi thường gặp

| HTTP status | Trường hợp | `message` |
| --- | --- | --- |
| 400 | Thiếu trường bắt buộc, trường trống hoặc `name`/`username` quá 255 ký tự | `Bad Request` |
| 400 | Mật khẩu vượt 72 byte UTF-8 | `Invalid request data` |
| 409 | Username đã tồn tại | `Request conflicts with existing data` |

Ví dụ trùng username — **HTTP 409 Conflict**:

```json
{
  "timestamp": "2026-10-24T03:00:00Z",
  "status": 409,
  "message": "Request conflicts with existing data",
  "path": "/api/v1/auth/register",
  "fieldErrors": []
}
```

## 2. Đăng nhập

```http
POST {BASE_URL}/api/v1/auth/login
```

### Request body

```json
{
  "username": "nguyenvana",
  "password": "12345678"
}
```

| Trường | Kiểu dữ liệu | Bắt buộc | Quy định |
| --- | --- | --- | --- |
| `username` | string | Có | Không được trống hoặc chỉ chứa khoảng trắng; tối đa 255 ký tự |
| `password` | string | Có | Không được trống hoặc chỉ chứa khoảng trắng; tối đa 72 ký tự và không vượt 72 byte UTF-8 |

### Response thành công

**HTTP 200 OK**

```json
{
  "accessToken": "<JWT_ACCESS_TOKEN>",
  "refreshToken": "<REFRESH_TOKEN>",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "refreshExpiresIn": 604800,
  "user": {
    "id": 1,
    "name": "Nguyễn Văn A",
    "username": "nguyenvana",
    "role": "USER"
  }
}
```

| Trường | Kiểu dữ liệu | Ý nghĩa |
| --- | --- | --- |
| `accessToken` | string | JWT dùng để gọi các API cần xác thực |
| `refreshToken` | string | Token dùng để lấy bộ token mới qua API refresh |
| `tokenType` | string | `Bearer` |
| `expiresIn` | number | Thời hạn access token, tính bằng giây; cấu hình hiện tại là 900 giây (15 phút) |
| `refreshExpiresIn` | number | Thời gian còn lại của refresh token, tính bằng giây; lúc đăng nhập khoảng 604800 giây (7 ngày) |
| `user` | object | Thông tin người dùng gồm `id`, `name`, `username`, `role` |

Thời hạn token có thể thay đổi theo cấu hình môi trường; app sử dụng giá trị backend trả về. `role` có thể là `USER` hoặc `ADMIN` tùy tài khoản.

Khi gọi API cần đăng nhập, gửi access token trong header:

```http
Authorization: Bearer <accessToken>
```

Mỗi lần đăng nhập thành công sẽ cấp refresh token mới và thay thế refresh token trước đó của tài khoản.

### Các lỗi thường gặp

| HTTP status | Trường hợp | `message` |
| --- | --- | --- |
| 400 | Thiếu trường bắt buộc, trường trống, `username` quá 255 ký tự hoặc `password` quá 72 ký tự | `Bad Request` |
| 400 | Mật khẩu vượt 72 byte UTF-8 nhưng vẫn nằm trong giới hạn 72 ký tự | `Invalid request data` |
| 401 | Sai username hoặc mật khẩu | `Invalid credentials or refresh token` |

Ví dụ sai thông tin đăng nhập — **HTTP 401 Unauthorized**:

```json
{
  "timestamp": "2026-10-24T03:00:00Z",
  "status": 401,
  "message": "Invalid credentials or refresh token",
  "path": "/api/v1/auth/login",
  "fieldErrors": []
}
```

## 3. Cấu trúc response lỗi

```json
{
  "timestamp": "2026-10-24T03:00:00Z",
  "status": 400,
  "message": "Bad Request",
  "path": "/api/v1/auth/login",
  "fieldErrors": [
    {
      "field": "username",
      "message": "Invalid value"
    }
  ]
}
```

| Trường | Ý nghĩa |
| --- | --- |
| `timestamp` | Thời điểm xảy ra lỗi theo UTC, định dạng ISO 8601 |
| `status` | Mã HTTP status |
| `message` | Thông báo lỗi chung |
| `path` | Đường dẫn API được gọi |
| `fieldErrors` | Danh sách trường không hợp lệ; là mảng rỗng khi không có lỗi theo trường |

JSON sai cú pháp trả **400 Bad Request**. Gửi body với `Content-Type: text/plain` trả **415 Unsupported Media Type**.

## 4. Luồng tích hợp app

1. Gọi API đăng ký khi người dùng tạo tài khoản.
2. Gọi API đăng nhập để lấy `accessToken`, `refreshToken` và thông tin `user`.
3. Gửi `Authorization: Bearer <accessToken>` khi gọi các API cần xác thực.
4. Khi cần gia hạn access token, gọi `POST {BASE_URL}/api/v1/auth/refresh` với header `Content-Type: application/json` và body bên dưới, không cần header `Authorization`:

```json
{
  "refreshToken": "<REFRESH_TOKEN>"
}
```

API refresh trả **HTTP 200 OK** với cùng cấu trúc response của API đăng nhập. App lưu cả access token và refresh token mới vì refresh token cũ bị thay thế sau mỗi lần refresh thành công. Refresh không kéo dài hạn kết thúc phiên tính từ lần đăng nhập. Nếu refresh token không hợp lệ hoặc hết hạn, API trả **401** và app yêu cầu đăng nhập lại.

## Nguồn đối chiếu trong backend

- [AuthController.java](../src/main/java/com/ht/robotvacuum/controller/AuthController.java)
- [User.java](../src/main/java/com/ht/robotvacuum/model/User.java)
- [ReqLoginDTO.java](../src/main/java/com/ht/robotvacuum/dto/request/ReqLoginDTO.java)
- [ResRegisterDTO.java](../src/main/java/com/ht/robotvacuum/dto/response/ResRegisterDTO.java)
- [ResTokenDTO.java](../src/main/java/com/ht/robotvacuum/dto/response/ResTokenDTO.java)
- [ResAccountDTO.java](../src/main/java/com/ht/robotvacuum/dto/response/ResAccountDTO.java)
- [RefreshTokenService.java](../src/main/java/com/ht/robotvacuum/security/RefreshTokenService.java)
- [GlobalExceptionHandler.java](../src/main/java/com/ht/robotvacuum/exception/GlobalExceptionHandler.java)
- [application.yaml](../src/main/resources/application.yaml)
