Run the project:

```powershell
.\start-library-8081.ps1
```

Web link: http://localhost:8081/register

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
