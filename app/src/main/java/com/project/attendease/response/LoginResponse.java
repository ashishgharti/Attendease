package com.project.attendease.response;


public class LoginResponse {
    private String message;
    private String token;
    private User user;

    public LoginResponse(String message, String token, User user) {
        this.message = message;
        this.token = token;
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    // Nested User class

    public static class User {
        private String id;
        private String email;
        private String name;
        private String phone;
        private String password;
        private String createdAt;
        private String photo;

        public User(String id, String email, String name, String phone, String password, String createdAt, String photo) {
            this.id = id;
            this.email = email;
            this.name = name;
            this.phone = phone;
            this.password = password;
            this.createdAt = createdAt;
            this.photo = photo;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getPhoto() {
            return photo;
        }

        public void setPhoto(String photo) {
            this.photo = photo;
        }
    }
}
