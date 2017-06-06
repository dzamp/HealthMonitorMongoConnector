/**
 * Created by jim on 22/5/2017.
 */
public class ExampleClass implements IGeneric{
    private int aNumber;
    private String aString;


    public ExampleClass() {
    }

    public int getaNumber() {
        return aNumber;
    }

    public void setaNumber(int aNumber) {
        this.aNumber = aNumber;
    }

    public String getaString() {
        return aString;
    }

    public void setaString(String aString) {
        this.aString = aString;
    }

    @Override
    public String getDefinition() {
        return aString+aNumber;
    }
}
