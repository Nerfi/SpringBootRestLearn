package com.exampleJPA2.JPA2demo.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


//import jakarta.validation.Valid;

import com.exampleJPA2.JPA2demo.models.ERole;
import com.exampleJPA2.JPA2demo.models.Role;
import com.exampleJPA2.JPA2demo.models.User;
import com.exampleJPA2.JPA2demo.repository.RoleRepository;
import com.exampleJPA2.JPA2demo.repository.UserRepository;
import com.exampleJPA2.JPA2demo.security.jwt.JwtUtils;
import com.exampleJPA2.JPA2demo.security.jwt.UserDetailsImpl;
import com.exampleJPA2.JPA2demo.security.jwt.payload.LoginRequest;
import com.exampleJPA2.JPA2demo.security.jwt.payload.MessageResponse;
import com.exampleJPA2.JPA2demo.security.jwt.payload.SignupRequest;
import com.exampleJPA2.JPA2demo.security.jwt.payload.UserInfoResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

   @Autowired
    PasswordEncoder encoder;

   @Autowired
   JwtUtils jwtUtils;


    @PostMapping("/signin")
    private ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new UserInfoResponse(userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        roles));
    }

    @PostMapping("/signup")
    private ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            //return ResponseEntity.badRequest().body(new MessageResponse("Error: Username already taken!"));
            return ResponseEntity.badRequest().body("Error: Username already taken!");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            // el original del tutorial seria asi
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
            // return ResponseEntity.badRequest().body("Error: email already take"); // esto tambien funciona pero no envia json
        }


        // create new user account if all good
        User user = new User(signupRequest.getUsername(), signupRequest.getEmail(), encoder.encode(signupRequest.getPassword()));
        Set<String> strRoles = signupRequest.getRole();

        Set<Role> roles = new HashSet<>();


        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role user is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role admin is not found."));
                        roles.add(adminRole);

                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role moderator is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role user is not found."));
                        roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);
        userRepository.save(user);
        //return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        return null;


    }
    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("good signed out");
        //.body(new MessageResponse("You've been signed out!"));
    }

}