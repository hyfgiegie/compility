package src.ir;

import src.ir.type.Type;
import src.ir.type.single.*;
import src.ir.value.*;
import src.ir.value.inst.Instruction;
import src.ir.value.inst.binary.AddInst;
import src.ir.value.inst.binary.MulInst;
import src.ir.value.inst.binary.SdivInst;
import src.ir.value.inst.binary.SubInst;
import src.ir.value.inst.conversion.ZextInst;
import src.ir.value.inst.memory.AllocInst;
import src.ir.value.inst.memory.GetPtrInst;
import src.ir.value.inst.memory.LoadInst;
import src.ir.value.inst.memory.StoreInst;
import src.ir.value.inst.other.CallInst;
import src.ir.value.inst.other.IcmpInst;
import src.ir.value.inst.terminal.BrInst;
import src.ir.value.inst.terminal.ReturnInst;

import java.util.ArrayList;

public class IRBuildFactory {
    private IRModule irModule;

    public IRBuildFactory() {
        irModule = IRModule.getInstance();
    }

    private BasicBlock getCurBlock() {
        return irModule.getCurBasicBlock();
    }

    private Function getCurFunction() {
        return irModule.getCurFunction();
    }

    public void buildFunction(Function function) {
        Function curFunction = getCurFunction();
        if (curFunction != null && !(getCurBlock().getLastInstruction() instanceof ReturnInst)) {
            buildReturnInst(null);
        }
        irModule.addFunction(function);
    }

    public void buildBasicBlock(BasicBlock basicBlock) {
        Instruction inst = getCurBlock().getLastInstruction();
        if (inst == null || !(inst instanceof BrInst) || !(inst instanceof ReturnInst)) {
            buildBrInst(null, basicBlock, null);
        }
        irModule.getCurFunction().addBasicBlock(basicBlock);
    }

    public void buildGlobalVar(GlobalVar value) {
        irModule.addGlobalVar(value);
    }

    public Value buildAllocInst(Type pointTo) {
        AllocInst allocInst = new AllocInst();
        allocInst.newName();
        allocInst.setType(new PointerType((SingleType) pointTo));

        getCurBlock().addInstruction(allocInst);
        return allocInst;
    }

    public void buildStoreInst(Value value, Value pointer) {
        StoreInst storeInst = new StoreInst();
        storeInst.addOperands(value);
        storeInst.addOperands(pointer);

        value.addUse(new Use(value, (User) pointer, 0));

        getCurBlock().addInstruction(storeInst);
    }

    public Value buildLoadInst(Value v) {
        LoadInst loadInst = new LoadInst();
        loadInst.newName();
        loadInst.setType(((PointerType)v.getType()).getPointTo());
        loadInst.addOperands(v);

        v.addUse(new Use(v, loadInst, 0));

        getCurBlock().addInstruction(loadInst);
        return loadInst;
    }

    public Value buildAddInst(Value v1, Value v2) {
        AddInst addInst = new AddInst();
        addInst.newName();
        addInst.setType(v1.getType());
        addInst.addOperands(v1);
        addInst.addOperands(v2);

        v1.addUse(new Use(v1, addInst, 0));
        v2.addUse(new Use(v2, addInst, 1));

        getCurBlock().addInstruction(addInst);
        return addInst;
    }

    public Value buildMulInst(Value v1, Value v2) {
        MulInst mulInst = new MulInst();
        mulInst.newName();
        mulInst.setType(v1.getType());
        mulInst.addOperands(v1);
        mulInst.addOperands(v2);

        v1.addUse(new Use(v1, mulInst, 0));
        v2.addUse(new Use(v2, mulInst, 1));

        getCurBlock().addInstruction(mulInst);
        return mulInst;
    }

    public Value buildSubInst(Value v1, Value v2) {
        SubInst subInst = new SubInst();
        subInst.newName();
        subInst.setType(v1.getType());
        subInst.addOperands(v1);
        subInst.addOperands(v2);

        v1.addUse(new Use(v1, subInst, 0));
        v2.addUse(new Use(v2, subInst, 1));

        getCurBlock().addInstruction(subInst);
        return subInst;
    }

