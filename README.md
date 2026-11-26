# GYM FITNESS PRO

Hệ thống quản lý phòng gym, là đồ án nhóm của sinh viên.

## Giới thiệu

Đây là ứng dụng desktop viết bằng Java Swing để quản lý một phòng gym: hội viên, gói tập, thanh toán, check-in, huấn luyện viên, nhân viên và thiết bị. Dữ liệu được lưu trong SQL Server.

Mã nguồn được chia theo kiểu MVC:

- model: các lớp dữ liệu (Member, Payment, Staff, ...)
- view: giao diện
- controller: xử lý sự kiện và kiểm tra dữ liệu
- dao: đọc và ghi database

## Chức năng chính

| Màn hình | Nội dung |
|---|---|
| Dashboard | Xem tổng số hội viên, hội viên đang hoạt động, số gói tập, lượt check-in hôm nay, tổng doanh thu, biểu đồ doanh thu 6 tháng và lịch sử ra vào mới nhất |
| Quản lý Hội viên | Thêm, sửa, xóa, tìm kiếm hội viên |
| Đăng ký Gói tập | Đăng ký, gia hạn, thu tiền, sửa, xóa; lọc theo trạng thái (sắp hết hạn, chưa thu đủ, ...) |
| Quản lý Thanh toán | Lập hóa đơn, xóa hóa đơn, lọc theo phương thức thanh toán |
| Check in / Check out | Ghi nhận giờ vào, giờ ra của hội viên |
| Quản lý Huấn luyện viên | Quản lý huấn luyện viên và chuyên môn, đặt và theo dõi lịch tập PT |
| Quản lý Nhân viên | Thêm, sửa, xóa nhân viên; cấp tài khoản hoặc đặt lại mật khẩu |
| Quản lý Thiết bị | Quản lý thiết bị, phân công và theo dõi bảo trì |
| Quản lý Gói tập | Thêm, sửa, xóa các gói tập |

Ngoài ra:

- Đăng nhập, đăng xuất, đổi mật khẩu.
- Phân quyền theo chức vụ: mỗi người chỉ thấy các màn hình được phép dùng.
- Mật khẩu không lưu dạng chữ thường mà được băm bằng PBKDF2 (có salt riêng).
- Giá gói được lưu lại lúc đăng ký, nên đổi giá gói sau này không làm đổi số tiền hội viên còn nợ.

## Công nghệ sử dụng

- Java, giao diện Java Swing (cần JDK 17 trở lên)
- Microsoft SQL Server
- JDBC với driver `mssql-jdbc-12.8.1.jre11.jar` (đã có sẵn trong `lib/`)
- Docker (dùng để chạy SQL Server trên macOS/Linux)

Dự án không dùng Maven hay Gradle, chỉ cần `javac` và `java`.

## Cấu trúc dự án

```
GYM-FITNESS-PRO/
├── src/main/java/com/gymmanagement/
│   ├── Main.java              # Điểm bắt đầu chương trình
│   ├── controller/            # Xử lý sự kiện, kiểm tra dữ liệu
│   ├── dao/                   # Truy vấn database
│   ├── model/                 # Các lớp dữ liệu
│   └── view/                  # Giao diện
├── database/
│   ├── 01_schema.sql          # Tạo database và các bảng
│   ├── 02_seed.sql            # Dữ liệu mẫu
│   ├── 03_app_login.sql       # Tạo tài khoản database cho ứng dụng
│   ├── 04 - 08 *.sql          # Nâng cấp database cũ
│   ├── setup_database.sh      # Cài database (macOS/Linux)
│   └── setup_database.ps1     # Cài database (Windows)
├── lib/                       # Thư viện JDBC
├── db.properties.example      # Mẫu file cấu hình kết nối
├── run.sh                     # Biên dịch và chạy (macOS/Linux)
├── compile.bat                # Biên dịch (Windows)
└── run.bat                    # Biên dịch và chạy (Windows)
```

## Cài đặt

### Bước 1: Tải mã nguồn

```bash
git clone https://github.com/NhatQuang2288/GYM-FITNESS-PRO.git
cd GYM-FITNESS-PRO
```

### Bước 2: Cài JDK

Cài JDK 17 trở lên, sau đó kiểm tra:

```bash
java -version
javac -version
```

Tiếp theo làm theo hệ điều hành bạn đang dùng.

### Bước 3 (macOS / Linux): Cài database bằng Docker

1. Cài Docker và mở Docker lên.
2. Đảm bảo cổng `1433` chưa bị chương trình khác dùng.
3. Ở thư mục gốc của dự án, chạy:

```bash
./database/setup_database.sh
```

Script sẽ tự làm các việc sau:

- Tạo (hoặc bật lại) container SQL Server tên `gym-sql`. Lần đầu phải tải image nên hơi lâu.
- Tạo database `GymManagementDB` và nạp dữ liệu mẫu. Nếu database đã có thì chỉ nâng cấp, giữ nguyên dữ liệu.
- Tạo tài khoản database `gym_app` và file `db.properties`.
- Lưu mật khẩu quản trị `sa` vào file `.mssql_sa_password`.

Khi thấy dòng `Hoan tat` là xong.

### Bước 3 (Windows): Cài database vào SQL Server trên máy

