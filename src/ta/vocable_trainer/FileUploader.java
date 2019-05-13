package ta.vocable_trainer;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements functionalities to upload a file to a server. In order to keep the program from blocking during the upload,
 * a curl process is started, that keeps running after the program has ended.
 */
public class FileUploader extends Thread{

    // server ip-address
    private String controlAddress;

    // server port
    private String controlPort;

    // vocable data to send
    private String vocables;

    private FileUploader(String controlAddress, String controlPort, String vocables){
        this.controlAddress = controlAddress;
        this.controlPort = controlPort;
        this.vocables = vocables;
    }

    public void run(){
        try {

            // not used, since it feels slow.
            // HttpResponse response = executeControlServerRequest(vocables);


            // runs in background after process terminated -> better user experience
            executeCurl(vocables);
        }
        catch (Exception e){
        }
    }

    /**
     * Starts a new thread that uploads the vocable data.
     * @param controlAddress Server ip-address.
     * @param controlPort Server port.
     * @param vocables Data to send.
     */
    static void uploadVocables(String controlAddress, String controlPort, String vocables){
        Thread instance = new FileUploader(controlAddress, controlPort, vocables);
        instance.start();
    }

    /**
     * Uploads the data. This method does not rely on external processes, but preents the program from exiting until
     * the data is uploaded.
     * @param message Data to send.
     * @return Http Response from the server.
     * @throws Exception If the communication with the server is unsuccessful.
     */
    private HttpResponse executeControlServerRequest(String message) throws Exception {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionSocketFactory)
                .build();

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        cm.setMaxTotal(100);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslConnectionSocketFactory)
                .setConnectionManager(cm)
                .build();

        HttpPost httppost = new HttpPost("https://" + controlAddress + ":" + controlPort + "/vocables");

        List<NameValuePair> params = new ArrayList<NameValuePair>(3);
        String credentials = createValidationCredentials();
        params.add(new BasicNameValuePair("msg", message));
        params.add(new BasicNameValuePair("cred", credentials));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        return httpclient.execute(httppost);
    }

    /**
     * Uploads the data using curl. Does not prevent the program from exiting.
     * @param message Data to send.
     */
    private void executeCurl(String message){
        message = URLEncoder.encode(message);
        String execString = "curl -k --data msg=" + message + "&cred=" + createValidationCredentials() + " https://" + controlAddress + ":" + controlPort + "/vocables &>/dev/null &";
        try {
            Runtime.getRuntime().exec(execString);
        }
        catch (IOException e){
            Utils.write("Could not start vocabulary uploading process.");
        }
    }

    /**
     * Method to create dummy credentials for the server.
     * @return
     */
    private static String createValidationCredentials(){
        return "vocableFileUploadRequest";
    }
}
