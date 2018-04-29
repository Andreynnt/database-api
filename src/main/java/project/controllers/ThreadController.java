package project.controllers;


import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.models.*;
import project.services.PostService;
import project.services.ThreadService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ThreadController {

    private ThreadService threadService;
    private PostService postService;

    public ThreadController(ThreadService threadService, PostService postService) {
        this.postService = postService;
        this.threadService = threadService;
    }


    @PostMapping(value = "/forum/{slug}/create", produces = "application/json")
    public ResponseEntity create(@RequestBody ThreadModel thread, @PathVariable(value = "slug") String forumSlug) {
        ThreadModel tr = thread;
        try {
            tr = threadService.create(thread, forumSlug);
        } catch (DuplicateKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(threadService.getFullBySlug(thread.getSlug()));
        } catch (DataAccessException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("NO_USER_OR_FORM"));
        }
        threadService.incrementForumThreads(forumSlug);
        return ResponseEntity.status(HttpStatus.CREATED).body(tr);
    }


    @PostMapping(value = "/thread/{slug_or_id}/create", produces = "application/json")
    public ResponseEntity createPost(@RequestBody List<PostModel> posts, @PathVariable String slug_or_id) {
        try {

            if (posts.isEmpty()) {
                threadService.getFullBySlugOrId(slug_or_id);
                return ResponseEntity.status(HttpStatus.CREATED).body("[]");
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(posts, slug_or_id));
        } catch (DuplicateKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorModel.getMessage("NO_PARENT"));
        } catch (DataAccessException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("NO_THREAD"));
        }
    }


    @GetMapping(value = "/thread/{slug_or_id}/details", produces = "application/json")
    public ResponseEntity getThread(@PathVariable String slug_or_id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(threadService.getFullBySlugOrId(slug_or_id));
        } catch (DataAccessException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("NO_THREAD"));
        }
    }


    @PostMapping(value = "/thread/{slug_or_id}/details", produces = "application/json")
    public ResponseEntity update(@RequestBody ThreadModel thread, @PathVariable String slug_or_id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(threadService.updateThread(thread, slug_or_id));
        } catch (DataAccessException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("NO_THREAD"));
        }
    }

    @PostMapping(value = "/thread/{slug_or_id}/vote", produces = "application/json")
    public ResponseEntity updateVote(@RequestBody VoteModel vote, @PathVariable String slug_or_id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(threadService.updateVotes(vote, slug_or_id));
        } catch (DataAccessException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("NO_THREAD"));
        }
    }
}
