package project.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import project.models.UserModel;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private JdbcTemplate jdbcTemplate;

    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserModel createUser(UserModel user) {
        jdbcTemplate.update("INSERT INTO users (fullname, about, email,  nickname) VALUES (?, ?, ?::citext, ?::citext)",
                user.getFullname(), user.getAbout(), user.getEmail(), user.getNickname());
        return user;
    }

    public List<UserModel> getSameUsers(UserModel user) {
        final String sql =
            "SELECT about, email, fullname, nickname FROM users WHERE email = ?::citext OR nickname = ?::citext";
        List<UserModel> sameUsers =
            jdbcTemplate.query(sql, UserModel::getUser, user.getEmail(), user.getNickname());
        return sameUsers;
    }

    public UserModel getUser(String nickname) {
        final String sql = "SELECT about, email, fullname, nickname FROM users WHERE nickname = ?::citext";
        return jdbcTemplate.queryForObject(sql, UserModel::getUser, nickname);
    }


    public UserModel changeUser(UserModel user, String nickname) {
        String sql = "UPDATE users SET ";
        ArrayList<String> values = new ArrayList<String>();

        if (user.getAbout() != null) {
            sql += " about = ? ,";
            values.add(user.getAbout());
        }

        if (user.getEmail() != null) {
            sql += " email = ?::citext,";
            values.add(user.getEmail());
        }

        if (user.getFullname() != null) {
            sql += " fullname = ?,";
            values.add(user.getFullname());
        }

        if (user.getNickname() != null) {
            sql += " nickname = ?::citext,";
            values.add(user.getNickname());
        }

        if (values.size() == 0) {
            return getUser(nickname);
        }

        sql = sql.substring(0, sql.length() - 1);
        sql += " WHERE nickname = ?::citext";
        values.add(nickname);
        jdbcTemplate.update(sql, values.toArray());

        if (user.getNickname() != null) {
            nickname = user.getNickname();
        }

        return getUser(nickname);
    }

    public Integer getIdByName(String name) {
        return jdbcTemplate.queryForObject("SELECT id FROM users WHERE nickname = ?::citext", Integer.class, name);
    }

}
