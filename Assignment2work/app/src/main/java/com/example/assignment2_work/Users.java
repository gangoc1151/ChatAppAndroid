package com.example.assignment2_work;

import com.google.firebase.firestore.auth.User;

public class Users {
    private String id;
    private String Username;
    private String Email;
    private String ImageURL;

    public Users(){

    }
    public Users(String id, String Email, String ImageURL, String Username) {
        this.id = id;

        this.Email = Email;
        this.ImageURL = ImageURL;
        this.Username = Username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public String getEmail() {
        return Email;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }

    public String getImageURL() {
        return ImageURL;
    }

    public void setImageURL(String ImageURL) {
        this.ImageURL = ImageURL;
    }
    public String getUsername() {
        return Username;
    }

    public void setUsername(String Username) {
        this.Username = Username;
    }
}
