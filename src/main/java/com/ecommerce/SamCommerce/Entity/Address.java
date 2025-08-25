package com.ecommerce.SamCommerce.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "Address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private int addressID;

    @NotBlank
    @Size(min = 5, message = "Street name must be least 5 Chars")
    @Column(name = "street")
    private String street;

    @NotBlank
    @Size(min = 5, message = "Building name must be least 5 Chars")
    @Column(name = "buildingname")
    private String buildingName;

    @NotBlank
    @Size(min = 5, message = "City name must be least 5 Chars")
    @Column(name = "city")
    private String city;

    @NotBlank
    @Size(min = 3, message = "State name must be least 3 Chars")
    @Column(name = "state")
    private String state;

    @NotBlank
    @Size(min = 3, message = "Country name must be least 3 Chars")
    @Column(name = "country")
    private String Country;

    @NotBlank
    @Size(min = 6, message = "Pincode must be least 6 Chars")
    @Column(name = "pincode")
    private String pinCode;

    public Address(String street, String buildingName, String city, String state, String country, String pinCode) {
        this.street = street;
        this.buildingName = buildingName;
        this.city = city;
        this.state = state;
        Country = country;
        this.pinCode = pinCode;
    }

    @ToString.Exclude
    @ManyToMany(mappedBy = "address")
    public List<User> user = new ArrayList<>();
}
