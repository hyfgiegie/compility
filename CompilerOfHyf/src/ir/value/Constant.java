package src.ir.value;

import src.ir.type.single.ArrayType;
import src.ir.type.single.IntType;
import src.ir.type.single.SingleType;

import java.util.ArrayList;

public class Constant extends Value {

    //the constant should have a numericalValue
    private String numericalValue;

    //array
    private ArrayList<Constant> constants;

    public Constant() {
    }

    public Constant(String numericalValue) {
        setName(numericalValue);
        this.numericalValue = numericalValue;
        //默认为 int type
        this.setType(new IntType());
    }

    public String getNumericalValue() {
        return numericalValue;
    }

    public void setNumericalValue(String numericalValue) {
        this.numericalValue = numericalValue;
        setName(numericalValue);
    }

    public ArrayList<Constant> getConstants() {
        return constants;
    }

    public void setConstants(ArrayList<Constant> constants) {
        this.constants = constants;
        ArrayList<Integer> maxLen = new ArrayList<>();
        maxLen.add(constants.size());
        this.setType(new ArrayType(maxLen, (SingleType) constants.get(0).getType()));
    }

    @Override
    public String toString() {
        if (constants == null) {
            return getType() + " " + numericalValue;
        }
        else {
            String s = getType().toString() + " [";
            for (int i = 0; i < constants.size(); i++) {
                if (i == constants.size() - 1) {
                    s += constants.get(i).toString();
                }
                else {
                    s += constants.get(i).toString() + ", ";
                }
            }
            s += "]";
            return s;
        }
    }
}
