Run the project:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/library_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Ho_Chi_Minh"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-mysql-password"
.\start-library-8081.ps1
```

Public reader registration page: http://localhost:8081/register

Admin login page: http://localhost:8081/login

Default admin account for local testing:

```powershell
Username: admin
Password: admin123
```

You can override the admin account before starting the app:

```powershell
$env:LIBRARY_ADMIN_USERNAME="admin"
$env:LIBRARY_ADMIN_PASSWORD="change-this-password"
.\start-library-8081.ps1
```

SMTP email configuration:

```powershell
# If port 8081 is already in use, stop the old process first.
# Example:
# Stop-Process -Id <PID> -Force

$env:SMTP_HOST="smtp.gmail.com"
$env:SMTP_PORT="587"
$env:SMTP_USERNAME="your-gmail-address@gmail.com"
$env:SMTP_PASSWORD="your-16-character-app-password-without-spaces"
$env:MAIL_FROM="your-gmail-address@gmail.com"
.\start-library-8081.ps1
```

For Gmail, use a Google App Password and enter the 16-character password without spaces.
