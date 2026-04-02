package com.example.library;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LibraryRegistrationRepository extends JpaRepository<LibraryRegistration, Long> {

    List<LibraryRegistration> findAllByOrderByCreatedAtDescIdDesc();

    boolean existsByLibraryUsernameIgnoreCaseAndIdNot(String libraryUsername, Long id);

    @Query("""
            select r
            from LibraryRegistration r
            where lower(r.name) like lower(concat('%', :keyword, '%'))
               or lower(r.email) like lower(concat('%', :keyword, '%'))
               or r.cccd like concat('%', :keyword, '%')
               or lower(r.storageCode) like lower(concat('%', :keyword, '%'))
               or lower(coalesce(r.libraryUsername, '')) like lower(concat('%', :keyword, '%'))
            order by r.createdAt desc, r.id desc
            """)
    List<LibraryRegistration> searchArchive(@Param("keyword") String keyword);
}
