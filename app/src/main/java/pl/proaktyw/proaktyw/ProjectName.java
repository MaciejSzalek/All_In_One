package pl.proaktyw.proaktyw;

/**
 * Created by Maciek Szalek on 2018-07-04.
 */

public class ProjectName {
    private int _id;
    private String _projectName;

    public ProjectName(){
    }

    public ProjectName(int _id, String _projectName){
        this._id = _id;
        this._projectName = _projectName;
    }
    public ProjectName(String _projectName){
        this._projectName = _projectName;
    }

    public int get_id(){
        return _id;
    }

    public String get_projectName(){
        return _projectName;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void set_projectName(String _projectName) {
        this._projectName = _projectName;
    }
}
