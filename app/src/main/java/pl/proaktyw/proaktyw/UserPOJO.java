package pl.proaktyw.proaktyw;

/**
 * Created by Maciej Szalek on 2018-10-19.
 */

public class UserPOJO {

    String email;
    String password;

    UserPOJO(){
    }
    public UserPOJO(String email, String password){
        this.email = email;
        this.password = password;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserPassword() {
        return password;
    }
    public void setUserPassword(String password) {
        this.password = password;
    }
}
