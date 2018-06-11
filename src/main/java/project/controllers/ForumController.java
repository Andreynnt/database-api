package project.controllers;


import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import project.models.ErrorModel;
import project.models.ForumModel;
import org.springframework.web.bind.annotation.*;
import project.models.ThreadModel;
import project.models.UserModel;
import project.services.ForumService;

import java.util.List;


@RestController
@RequestMapping("/api")
public class ForumController {

    private ForumService forumsService;


    public ForumController(ForumService forumService) {
        this.forumsService = forumService;
    }

    @PostMapping(value = "/forum/create", produces = "application/json")
    public ResponseEntity createForum(@RequestBody ForumModel forum) {
        try {
            forumsService.create(forum);
        } catch (DuplicateKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(forumsService.getForumBySlug(forum.getSlug()));
        } catch (DataAccessException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("NO_USER"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(forumsService.getForumBySlug(forum.getSlug()));
    }

    @GetMapping(value = "/forum/{slug}/threads", produces = "application/json")
    public ResponseEntity threads(@PathVariable String slug,
                                  @RequestParam(value = "limit", required = false) Integer limit,
                                  @RequestParam(value = "since", required = false) String since,
                                  @RequestParam(value = "desc", required = false) Boolean desc) {
            try {
                List<ThreadModel> threads = forumsService.getThreads(slug, limit, since, desc);
                //todo убрать эту проверку
                if (threads.isEmpty()) {
                    forumsService.getForumBySlug(slug);
                }
                return ResponseEntity.status(HttpStatus.OK).body(threads);
            } catch (DataAccessException exception) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("NO_FORUM"));
            }
    }

    @GetMapping(value = "/forum/{slug}/details", produces = "application/json")
    public ResponseEntity details(@PathVariable String slug) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(forumsService.getForumBySlug(slug));
        } catch (DataAccessException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("NO_FORUM"));
        }
    }

    @GetMapping(value = "/forum/{slug}/users", produces = "application/json")
    public ResponseEntity users(@PathVariable String slug,
                                @RequestParam(value = "limit", required = false) Integer limit,
                                @RequestParam(value = "since", required = false) String since,
                                @RequestParam(value = "desc", required = false) Boolean desc) {
        try {
            List<UserModel> users = forumsService.getUsers(slug, since, desc, limit);
            return ResponseEntity.status(HttpStatus.OK).body(users);
        } catch (DataAccessException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("ERROR"));
        }

    }
}
