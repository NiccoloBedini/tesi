package test;

public class Parametri {
    public static int DEFAULT_TEST_DURATION = 10;  // seconds;
    public static int DEFAULT_TRANSFER_DIMENSION = 1024 * 1024;  // Bytes
    public static int DEFAULT_BT_TRANSFER_DIMENSION = 16397;  // Bytes
    public static int DEFAULT_HTTP_TRANSFER_DIMENSION = 260 + 9437184 + 2;  // Bytes (header + file)
    public static final int BITTORRENT_PORT = 6881;
    public static final int BITTORRENT_ALTERNATIVE_PORT = 51413;
    public static final int BITTORRENT_REQUEST_LENGTH = 13;
    public static final int BITTORRENT_REQUEST_TOTAL_LENGTH = 17;
    public static final int BITTORRENT_REQUEST_TYPE = 0x6;
    public static final int BITTORRENT_BLOCK_DIMENSION = 0x4000;
    public static final int BITTORRENT_PIECE_DIMENSION = 0x20000;
    public static final int BITTORRENT_START_INDEX = 0x0;
    public static final int BITTORRENT_START_OFFSET = 0x0;
    public static final int BITTORRENT_RESPONSE_LENGTH = 0x9;
    public static final int BITTORRENT_PIECE_TYPE = 0x7;
    public static final int NUMBER_OF_REQUESTS = 80;
}