    public Value buildSdivInst(Value v1, Value v2) {
        SdivInst sdivInst = new SdivInst();
        sdivInst.newName();
        sdivInst.setType(v1.getType());
        sdivInst.addOperands(v1);
        sdivInst.addOperands(v2);

        v1.addUse(new Use(v1, sdivInst, 0));
        v2.addUse(new Use(v2, sdivInst, 1));

        getCurBlock().addInstruction(sdivInst);
        return sdivInst;
    }

    public Value buildModInst(Value v1, Value v2) {
        //v1 - v1 / v2 * v2
        Value v3 = buildSdivInst(v1, v2);
        Value v4 = buildMulInst(v3, v2);
        Value v5 = buildSubInst(v1, v4);
        return v5;
    }

    public Value buildGetPtrInst(Value pointer, ArrayList<Value> offsets) {
        GetPtrInst getPtrInst = new GetPtrInst();
        getPtrInst.newName();
        //type is what? 脱掉 offsets.size() - 1
        Type pointTo = ((PointerType)pointer.getType()).getPointTo();
        for (int i = 0; i < offsets.size() - 1; i++) {
            pointTo = ((ArrayType) pointTo).getInnerType();
        }
        getPtrInst.setType(new PointerType((SingleType) pointTo));
        getPtrInst.addOperands(pointer);
        pointer.addUse(new Use(pointer, getPtrInst, 0));
        for (int i = 0; i < offsets.size(); i++) {
            Value value = offsets.get(i);
            getPtrInst.addOperands(value);
            value.addUse(new Use(value, getPtrInst, i + 1));
        }

        getCurBlock().addInstruction(getPtrInst);
        return getPtrInst;
    }

    public Value buildReturnInst(Value value) {
        ReturnInst returnInst = new ReturnInst();
        if (value != null) {
            returnInst.addOperands(value);
            value.addUse(new Use(value, returnInst, 0));
        }

        getCurBlock().addInstruction(returnInst);
        return returnInst;
    }

    public Value buildCallInst(Function function, ArrayList<Value> rParams) {
        CallInst callInst = new CallInst();
        callInst.setType(function.getType());
        if (!(callInst.getType() instanceof VoidType)) {
            callInst.newName();
        }

        callInst.addOperands(function);
        function.addUse(new Use(function, callInst, 0));
        if (rParams != null) {
            for (int i = 0; i < rParams.size(); i++) {
                Value v = rParams.get(i);
                callInst.addOperands(v);
                v.addUse(new Use(v, callInst, i + 1));
            }
        }

        getCurBlock().addInstruction(callInst);
        return callInst;
    }



    public Value buildIcmpInst(Value op1, Value op2, String operation) {
        String cond;
        switch (operation) {
            case "==" :
                cond = "eq";
                break;
            case "!=":
                cond = "ne";
                break;
            case ">" :
                cond = "sgt";
                break;
            case "<":
                cond = "slt";
                break;
            case ">=":
                cond = "sge";
                break;
            case "<=":
                cond = "sle";
                break;
            default:
                cond = "?";
                break;
        }
        IcmpInst icmpInst = new IcmpInst();
        icmpInst.newName();
        icmpInst.setType(new BooleanType());
        icmpInst.setCond(cond);
        icmpInst.addOperands(op1);
        icmpInst.addOperands(op2);
        op1.addUse(new Use(op1, icmpInst, 0));
        op2.addUse(new Use(op2, icmpInst, 1));
        getCurBlock().addInstruction(icmpInst);
        return icmpInst;
    }

    public Value buildZextInst(Value v, Type t2) {
        ZextInst zextInst = new ZextInst();
        zextInst.newName();
        zextInst.setType(t2);
        zextInst.setToType(t2);
        zextInst.addOperands(v);
        v.addUse(new Use(v, zextInst, 0));
        getCurBlock().addInstruction(zextInst);
        return zextInst;
    }

    public Value buildBrInst(Value cond, BasicBlock b1, BasicBlock b2) {
        BrInst brInst = new BrInst();
        if (cond == null) {
            brInst.addOperands(b1);
            b1.addUse(new Use(b1, brInst, 0));
        }
        else {
            brInst.setCond(cond);
            brInst.addOperands(b1);
            brInst.addOperands(b2);
            b1.addUse(new Use(b1, brInst, 0));
            b2.addUse(new Use(b2, brInst, 1));
        }
        getCurBlock().addInstruction(brInst);
        return brInst;
    }

}
