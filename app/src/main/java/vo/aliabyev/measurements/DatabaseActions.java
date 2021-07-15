package vo.aliabyev.measurements;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Date;

//TODO в будущем можно предусмотреть добавление собственного типа параметров на основе существующих(както)
//TODO просмотр, ручное удаление всех элементов таблицы(?)
//TODO Перевод в капс всех строк, чтобы уменьшить число дупликатов

//класс действий с бд
public class DatabaseActions {

    public static boolean CreateTables(SQLiteDatabase db){
        try {
            //Таблицы главного блока
            //Создание таблицы физических процессов
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS Physical_process (" +
                            "'id_Php' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                            "'name' VARCHAR(100) NOT NULL);"
            );


            //Создание таблицы оборудования
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS Type_of_power_equipment (" +
                            "'id_Tpe' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                            "'name' VARCHAR(100) NOT NULL);"
            );

            //Создание таблицы оборудования + физических процессов
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS 'Php_Tpe' (" +
                            "'id_Php' INT NOT NULL, " +
                            "'id_Tpe' INT NOT NULL, " +
                            "'id_Php_Tpe' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                            "FOREIGN KEY ('id_Php') REFERENCES Physical_process ('id_Php')" +
                            "ON UPDATE CASCADE " +
                            "ON DELETE CASCADE," +
                            "FOREIGN KEY ('id_Tpe') REFERENCES Type_of_power_equipment ('id_Tpe') " +
                            "ON UPDATE CASCADE " +
                            "ON DELETE CASCADE" +
                            ")"
            );

