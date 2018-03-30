package project.controllers;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.models.ErrorModel;
import project.models.UserModel;
import project.services.UserService;


@RestController
@RequestMapping("/api/user")
public class UserController {

    private UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/{nickname}/create", produces = "application/json")
    public ResponseEntity createUser(@PathVariable String nickname, @RequestBody UserModel user) {
        user.setNickname(nickname);
        try {
            this.userService.create(user);
        } catch (DuplicateKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(userService.getSameUsers(user));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping(value = "/{nickname}/profile", produces = "application/json")
    public ResponseEntity getUser(@PathVariable String nickname) {
        UserModel user;
        try {
            user = this.userService.getUser(nickname);
        } catch (DataAccessException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("NO_USER"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping(value = "/{nickname}/profile", produces = "application/json")
    public ResponseEntity changeUser(@PathVariable String nickname, @RequestBody UserModel user) {
        UserModel updatedUser = user;
        try {
            updatedUser = userService.changeUser(user, nickname);
        } catch (DuplicateKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorModel.getMessage("USER_CONFLICT"));
        } catch (DataAccessException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("NO_USER"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
    }
}