package com.example.assignment2_work;

public class Upload {
    private String mName;
    private String mImageUrl;
    private String UserID;

    public Upload(){


    }

    public Upload (String name, String imageUrl, String UserID){
        if (name.trim().equals("")){
            name="No Name";
        }
        mName = name;
        mImageUrl = imageUrl;
        this.UserID = UserID;

    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmImageUrl() {
        return mImageUrl;
    }

    public void setmImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }
}
