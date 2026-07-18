package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        Address address = modelMapper.map(addressDTO, Address.class);

        // Link both sides. User owns the user_address join table (@JoinTable on User.addresses),
        // so we save the User to write the join row; cascade PERSIST also stores the new Address.
        // Mutable list required: Hibernate manages this collection and cannot use List.of() (immutable).
        List<User> owners = new ArrayList<>();
        owners.add(user);
        address.setUser(owners);
        Address savedAddress = addressRepository.save(address);

        user.getAddresses().add(savedAddress);
        userRepository.save(user);

        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAddresses() {
        List<Address> addresses = addressRepository.findAll();
        return addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));
        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getUserAddresses(User user) {
        return user.getAddresses().stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        address.setStreet(addressDTO.getStreet());
        address.setBuilding(addressDTO.getBuilding());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setCountry(addressDTO.getCountry());
        address.setPincode(addressDTO.getPincode());

        Address updated = addressRepository.save(address);
        return modelMapper.map(updated, AddressDTO.class);
    }

    @Override
    public String deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        // Detach from every owning user so the join row goes away before deleting the address.
        address.getUser().forEach(user -> {
            user.getAddresses().removeIf(a -> a.getAddressId().equals(addressId));
            userRepository.save(user);
        });

        addressRepository.delete(address);
        return "Address deleted successfully with addressId: " + addressId;
    }
}
