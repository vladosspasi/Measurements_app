package vo.aliabyev.measurements;

//класс шаблона параметра - данные о параметре
public class TemplateParam {//параметр, который можо добавить в шаблон измерения
    protected String name;
    protected String shortName;
    protected int typeInt;
    protected String unit;
    protected String date;

    public TemplateParam() {
        name = "";
        shortName = "";
        unit = "";
        typeInt = -1;//0 - числ, 1 - строк, 2 - диапазон, 3 - изображение
        date = "";
    }

    public boolean isEmpty(){
        if(name.equals("")||shortName.equals("")||typeInt==-1||((typeInt==0||typeInt==2)&&unit.equals(""))){
            return true;
        }
        return false;
    }

    //get
    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public int getType() {
        return typeInt;
    }

    public String getStringType(){
        if (typeInt==0){
            return "Численный";
        }else if(typeInt==1){
            return "Строковый";
        }else if(typeInt==2){
            return "Диапазон";
        }else if(typeInt==3){
            return "Изображение";
        }
        return null;
    }

    public String getUnit() {
        return unit;
    }

    public String getDate(){return this.date;}
    //set

    public void setName(String name) {
        this.name = name;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public void setType(int type){
        this.typeInt = type;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setDate(String date){ this.date = date;}
}
