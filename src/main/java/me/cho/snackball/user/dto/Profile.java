package me.cho.snackball.user.dto;

import lombok.Data;
import me.cho.snackball.user.domain.User;

@Data
public class Profile {

    private String username;

    private String nickname;

    private String description;

    private String occupation;

    private String company;

    private String url;

    public Profile(User user) {
        this.username = user.getUsername();
        this.nickname = user.getNickname();
        this.description = user.getDescription();
        this.occupation = user.getOccupation();
        this.company = user.getCompany();
        this.url = user.getUrl();
    }
}
