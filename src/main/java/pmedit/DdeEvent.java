package pmedit;

import java.util.List;

public interface DdeEvent {

    void ddeExecute(List<String> command);

    void ddeActivate(List<String> command);

}
