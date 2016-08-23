package Functions;

import Interfaces.IFunction;
import Results.*;

public class LastFunction extends BaseFunction {
    public LastFunction(String valueType) {
        super(valueType);
        this.lastResults = new Object[1];
    }

//	private Double lastValue;

    @Override
    public void add(BaseResult result) {
        switch (this.valueType) {
            case "RT":
                if (result instanceof WebResult)
                    this.lastResults[0] = ((WebResult) result).getResponseTime();
                else
                    this.lastResults[0] = ((PortResult) result).getResponseTime();
                break;
            case "RC":
                this.lastResults[0] = ((WebResult) result).getStatusCode();
                break;
            case "PS":
                this.lastResults[0] = ((WebResult) result).getPageSize();
                break;
            case "ST":
                this.lastResults[0] = ((PortResult) result).isActive();
                break;
            case "RTA":
                this.lastResults[0] = ((PingResult) result).getRtt();
                break;
            case "PL":
                this.lastResults[0] = ((PingResult) result).getPacketLost();
                break;
//            case "DFDS":
//                this.lastResults[0] = null;
//                break;
//            case "DUDS":
//                this.lastResults[0] = ((WebResult) result).getStatusCode();
//                break;
//            case "DBI":
//                this.lastResults[0] = ((WebResult) result).getStatusCode();
//                break;
//            case "DBO":
//                this.lastResults[0] = ((WebResult) result).getStatusCode();
//                break;
//            case "WSERT":
//                this.lastResults[0] = ((WebResult) result).getStatusCode();
//                break;
//            case "WAERC":
//                this.lastResults[0] = ((WebResult) result).getStatusCode();
//                break;
//            case "TRARHRT":
//                this.lastResults[0] = ((WebResult) result).getStatusCode();
//                break;
//            case "TRDHRT":
//                this.lastResults[0] = ((WebResult) result).getStatusCode();
//                break;
//            case "DTDS":
//                this.lastResults[0] = ((WebResult) result).getStatusCode();
//                break;
        }
    }

    @Override
    public Object[] get() {
        return this.lastResults;
    }

}