Cần có sẵn:

- SQL Server 2017 trở lên, nghe ở cổng `1433` (bật TCP/IP).
- Bật chế độ đăng nhập SQL Server and Windows Authentication, vì ứng dụng đăng nhập bằng tài khoản `gym_app`.
- Lệnh `sqlcmd`.
- Tài khoản Windows đang dùng có quyền `sysadmin` trên SQL Server.

Ở thư mục gốc của dự án, chạy:

```powershell
powershell -ExecutionPolicy Bypass -File database\setup_database.ps1
```

Nếu SQL Server không nằm ở `localhost` thì thêm `-Server <tên máy>`.

Lưu ý: nếu thêm `-Reset` (Windows) hoặc `--reset` (macOS/Linux) thì toàn bộ dữ liệu sẽ bị xóa rồi tạo lại database.

### Về file `db.properties`

Script cài đặt tự tạo file này. Nếu muốn tự cấu hình, chép từ `db.properties.example` và sửa lại mật khẩu (`db.password`). Tài khoản đó phải tồn tại sẵn trong SQL Server.

File `db.properties` và `.mssql_sa_password` có chứa mật khẩu, không được commit lên Git.

## Chạy chương trình

Chạy ở thư mục gốc của dự án (chương trình đọc `db.properties` ở thư mục đang chạy).

### macOS / Linux

```bash
./run.sh
```

### Windows

```bat
run.bat
```

Cả hai script đều biên dịch lại mã nguồn vào thư mục `bin/` rồi mở ứng dụng.

## Hướng dẫn sử dụng

### 1. Đăng nhập

Tài khoản mẫu (mật khẩu đều là `123456`):

| Tên đăng nhập | Chức vụ |
|---|---|
| `admin` | Quản lý |
| `letan1` | Lễ tân |
| `letan2` | Lễ tân |

Các tài khoản này chỉ để chạy thử.

### 2. Quyền của từng chức vụ (theo dữ liệu mẫu)

| Màn hình | Quản lý | Lễ tân |
|---|---|---|
| Dashboard | Toàn quyền | Không có |
| Hội viên | Toàn quyền | Toàn quyền |
| Đăng ký gói tập | Toàn quyền | Toàn quyền |
| Thanh toán | Toàn quyền | Toàn quyền |
| Check in / Check out | Toàn quyền | Toàn quyền |
| Lịch tập PT | Toàn quyền | Toàn quyền |
| Huấn luyện viên | Toàn quyền | Chỉ xem |
| Nhân viên | Toàn quyền | Không có |
| Thiết bị và bảo trì | Toàn quyền | Không có |
| Gói tập | Toàn quyền | Không có |

Kỹ thuật viên, tạp vụ và huấn luyện viên không được cấp tài khoản đăng nhập.

### 3. Thao tác cơ bản

- Chọn màn hình ở thanh menu bên trái. Riêng Dashboard mở bằng cách bấm vào logo "GYM FITNESS PRO".
- Nút + (ví dụ "+ Thêm hội viên") dùng để thêm mới.
- Các biểu tượng ở cột Thao tác dùng để sửa, xóa, gia hạn, thu tiền, ...
- Lịch tập PT nằm trong màn hình "Quản lý Huấn luyện viên".
- Bảo trì thiết bị là tab thứ hai trong màn hình "Quản lý Thiết bị".
- Đổi mật khẩu và Đăng xuất nằm ở cuối thanh menu.

### 4. Ví dụ: đăng ký gói cho hội viên mới

1. Vào Quản lý Hội viên, bấm + Thêm hội viên, nhập thông tin rồi lưu.
2. Vào Đăng ký Gói tập, bấm + Đăng ký mới.
3. Chọn hội viên và gói tập. Ngày kết thúc được tính tự động.
4. Ô Thu tiền ngay được chọn sẵn (bỏ chọn nếu khách chưa trả tiền), rồi bấm Đăng ký.
5. Khi hội viên đến tập, vào Check in / Check out và bấm + Check-in vào.

### 5. Một số quy tắc

- Đăng ký đã có hóa đơn thì không xóa được, phải xóa hóa đơn trước.
- Không thể tự xóa tài khoản nhân viên mà mình đang đăng nhập.
- Phiếu bảo trì đã "Đã hoàn thành" thì không đổi trạng thái được nữa.
- Mật khẩu phải có ít nhất 6 ký tự.

## Lỗi thường gặp

### `Docker chua chay`
Mở Docker rồi chạy lại script.

### `SQL Server khong khoi dong duoc` (macOS/Linux)
Xem log bằng `docker logs gym-sql`. Lỗi này cũng xảy ra khi mật khẩu trong `.mssql_sa_password` không khớp với container `gym-sql` đã tạo trước đó (ví dụ bạn clone dự án thêm một lần trên cùng máy). Khi đó hãy chép file `.mssql_sa_password` và `db.properties` từ thư mục dự án cũ sang.

### App báo sai tài khoản kết nối database
Kiểm tra lại `db.properties`, hoặc chạy lại script cài database.

## Thành viên nhóm

- Lê Nguyễn Nhật Quang
- Bach Quoc Thoai
- Nguyen Thi Tuong Vy
- Tang Thanh Tai
- Trần Thị Hồng Vân
