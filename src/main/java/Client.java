import java.util.HashMap;
import java.util.logging.*;
import test.*;
import handlers.*;
import static handlers.Parametri.*;


/*      MetaProducer class non implementata
 * 
 * 
 */

public class Client {

    public static final Logger logger = Logger.getLogger("neutmon");

    
    public static void main(String[] args) throws ControllerException{ // per via della handler.controller_send che non ha la catch

        // inserimento dei dati personalizzati

        // String operator = "default"; non implementata dal codice originale
        int duration = 0; //durata del test -> default 0 (POI DIVENTA DEFAULT TIME)
        String server_address = "localhost"; //server_address default "localhost"
        int server_port = 10000; //porta connessione socket server
        boolean run_http_test = false; //se bisgona fare test http
        String http_file = "http_test.txt"; //default file da scaricare dal server con test http
        //log_level
        //verbose
        //Level logLevel = Level.INFO;
        //String logFile = "neutmon.log";

        SimpleFormatter formatter = new SimpleFormatter() {
            @Override
            public synchronized String format(LogRecord lr) {
                return String.format("[%s][%s][%s] %s%n",
                        lr.getSourceClassName(),
                        lr.getSourceMethodName(),
                        lr.getLevel().getLocalizedName(),
                        lr.getMessage()
                );
            }
        };

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);

        logger.info("Initializing control connection to server");
        //if(server_address == null)
        //    server_address = DEFAULT_SERVER_ADDRESS;

        if(server_port != 10000)
            server_port = SERVER_PORT;
        
        if(duration != 0)
            duration = 0;

        // ############ INIZIO DEL TEST (for interface in interfaces) ###########
        // ######################################################################
        TCPBTTest bt_test = new TCPBTTest();
        TCPRandomTest ct_test = new TCPRandomTest();


        // ######### GESTIONE TEST HTTP ##############
        // ###########################################
        HashMap<Double, Integer> http_result = new HashMap<Double,Integer>();

        if(run_http_test){
            //if(nome_file != null)
            //    http_file = nome_file;
            //else
            http_file = DEFAULT_HTTP_TEST_PATH;
            TCPHTTPTest http_test = new TCPHTTPTest(server_address,http_file);

            try{
                logger.info("Instantiate HTTP tester");
                Tester tester = new Tester(80);
                logger.info("Connecting tester to server");
                tester.connect(server_address);
                logger.info("Starting test");
                tester.do_test(http_test, TEST_DOWNLINK_PHASE, TEST_SPEEDTEST_TYPE, http_result);
                logger.info("Closing test connection");
                tester.close_test_connection();
            }
            catch(TesterException te){
                if(te.errno == null)
                    logger.severe("Test failed " + te.msg + ", " + te.error );
                else
                    logger.severe("Test failed " + te.msg + ", " + te.error + ", " + te.errno );

                http_result.put((double)0, -1); // test fallito (non so se va bene)
            }
        }
        else
            logger.info("HTTP test not requested");
        
        // ####### FINE GESTIONE HTTP ###################

        logger.info("Initializing control connection to server");

        Connector connector = new Connector();
        try{
            connector.connect(server_address, server_port,30);
        }
        catch(ConnectorException ce){
            logger.severe("Test failed " + ce.msg);
        }

        Controller controller = new Controller(connector.connector_socket);

