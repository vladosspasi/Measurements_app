package vo.aliabyev.measurements;

//класс информации об эксперименте
public class Experiment {

    protected String area = "";
    protected String parentSubArea = "";
    protected String subArea = "";
    protected String physicalProcess = "";
    protected String powerEquipment = "";
    protected String mainPicture = "";
    protected String geomPicture = "";
    protected String teplPicture = "";
    protected String regPicture = "";
    protected String dataType = "";

    public Experiment(){
    }

    public boolean isEmpty(){
        if(area.equals("")||subArea.equals("")||physicalProcess.equals("")||powerEquipment.equals("")||
        dataType.equals("")) return true;
        return false;
    }

    //get
    public String getArea(){ return area; }
    public String getSubArea(){ return subArea; }
    public String getParentSubArea(){ return parentSubArea; }
    public String getPhysicalProcess(){ return physicalProcess; }
    public String getPowerEquipment(){return powerEquipment;}
    public String getMainPicture(){return mainPicture;}
    public String getGeomPicture(){return geomPicture;}
    public String getTeplPicture(){return teplPicture;}
    public String getRegPicture(){return regPicture;}
    public String getDataType(){return dataType;}
    //set
    public void setArea(String area) {this.area = area;}
    public void setSubArea(String subArea) {this.subArea = subArea;}
    public void setParentSubArea(String parentSubArea) {this.parentSubArea = parentSubArea;}
    public void setPhysicalProcess(String physicalProcess) { this.physicalProcess = physicalProcess; }
    public void setPowerEquipment(String powerEquipment) { this.powerEquipment = powerEquipment;}
    public void setDataType(String dataType) {this.dataType = dataType;}
    public void setTeplPicture(String teplPicture) {this.teplPicture = teplPicture;}
    public void setRegPicture(String regPicture) {this.regPicture = regPicture;}
    public void setMainPicture(String mainPicture) {this.mainPicture = mainPicture;}
    public void setGeomPicture(String geomPicture) {this.geomPicture = geomPicture; }
}
