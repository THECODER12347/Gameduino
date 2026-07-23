package Variable;

public class Variable {
    public Integer type;
    public String identifier;
    public boolean isParameter;

    public Variable(String identifier, int type, boolean isParameter){
        this.identifier = identifier;
        this.type = type;
        this.isParameter = isParameter;
    }

    public Variable(String identifier, int type){
        this.identifier = identifier;
        this.type = type;
        this.isParameter = false;
    }

    @Override
    public String toString() {
        return identifier+": "+type.toString()+", "+isParameter;
    }
}
