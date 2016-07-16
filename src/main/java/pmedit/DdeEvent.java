package pmedit;

import java.util.List;

public interface DdeEvent {
	
    public void ddeExecute(List<String> command);

    public void ddeActivate(List<String> command);

}
