package com.siemens.internship.model;

import com.siemens.internship.validation.ValidEmail;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This is the model class for an item to be processed in the Internship Application.
 * It contains name, description and email as metadata,and processing status.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    /**
     * Unique identifier, it is auto-generated.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name of the item; It is required, max length 100 chars.
     */
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    /**
     * Detailed description, optional, max length 250.
     */
    @Size(max = 250, message = "Description must be at most 250 characters")
    private String description;

    /**
     * Current status; required
     */
    @NotBlank(message = "Status is required")
    private String status;

    /**
     * Email address; required; must be valid format, checked using our custom @ValidEmail annotation
     * Email checked using regex:
     * "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$"
     */
    @NotBlank(message = "Email is required")
    @ValidEmail(message = "Email must be valid: example@domain.com")
    private String email;
}