package com.example.library;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterForm {

    @NotBlank(message = "Vui long nhap ho va ten.")
    @Size(max = 150, message = "Ho va ten khong duoc vuot qua 150 ky tu.")
    private String name;

    @NotBlank(message = "Vui long chon gioi tinh.")
    private String gender;

    @NotNull(message = "Vui long chon ngay sinh.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dob;

    @NotBlank(message = "Vui long nhap noi sinh.")
    @Size(max = 150, message = "Noi sinh khong duoc vuot qua 150 ky tu.")
    private String birthPlace;

    @NotBlank(message = "Vui long nhap so CCCD.")
    @Pattern(regexp = "\\d{12}", message = "So CCCD phai gom dung 12 chu so.")
    private String cccd;

    @NotBlank(message = "Vui long nhap noi cap.")
    @Size(max = 150, message = "Noi cap khong duoc vuot qua 150 ky tu.")
    private String issuePlace;

    @NotBlank(message = "Vui long nhap chuyen mon.")
    @Size(max = 150, message = "Chuyen mon khong duoc vuot qua 150 ky tu.")
    private String specialization;

    @NotBlank(message = "Vui long nhap noi cong tac.")
    @Size(max = 150, message = "Noi cong tac khong duoc vuot qua 150 ky tu.")
    private String workplace;

    @NotBlank(message = "Vui long nhap dia chi lien he.")
    @Size(max = 255, message = "Dia chi lien he khong duoc vuot qua 255 ky tu.")
    private String address;

    @NotBlank(message = "Vui long nhap so dien thoai.")
    @Pattern(regexp = "\\d{10,11}", message = "So dien thoai phai co 10 hoac 11 chu so.")
    private String phone;

    @NotBlank(message = "Vui long nhap email.")
    @Email(message = "Email khong dung dinh dang.")
    @Size(max = 255, message = "Email khong duoc vuot qua 255 ky tu.")
    private String email;

    private MultipartFile avatar;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getBirthPlace() { return birthPlace; }
    public void setBirthPlace(String birthPlace) { this.birthPlace = birthPlace; }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public String getIssuePlace() { return issuePlace; }
    public void setIssuePlace(String issuePlace) { this.issuePlace = issuePlace; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getWorkplace() { return workplace; }
    public void setWorkplace(String workplace) { this.workplace = workplace; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public MultipartFile getAvatar() { return avatar; }
    public void setAvatar(MultipartFile avatar) { this.avatar = avatar; }
}