            //Создание таблицы областей
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS Area (" +
                            "'id_area' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                            "'name_area' VARCHAR(100) NOT NULL);"
            );

            //Создание таблицы подобластей
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS Areas_tree (" +
                            "'id_area' INT NOT NULL," +
                            "'id_subarea' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                            "'name_subarea' VARCHAR(100) NOT NULL," +
                            "'id_parent_area' INT," +
                            "FOREIGN KEY ('id_area') " +
                            "REFERENCES Area ('id_area') " +
                            "ON UPDATE CASCADE " +
                            "ON DELETE CASCADE," +
                            "FOREIGN KEY ('id_parent_area') " +
                            "REFERENCES Areas_tree ('id_subarea') " +
                            "ON UPDATE CASCADE " +
                            "ON DELETE CASCADE" +
                            ");"
            );

            //внесение корня дерева подобластей (id=1)
            AddArea(db, "root");
            AddSubArea(db, "root", 1, 1);

            //Создание таблицы типа эксперимента
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS Experiment_class (" +
                            "'id_Php_Tpe' INT NOT NULL," +
                            "'id_subarea' INT NOT NULL," +
                            "'ID' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                            "'Main_pict' VARCHAR(100)," +
                            "'Geom_pict' VARCHAR(100)," +
                            "'Reg_pict' VARCHAR(100)," +
                            "'Tepl_pict' VARCHAR(100)," +
                            "FOREIGN KEY ('id_Php_Tpe') " +
                            "REFERENCES 'Php_Tpe' ('id_Php_Tpe') " +
                            "ON UPDATE CASCADE " +
                            "ON DELETE CASCADE," +
                            "FOREIGN KEY ('id_subarea') " +
                            "REFERENCES Areas_tree ('id_subarea') " +
                            "ON UPDATE CASCADE " +
                            "ON DELETE CASCADE" +
                            ");"
            );

            //Создание таблицы типа параметров
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS Type_of_parameters (" +
                            "'id_type' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                            "'name' VARCHAR(100) NOT NULL);"
            );

            //Внесение предусмотренных типов параметров
            AddParamType(db, "Численный"); //id=1
            AddParamType(db, "Строковый"); //id=2
            AddParamType(db, "Диапазон"); //id=3
            AddParamType(db, "Изображение"); //id=4

            //Создание таблицы типа данных
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS Data_type (" +
                            "'id_data' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                            "'name_data' VARCHAR(50) NOT NULL);"
            );

            //Создание таблицы параметров
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS Parameters (" +
                            "'id_type' INT NOT NULL," +
                            "'id_param' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                            "'name_param' VARCHAR(100) NOT NULL," +
                            "'short_name_param' VARCHAR(100)," +
                            "'unit_param' VARCHAR(100)," +
                            "FOREIGN KEY ('id_type')" +
                            "REFERENCES Type_of_parameters ('id_type') " +
                            "ON UPDATE CASCADE " +
                            "ON DELETE CASCADE" +
                            ");"
            );

            /*
            //Создание таблицы строковых величин
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS String_values (" +
                            "'id_param' INT NOT NULL," +
                            "'value' VARCHAR(100) NOT NULL," +
                            "PRIMARY KEY ('id_param', 'value')," +
                            "FOREIGN KEY ('id_param')" +
                            "REFERENCES Parameters (id_param)" +
                            "ON UPDATE CASCADE " +
                            "ON DELETE CASCADE" +
                            ");"
            );
            */
            //Создание таблицы эксперимента
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS Experiment (" +
                            "'ID' INT NOT NULL," +
                            "'id_exp' INT NOT NULL," +
                            "'id_param' INT NOT NULL," +
                            "'id_value' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                            "'id_data' INT NOT NULL," +
                            "FOREIGN KEY ('id_data')" +
                            "REFERENCES Data_type ('id_data')" +
                            "ON UPDATE CASCADE " +
                            "ON DELETE CASCADE," +
                            "FOREIGN KEY ('ID')" +
                            "REFERENCES Experiment_class ('ID')" +
                            "ON UPDATE CASCADE " +
                            "ON DELETE CASCADE," +
                            "FOREIGN KEY ('id_param')" +
                            "REFERENCES Parameters (id_param)" +
                            "ON UPDATE CASCADE " +
                            "ON DELETE CASCADE" +
                            ");"
            );

            //Создание таблицы значений параметров
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS Parameters_values (" +
                            "'id_value' INT NOT NULL PRIMARY KEY," +
                            "'value_number' DECIMAL(14, 7)," +
                            "'value_range1' DECIMAL(14, 7)," +
                            "'value_range2' DECIMAL(14, 7)," +
                            "'value_image' VARCHAR(100)," +
                            "'value_string' VARCHAR(100)," +
                            "'date' DATETIME DEFAULT CURRENT_DATE," +
                            "FOREIGN KEY ('id_value')" +
                            "REFERENCES Experiment ('id_value') " +
                            "ON UPDATE CASCADE " +
                            "ON DELETE CASCADE" +
                            ");"
            );

            /*
            //Создание таблицы пользователей
            db.execSQL("CREATE TABLE IF NOT EXISTS Users(" +
                    "'Login' VARCHAR(100) NOT NULL PRIMARY KEY," +
                    "'Password' VARCHAR(50) NOT NULL," +
                    "'Level' TINYINT NOT NULL" +
                    ");"
            );

            //Создание таблицы логов
            db.execSQL("CREATE TABLE IF NOT EXISTS Log_Table(" +
                    "'user' VARCHAR(100) NOT NULL," +
                    "'action' VARCHAR(100) NOT NULL PRIMARY KEY," +
                    "'date' DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "'Level' TINYINT NOT NULL" +
                    ");"
            );

            //Таблицы блока FCA
            //Создание таблицы конфигурации софта
            db.execSQL("CREATE TABLE IF NOT EXISTS 'Software_configuration'(" +
                    "'id_soft' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "'name' VARCHAR(100) NOT NULL," +
                    "'grid_gen' VARCHAR(100) NOT NULL," +
                    "'type_license' VARCHAR(100) NOT NULL," +
                    "'license_term' VARCHAR(100) NOT NULL," +
                    "'workplaces' INT NOT NULL," +
                    "'price' INT NOT NULL" +
                    ");"
            );

            //Создание таблицы конфигурации компьютера
            db.execSQL("CREATE TABLE IF NOT EXISTS 'PC_configuration'(" +
                    "'id_PC' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "'proc_name' VARCHAR(100) NOT NULL," +
                    "'proc_freq' DECIMAL NOT NULL," +
                    "'proc_arch' VARCHAR(100) NOT NULL," +
                    "'amount_RAM' DECIMAL NOT NULL," +
                    "'type_RAM' VARCHAR(100) NOT NULL," +
                    "'sys_cap' INT NOT NULL," +
                    "'GPU' VARCHAR(100) NOT NULL," +
                    "'price' INT NOT NULL" +
                    ");"
            );

            //Создание таблицы уникальных экспериментов
            db.execSQL("CREATE TABLE IF NOT EXISTS Unic_Exp(" +
                    "'ID' INT NOT NULL," +
                    "'id_exp' INT NOT NULL," +
                    "'EXP' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "FOREIGN KEY ('ID')" +
                    "REFERENCES Experiment_class ('ID')" +
                    "ON UPDATE CASCADE " +
                    "ON DELETE CASCADE" +
                    ");"
            );

            //Создание таблицы FCA
            db.execSQL("CREATE TABLE IF NOT EXISTS FCA(" +
                    "'EXP' INT NOT NULL," +
                    "'id_PC' INT NOT NULL," +
                    "'grid_time' VARCHAR(100) NOT NULL," +
                    "'calc_time' VARCHAR(100) NOT NULL," +
                    "PRIMARY KEY('EXP', 'id_PC')," +
                    "FOREIGN KEY ('id_PC')" +
                    "REFERENCES 'PC_Configuration' ('id_PC')" +
                    "ON UPDATE CASCADE " +
                    "ON DELETE CASCADE," +
                    "FOREIGN KEY ('EXP')" +
                    "REFERENCES Unic_Exp ('EXP')" +
                    "ON UPDATE CASCADE " +
                    "ON DELETE CASCADE" +
                    ");"
            );
            */

            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    //----------------------------------------
    //Процедуры добавления элементов в таблиц
    //----------------------------------------

    //Добавить область
    public static boolean AddArea(SQLiteDatabase db,String name){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("name_area", name);
            long values = db.insertOrThrow("Area", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            return false;
        }
    }
    //Добавить подобласть
    public static boolean AddSubArea(SQLiteDatabase db,String name, int id_area, int id_parentArea){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("id_area", id_area);
            contentValues.put("name_subarea", name);
            contentValues.put("id_parent_area", id_parentArea);
            long values = db.insertOrThrow("Areas_tree", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            return false;
        }
    }
    //Добавить оборудование
    public static boolean AddPowEquipment(SQLiteDatabase db,String name){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("name", name);
            long values = db.insertOrThrow("Type_of_power_equipment", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            return false;
        }
    }
    //Добавить физический процесс
    public static boolean AddPhProcess(SQLiteDatabase db,String name){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("name", name);
            long values = db.insertOrThrow("Physical_process", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            return false;
        }
    }
    //Добавить физпроцесс + оборудование
    public static boolean AddPhProcessAndPowEquipment(SQLiteDatabase db,int id_PhProc, int id_PowEq){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("id_Php", id_PhProc);
            contentValues.put("id_Tpe", id_PowEq);
            long values = db.insertOrThrow("Php_Tpe", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            return false;
        }
    }
    //Добавить тип эксперимента
    public static boolean AddExpClass(SQLiteDatabase db,int idPhPow, int id_subarea, String Main_pict, String Geom_pict, String Reg_pict, String Tepl_pict){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("id_Php_Tpe", idPhPow);
            contentValues.put("id_subarea", id_subarea);
            contentValues.put("Main_pict", Main_pict);
            contentValues.put("Geom_pict", Geom_pict);
            contentValues.put("Reg_pict", Reg_pict);
            contentValues.put("Tepl_pict", Tepl_pict);
            long values = db.insertOrThrow("Experiment_class", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            return false;
        }
    }
    //Добавить тип параметров
    public static boolean AddParamType(SQLiteDatabase db,String name){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("name", name);
            long values = db.insertOrThrow("Type_of_parameters", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            return false;
        }
    }
    //Добавить тип данных
    public static boolean AddDataType(SQLiteDatabase db,String name){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("name_data", name);
            long values = db.insertOrThrow("Data_type", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            return false;
        }
    }
    //Добавить параметр
    public static boolean AddParameter(SQLiteDatabase db,int id_type, String name, String short_name, String unit){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("id_type", id_type);
            contentValues.put("name_param", name);
            contentValues.put("short_name_param", short_name);
            contentValues.put("unit_param", unit);
            long values = db.insertOrThrow("Parameters", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            return false;
        }
    }
    //Добавить эксперимент
    public static boolean AddExperiment(SQLiteDatabase db,int ID, int id_exp, int id_param, int id_data){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("ID", ID);
            contentValues.put("id_exp", id_exp);
            contentValues.put("id_param", id_param);
            contentValues.put("id_data", id_data);
            long values = db.insertOrThrow("Experiment", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            return false;
        }
    }
    //Добавить значение параметра
    //численное
    public static boolean AddParValue(SQLiteDatabase db, int id_value, float value_number){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("id_value", id_value);
            contentValues.put("value_number", value_number);
            long values = db.insertOrThrow("Parameters_values", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            return false;
        }
    }
    //строковое
    public static boolean AddParValue(SQLiteDatabase db,int id_value, String value_string){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("id_value", id_value);
            contentValues.put("value_string", value_string);
            long values = db.insertOrThrow("Parameters_values", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            return false;
        }
    }
    //диапазон
    public static boolean AddParValue(SQLiteDatabase db, int id_value, float value1, float value2){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("id_value", id_value);
            contentValues.put("value_range1", value1);
            contentValues.put("value_range2", value2);
            long values = db.insertOrThrow("Parameters_values", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            return false;
        }
    }
    //изображение
    public static boolean AddParValue(SQLiteDatabase db,int id_value, String value_image, String imageTag){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("id_value", id_value);
            contentValues.put("value_image", value_image);
            long values = db.insertOrThrow("Parameters_values", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            return false;
        }
    }
    //общее
    public static boolean AddParValue(SQLiteDatabase db,int id_value, String value_image, String value_string, float value_range1,
                                      float value_range2, float value_number){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("id_value", id_value);
            contentValues.put("value_image", value_image);
            contentValues.put("value_string", value_string);
            contentValues.put("value_range1", value_range1);
            contentValues.put("value_range2", value_range2);
            contentValues.put("value_number", value_number);
            long values = db.insertOrThrow("Parameters_values", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            Log.d("ОШИБКА:", "величина не добавлена: " + e.toString() );
            return false;
        }
    }

