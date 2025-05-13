package pmedit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RunCmdTest {

    @Test
    public void testArgumentsWithSpacesStd(){
        var r = RunCmd.execCmd(new String[]{
                "python",
                "-c",
                "import sys;a='\\n'.join(sys.argv);print(a);print(a,file=sys.stderr)",
                "file 1",
                "file 2",
                "file 3",
        });
        assertTrue(r.ok());
        assertArrayEquals(new String[]{
                "-c",
                "file 1",
                "file 2",
                "file 3",
        }, r.outLines().toArray());
        assertArrayEquals(new String[]{
                "-c",
                "file 1",
                "file 2",
                "file 3",
        }, r.errLines().toArray());
    }
}
