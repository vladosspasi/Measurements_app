package vo.aliabyev.measurements;

//класс параметра - хранит значения
public class Parameter extends TemplateParam {

    private float value1;
    private float value2;
    private String stringValue;


    public Parameter(){
        super();
        value1 = Float.MIN_VALUE;
        value2 = Float.MIN_VALUE;
        stringValue = "";
    }

    public Parameter(TemplateParam temp, String value){ //констурктор для строки/изображения
        this.name = temp.getName();
        this.shortName = temp.getShortName();
        this.unit = temp.getUnit();
        this.typeInt = temp.getType();
        this.stringValue = value;
        this.value1 = Float.MIN_VALUE;
        this.value2 = Float.MIN_VALUE;
    }

    public Parameter(TemplateParam temp, float value){ //конструктор для числа
        this.name = temp.getName();
        this.shortName = temp.getShortName();
        this.unit = temp.getUnit();
        this.typeInt = temp.getType();
        this.value1 = value;
        this.value2 = Float.MIN_VALUE;
        this.stringValue = "";
    }

    public Parameter(TemplateParam temp, float from, float to){ //конструктор для диапазона
        this.name = temp.getName();
        this.shortName = temp.getShortName();
        this.unit = temp.getUnit();
        this.typeInt = temp.getType();
        this.value1 = from;
        this.value2 = to;
        this.stringValue = "";
    }

    public boolean isEmptyValue(){
        return (typeInt == 0 && value1 == Float.MIN_VALUE) || (typeInt == 2 && (value1 == Float.MIN_VALUE || value2 == Float.MIN_VALUE)) ||
                ((typeInt == 1 || typeInt == 3) && (stringValue.equals(""))) || typeInt == -1;
    }

    //get
    public float getFloatValue(){ return value1;}
    public float getRangeValue1(){return value1;}
    public float getRangeValue2(){return value2;}
    public String getStringValue(){return stringValue;}

    //set
    public void setValue(float value){this.value1 = value;}
    public void setDiapason(float from, float to){
        this.value1 = from;
        this.value2 = to;
    }
    public void setStringValue(String stringValue) {this.stringValue = stringValue;}
}
