package pl.proaktyw.proaktyw;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by Maciej Szalek on 2018-07-14.
 */

public class MarkerPOJO {
    private int _id;
    private String _project_name;
    private Integer _marker_index;
    private double _marker_latitude;
    private double _marker_longitude;
    private String _challenge;
    private String _challenge_password;
    private String _challenge_name;

    public MarkerPOJO(){
    }

    public MarkerPOJO(int _id, Integer _marker_index, double _marker_latitude, double _marker_longitude,
                      String _project_name, String _challenge, String _challenge_password,
                      String _challenge_name){
        this._id = _id;
        this._project_name = _project_name;
        this._marker_index = _marker_index;
        this._marker_latitude = _marker_latitude;
        this._marker_longitude = _marker_longitude;
        this._challenge = _challenge;
        this._challenge_password = _challenge_password;
        this._challenge_name = _challenge_name;
    }


    public int get_id(){
        return _id;
    }
    public String get_project_name(){
        return _project_name;
    }
    public Integer get_marker_index(){
        return _marker_index;
    }
    public Double get_marker_latitude(){
        return _marker_latitude;
    }
    public Double get_marker_longitude(){
        return _marker_longitude;
    }
    public String get_challenge(){
        return _challenge;
    }
    public String get_challenge_password(){
        return _challenge_password;
    }
    public String get_challenge_name() { return _challenge_name; }

    public void set_id(int _id){
        this._id = _id;
    }
    public void set_project_name(String _project_name){
        this._project_name = _project_name;
    }
    public  void set_marker_index(Integer _marker_index){
        this._marker_index = _marker_index;
    }
    public void set_marker_latitude(Double _marker_latitude){
        this._marker_latitude = _marker_latitude;
    }
    public void set_marker_longitude(Double _marker_longitude){
        this._marker_longitude = _marker_longitude;
    }

    public void set_challenge(String _challenge) {
        this._challenge = _challenge;
    }

    public void set_challenge_password(String _challenge_password) {
        this._challenge_password = _challenge_password;
    }

    public void set_challenge_name(String _challenge_name) {
        this._challenge_name = _challenge_name;
    }
}

