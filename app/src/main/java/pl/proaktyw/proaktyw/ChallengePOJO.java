package pl.proaktyw.proaktyw;

/**
 * Created by Maciej Szalek on 2018-08-18.
 */

public class ChallengePOJO {
    private int _id;
    private String _challenge_name;
    private String _challenge;
    private String _challenge_password;

    public ChallengePOJO(){
    }

    public ChallengePOJO(int _id, String _challenge_name, int _marker_index, String _challenge,
                         String _challenge_password){
    }

    public int get_id(){
        return _id;
    }
    public String get_challenge_name(){
        return _challenge_name;
    }
    public String get_challenge(){
        return _challenge;
    }
    public String get_challenge_password(){
        return _challenge_password;
    }


    public void set_id(int _id) {
        this._id = _id;
    }
    public void set_challenge_name(String _challenge_name) {
        this._challenge_name = _challenge_name;
    }
    public void set_challenge(String _challenge){
        this._challenge = _challenge;
    }
    public void set_challenge_password(String _challenge_password) {
        this._challenge_password = _challenge_password;
    }
}
