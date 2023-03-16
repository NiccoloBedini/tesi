package handlers;

import java.util.logging.Logger;

public class Parametri {
    public static String DEFAULT_SERVER_ADDRESS = "localhost";
    public static String DEFAULT_HTTP_TEST_PATH = "http_test.txt";
    public static String SERVER_BINDING_ADDRESS = "0.0.0.0";
    public static int  SERVER_PORT = 10000;
    public static int BT_PORT = 6881;
    public static int ALT_BT_PORT = 53674;
    public static int TT_PORT = 54894;
    // ALT_BT_PORTS = range(50000, 65536);
    public static int BACKLOG_QUEUE_SIZE = 5;
    public static int ROLE_SERVER = 0;
    public static int ROLE_CLIENT = 1;

    public static int CONTROLLER_START_UB_MSG = 0;
    public static int CONTROLLER_START_UC_MSG = 1;
    public static int CONTROLLER_START_DB_MSG = 2;
    public static int CONTROLLER_START_DC_MSG = 3;
    public static int CONTROLLER_START_UT_MSG = 4;
    public static int CONTROLLER_START_DT_MSG = 5;
    public static int CONTROLLER_SEND_META_DATA_MSG = 6;
    public static int CONTROLLER_ABORT_MEASURE_MSG = 7;
    public static int CONTROLLER_FINISH_MEASURE_MSG = 8;
    public static int CONTROLLER_OK_MSG = 9;
    public static int CONTROLLER_CLIENT_CONNECT_REFUSED_ERROR = 10;  // Connection refused or reset or abort timeout on selected port
    public static int CONTROLLER_CLIENT_CONNECT_TIMEOUT_ERROR = 11;  // Connection timeout on selected port
    public static int CONTROLLER_CLIENT_CONNECT_GENERIC_ERROR = 12;  // Connection generic error
    public static int CONTROLLER_CLIENT_TEST_RESET_ERROR = 13;  // Connection reset when testing
    public static int CONTROLLER_CLIENT_TEST_ABORT_ERROR = 14;  // Connection aborted when testing
    public static int CONTROLLER_CLIENT_TEST_TIMEOUT_ERROR = 15;  // Connection timeout when testing
    public static int CONTROLLER_CLIENT_TEST_GENERIC_ERROR = 16;  // Generic error when testing
    public static int CONTROLLER_CLIENT_TEST_INIT_ERROR = 17;  // Generic error when initialising tester

    public static int TESTER_OK = 9;
    public static int TESTER_CONNECT_REFUSED_ERROR = 10;
    public static int TESTER_CONNECT_TIMEOUT_ERROR = 11;
    public static int TESTER_CONNECT_GENERIC_ERROR = 12;
    public static int TESTER_TEST_RESET_ERROR = 13;
    public static int TESTER_TEST_ABORT_ERROR = 14;
    public static int TESTER_TEST_TIMEOUT_ERROR = 15;
    public static int TESTER_TEST_GENERIC_ERROR = 16;
    public static int TESTER_INIT_CLIENT_ERROR = 17;
    public static int TESTER_INIT_SERVER_ERROR = 18;
    public static int TESTER_ACCEPT_TIMEOUT_ERROR = 19;
    public static int TESTER_ACCEPT_GENERIC_ERROR = 20;

    public static int TEST_UPLINK_PHASE = 0;
    public static int TEST_DOWNLINK_PHASE = 1;

    public static int TEST_SPEEDTEST_TYPE = 0;
    public static int TEST_TRACEROUTE_TYPE = 1;

    public static Logger logger = Logger.getLogger("neutmon.HANDLERS");
}
