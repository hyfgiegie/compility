package src.ir.type.single;

import src.ir.type.Type;

import java.util.ArrayList;
import java.util.Arrays;

public class ArrayType extends SingleType {
    private ArrayList<Integer> maxLenOfEachDimension;
    private SingleType basicType;

    public ArrayType(ArrayList<Integer> maxLenOfEachDimension, SingleType basicType) {
        this.maxLenOfEachDimension = maxLenOfEachDimension;
        this.basicType = basicType;
    }

    public int getInnerSize() {
        return maxLenOfEachDimension.get(0);
    }

    public Type getInnerType() {
        if (maxLenOfEachDimension.size() > 1) {
            // >= 2 dimension
            ArrayList<Integer> newDimen = new ArrayList<>();
            for (int i = 1; i < maxLenOfEachDimension.size(); i++) {
                newDimen.add(maxLenOfEachDimension.get(i));
            }
            return new ArrayType(newDimen, basicType);
        }
        else {
            // 1 dimension
            return basicType;
        }
    }

    @Override
    public String toString() {
        ArrayList<String> rets = new ArrayList<>();
        rets.add(basicType.toString());
        int dimension = maxLenOfEachDimension.size();
        for (int i = dimension - 1; i >= 0; i--) {
            int len = maxLenOfEachDimension.get(i);
            String s = "[" + len + " x " + rets.get(rets.size() - 1) + "]";
            rets.add(s);
        }
        return rets.get(rets.size() - 1);
    }
}
