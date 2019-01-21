package pl.proaktyw.proaktyw;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maciej Szalek on 2018-07-11.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "projects";

    //Table: project name
    private static final String TABLE_PROJECT_NAME = "project_name";
    //Table: markers list
    private static final String TABLE_MARKERS_LIST = "markers_list";
    //Table: challenge
    private static final String TABLE_CHALLENGE = "challenge";
    //Table: user
    private static final String TABLE_USER = "user";

    //Columns project name
    private static final String KEY_ID = "id";
    private static final String KEY_PROJECT_NAME = "project_name";

    //Columns marker list
    //private static final String KEY_ID = "id";
    //private static final String KEY_PROJECT_NAME = "project_name";
    private static final String KEY_MARKER_INDEX = "marker_index" ;
    private static final String KEY_MARKER_LATITUDE = "latitude";
    private static final String KEY_MARKER_LONGITUDE = "longitude";
    private static final String KEY_CHALLENGE_TEXT = "challenge";
    private static final String KEY_CHALLENGE_PASSWORD = "challenge_password";

    //Columns challenge
    //private static final String KEY_ID = "id";
    private static final String KEY_CHALLENGE_NAME = "challenge_name";
    //private static final String KEY_CHALLENGE_TEXT = "challenge";
    //private static final String KEY_CHALLENGE_PASSWORD = "challenge_password";

    //Columns users
    //private static final String KEY_ID = "id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PASSWORD = "user_password";


    //String create table PROJECT_NAME
    private static final String CREATE_PROJECT_NAME_TABLE =
            "CREATE TABLE "+TABLE_PROJECT_NAME + "("
                    + KEY_ID + " INTEGER PRIMARY KEY,"
                    + KEY_PROJECT_NAME + " TEXT NOT NULL " + ")";

    // String create table MARKERS_LIST
    private static final String CREATE_MARKERS_LIST_TABLE =
            "CREATE TABLE " + TABLE_MARKERS_LIST + "("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_PROJECT_NAME + " TEXT,"
                    + KEY_MARKER_INDEX + " INTEGER,"
                    + KEY_MARKER_LATITUDE + " REAL,"
                    + KEY_MARKER_LONGITUDE + " REAL,"
                    + KEY_CHALLENGE_TEXT + " TEXT,"
                    + KEY_CHALLENGE_PASSWORD + " TEXT,"
                    + KEY_CHALLENGE_NAME + " TEXT" + ")";

    //String create table CHALLENGE
    private static final String CREATE_CHALLENGE_TABLE =
            "CREATE TABLE " + TABLE_CHALLENGE + "("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_CHALLENGE_NAME + " TEXT,"
                    + KEY_CHALLENGE_TEXT + " TEXT,"
                    + KEY_CHALLENGE_PASSWORD + " TEXT" + ")";

    //String create table USER
    private static final String CREATE_USER_TABLE =
            "CREATE TABLE " + TABLE_USER + "("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_USER_EMAIL + " TEXT,"
                    + KEY_USER_PASSWORD + " TEXT" + ")";

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PROJECT_NAME_TABLE);
        db.execSQL(CREATE_MARKERS_LIST_TABLE);
        db.execSQL(CREATE_CHALLENGE_TABLE);
        db.execSQL(CREATE_USER_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECT_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKERS_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHALLENGE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    public void addNewProjectName(ProjectName newProjectName){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PROJECT_NAME, newProjectName.get_projectName());

        db.insert(TABLE_PROJECT_NAME, null, values);
        db.close();
    }
    public void createUser(UserPOJO userEmail, UserPOJO userPassword){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_EMAIL, userEmail.getEmail());
        values.put(KEY_USER_PASSWORD, userPassword.getUserPassword());

        db.insert(TABLE_USER, null, values);
        db.close();
    }

    public void addMarkersToTable(MarkerPOJO projectName,
                                  MarkerPOJO markerIndex,
                                  MarkerPOJO markerLatitude,
                                  MarkerPOJO markerLongitude){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PROJECT_NAME, projectName.get_project_name());
        values.put(KEY_MARKER_INDEX, markerIndex.get_marker_index());
        values.put(KEY_MARKER_LATITUDE, markerLatitude.get_marker_latitude());
        values.put(KEY_MARKER_LONGITUDE, markerLongitude.get_marker_longitude());

        db.insert(TABLE_MARKERS_LIST, null, values);
        db.close();
    }
    public void addChallengeToMarkerList(String projectName,
                                         String markerIndex,
                                         MarkerPOJO challenge,
                                         MarkerPOJO challengePassword,
                                         MarkerPOJO challengeName){

        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = KEY_PROJECT_NAME + " = ? and " + KEY_MARKER_INDEX + " = ? ";
        String[] whereArgs = {projectName, markerIndex};

        ContentValues values = new ContentValues();
        values.put(KEY_CHALLENGE_TEXT, challenge.get_challenge());
        values.put(KEY_CHALLENGE_PASSWORD, challengePassword.get_challenge_password());
        values.put(KEY_CHALLENGE_NAME, challengeName.get_challenge_name());

        db.update(TABLE_MARKERS_LIST, values, whereClause, whereArgs);
    }

    public void createNewChallenge(ChallengePOJO challengeName,
                                ChallengePOJO challenge,
                                ChallengePOJO challenge_password){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CHALLENGE_NAME, challengeName.get_challenge_name());
        values.put(KEY_CHALLENGE_TEXT, challenge.get_challenge());
        values.put(KEY_CHALLENGE_PASSWORD, challenge_password.get_challenge_password());

        db.insert(TABLE_CHALLENGE, null, values);
        db.close();
    }

    public int deleteProjectName(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] whereArgs = {name};
        int count =  db.delete(TABLE_PROJECT_NAME, KEY_PROJECT_NAME + " = ?", whereArgs);
        db.close();
        return count;
    }

    public int deleteMarkersFromList(String projectName){
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = KEY_PROJECT_NAME + " = ? ";
        String[] whereArgs = {projectName};
        int count = db.delete(TABLE_MARKERS_LIST, whereClause, whereArgs);
        db.close();
        return count;
    }
    public int deleteSingleMarker(String projectName, String markerIndex){
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = KEY_PROJECT_NAME + " = ? and " + KEY_MARKER_INDEX + " = ? ";
        String[] whereArgs ={projectName, markerIndex};
        int count = db.delete(TABLE_MARKERS_LIST, whereClause, whereArgs);
        db.close();
        return count;
    }

    public int deleteChallenge(String challengeName){
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = KEY_CHALLENGE_NAME + " = ? ";
        String[] whereArgs = {challengeName};
        int count = db.delete(TABLE_CHALLENGE, whereClause, whereArgs);
        db.close();
        return count;
    }

    public void deleteSingleChallengeFromMarkerList(String projectName, String markerIndex,
                                                    MarkerPOJO challenge,
                                                    MarkerPOJO challengePassword,
                                                    MarkerPOJO challengeName){
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = KEY_PROJECT_NAME + " = ? and " + KEY_MARKER_INDEX + " = ? ";
        String[] whereArgs = {projectName, markerIndex};

        ContentValues values = new ContentValues();
        values.put(KEY_CHALLENGE_TEXT, challenge.get_challenge());
        values.put(KEY_CHALLENGE_PASSWORD, challengePassword.get_challenge_password());
        values.put(KEY_CHALLENGE_NAME, challengeName.get_challenge_name());
        db.update(TABLE_MARKERS_LIST, values, whereClause, whereArgs);
        db.close();
    }
    public void updateUser(String id, UserPOJO userEmail, UserPOJO userPassword){
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = KEY_ID + " = ? ";
        String[] whereArgs = {id};
        ContentValues values = new ContentValues();
        values.put(KEY_USER_EMAIL, userEmail.getEmail());
        values.put(KEY_USER_PASSWORD, userPassword.getUserPassword());
        db.update(TABLE_USER, values, whereClause, whereArgs);
        db.close();
    }

    public void updateMarkerIndex(String projectName, String id, MarkerPOJO markerIndex){
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = KEY_PROJECT_NAME + " = ? and " + KEY_ID + " = ? ";
        String[] whereArgs = {projectName, id};

        ContentValues values = new ContentValues();
        values.put(KEY_MARKER_INDEX, markerIndex.get_marker_index());
        db.update(TABLE_MARKERS_LIST, values, whereClause, whereArgs);
        db.close();
    }
    public void updateProjectMarkerList(String projectName, String markerIndexString,
                                        MarkerPOJO markerLatitude,
                                        MarkerPOJO markerLongitude){
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = KEY_PROJECT_NAME + " = ? and " + KEY_MARKER_INDEX + " = ? ";
        String[] whereArgs = {projectName, markerIndexString};

        ContentValues values = new ContentValues();
        values.put(KEY_MARKER_LATITUDE, markerLatitude.get_marker_latitude());
        values.put(KEY_MARKER_LONGITUDE, markerLongitude.get_marker_longitude());
        db.update(TABLE_MARKERS_LIST, values, whereClause, whereArgs);
        db.close();
    }
    public void updateChallenge(String challengeName,
                                ChallengePOJO challenge,
                                ChallengePOJO challengePassword){

        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = KEY_CHALLENGE_NAME + " = ? ";
        String[] whereArgs = {challengeName};

        ContentValues values = new ContentValues();

        values.put(KEY_CHALLENGE_TEXT, challenge.get_challenge());
        values.put(KEY_CHALLENGE_PASSWORD , challengePassword.get_challenge_password());
        db.update(TABLE_CHALLENGE, values, whereClause, whereArgs);
        db.close();
    }
    public boolean checkUserExists(String id){
        SQLiteDatabase db = getReadableDatabase();
        String[] columns ={KEY_ID, KEY_USER_EMAIL};
        String selection = KEY_ID + " = ? and " + KEY_USER_EMAIL + " is not NULL ";
        String[] selectionArgs = {id};
        String limit = "1";
        Cursor cursor = db.query(TABLE_USER, columns, selection, selectionArgs,
                null, null, null, limit);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public boolean checkMarkerExists(String projectName, String markerIndex){
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {KEY_PROJECT_NAME, KEY_MARKER_INDEX};
        String selection = KEY_PROJECT_NAME + " = ? and " + KEY_MARKER_INDEX + " = ? ";
        String[] selectionArgs = {projectName, markerIndex};
        String limit = "1";

        Cursor cursor = db.query(TABLE_MARKERS_LIST, columns, selection, selectionArgs,
                null, null, null, limit);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public boolean checkMarkerHaveChallenge(String projectName, String markerIndex){
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {KEY_PROJECT_NAME, KEY_MARKER_INDEX, KEY_CHALLENGE_TEXT};
        String selection = KEY_PROJECT_NAME + " = ? and "
                + KEY_MARKER_INDEX + " = ? and "
                + KEY_CHALLENGE_TEXT + " is not NULL ";
        String[] selectionArgs = {projectName, markerIndex};
        String limit = "1";
        Cursor cursor = db.query(TABLE_MARKERS_LIST, columns, selection, selectionArgs,
                 null, null, null, limit);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }
    public boolean checkChallengeExists(String challengeName){
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {KEY_CHALLENGE_NAME};
        String selection = KEY_CHALLENGE_NAME + " = ? ";
        String[] selectionArgs = {challengeName};
        String limit = "1";

        Cursor cursor = db.query(TABLE_CHALLENGE, columns, selection, selectionArgs,
                null, null, null, limit);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public List<ProjectName> getAllProjectName(){

        List<ProjectName> projectNameList = new ArrayList<ProjectName>();

        String selectQuery = "SELECT * FROM " + TABLE_PROJECT_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                ProjectName projectName = new ProjectName();
                projectName.set_id(Integer.parseInt(cursor.getString(0)));
                projectName.set_projectName(cursor.getString(1));

                projectNameList.add(projectName);

            }while ((cursor.moveToNext()));
        }
        return projectNameList;
    }
    public List<MarkerPOJO> getAllMarkers(String projectName){

        List<MarkerPOJO> markerPOJOList = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_MARKERS_LIST + " WHERE "
                + KEY_PROJECT_NAME + "='" + projectName + "'";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                MarkerPOJO markerPOJO = new MarkerPOJO();
                markerPOJO.set_id(cursor.getInt(0));
                markerPOJO.set_project_name(cursor.getString(1));
                markerPOJO.set_marker_index(cursor.getInt(2));
                markerPOJO.set_marker_latitude(cursor.getDouble(3));
                markerPOJO.set_marker_longitude(cursor.getDouble(4));
                markerPOJO.set_challenge(cursor.getString(5));
                markerPOJO.set_challenge_password(cursor.getString(6));
                markerPOJO.set_challenge_name(cursor.getString(7));

                markerPOJOList.add(markerPOJO);

            }while((cursor.moveToNext()));
        }
        return markerPOJOList;
    }
    public List<MarkerPOJO> getChallengeDataFromMarkerTable(String projectName){

        List<MarkerPOJO> markerPOJOList = new ArrayList<>();
        SQLiteDatabase db  = getWritableDatabase();
        String selectQuery = " SELECT * FROM " + TABLE_MARKERS_LIST + " WHERE "
                + KEY_PROJECT_NAME + "='" + projectName + "'" + " and "
                + KEY_CHALLENGE_TEXT + " is not NULL ";

        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            do{
                MarkerPOJO markerPOJO = new MarkerPOJO();
                markerPOJO.set_marker_index(cursor.getInt(2));
                markerPOJO.set_marker_latitude(cursor.getDouble(3));
                markerPOJO.set_marker_longitude(cursor.getDouble(4));
                markerPOJO.set_challenge(cursor.getString(5));
                markerPOJO.set_challenge_password(cursor.getString(6));
                markerPOJO.set_challenge_name(cursor.getString(7));

                markerPOJOList.add(markerPOJO);

            }while(cursor.moveToNext());
        }
        return markerPOJOList;
    }
    public UserPOJO getUserDatabase(String id){
        UserPOJO userPOJO = new UserPOJO();
        SQLiteDatabase db = getReadableDatabase();
        String selectQuery = " SELECT * FROM " + TABLE_USER + " WHERE "
                + KEY_ID + "='" + id + "'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            do{
                userPOJO = new UserPOJO();
                userPOJO.setEmail(cursor.getString(1));
                userPOJO.setUserPassword(cursor.getString(2));
            }while(cursor.moveToNext());
        }
        return userPOJO;
    }

    public MarkerPOJO getSingleMarkerData(String projectName, String markerIndex){
        MarkerPOJO markerPOJO = new MarkerPOJO();
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = " SELECT * FROM " + TABLE_MARKERS_LIST + " WHERE "
                + KEY_PROJECT_NAME + "='" + projectName + "'" + " and "
                + KEY_MARKER_INDEX + "='" + markerIndex + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            do{
                markerPOJO = new MarkerPOJO();
                markerPOJO.set_marker_index(cursor.getInt(2));
                markerPOJO.set_marker_latitude(cursor.getDouble(3));
                markerPOJO.set_marker_longitude(cursor.getDouble(4));
                markerPOJO.set_challenge(cursor.getString(5));
                markerPOJO.set_challenge_password(cursor.getString(6));
                markerPOJO.set_challenge_name(cursor.getString(7));
            }while(cursor.moveToNext());
        }
        return markerPOJO;
    }

    public MarkerPOJO getSingleChallengeFromMarkerTable(String projectName,
                                                        String markerIndex){

        MarkerPOJO markerPOJO = new MarkerPOJO();
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = " SELECT * FROM " + TABLE_MARKERS_LIST + " WHERE "
                + KEY_PROJECT_NAME + "='" + projectName + "'" + " and "
                + KEY_MARKER_INDEX + "='" + markerIndex + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            do{
                markerPOJO = new MarkerPOJO();
                markerPOJO.set_challenge(cursor.getString(5));
                markerPOJO.set_challenge_password(cursor.getString(6));
                markerPOJO.set_challenge_name(cursor.getString(7));
            }while(cursor.moveToNext());
        }
        return markerPOJO;
    }

    public ChallengePOJO getChallengeFromDatabase(String challengeName){

        ChallengePOJO challengePOJO = new ChallengePOJO();
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = " SELECT * FROM " + TABLE_CHALLENGE + " WHERE "
                + KEY_CHALLENGE_NAME + "='" + challengeName + "'" ;

        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            do{
                challengePOJO = new ChallengePOJO();
                challengePOJO.set_challenge_name(cursor.getString(1));
                challengePOJO.set_challenge(cursor.getString(2));
                challengePOJO.set_challenge_password(cursor.getString(3));
            }while(cursor.moveToNext());
        }
        return challengePOJO;
    }
    public List<ChallengePOJO> getChallengeList(){

        List<ChallengePOJO> challengePOJOList = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();

        String selectQuery = " SELECT * FROM " + TABLE_CHALLENGE;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            do{
                ChallengePOJO challengePOJO = new ChallengePOJO();
                challengePOJO.set_id(cursor.getInt(0));
                challengePOJO.set_challenge_name(cursor.getString(1));
                challengePOJO.set_challenge(cursor.getString(2));
                challengePOJO.set_challenge_password(cursor.getString(3));

                challengePOJOList.add(challengePOJO);

            }while((cursor.moveToNext()));
        }
        return challengePOJOList;
    }
    public List<MarkerPOJO> getAllforAll(){
        List<MarkerPOJO> markerPOJOList = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_MARKERS_LIST;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                MarkerPOJO markerPOJO = new MarkerPOJO();
                markerPOJO.set_id(cursor.getInt(0));
                markerPOJO.set_project_name(cursor.getString(1));
                markerPOJO.set_marker_index(cursor.getInt(2));
                markerPOJO.set_marker_latitude(cursor.getDouble(3));
                markerPOJO.set_marker_longitude(cursor.getDouble(4));

                markerPOJOList.add(markerPOJO);

            }while((cursor.moveToNext()));
        }
        return markerPOJOList;
    }
}
