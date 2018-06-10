package project.controllers;


import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.models.*;
import project.services.ForumService;
import project.services.PostService;
import project.services.ThreadService;
import project.services.UserService;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ThreadController {

    private ThreadService threadService;
    private PostService postService;
    private UserService userService;
    private ForumService forumService;

    public ThreadController(ThreadService threadService, PostService postService,
                            UserService userService, ForumService forumService) {
        this.postService = postService;
        this.threadService = threadService;
        this.userService = userService;
        this.forumService = forumService;
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
        final ThreadModel thread;
        try {
            thread = threadService.getFullBySlugOrId(slug_or_id);
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("NO_THREAD"));
        }

        if (posts.size() == 0) {
            return ResponseEntity.status(HttpStatus.CREATED).body(posts);
        }

        try {
            final String currentTime = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
            final Integer forumId = threadService.gerForumIdByThreadID(thread.getId());
            final String forumSlug = forumService.getSlugById(forumId);

            for (PostModel p : posts) {
                //подразумеваю, что в случае ошибки выйдем
                p.setAuthorId(userService.getIdByName(p.getAuthor()));

                Integer parentId = 0;
                if (p.getParent() != null && p.getParent() != 0) {
                    try {
                        parentId = postService.getParent(thread.getId(), p.getParent());
                    } catch (DataAccessException ex) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorModel.getMessage("ERROR"));
                    }
                }

                p.setParent(parentId);
                p.setThread(thread.getId());
                p.setForumId(forumId);
                p.setForum(forumSlug);
                p.setCreated(currentTime);
            }
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("ERROR"));
        }

        try {
            postService.create2(posts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("ERROR_IN_CREATE2"));
        }

        for (PostModel post : posts) {
            post.setAuthorId(null);
            post.setForumId(null);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(posts);
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
