package project.controllers;


import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.models.ErrorModel;
import project.services.ManagerService;

@RestController
@RequestMapping("/api/service")
public class ManagerController {

    private ManagerService managerService;

    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }


    @GetMapping(value = "/status", produces = "application/json")
    public ResponseEntity getInfo() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(managerService.databaseStatus());
        } catch (DataAccessException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("ERROR"));
        }
    }

    @PostMapping(value = "/clear")
    public ResponseEntity clearAll() {
        try {
            managerService.clear();
            return ResponseEntity.status(HttpStatus.OK).body("All is clear");
        } catch (DataAccessException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("ERROR"));
        }
    }

}
