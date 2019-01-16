package com.munichre.bsmrv;

import java.io.File;
import java.io.IOException;
import de.gdv.bsm.intern.csv.LineFormatException;
import de.gdv.bsm.intern.params.VuParameter;

public class MrVuParameter extends VuParameter {

    private static final String MR_PARAMETER_ZEITUNABHAENGIG_FILENAME = "zeitunabh.RV.csv";
    private static final String MR_PARAMETER_ZEITABHAENGIG_FILENAME = "zeitabh.RV.csv";

    private final MrParameterZeitunabhaengig mrParameterZeitUnabhaengig;
    private final MrParameterZeitabhaengig mrParameterZeitabhaengig;

    public MrVuParameter(File transferVerzeichnis) throws IOException, LineFormatException {
        super(transferVerzeichnis);

        mrParameterZeitUnabhaengig = new MrParameterZeitunabhaengig(
                new File(transferVerzeichnis, MR_PARAMETER_ZEITUNABHAENGIG_FILENAME));
        mrParameterZeitabhaengig = new MrParameterZeitabhaengig(
                new File(transferVerzeichnis, MR_PARAMETER_ZEITABHAENGIG_FILENAME));
    }

    public MrParameterZeitabhaengig getMrParameterZeitabhaengig() {
        return mrParameterZeitabhaengig;
    }

    public MrParameterZeitunabhaengig getMrParameterZeitunabhaengig() {
        return mrParameterZeitUnabhaengig;
    }
}
