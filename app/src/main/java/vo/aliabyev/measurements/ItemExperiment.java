package vo.aliabyev.measurements;

import java.util.ArrayList;

//класс для вывода данных в сокращенной форме в списке экспериментов

public class ItemExperiment {
    private int id;
    private String subArea;
    private String equipment;
    private String process;
    private String param1;
    private String param2;
    private String param3;
    private boolean isMore;

    public ItemExperiment(){
        this.id = 0;
        this.subArea = "";
        this.equipment = "";
        this.process = "";
        this.param1 = "";
        this.param2 = "";
        this.param3 = "";
        this.isMore = false;
    }

    //get
    public int getID() {
        return id;
    }
    public String getEquipment() {
        return equipment;
    }
    public String getProcess() {
        return process;
    }
    public String getSubArea() {
        return subArea;
    }
    public String getParam1(){ return param1;}
    public String getParam2(){ return param2;}
    public String getParam3(){ return param3;}
    public boolean isMore() { return isMore;}

    //set
    public void setId(int id) { this.id = id; }
    public void setSubArea(String subArea) { this.subArea = subArea; }
    public void setEquipment(String equipment) { this.equipment = equipment; }
    public void setProcess(String process) { this.process = process; }
    public void setParam1(String param1) { this.param1 = param1; }
    public void setParam2(String param2) { this.param2 = param2; }
    public void setParam3(String param3) { this.param3 = param3; }
    public void setMore(boolean b){ this.isMore = b;}
}
