package project.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import project.models.UserModel;
import project.rowmapper.ApiRowMapper;

import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private JdbcTemplate jdbcTemplate;


    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserModel create(UserModel user) {
        final String sql =
            "INSERT INTO users (about, fullname, nickname, email) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getAbout(), user.getFullname(), user.getNickname(), user.getEmail());
        return user;
    }

    public List<UserModel> getSameUsers(UserModel user) {
        final String sql =
            "SELECT about, email, fullname, nickname FROM users WHERE email = ? OR nickname = ?";
        List<UserModel> sameUsers =
            jdbcTemplate.query(sql, ApiRowMapper.getUser, user.getEmail(), user.getNickname());
        return sameUsers;
    }

    public UserModel getUser(String nickname) {
        final String sql = "SELECT about, email, fullname, nickname FROM users WHERE nickname = ?";
        return  jdbcTemplate.queryForObject(sql, ApiRowMapper.getUser, nickname);
    }


    public UserModel changeUser(UserModel user, String nickname) {
        String sql = "UPDATE users SET ";
        ArrayList<String> values = new ArrayList<String>();

        if (user.getAbout() != null) {
            sql += " about = ? ,";
            values.add(user.getAbout());
        }

        if (user.getEmail() != null) {
            sql += " email = ? ,";
            values.add(user.getEmail());
        }

        if (user.getFullname() != null) {
            sql += " fullname = ? ,";
            values.add(user.getFullname());
        }

        if (user.getNickname() != null) {
            sql += " nickname = ? ,";
            values.add(user.getNickname());
        }

        if (values.size() == 0) {
            return getUser(nickname);
        }

        sql = sql.substring(0, sql.length() - 1);
        sql += " WHERE nickname = ?";
        values.add(nickname);
        jdbcTemplate.update(sql, values.toArray());

        if (user.getNickname() != null) {
            nickname = user.getNickname();
        }

        return getUser(nickname);
    }

}