        //variabili dentro while
        int msg = 0;
        Integer port = null;
        TCPTest test_var = null;
        int phase = 0;
        int debug = 0;
        while(true){
            try{
                System.out.println("Entro nel while n. " + debug);
                if(debug == 5)
                    return;
                debug++;
                Integer[] received_buffer = controller.recv_control_msg();
                msg = received_buffer[0];
                port = received_buffer[1];
            }
            catch(ControllerException ce){
                logger.warning("Controller error, exiting: " + ce.msg);
            }
            

            // ####### GESTIONE MESSAGGIO DI CONTROLLO DAL SERVER #######

            if (msg == CONTROLLER_ABORT_MEASURE_MSG)
                logger.info("Received abort measure message");

            else if (msg == CONTROLLER_FINISH_MEASURE_MSG)
                logger.info("Received finish measure message");

            else if ( msg == CONTROLLER_START_UB_MSG){
                if (port == null){
                    logger.severe("Error: port is None");
                    continue;
                }
                logger.info("Received message start UB, port " + port);
                test_var = bt_test;
                phase = TEST_UPLINK_PHASE;
            }

            else if ( msg == CONTROLLER_START_UC_MSG){
                if (port == null){
                    logger.severe("Error: port is None");
                    continue;
                }
                logger.info("Received message start UC, port " + port);
                test_var = ct_test;
                phase = TEST_UPLINK_PHASE;
            }

            else if ( msg == CONTROLLER_START_DB_MSG){
                if (port == null){
                    logger.severe("Error: port is None");
                    continue;
                }
                logger.info("Received message start DB, port " + port);
                test_var = bt_test;
                phase = TEST_DOWNLINK_PHASE;
            }

            else if ( msg == CONTROLLER_START_DC_MSG){    
                if (port == null){
                    logger.severe("Error: port is None");
                    continue;
                }
                logger.info("Received message start DC, port " + port);
                test_var = ct_test;
                phase = TEST_DOWNLINK_PHASE;
            }

            else if ( msg == CONTROLLER_START_UT_MSG){
                if (port == null){
                    logger.severe("Error: port is None");
                    continue;
                }
                logger.info("Received message start UT, port " + port);
                test_var = ct_test;
                phase = TEST_UPLINK_PHASE;
            }

            else if ( msg == CONTROLLER_START_DT_MSG){
                if (port == null){
                    logger.severe("Error: port is None");
                    continue;
                }
                logger.info("Received message start DT, port " + port);
                test_var = ct_test;
                phase = TEST_DOWNLINK_PHASE;
            }

            else if ( msg == CONTROLLER_SEND_META_DATA_MSG){
                logger.info("Received message send meta data");
                HashMap<String, HashMap<Double, Integer>> meta_data = new HashMap<String, HashMap<Double, Integer>>();
                meta_data.put("http_test",http_result);
                logger.info("Metadata: " + meta_data);
                logger.info("Sending data to server");
                // controller.send_control_msg(CONTROLLER_OK_MSG, meta_data); va implementato bene controller in handler
                continue;
            }

            // ######### inizio dei test ###########
            try{

                HashMap<Double, Integer> result = new HashMap<Double, Integer>();
                logger.info("Instantiate tester");
                Tester tester = new Tester(port);

                try{
                    logger.info("Connecting tester to server");
                    tester.connect(server_address);
                    logger.info("Starting test");

                    for(int i = 0; i < 1; i++ ){ // sarebbe 2
                        logger.info("Starting test" + i);
                        tester.do_test(test_var, phase, i, result, duration);
                        if (phase == TEST_UPLINK_PHASE && i == TEST_SPEEDTEST_TYPE)
                            logger.info("Sleeping");
                            try{
                                Thread.sleep(10000); // non so se va bene
                            }
                            catch(InterruptedException ie){
                                logger.severe("Sleep Interrupted");
                            }
                        if( msg == CONTROLLER_START_UT_MSG || msg == CONTROLLER_START_DT_MSG)
                            break;
                    }

                    /* per vedere l'hashmap che si manda al server */
                    logger.info("Sending result to server");
                    System.out.println("######### Result");
                    /* 
                    for (Map.Entry<Double, Integer> entry : result.entrySet()) {
                        System.out.println("Chiave: " + entry.getKey() + " - Valore: " + entry.getValue());
                    }
                    */
                    
                    controller.send_control_msg(CONTROLLER_OK_MSG, result);
                }
                catch(TesterException te){
                    if(te.errno == null)
                        logger.severe("Test failed " + te.msg + ", " + te.error );
                    else
                        logger.severe("Test failed " + te.msg + ", " + te.error + ", " + te.errno );


                    if (te.error == TESTER_CONNECT_TIMEOUT_ERROR)
                        controller.send_control_msg(CONTROLLER_CLIENT_CONNECT_TIMEOUT_ERROR);

                    else if( te.error == TESTER_CONNECT_REFUSED_ERROR)
                        controller.send_control_msg(CONTROLLER_CLIENT_CONNECT_REFUSED_ERROR);

                    else if( te.error == TESTER_CONNECT_GENERIC_ERROR)
                        controller.send_control_msg(CONTROLLER_CLIENT_CONNECT_GENERIC_ERROR);

                    else if( te.error == TESTER_TEST_RESET_ERROR)
                        controller.send_control_msg(CONTROLLER_CLIENT_TEST_RESET_ERROR, result);

                    else if( te.error == TESTER_TEST_ABORT_ERROR)
                        controller.send_control_msg(CONTROLLER_CLIENT_TEST_ABORT_ERROR, result);

                    else if( te.error == TESTER_TEST_TIMEOUT_ERROR)
                        controller.send_control_msg(CONTROLLER_CLIENT_TEST_TIMEOUT_ERROR, result);

                    else if( te.error == TESTER_TEST_GENERIC_ERROR)
                        controller.send_control_msg(CONTROLLER_CLIENT_TEST_GENERIC_ERROR, result);

                }
                finally{
                    logger.info("Closing test connection");
                    tester.close_test_connection();
                }
            }
            catch(TesterException te){
                if(te.errno == null)
                        logger.severe("Test failed " + te.msg + ", " + te.error );
                    else
                        logger.severe("Test failed " + te.msg + ", " + te.error + ", " + te.errno );

                if (te.error == TESTER_INIT_CLIENT_ERROR)
                    controller.send_control_msg(CONTROLLER_CLIENT_TEST_INIT_ERROR);
            }
        }
    }
    
}

class SimpleLogFormatter extends SimpleFormatter {
    public String format(LogRecord record) {
        return String.format("[%s][%s][%s] %s%n",
                        record.getSourceClassName(),
                        record.getSourceMethodName(),
                        record.getLevel().getLocalizedName(),
                        record.getMessage()
                );
    }
}


