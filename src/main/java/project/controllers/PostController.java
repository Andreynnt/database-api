package project.controllers;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.models.ErrorModel;
import project.models.PostModel;
import project.services.PostService;
import project.services.ThreadService;

import java.util.List;


@RestController
@RequestMapping("/api")
public class PostController {
    private PostService postService;

    public PostController(PostService postService, ThreadService threadService) {
        this.postService = postService;
    }

    @GetMapping(value = "/thread/{slug_or_id}/posts", produces = "application/json")
    public ResponseEntity getSortedPosts(@PathVariable String slug_or_id,
                                         @RequestParam(value = "limit", required = false) Integer limit,
                                         @RequestParam(value = "sort", required = false) String sort,
                                         @RequestParam(value = "since", required = false) Integer since,
                                         @RequestParam(value = "desc", required = false) Boolean desc) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(postService.getSortedPosts(slug_or_id, limit, since, sort, desc));
        } catch (DataAccessException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("NO_THREAD"));
        }
    }

    @PostMapping(value = "/post/{id}/details", produces = "application/json")
    public ResponseEntity changePost(@RequestBody PostModel post, @PathVariable Integer id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(postService.updatePost(post, id));
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("NO_USER"));
        }
    }

    @GetMapping(value = "/post/{id}/details", produces = "application/json")
    public ResponseEntity getPost(@PathVariable Integer id, @RequestParam(value = "related",
            required = false) List<String> related) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(postService.getFullPost(id, related));
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("NO_POST"));
        }
    }
}
