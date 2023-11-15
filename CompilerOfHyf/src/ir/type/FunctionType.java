package src.ir.type;

import src.ir.type.single.ArrayType;
import src.ir.type.single.SingleType;

import java.util.ArrayList;

public class FunctionType extends Type {
    //ret_type and funcFParams_type
    private SingleType retType;
    private ArrayList<SingleType> paramsType;

    public FunctionType(SingleType retType) {
        this.retType = retType;
    }

    public FunctionType(SingleType retType, ArrayList<SingleType> paramsType) {
        this.retType = retType;
        this.paramsType = paramsType;
    }

}
