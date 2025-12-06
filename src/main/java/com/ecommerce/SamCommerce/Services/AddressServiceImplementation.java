package com.ecommerce.SamCommerce.Services;

import com.ecommerce.SamCommerce.Entity.Address;
import com.ecommerce.SamCommerce.Entity.User;
import com.ecommerce.SamCommerce.Exceptions.ResourceNotFoundException;
import com.ecommerce.SamCommerce.Payload.AddressDTO;
import com.ecommerce.SamCommerce.Repositories.AddressRepository;
import com.ecommerce.SamCommerce.Repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImplementation implements AddressService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        //GETTING THE ADDRESS OBJECT OF THE MODEL
        Address address = modelMapper.map(addressDTO, Address.class);

        //UPDATING THE ADDRESS AGAINST THE USER
        //GET THE ADDRESSES WHICH THE USER OBJECT HAS
        List<Address> addressList = user.getAddress();
        //NOW WE NEED TO UPDATE THE ADDRESS LIST
        addressList.add(address);
        //NOW UPDATE THE USER ADDRESS
        user.setAddress(addressList);

        //SETTING THE USER OBJECT WITHIN THE ADDRESS
        address.setUser(user);
        Address savedAddress = addressRepository.save(address);
        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAddress() {
        List<Address> address = addressRepository.findAll();
        List<AddressDTO> addressDTOList = address.stream().map(add -> modelMapper.map(add, AddressDTO.class)).toList();
        return addressDTOList;
    }

    @Override
    public AddressDTO getAddressByID(Integer addressId) {
        Address address = addressRepository.findById(addressId).orElse(null);
        AddressDTO addressDTO = modelMapper.map(address, AddressDTO.class);;
        return addressDTO;
    }

    @Override
    public List<AddressDTO> getAddressUser(User user) {
        List<Address> addressList = user.getAddress();
        List<AddressDTO> addressDTOList = addressList.stream().map(add -> modelMapper.map(add, AddressDTO.class)).toList();
        return addressDTOList;
    }

    @Override
    public AddressDTO updateAddressByID(Integer addressId, AddressDTO addressDTO) {
        Address address = addressRepository.findById(addressId).orElse(null);
        address.setCity(addressDTO.getCity());
        address.setCountry(addressDTO.getCountry());
        address.setStreet(addressDTO.getStreet());
        address.setPincode(addressDTO.getPincode());
        address.setCountry(addressDTO.getCountry());
        address.setBuildingName(addressDTO.getBuildingName());
        Address updatedAddress = addressRepository.save(address);

        User user = updatedAddress.getUser();
        //user.getAddress().removeIf(add -> add.getAddressID().equals(addressId));
        user.getAddress().add(updatedAddress);
        userRepository.save(user);
        return modelMapper.map(updatedAddress, AddressDTO.class);
    }

    @Override
    public String deleteAddressByID(Integer addressId) {
        Address address = addressRepository.findById(addressId).orElseThrow(
                ()-> new ResourceNotFoundException("Address", "addressId", addressId)
        );
        //DELETING FROM USER
        User user = address.getUser();
        user.getAddress().remove(address);
        userRepository.save(user);
        //DELETING FROM ADDRESS
        addressRepository.delete(address);
        return "Address is deleted successfully";
    }
}
