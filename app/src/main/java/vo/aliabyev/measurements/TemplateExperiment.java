package vo.aliabyev.measurements;

//эксперимент + имя -- для вывода
public class TemplateExperiment extends Experiment {
    private String name;

    public TemplateExperiment(){
        name = "";
    }

    public boolean isEmpty(){
        return name.equals("") | super.isEmpty();
    }

    //get
    public String getName(){
        return name;
    }

    public String getFileName(){
        return name + ".json";
    }

    public void setName(String name) {
        this.name = name;
    }

}
