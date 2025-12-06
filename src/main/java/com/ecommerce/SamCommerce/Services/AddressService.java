package com.ecommerce.SamCommerce.Services;

import com.ecommerce.SamCommerce.Entity.User;
import com.ecommerce.SamCommerce.Payload.AddressDTO;

import java.util.List;

public interface AddressService {
    AddressDTO createAddress(AddressDTO addressDTO, User user);

    List<AddressDTO> getAddress();

    AddressDTO getAddressByID(Integer addressId);

    List<AddressDTO> getAddressUser(User user);

    AddressDTO updateAddressByID(Integer addressId, AddressDTO addressDTO);

    String deleteAddressByID(Integer addressId);
}
