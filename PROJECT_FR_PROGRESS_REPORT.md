# Bao cao tien do cac FR da thuc hien

## 1. Tong quan

Du an hien tai la ung dung Spring Boot phuc vu nghiep vu dang ky tai khoan thu vien. Phan da trien khai tap trung vao luong tiep nhan ho so dang ky, luu tru ho so, tra cuu ho so va kich hoat tai khoan thu vien cho ban doc.

Backend da duoc tach theo module nghiep vu `registration`:

- `controller`: tiep nhan request va dieu huong man hinh.
- `dto`: doi tuong nhan du lieu form va preview thong bao.
- `model`: entity JPA va enum trang thai.
- `repository`: truy van du lieu bang Spring Data JPA.
- `service`: xu ly nghiep vu dang ky, luu tru, kich hoat va thong bao.
- `config`: khoi tao/migration schema cho du lieu luu tru ho so.

Frontend su dung Thymeleaf templates va CSS dung chung tai `src/main/resources/static/css/registration.css` de tranh trung lap style trong tung template.

## 2. FR-07 - Nhap va luu phieu dang ky tai khoan thu vien

### Muc tieu nghiep vu

Chuyen vien luu hanh nhap thong tin cua ban doc vao phieu dang ky tai khoan thu vien, dinh kem anh nguoi dang ky va luu ho so vao he thong de phuc vu cac buoc xu ly tiep theo.

### Chuc nang da trien khai

- Tao man hinh nhap phieu dang ky tai `/register`.
- Thu thap cac truong thong tin: ho ten, gioi tinh, ngay sinh, noi sinh, CCCD, noi cap, chuyen mon, noi cong tac, dia chi, so dien thoai, email va anh nguoi dang ky.
- Validate du lieu dau vao bang Bean Validation va validate bo sung cho anh upload.
- Gioi han anh upload toi da 5MB va chi chap nhan file hinh anh.
- Luu ho so vao bang `library_registrations`.
- Luu thong tin anh gom ten file, content type va du lieu anh.
- Sinh ma luu tru ho so `storageCode`.
- Gan noi luu mac dinh la `Bo phan Luu hanh`.
- Hien thi trang thanh cong tai `/register/success/{id}` sau khi luu.
- Cho phep xem lai anh da upload qua `/register/avatar/{id}`.

### File lien quan

- `src/main/resources/templates/register.html`
- `src/main/resources/templates/register-success.html`
- `src/main/java/com/example/library/backend/registration/controller/RegisterController.java`
- `src/main/java/com/example/library/backend/registration/dto/RegisterForm.java`
- `src/main/java/com/example/library/backend/registration/service/RegisterService.java`
- `src/main/java/com/example/library/backend/registration/model/LibraryRegistration.java`

## 3. FR-10 - Luu tru, tra cuu va quan ly ho so dang ky

### Muc tieu nghiep vu

Sau khi ho so dang ky duoc nhap, chuyen vien can co noi de xem danh sach ho so da luu, tim kiem ho so va mo chi tiet tung ho so de xu ly cac nghiep vu lien quan.

### Chuc nang da trien khai

