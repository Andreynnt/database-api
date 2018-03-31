package project.controllers;


import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.models.ErrorModel;
import project.models.ThreadModel;
import project.services.ThreadService;

@RestController
@RequestMapping("/api")
public class ThreadController {

    private ThreadService threadService;


    public ThreadController(ThreadService threadService) {
        this.threadService = threadService;
    }

    @PostMapping(value = "/forum/{slug}/create", produces = "application/json")
    public ResponseEntity create(@RequestBody ThreadModel thread, @PathVariable(value = "slug") String forumSlug) {
        ThreadModel tr = thread;

        try {
            tr = threadService.create(thread, forumSlug);
        } catch (DuplicateKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(threadService.getThread(thread, forumSlug));
        } catch (DataAccessException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel.getMessage("NO_USER_OR_FORM"));
        }

        threadService.incrementForumThreads(forumSlug);
        return ResponseEntity.status(HttpStatus.CREATED).body(tr);
    }

}
