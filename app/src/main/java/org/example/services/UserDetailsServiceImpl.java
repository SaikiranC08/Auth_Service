package org.example.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.entities.UserInfo;
import org.example.eventProducer.UserInfoProducer;
import org.example.model.UserInfoDto;
import org.example.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;


@Component
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private final UserInfoRepository userInfoRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private final UserInfoProducer userInfoProducer;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user = userInfoRepository.findByUserName(username);
        if(user == null) {
            throw new RuntimeException("could not found user ...");
        }
        return new CustomUserDetails(user);
    }

    public UserInfo checkIfUserExists(UserInfoDto userInfoDto){
        return userInfoRepository.findByUserName(userInfoDto.getUserName());
    }


    //validating email
    public boolean validateEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(emailRegex);
    }

    //validating phone number
    public boolean validateNumber(String number) {
        if (number == null || number.trim().isEmpty()) {
            System.out.println("Phone number is null or empty");
            return false;
        }
        
        // Remove any spaces and trim
        String cleanNumber = number.trim().replaceAll("\\s+", "");
        System.out.println("Validating phone number: '" + cleanNumber + "'");
        
        // Allow formats: +1234567890, 1234567890, +91234567890 etc (10-15 digits total)
        String regex = "^\\+?[0-9]{10,15}$";
        boolean isValid = cleanNumber.matches(regex);
        System.out.println("Phone number validation result: " + isValid);
        return isValid;
    }





    public boolean siginupUser(UserInfoDto userInfoDto){

        //validation
        if (!validateEmail(userInfoDto.getEmail())) {
            throw new IllegalArgumentException("Invalid email format!");
        }
        if(!validateNumber(userInfoDto.getPhoneNumber())){
            throw new IllegalArgumentException("Invalid phone number");
        }


        userInfoDto.setPassword(passwordEncoder.encode(userInfoDto.getPassword()));
        if(Objects.nonNull(checkIfUserExists(userInfoDto))){
            return false;
        }
        String userId = UUID.randomUUID().toString();
        userInfoRepository.save(new UserInfo(userId,userInfoDto.getUserName(),userInfoDto.getPassword(),
                userInfoDto.getEmail(), userInfoDto.getPhoneNumber(), new HashSet<>()));
        // Sending to kafka
        userInfoProducer.sendEventToKafka(userInfoDto);
        return true;
    }

}