- Tao man hinh danh sach ho so tai `/registrations`.
- Hien thi thong tin chinh cua tung ho so: ma luu tru, ho ten, thong tin lien he, trang thai tai khoan, trang thai thong bao, thoi gian luu.
- Ho tro tim kiem theo ho ten, email, CCCD, ma luu tru va ten dang nhap thu vien.
- Sap xep ho so theo thoi gian tao moi nhat.
- Tao man hinh chi tiet ho so tai `/registrations/{id}`.
- Hien thi thong tin luu tru, thong tin dang ky va anh nguoi dang ky.
- Them schema initializer de bo sung cot phuc vu luu tru, tai khoan va thong bao kich hoat khi can.
- Cau hinh luu du lieu vao MySQL thong qua bien moi truong `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
- Tach CSS dung chung vao static file de template gon hon va tranh trung lap.

### File lien quan

- `src/main/resources/templates/registrations.html`
- `src/main/resources/templates/registration-detail.html`
- `src/main/resources/static/css/registration.css`
- `src/main/java/com/example/library/backend/registration/repository/LibraryRegistrationRepository.java`
- `src/main/java/com/example/library/backend/registration/config/RegistrationArchiveSchemaInitializer.java`
- `src/main/resources/application.properties`

## 4. FR-08 - Kich hoat tai khoan thu vien va thong bao kich hoat

### Muc tieu nghiep vu

Sau khi ho so dang ky hop le da duoc luu, chuyen vien kich hoat tai khoan thu vien cho ban doc, tao thong tin dang nhap va chuan bi noi dung thong bao kich hoat.

### Chuc nang da trien khai

- Them trang thai tai khoan thu vien:
  - `PENDING_ACTIVATION`: chua kich hoat.
  - `ACTIVATED`: da kich hoat.
- Them trang thai thong bao kich hoat:
  - `NOT_PREPARED`: chua soan thong bao.
  - `READY_TO_SEND`: da san sang gui.
  - `NOTIFIED`: da ghi nhan gui thong bao.
- Cho phep kich hoat tai khoan tu trang chi tiet ho so.
- Su dung email cua ban doc lam ten dang nhap thu vien.
- Sinh mat khau tam thoi ngau nhien.
- Luu hash SHA-256 cua mat khau tam thoi.
- Luu mat khau tam thoi dang plain text tam thoi de hien thi trong noi dung thong bao truoc khi gui.
- Dat han su dung tai khoan mac dinh la 1 nam tu ngay kich hoat.
- Tao preview noi dung thong bao kich hoat gom ten dang nhap, mat khau tam thoi, han su dung va ma ho so.
- Tao link `mailto:` de mo ung dung email.
- Cho phep ghi nhan da gui thong bao; sau khi ghi nhan, xoa mat khau tam thoi plain text khoi ho so.

### File lien quan

- `src/main/resources/templates/registration-detail.html`
- `src/main/java/com/example/library/backend/registration/dto/ActivationNotificationPreview.java`
- `src/main/java/com/example/library/backend/registration/model/LibraryAccountStatus.java`
- `src/main/java/com/example/library/backend/registration/model/ActivationNotificationStatus.java`
- `src/main/java/com/example/library/backend/registration/service/RegisterService.java`
- `src/main/java/com/example/library/backend/registration/controller/RegisterController.java`

## 5. Kiem thu da thuc hien

Du an co test web bang MockMvc tai:

- `src/test/java/com/example/library/backend/registration/controller/RegisterControllerWebTest.java`

Nhung kich ban chinh da duoc kiem thu:

- Mo form dang ky `/register`.
- Submit form hop le va luu ho so.
- Hien thi trang luu thanh cong.
- Validate du lieu sai va khong luu vao database.
- Tim kiem va sap xep danh sach ho so.
- Xem chi tiet ho so da luu.
- Kich hoat tai khoan thu vien.
- Ghi nhan da gui thong bao kich hoat.

Lenh kiem thu:

```powershell
.\run-maven-ascii.ps1 test
```

Ket qua gan nhat:

```text
Tests run: 9, Failures: 0, Errors: 0
BUILD SUCCESS
```

## 6. Cau hinh va du lieu

Ung dung hien cau hinh mac dinh de ket noi MySQL:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/library_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Ho_Chi_Minh}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
```

Nguoi chay du an can tao database:

```sql
CREATE DATABASE library_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Sau do set bien moi truong phu hop:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="mat_khau_mysql"
```

Ung dung dung `spring.jpa.hibernate.ddl-auto=update`, nen bang du lieu se duoc tao/cap nhat tu entity khi chay.

## 7. Danh gia hien trang

### Da hoan thanh

- FR-07 da co luong nhap va luu ho so dang ky day du.
- FR-10 da co luong luu tru, danh sach, tim kiem va xem chi tiet ho so.
- FR-08 da co luong kich hoat tai khoan, tao thong bao va ghi nhan da gui.
- Backend da duoc to chuc lai theo module `backend/registration`.
- CSS da duoc tach ra file static chung, khong con style inline trong template.
- Co test MockMvc bao phu cac luong nghiep vu chinh.

### Con han che / de xuat tiep theo

- Chua co dang nhap/phan quyen cho chuyen vien luu hanh.
- Chua gui email that; hien tai chi tao preview va link `mailto:`.
- Mat khau tam thoi chi duoc luu plain text tam thoi den khi chuyen vien bam "Da gui thong bao"; can can nhac giai phap an toan hon neu dua vao moi truong that.
- Chua co man hinh sua/xoa ho so da luu.
- Chua co migration versioned bang Flyway/Liquibase; hien dang dung initializer tu viet va Hibernate update.
- Chua co logging/audit trail cho thao tac kich hoat tai khoan va gui thong bao.

## 8. Ket luan

Phan da thuc hien dap ung cac luong nghiep vu cot loi cua FR-07, FR-08 va FR-10: tiep nhan ho so dang ky, luu tru/tra cuu ho so va kich hoat tai khoan thu vien. Du an da co cau truc backend ro rang hon, giao dien tach CSS chung va bo test web gan voi nghiep vu de kiem tra cac luong chinh.
