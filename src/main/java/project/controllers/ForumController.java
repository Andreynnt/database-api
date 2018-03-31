package project.controllers;


import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import project.models.ErrorModel;
import project.models.ForumModel;
import org.springframework.web.bind.annotation.*;
import project.services.ForumService;



@RestController
@RequestMapping("/api/forum")
public class ForumController {

    private ForumService forumsService;


    public  ForumController(ForumService forumService) {
        this.forumsService = forumService;
    }

    @PostMapping(value = "/create", produces = "application/json")
    public ResponseEntity createForum(@RequestBody ForumModel forum) {
        try {
            forumsService.create(forum);
        } catch (DuplicateKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(forumsService.getForumBySlug(forum.getSlug()));
        } catch (DataAccessException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("NO_USER"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(forum);
    }

    @GetMapping(value = "/{slug}/details", produces = "application/json")
    public ResponseEntity details(@PathVariable String slug) {
            try {
                return ResponseEntity.status(HttpStatus.OK).body(forumsService.getForumBySlug(slug));
            } catch (DataAccessException exception) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("NO_FORUM"));
            }
    }
}
