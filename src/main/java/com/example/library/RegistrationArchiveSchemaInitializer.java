package com.example.library;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class RegistrationArchiveSchemaInitializer implements ApplicationRunner {

    private static final String STORAGE_LOCATION = "Bo phan Luu hanh";
    private static final DateTimeFormatter STORAGE_CODE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String DEFAULT_ACCOUNT_STATUS = LibraryAccountStatus.PENDING_ACTIVATION.name();
    private static final String DEFAULT_NOTIFICATION_STATUS = ActivationNotificationStatus.NOT_PREPARED.name();

    private final JdbcTemplate jdbcTemplate;

    public RegistrationArchiveSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("ALTER TABLE library_registrations ADD COLUMN IF NOT EXISTS storage_code VARCHAR(40)");
        jdbcTemplate.execute("ALTER TABLE library_registrations ADD COLUMN IF NOT EXISTS storage_location VARCHAR(120)");
        jdbcTemplate.execute("ALTER TABLE library_registrations ADD COLUMN IF NOT EXISTS library_username VARCHAR(255)");
        jdbcTemplate.execute("ALTER TABLE library_registrations ADD COLUMN IF NOT EXISTS library_password_hash VARCHAR(255)");
        jdbcTemplate.execute("ALTER TABLE library_registrations ADD COLUMN IF NOT EXISTS notification_password_plaintext VARCHAR(120)");
        jdbcTemplate.execute("ALTER TABLE library_registrations ADD COLUMN IF NOT EXISTS account_status VARCHAR(30)");
        jdbcTemplate.execute("ALTER TABLE library_registrations ADD COLUMN IF NOT EXISTS activation_notification_status VARCHAR(30)");
        jdbcTemplate.execute("ALTER TABLE library_registrations ADD COLUMN IF NOT EXISTS account_activated_at TIMESTAMP(6)");
        jdbcTemplate.execute("ALTER TABLE library_registrations ADD COLUMN IF NOT EXISTS account_expires_at DATE");
        jdbcTemplate.execute("ALTER TABLE library_registrations ADD COLUMN IF NOT EXISTS activation_notification_prepared_at TIMESTAMP(6)");
        jdbcTemplate.execute("ALTER TABLE library_registrations ADD COLUMN IF NOT EXISTS activation_notification_sent_at TIMESTAMP(6)");

        List<LegacyRegistrationRow> legacyRows = jdbcTemplate.query(
                """
                select id, created_at
                from library_registrations
                where storage_code is null or trim(storage_code) = ''
                """,
                (rs, rowNum) -> new LegacyRegistrationRow(
                        rs.getLong("id"),
                        toLocalDateTime(rs.getTimestamp("created_at"))));

        for (LegacyRegistrationRow legacyRow : legacyRows) {
            jdbcTemplate.update(
                    "update library_registrations set storage_code = ? where id = ?",
                    generateStorageCode(legacyRow.id(), legacyRow.createdAt()),
                    legacyRow.id());
        }

        jdbcTemplate.update(
                """
                update library_registrations
                set storage_location = ?
                where storage_location is null or trim(storage_location) = ''
                """,
                STORAGE_LOCATION);

        jdbcTemplate.update(
                """
                update library_registrations
                set account_status = ?
                where account_status is null or trim(account_status) = ''
                """,
                DEFAULT_ACCOUNT_STATUS);

        jdbcTemplate.update(
                """
                update library_registrations
                set activation_notification_status = ?
                where activation_notification_status is null or trim(activation_notification_status) = ''
                """,
                DEFAULT_NOTIFICATION_STATUS);

        jdbcTemplate.execute(
                "CREATE UNIQUE INDEX IF NOT EXISTS idx_library_registrations_storage_code ON library_registrations(storage_code)");
        jdbcTemplate.execute(
                "CREATE UNIQUE INDEX IF NOT EXISTS idx_library_registrations_library_username ON library_registrations(library_username)");
        jdbcTemplate.execute("ALTER TABLE library_registrations ALTER COLUMN storage_code SET NOT NULL");
        jdbcTemplate.execute("ALTER TABLE library_registrations ALTER COLUMN storage_location SET NOT NULL");
        jdbcTemplate.execute("ALTER TABLE library_registrations ALTER COLUMN account_status SET NOT NULL");
        jdbcTemplate.execute("ALTER TABLE library_registrations ALTER COLUMN activation_notification_status SET NOT NULL");
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp != null) {
            return timestamp.toLocalDateTime();
        }
        return LocalDateTime.now();
    }

    private String generateStorageCode(Long id, LocalDateTime createdAt) {
        return "HS-" + createdAt.format(STORAGE_CODE_TIME_FORMAT) + "-L" + String.format("%04d", id);
    }

    private record LegacyRegistrationRow(Long id, LocalDateTime createdAt) {
    }
}
