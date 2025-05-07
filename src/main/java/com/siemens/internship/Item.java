
package com.siemens.internship;

import com.siemens.internship.validation.EmailValidation;
import com.siemens.internship.validation.StatusValidation;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Name mandatory")
    private String name;

    @NotBlank(message = "Description mandatory")
    private String description;

    @NotBlank(message = "Status mandatory")
    @EmailValidation(message = "Please provide a valid status. Allowed values: Processed, Not_Processed")
    private String status;

    @NotBlank(message = "Email mandatory")
    @EmailValidation(message = "Please provide a valid email address")
    private String email;
}
