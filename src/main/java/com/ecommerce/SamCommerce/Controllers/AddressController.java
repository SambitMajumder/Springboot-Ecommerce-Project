package com.ecommerce.SamCommerce.Controllers;

import com.ecommerce.SamCommerce.Entity.User;
import com.ecommerce.SamCommerce.Payload.AddressDTO;
import com.ecommerce.SamCommerce.Services.AddressService;
import com.ecommerce.SamCommerce.Util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {
    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private AddressService addressService;

    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO) {
        User user = authUtil.loggedInUser();
        AddressDTO savedAddress = addressService.createAddress(addressDTO, user);
        return new ResponseEntity<>(savedAddress, HttpStatus.CREATED);
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAddress() {
        List<AddressDTO> addressDTOList = addressService.getAddress();
        return new ResponseEntity<>(addressDTOList, HttpStatus.OK);
    }

    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Integer addressId) {
        AddressDTO addressDTO = addressService.getAddressByID(addressId);
        return new ResponseEntity<>(addressDTO, HttpStatus.OK);
    }

    @GetMapping("/users/addresses")
    public ResponseEntity<List<AddressDTO>> getAddressByUser() {
        User user = authUtil.loggedInUser();
        List<AddressDTO> addressDTOList = addressService.getAddressUser(user);
        return new ResponseEntity<>(addressDTOList, HttpStatus.OK);
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> updateAddressById(@PathVariable Integer addressId,
                                                     @RequestBody AddressDTO addressDTO) {
        AddressDTO updatedAddress = addressService.updateAddressByID(addressId, addressDTO);
        return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<String> deleteAddressById(@PathVariable Integer addressId) {
        String deletedAddressStatus = addressService.deleteAddressByID(addressId);
        return new ResponseEntity<>(deletedAddressStatus, HttpStatus.OK);
    }


}
