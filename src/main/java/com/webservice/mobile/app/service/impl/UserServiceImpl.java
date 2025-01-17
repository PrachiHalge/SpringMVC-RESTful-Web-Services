package com.webservice.mobile.app.service.impl;


import com.webservice.mobile.app.exceptions.UserServiceException;
import com.webservice.mobile.app.io.entity.UserEntity;
import com.webservice.mobile.app.io.repositories.UserRepository;
import com.webservice.mobile.app.service.UserService;
import com.webservice.mobile.app.shared.Utils;
import com.webservice.mobile.app.shared.dto.UserDTO;
import com.webservice.mobile.app.ui.model.response.ErrorMessages;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.rmi.runtime.Log;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    Utils utils;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDTO createUser(UserDTO userDTO) {


        if (userRepository != null){
            if (userRepository.findUserByEmail(userDTO.getEmail()) !=null)
                throw new UserServiceException(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage());
        }


        UserEntity userEntity = new UserEntity();
        BeanUtils.copyProperties(userDTO,userEntity);

        String autoGeneratedPublicUserID = utils.generateUserId(30);
        userEntity.setUserId(autoGeneratedPublicUserID);
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(userDTO.getPassword()));

        UserEntity storedUSerDeatils =userRepository.save(userEntity);
        UserDTO returnValue = new UserDTO();
        BeanUtils.copyProperties(storedUSerDeatils,returnValue);

        return returnValue;
    }

    @Override
    public UserDTO getUser(String email) {
        UserEntity userEntity = userRepository.findUserByEmail(email);
        if (userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        UserDTO returnValue = new UserDTO();
        BeanUtils.copyProperties(userEntity,returnValue);
        return returnValue;

    }

    @Override
    public UserDTO getUserByUserId(String userId) {

        UserDTO returnValue = new UserDTO();
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        BeanUtils.copyProperties(userEntity,returnValue);

        return returnValue;
    }

    @Override
    public UserDTO updateUser(String userId, UserDTO user) {

        UserDTO returnValue = new UserDTO();
        UserEntity  userEntity = userRepository.findByUserId(userId);
        if (userEntity == null)
            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        userEntity.setFirstName(user.getFirstName());
        userEntity.setLastName(user.getLastName());
        UserEntity updatedUserDetails=userRepository.save(userEntity);
        BeanUtils.copyProperties(updatedUserDetails,returnValue);

        return returnValue;
    }

    @Transactional
    @Override
    public void deleteUser(String userId) {

        UserEntity  userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) throw new
                UserServiceException((ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));
        userRepository.delete(userEntity);

    }

    @Override
    public List<UserDTO> getUsers(int page, int limit) {

        List<UserDTO> returnValue = new ArrayList<>();

        if (page>0) page =  page-1;
        Pageable pageable = PageRequest.of(page,limit);
        Page<UserEntity> userEntityPage = userRepository.findAll(pageable);
        List<UserEntity> users = userEntityPage.getContent();

        for (UserEntity userEntity:users){
            UserDTO userDTO  = new UserDTO();
            BeanUtils.copyProperties(userEntity,userDTO);
            returnValue.add(userDTO);
        }


        return returnValue;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity= userRepository.findUserByEmail(email);
        if (userEntity ==null)throw new UsernameNotFoundException(email);

        return new User(userEntity.getEmail(),userEntity.getEncryptedPassword(),new ArrayList<>());
    }

	@Override
	public boolean resetPassword(String id, String password) {
		 UserEntity  userEntity = userRepository.findByUserId(id);
	        if (userEntity == null)
	            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
	        
	     userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(password));	     
	     UserEntity updatedUserDetails=userRepository.save(userEntity);
	     
	     if(updatedUserDetails!=null){
	    	 return true;
	     }else{
	    	 return false;
	     }
		
	} 
    
}