/*

//Добавить строковую величину
    public static boolean AddStringValue(SQLiteDatabase db,int id_param, String value){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("id_param", id_param);
            contentValues.put("value", value);
            long values = db.insertOrThrow("String_values", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            return false;
        }
    }

    //Добавить пользователя
    public static boolean AddUser(SQLiteDatabase db,String login, String password, int level ){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("Login", login);
            contentValues.put("Password", password);
            contentValues.put("Level", level);
            long values = db.insertOrThrow("Users", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            return false;
        }
    }

    //Добавить запись в лог
    public static boolean AddLog(SQLiteDatabase db,String userName, String action, int level ){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("user", userName);
            contentValues.put("action", action);
            contentValues.put("Level", level);
            long values = db.insertOrThrow("Log_Table", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            return false;
        }
    }

    //Добавить конфигурацию софта
    public static boolean AddSoftConf(SQLiteDatabase db,String name, String grid_gen, String type_license, String license_term, int workplace, int price ){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("name", name);
            contentValues.put("grid_gen", grid_gen);
            contentValues.put("type_license", type_license);
            contentValues.put("license_term", license_term);
            contentValues.put("workplaces", workplace);
            contentValues.put("price", price);
            long values = db.insertOrThrow("Software_configuration", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            return false;
        }
    }

    //Добавить конфигурацию ПК
    public static boolean AddPCConf(SQLiteDatabase db, String proc_name, float proc_freq, String proc_arch, float amount_RAM,
                             String type_RAM, int sys_cap, String GPU, int price ){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("proc_name", proc_name);
            contentValues.put("proc_freq", proc_freq);
            contentValues.put("proc_arch", proc_arch);
            contentValues.put("amount_RAM", amount_RAM);
            contentValues.put("type_RAM", type_RAM);
            contentValues.put("sys_cap", sys_cap);
            contentValues.put("GPU", GPU);
            contentValues.put("price", price);
            long values = db.insertOrThrow("PC_configuration", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            return false;
        }
    }

    //Добавить Уникальный эксперимент
    public static boolean AddUnicExp(SQLiteDatabase db,int ID, int id_exp){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("ID", ID);
            contentValues.put("id_exp", id_exp);
            long values = db.insertOrThrow("Unic_Exp", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            return false;
        }
    }

    //Добавить FCA
    public static boolean AddFCA(SQLiteDatabase db, int EXP, int id_PC, String grid_time, String calc_time){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("EXP", EXP);
            contentValues.put("id_PC", id_PC);
            contentValues.put("grid_time", grid_time);
            contentValues.put("calc_time", calc_time);
            long values = db.insertOrThrow("FCA", null, contentValues);
            return values > 1;
        } catch (SQLiteException e) {
            return false;
        }
    }
*/
    //Другие процедуры

    //--добавление измерения в бд--//
    public static boolean AddFullExperiment(SQLiteDatabase db, Experiment experimentInfo, ArrayList<Parameter> parameters){

        int areaID;
        int parentsubareaID;
        int subareaID;
        int equipmentID;
        int physicalPrID;
        int equipAndPhysID;
        int expClassID;
        int dataTypeID;
        int experimentID;

        //проверка, созданы ли таблицы - обращение к любой таблице
        try{
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM Area;",null);
            cursor.moveToFirst();
            cursor.close();
        }catch (SQLiteException e){
            //создание таблиц
            try{
                CreateTables(db);
            }catch (SQLiteException e1){
                db.close();
                return false;
            }
        }

        //проверка, существует ли область
        //если да, то получить id
        //если нет, то добавить и получить id
        try{
            Cursor cursor = db.rawQuery("SELECT * FROM Area WHERE name_area = '"+ experimentInfo.getArea() +"';",null);
                if(!cursor.moveToFirst()){
                    AddArea(db, experimentInfo.getArea());
                    cursor = db.rawQuery(
                            "SELECT * FROM Area WHERE name_area = '"+ experimentInfo.getArea() +"';",null);
                    cursor.moveToFirst();
                }
                areaID = cursor.getInt(cursor.getColumnIndex("id_area"));
            cursor.close();
        }catch (SQLiteException e){
            db.close();
            return false;
        }

        //то же самое для физ процесса
        try{
            Cursor cursor = db.rawQuery("SELECT * FROM Physical_process WHERE name = '"+ experimentInfo.getPhysicalProcess() +"';",null);
            if(!cursor.moveToFirst()){
                AddPhProcess(db, experimentInfo.getPhysicalProcess());
                cursor = db.rawQuery("SELECT * FROM Physical_process WHERE name = '"+ experimentInfo.getPhysicalProcess() +"';",null);
                cursor.moveToFirst();
            }
            physicalPrID = cursor.getInt(cursor.getColumnIndex("id_Php"));
            cursor.close();
        }catch (SQLiteException e){
            db.close();
            return false;
        }

        //то же самое для оборудования
        try{
            Cursor cursor = db.rawQuery("SELECT * FROM Type_of_power_equipment WHERE name = '"+ experimentInfo.getPowerEquipment() +"';",null);
            if(!cursor.moveToFirst()){
                AddPowEquipment(db, experimentInfo.getPowerEquipment());
                cursor = db.rawQuery("SELECT * FROM Type_of_power_equipment WHERE name = '"+ experimentInfo.getPowerEquipment() +"';",null);
                cursor.moveToFirst();
            }
            equipmentID = cursor.getInt(cursor.getColumnIndex("id_Tpe"));
            cursor.close();
        }catch (SQLiteException e){
            db.close();
            return false;
        }

        //проверка существует ли физ+обор с такими id
        //если да, то получить id
        //eсли нет, то добавить и получить id
        try{
            Cursor cursor = db.rawQuery("SELECT * FROM Php_Tpe WHERE id_Php = "+ physicalPrID +" AND id_Tpe = "+ equipmentID +" ;",null);
            if(!cursor.moveToFirst()){
                AddPhProcessAndPowEquipment(db, physicalPrID, equipmentID);
                cursor = db.rawQuery("SELECT * FROM Php_Tpe WHERE id_Php = "+ physicalPrID +" AND id_Tpe = "+ equipmentID +" ;",null);
                cursor.moveToFirst();
            }
            equipAndPhysID = cursor.getInt(cursor.getColumnIndex("id_Php_Tpe"));
            cursor.close();
        }catch (SQLiteException e){
            db.close();
            return false;
        }

        //проверка, существует ли родительская подобласть в такой области (при ее необходимости)
        //если да, то получить id
        //если нет - добавить, установить родительскую для нее как корневую, получить id
        if(!experimentInfo.getParentSubArea().equals("")){
            //родительская есть в эксперименте
            try{
                Cursor cursor = db.rawQuery("SELECT * FROM Areas_tree WHERE id_area ="+ areaID +" AND name_subarea='"+ experimentInfo.getParentSubArea() +"';",null);
                if(!cursor.moveToFirst()){
                    AddSubArea(db, experimentInfo.getParentSubArea(), areaID, 1);
                    cursor = db.rawQuery("SELECT * FROM Areas_tree WHERE id_area ="+ areaID +" AND name_subarea='"+ experimentInfo.getParentSubArea() +"';",null);
                    cursor.moveToFirst();
                }
                parentsubareaID = cursor.getInt(cursor.getColumnIndex("id_subarea"));
                cursor.close();
            }catch (SQLiteException e){
                db.close();
                return false;
            }
        }else{
            parentsubareaID = 1;
        }

        //проверка существует ли подобласть в такой родительской подобласти
        //если да, то получить id
        //если нет, то добавить, установив родительску, если указана и установив корневую, если не указана, получить id
        try{
            Cursor cursor = db.rawQuery("SELECT * FROM Areas_tree WHERE"+
                    " name_subarea='"+ experimentInfo.getSubArea() +"'" +
                    " AND id_parent_area = "+ parentsubareaID +";",null);

            if(!cursor.moveToFirst()){
                AddSubArea(db, experimentInfo.getSubArea(), areaID, parentsubareaID);
                cursor = db.rawQuery("SELECT * FROM Areas_tree WHERE"+
                        " name_subarea='"+ experimentInfo.getSubArea() +"'" +
                        " AND id_parent_area = "+ parentsubareaID +";",null);
                cursor.moveToFirst();
            }
            subareaID = cursor.getInt(cursor.getColumnIndex("id_subarea"));
            cursor.close();
        }catch (SQLiteException e){
            db.close();
            return false;
        }

        //проверить, существует ли такой класс эксперимента со всеми этими id
        //если да, то получить id
        //если нет, то добавить, получить id
        try{
            Cursor cursor = db.rawQuery("SELECT * FROM Experiment_class WHERE " +
                    "id_Php_Tpe = " + equipAndPhysID + " AND " +
                    "id_subarea = " + subareaID + ";",null);
            if(!cursor.moveToFirst()){
                AddExpClass(db, equipAndPhysID, subareaID, experimentInfo.getMainPicture(), experimentInfo.getGeomPicture(),
                        experimentInfo.getRegPicture(), experimentInfo.getTeplPicture());
                cursor = db.rawQuery("SELECT * FROM Experiment_class WHERE " +
                        "id_Php_Tpe = " + equipAndPhysID + " AND " +
                        "id_subarea = " + subareaID + ";",null);
                cursor.moveToFirst();
            }
            expClassID = cursor.getInt(cursor.getColumnIndex("ID"));
            cursor.close();

        }catch (SQLiteException e){
            db.close();
            return false;
        }


        //проверить, существует ли такой тип данных
        //если да, то получить id
        //если нет, то добавить, получить id
        try{
            Cursor cursor = db.rawQuery("SELECT * FROM Data_type WHERE " +
                    "name_data = '" + experimentInfo.getDataType() + "';",null);
            if(!cursor.moveToFirst()){
                AddDataType(db, experimentInfo.getDataType());
                cursor = db.rawQuery("SELECT * FROM Data_type WHERE " +
                        "name_data = '" + experimentInfo.getDataType() + "';",null);
                cursor.moveToFirst();
            }
            dataTypeID = cursor.getInt(cursor.getColumnIndex("id_data"));
            cursor.close();
        }catch (SQLiteException e){
            db.close();
            return false;
        }

        //Получение id эксперимента, чтобы все параметры принадлежали одному эксперименту
        try {
            Cursor cursor = db.rawQuery("SELECT ID FROM Experiment;", null);
            if(cursor.moveToLast()){
                experimentID = cursor.getInt(cursor.getColumnIndex("ID")) + 1;
            }else{
                experimentID = 1;
            }
            cursor.close();
        }catch (SQLiteException e){
            db.close();
            return false;
        }

        //Внесение массива параметров в бд
        for (Parameter param:
             parameters) {

            int actualType = param.getType()+1;
            int paramTypeID;
            int paramID;
            int valueID;

            //узнать тип параметров, получить его id
            try{
                Cursor cursor = db.rawQuery("SELECT * FROM Type_of_parameters WHERE id_type = " + actualType + ";",null);
                if(!cursor.moveToFirst()){
                    //несуществующий тип
                    db.close();
                    return false;
                }
                paramTypeID = cursor.getInt(cursor.getColumnIndex("id_type"));
                cursor.close();
            }catch (SQLiteException e){
                db.close();
                return false;
            }

            //Проверить, существует ли такой параметр в бд
            //если нет, то добавить и получить id
            //если да, то получить id
            try{
                Cursor cursor = db.rawQuery("SELECT * FROM Parameters WHERE " +
                        "id_type = " + paramTypeID + " AND " +
                        "name_param = '" + param.getName() + "' AND " +
                        "unit_param = '" + param.getUnit() +"';",null);
                if(!cursor.moveToFirst()){
                    AddParameter(db, paramTypeID, param.getName(), param.getShortName(), param.getUnit());
                    cursor = db.rawQuery("SELECT * FROM Parameters WHERE " +
                            "id_type = " + paramTypeID + " AND " +
                            "name_param = '" + param.getName() + "' AND " +
                            "unit_param = '" + param.getUnit() +"';",null);
                    cursor.moveToFirst();
                }
                paramID = cursor.getInt(cursor.getColumnIndex("id_param"));
                cursor.close();
            }catch (SQLiteException e){
                db.close();
                return false;
            }


            //добавить эксперимент, получить его idВеличины
            try{
                AddExperiment(db, experimentID,expClassID, paramID, dataTypeID);
                Cursor cursor = db.rawQuery("SELECT * FROM Experiment",null); //получение id величины, которую мы заполним
                cursor.moveToLast();
                valueID = cursor.getInt(cursor.getColumnIndex("id_value"));
                cursor.close();
            }catch (SQLiteException e){
                db.close();
                return false;
            }

            //добавить величину с использованием этого id в зависимости от типа величины

            try{
                if(paramTypeID == 1){
                    AddParValue(db, valueID, param.getFloatValue());
                }else  if(paramTypeID == 2){
                    AddParValue(db, valueID, param.getStringValue());
                }else  if(paramTypeID == 3){
                    AddParValue(db, valueID, param.getRangeValue1(), param.getRangeValue2());
                }else  if(paramTypeID == 4){
                    AddParValue(db, valueID, param.getStringValue(), "img");
                }
            }catch (SQLiteException e){
                db.close();
                return false;
            }


        }
        return true;
    }

    public static int isExperimentTableEmpty(SQLiteDatabase db){
        //1 full //0 empty //-1 error
        try {//попробовать получить измерение
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM Experiment;", null);
            if (!cursor.moveToFirst()) {
                return 1; //если пусто, то флаг
            }
            cursor.close();
        } catch (SQLiteException e) {//если таблиц не существует
            try{
                CreateTables(db);//создать таблицы
            }catch (SQLiteException e1){
                return -1;
            }
            return 1;
        }
        return 0;
    }

    //Проверка дб на пустоту/существование
    public static int isDBEmpty(SQLiteDatabase db){
        try {//попробовать получить измерение
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM Physical_process;", null);
            if (!cursor.moveToFirst()) {
                return 1; //если пусто, то флаг
            }
            cursor.close();
        } catch (SQLiteException e) {//если таблиц не существует
            try{
                CreateTables(db);//создать таблицы
            }catch (SQLiteException e1){
                return -1;
            }
            return 1;
        }
        return 0;
    }

    //получение списка всех экспериментов
    public static ArrayList<ItemExperiment> getExperimentList(SQLiteDatabase db){
        //получение массива всех уникальных id, которые есть в таблице
        ArrayList<Integer> expID = new ArrayList<>();
        try {//попробовать считать
            Cursor cursor = db.rawQuery("SELECT DISTINCT ID FROM Experiment", null);
            if(cursor.moveToFirst()){
                do {
                    expID.add(cursor.getInt(0));
                } while (cursor.moveToNext());
            }else{
                return new ArrayList<>();
            }
            cursor.close();
        }catch (SQLiteException e) {
            return new ArrayList<>();
        }

        //Получение непосредственно списка экспериментов
        ArrayList<ItemExperiment> itemExperiments = new ArrayList<>();
        //для каждого id происходит поиск значений
        for (Integer integer : expID) {
            ItemExperiment item = new ItemExperiment();

            try {//попробовать считать
                Cursor cursor = db.rawQuery("SELECT Experiment.ID AS ID, Physical_process.name AS procName, " +
                        "Type_of_power_equipment.name AS equipName, Areas_tree.name_subarea AS subareaName, " +
                        "Parameters.name_param AS parameterName " +
                        "FROM Experiment " +
                        "JOIN Experiment_class ON Experiment.id_exp = Experiment_class.ID " +
                        "JOIN Areas_tree ON Experiment_class.id_subarea = Areas_tree.id_subarea " +
                        "JOIN Php_Tpe ON Experiment_class.id_Php_Tpe = Php_Tpe.id_Php_Tpe " +
                        "JOIN Physical_process ON Php_Tpe.id_Php = Physical_process.id_Php " +
                        "JOIN Type_of_power_equipment ON Php_Tpe.id_Tpe = Type_of_power_equipment.id_Tpe " +
                        "JOIN Parameters ON Experiment.id_param = Parameters.id_param " +
                        "WHERE Experiment.ID =" + integer + ";", null);
                cursor.moveToFirst();

                //сначала считывание инфы
                item.setId(integer);
                item.setSubArea(cursor.getString(cursor.getColumnIndex("subareaName")));
                item.setProcess(cursor.getString(cursor.getColumnIndex("procName")));
                item.setEquipment(cursor.getString(cursor.getColumnIndex("equipName")));

                //потом считывание параметров ( не больше 3х штук)
                int j = 0;
                do {
                    if (j == 0) {
                        item.setParam1(cursor.getString(cursor.getColumnIndex("parameterName")));
                        j++;
                    } else if (j == 1 && !item.getParam1().equals(cursor.getString(cursor.getColumnIndex("parameterName")))) {
                        item.setParam2(cursor.getString(cursor.getColumnIndex("parameterName")));
                        j++;
                    } else if (j == 2 && !item.getParam1().equals(cursor.getString(cursor.getColumnIndex("parameterName")))
                            && !item.getParam2().equals(cursor.getString(cursor.getColumnIndex("parameterName")))) {
                        item.setParam3(cursor.getString(cursor.getColumnIndex("parameterName")));
                        j++;
                    }

                    if (cursor.moveToNext() && j == 3) {
                        item.setMore(true);
                    } else {
                        cursor.moveToPrevious();
                    }
                } while (cursor.moveToNext() && j < 3);
                cursor.close();
            } catch (SQLiteException e) {
                return new ArrayList<>();
            }
            itemExperiments.add(item);
        }
        return itemExperiments;
    }

    //Получить массив параметров для эксперимента
    public static ArrayList<Parameter> GetParametersList(SQLiteDatabase db, int experimentID){

        ArrayList<Parameter> parameters = new ArrayList<>();
        Parameter param;

        try{
            Cursor cursor = db.rawQuery("SELECT Experiment.ID as ID, " +
                    "Type_of_parameters.id_type AS type, " +
                    "Parameters.name_param AS name, " +
                    "Parameters.short_name_param AS shortName, " +
                    "Parameters.unit_param AS unit, " +
                    "Parameters_values.value_number AS floatValue, " +
                    "Parameters_values.value_range1 AS valueRange1, " +
                    "Parameters_values.value_range2 AS valueRange2, " +
                    "Parameters_values.value_string AS valueString, " +
                    "Parameters_values.value_image AS valueImage, " +
                    "Parameters_values.date AS date " +
                    "FROM Experiment " +
                    "JOIN Data_type ON Data_type.id_data = Experiment.id_data " +
                    "JOIN Parameters ON Parameters.id_param = Experiment.id_param " + //android.database.CursorIndexOutOfBoundsException: Index 0 requested, with a size of 0
                    "JOIN Type_of_parameters ON Type_of_parameters.id_type = Parameters.id_type " +
                    "JOIN Parameters_values ON Parameters_values.id_value = Experiment.id_value " +
                    "WHERE ID = " + experimentID +";", null);

            cursor.moveToFirst();
            do{
                param = new Parameter();
                param.setName(cursor.getString(cursor.getColumnIndex("name")));//android.database.CursorIndexOutOfBoundsException: Index 0 requested, with a size of 0
                param.setShortName(cursor.getString(cursor.getColumnIndex("shortName")));
                param.setType(cursor.getInt(cursor.getColumnIndex("type"))-1);//не забыть про -1
                param.setDate(cursor.getString(cursor.getColumnIndex("date")));

                if(param.getType()==0){
                    param.setValue(cursor.getFloat(cursor.getColumnIndex("floatValue")));
                    param.setUnit(cursor.getString(cursor.getColumnIndex("unit")));
                }else if(param.getType()==1){
                    param.setStringValue(cursor.getString(cursor.getColumnIndex("valueString")));
                }else if(param.getType()==2){
                    param.setDiapason(cursor.getFloat(cursor.getColumnIndex("valueRange1")), cursor.getFloat(cursor.getColumnIndex("valueRange2")));
                    param.setUnit(cursor.getString(cursor.getColumnIndex("unit")));
                }else if(param.getType()==3){
                    param.setStringValue(cursor.getString(cursor.getColumnIndex("valueImage")));
                }

                parameters.add(param);

            }while (cursor.moveToNext());

            cursor.close();
        }catch (SQLiteException e){
            return new ArrayList<>();
        }
        return parameters;
    }

    //Получить информацию об эксперименте
    public static Experiment GetExperimentInfo(SQLiteDatabase db, int experimentID){
        Experiment experiment = new Experiment();
        int parentAreaId;
        try{
            Cursor cursor = db.rawQuery("SELECT Experiment.ID AS ID, " +
                    "Physical_process.name AS procName, " +
                    "Type_of_power_equipment.name AS equipName, " +
                    "Areas_tree.id_parent_area AS parentSubareaId, " +
                    "Areas_tree.name_subarea AS subareaName, " +
                    "Area.name_area AS areaName, " +
                    "Data_type.name_data AS dataType, " +
                    "Experiment_class.Main_pict AS mainPic, " +
                    "Experiment_class.Geom_pict AS geomPic, " +
                    "Experiment_class.Reg_pict AS regPic, " +
                    "Experiment_class.Tepl_pict AS teplPic " +
                    "FROM Experiment " +
                    "JOIN Experiment_class ON Experiment.id_exp = Experiment_class.ID " +
                    "JOIN Areas_tree ON Experiment_class.id_subarea = Areas_tree.id_subarea " +
                    "JOIN Area ON Areas_tree.id_area = Area.id_area " +
                    "JOIN Php_Tpe ON Experiment_class.id_Php_Tpe = Php_Tpe.id_Php_Tpe " +
                    "JOIN Physical_process ON Php_Tpe.id_Php = Physical_process.id_Php " +
                    "JOIN Type_of_power_equipment ON Php_Tpe.id_Tpe = Type_of_power_equipment.id_Tpe " +
                    "JOIN Data_type ON Experiment.id_data = Data_type.id_data " +
                    "WHERE Experiment.ID =" + experimentID + ";", null);
            cursor.moveToFirst();
            experiment.setArea(cursor.getString(cursor.getColumnIndex("areaName")));
            experiment.setSubArea(cursor.getString(cursor.getColumnIndex("subareaName")));
            parentAreaId = cursor.getInt(cursor.getColumnIndex("parentSubareaId"));
            experiment.setPhysicalProcess(cursor.getString(cursor.getColumnIndex("procName")));
            experiment.setPowerEquipment(cursor.getString(cursor.getColumnIndex("equipName")));
            experiment.setDataType(cursor.getString(cursor.getColumnIndex("dataType")));
            experiment.setMainPicture(cursor.getString(cursor.getColumnIndex("mainPic")));
            experiment.setTeplPicture(cursor.getString(cursor.getColumnIndex("teplPic")));
            experiment.setRegPicture(cursor.getString(cursor.getColumnIndex("regPic")));
            experiment.setGeomPicture(cursor.getString(cursor.getColumnIndex("geomPic")));
            cursor.close();
            cursor = db.rawQuery("SELECT name_subarea FROM Areas_tree WHERE id_subarea = " + parentAreaId + ";", null);
            cursor.moveToFirst();
            experiment.setParentSubArea(cursor.getString(cursor.getColumnIndex("name_subarea")));
            cursor.close();
        }catch (SQLiteException exp){
            return new Experiment();
        }
        return experiment;
    }

    //удалить эксперимент
    public static boolean DeleteExperiment(SQLiteDatabase db, int experimentID){
        //удаление всех величин, связанных с этим экспериментом

        Cursor cursor = db.rawQuery("SELECT Experiment.ID AS ID, Parameters_values.id_value AS valueID " +
                "FROM Experiment " +
                "JOIN Parameters_values ON Experiment.id_value = Parameters_values.id_value " +
                "WHERE ID = " + experimentID + ";", null);

        if(cursor.moveToFirst()){
            do{
                //тут ошибка, в курсоре нет такого поля
                db.execSQL("DELETE FROM Parameters_values WHERE id_value = " + cursor.getInt(cursor.getColumnIndex("valueID")) + ";");
            }while(cursor.moveToNext());
        }
        cursor.close();

        try{
            db.execSQL("DELETE FROM Experiment WHERE ID = " + experimentID +" ;");
        }catch (SQLException e){
            return false;
        }
        return true;
    }

    //получить JSON массив из таблицы бд
    private static JSONArray getTableContent(String tableName, SQLiteDatabase db){
        JSONArray resultSet = new JSONArray();

        Cursor cursor = db.rawQuery("SELECT  * FROM " + tableName, null );
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();

            for( int i=0 ;  i< totalColumn ; i++ )
            {
                if( cursor.getColumnName(i) != null )
                {
                    try
                    {
                        if( cursor.getString(i) != null )
                        {
                            rowObject.put(cursor.getColumnName(i) ,  cursor.getString(i) );
                        }
                        else
                        {
                            rowObject.put( cursor.getColumnName(i) ,  "" );
                        }
                    }
                    catch( Exception e )
                    {
                        Log.d("TAG_NAME", e.getMessage()  );
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        return resultSet;
    }






    //получить JSON объект со всем содержимым БД
    public static JSONObject GetDbContent(SQLiteDatabase db){
        JSONObject content = new JSONObject();
        JSONObject info = new JSONObject();
        JSONArray params = new JSONArray();
        Date date = new Date();

        try {
            content.put("DateOfUpload", date.toString());
            content.put("Area", getTableContent("Area", db));
            content.put("Areas_Tree", getTableContent("Areas_Tree", db));
            content.put("Physical_process", getTableContent("Physical_process", db));
            content.put("Type_of_power_equipment", getTableContent("Type_of_power_equipment", db));
            content.put("Php_Tpe", getTableContent("Php_Tpe", db));
            content.put("Experiment_class", getTableContent("Experiment_class", db));
            content.put("Type_of_parameters", getTableContent("Type_of_parameters", db));
            content.put("Data_type", getTableContent("Data_type", db));
            content.put("Parameters", getTableContent("Parameters", db));
            content.put("Experiment", getTableContent("Experiment", db));
            content.put("Parameters_values", getTableContent("Parameters_values", db));

            //content.put("String_values", getTableContent("String_values", db));
            /*content.put("Users", getTableContent("Users", db));
            content.put("Log_Table", getTableContent("Log_Table", db));
            content.put("Software_configuration", getTableContent("Software_configuration", db));
            content.put("PC_configuration", getTableContent("PC_configuration", db));
            content.put("Unic_Exp", getTableContent("Unic_Exp", db));
            content.put("FCA", getTableContent("FCA", db));
            */








        }catch (JSONException e){
            return new JSONObject();
        }
        return content;
    }

    //Удалить вообще все
    public static boolean ResetDB(SQLiteDatabase db){
        try{

            db.execSQL("DROP TABLE Area;");
            db.execSQL("DROP TABLE Areas_Tree;");
            db.execSQL("DROP TABLE Physical_process;");
            db.execSQL("DROP TABLE Type_of_power_equipment;");
            db.execSQL("DROP TABLE Php_Tpe;");
            db.execSQL("DROP TABLE Experiment_class;");
            db.execSQL("DROP TABLE Type_of_parameters;");
            db.execSQL("DROP TABLE Data_type;");
            db.execSQL("DROP TABLE Parameters;");
            db.execSQL("DROP TABLE Experiment;");
            db.execSQL("DROP TABLE Parameters_values;");

            CreateTables(db);
        }catch (SQLiteException e){
            return false;
        }
        return true;
    }

    public static boolean AddFromJSON(SQLiteDatabase db, JSONObject obj){

        JSONArray array = null;
        JSONObject rowObj;

        try{
            array = obj.getJSONArray("Area");
            if(array.length()!=0){
                for(int i = 1; i<array.length();i++){
                    rowObj = array.getJSONObject(i);
                    AddArea(db, rowObj.getString("name_area"));
                }
            }
            array = obj.getJSONArray("Areas_Tree");
            if(array.length()!=0){
                for(int i = 1; i<array.length();i++){
                    rowObj = array.getJSONObject(i);
                    AddSubArea(db, rowObj.getString("name_subarea"), rowObj.getInt("id_area"), rowObj.getInt("id_parent_area"));
                }
            }
            array = obj.getJSONArray("Physical_process");
            if(array.length()!=0){
                for(int i = 0; i<array.length();i++){
                    rowObj = array.getJSONObject(i);
                    AddPhProcess(db, rowObj.getString("name"));
                }
            }
            array = obj.getJSONArray("Type_of_power_equipment");
            if(array.length()!=0){
                for(int i = 0; i<array.length();i++){
                    rowObj = array.getJSONObject(i);
                    AddPowEquipment(db, rowObj.getString("name"));
                }
            }
            array = obj.getJSONArray("Php_Tpe");
            if(array.length()!=0){
                for(int i = 0; i<array.length();i++){
                    rowObj = array.getJSONObject(i);
                    AddPhProcessAndPowEquipment(db, rowObj.getInt("id_Php"), rowObj.getInt("id_Tpe"));
                }
            }
            array = obj.getJSONArray("Experiment_class");
            if(array.length()!=0){
                for(int i = 0; i<array.length();i++){
                    rowObj = array.getJSONObject(i);
                    AddExpClass(db, rowObj.getInt("id_Php_Tpe"), rowObj.getInt("id_subarea"),
                            rowObj.getString("Main_pict"), rowObj.getString("Geom_pict"),
                            rowObj.getString("Reg_pict"), rowObj.getString("Tepl_pict"));
                }
            }
            /*array = obj.getJSONArray("Type_of_parameters");
            if(array.length()!=0){
                for(int i = 0; i<array.length();i++){
                    rowObj = array.getJSONObject(i);
                    //все типы уже существуют
                }

            }*/
            array = obj.getJSONArray("Data_type");
            if(array.length()!=0){
                for(int i = 0; i<array.length();i++){
                    rowObj = array.getJSONObject(i);
                    AddDataType(db, rowObj.getString("name_data"));
                }
            }
            array = obj.getJSONArray("Parameters");
            if(array.length()!=0){
                for(int i = 0; i<array.length();i++){
                    rowObj = array.getJSONObject(i);
                    AddParameter(db, rowObj.getInt("id_type"), rowObj.getString("name_param"),
                            rowObj.getString("short_name_param"), rowObj.getString("unit_param"));
                }
            }
            array = obj.getJSONArray("Experiment");
            if(array.length()!=0){
                for(int i = 0; i<array.length();i++){
                    rowObj = array.getJSONObject(i);
                    AddExperiment(db, rowObj.getInt("ID"), rowObj.getInt("id_exp"), rowObj.getInt("id_param"),
                            rowObj.getInt("id_data"));
                }
            }
            array = obj.getJSONArray("Parameters_values");
            if(array.length()!=0){
                for(int i = 0; i<array.length();i++){
                    rowObj = array.getJSONObject(i);

                    float valuer1 = Float.MIN_VALUE;
                    float valuer2 = Float.MIN_VALUE;
                    float value = Float.MIN_VALUE;

                    try{
                        valuer1 = Float.parseFloat(rowObj.getString("value_range1"));
                    }catch (NumberFormatException ignored){}
                    try{
                        valuer2 = Float.parseFloat(rowObj.getString("value_range2"));
                    }catch (NumberFormatException ignored){}
                    try{
                        value = Float.parseFloat(rowObj.getString("value_number"));
                    }catch (NumberFormatException ignored){}

                    AddParValue(db, rowObj.getInt("id_value"),  rowObj.getString("value_image"), rowObj.getString("value_string"),
                             valuer1, valuer2, value);
                }
            }

        }catch (JSONException e){
            Log.d("ОШИБКА:", e.toString());
            return false;
        }
        return true;
    }

}
