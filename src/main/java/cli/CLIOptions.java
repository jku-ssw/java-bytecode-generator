package cli;

enum CLIOptions {
    L,
    H,
    F,
    LV,
    GA,
    LA,
    M,
    MC,
    ML,
    MP,
    MO,
    P,
    JLM,
    CF,
    CL,
    CD,
    MLI,
    WHILE,
    FOR,
    DOWHILE,
    IF,
    IBF,
    OS,
    AS,
    LS,
    BS,
    ALS,
    ABS,
    LBS,
    ALBS,
    MOPS,
    OF,
    DZ,
    FILENAME,
    DIRECTORY,
    XRUNS,
    SNIPPET;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}

