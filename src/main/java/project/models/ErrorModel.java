package project.models;


import java.util.HashMap;

public class ErrorModel {

    private static final HashMap<String, String> ERRORS = createMap();

    private static HashMap<String, String> createMap() {
        HashMap<String, String> map= new HashMap<String, String>();

        map.put("NO_USER", "User doesn't exist");
        map.put("NO_FORUM", "Forum doesn't exist");
        map.put("FORUM_EXISTS", "This forum already exists");
        map.put("USER_CONFLICT", "New data conflicts with someone");
        map.put("NO_USER_OR_FORM", "User or forum doesn't exists");

        return map;
    }

    public static MessageModel getMessage(String code) {
        final String answer = ERRORS.get(code);
        return new MessageModel(answer);
    }
}
