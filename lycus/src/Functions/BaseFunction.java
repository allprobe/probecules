package Functions;

import Results.BaseResult;

/**
 * Created by roi on 8/17/16.
 */
public abstract class BaseFunction {
    protected String valueType;
    protected Object[] lastResults;
    public abstract Object[] get();
    public abstract void add(BaseResult result);
    public BaseFunction(String valueType)
    {
        this.valueType=valueType;
    }
}
