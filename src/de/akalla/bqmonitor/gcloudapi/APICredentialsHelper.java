package de.akalla.bqmonitor.gcloudapi;

import java.io.File;
import java.io.FileInputStream;

import org.apache.log4j.Logger;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

/**
 * Initializes the Google Cloud Credentials by a key-file to access BiqQuery API
 *
 * @author Andreas Kalla, Jan. 2020
 */
public class APICredentialsHelper {
    private static final Logger log = Logger.getLogger(APICredentialsHelper.class);

    private GoogleCredentials credentials = null;

    public APICredentialsHelper(String[] args) throws Exception {
        File credFile = getFileTryArgumentOrDefault(args);
        credentials = getCredetialsFromKeyFile(credFile);
    }

    public APICredentialsHelper(File file) throws Exception {
        credentials = getCredetialsFromKeyFile(file);

    }

    /**
     * load key from file, if first argument is set if not set, expect Xkey.json in
     * current directory
     *
     * @param args
     * @return
     * @throws Exception
     */
    private File getFileTryArgumentOrDefault(String[] args) throws Exception {
        if (args != null && args.length == 1 && !args[0].isEmpty()) {
            // load key from file, if first argument is set
            try {
                return new File(args[0]);
            } catch (NullPointerException e) {
                throw new Exception(
                        "The given file path is not valid. Please load a key file from goolge cloud console (new service account with BigQuery access role) and store it into the current directory with name 'Xkey.json' or specify path to file. ");
            }
        } else {
            // try "Xkey.json" in current folder
            try {
                return new File("Xkey.json");
            } catch (NullPointerException e) {
                throw new Exception(
                        "Missing Xkey.json - Please load a key file from goolge cloud console (new service account with BigQuery access role) and store it into the current directory with name 'Xkey.json'. ");
            }
        }
    }

    private GoogleCredentials getCredetialsFromKeyFile(File file) throws Exception {
        try (FileInputStream serviceAccountStream = new FileInputStream(file)) {
            credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);

        } catch (Exception e) {
            log.error("🛑" + e);
            e.printStackTrace();
            throw e;
        }
        return credentials;

    }

    public GoogleCredentials getCredentials() throws Exception {
        return this.credentials;

    }
}
