package de.akalla.bqmonitor.gui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.*;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.akalla.bqmonitor.entities.JobEntity;
import de.akalla.bqmonitor.gcloudapi.APIJobFinder;
import de.akalla.bqmonitor.gcloudapi.JobController;
import de.akalla.bqmonitor.logaccess.LogAccessController;
import de.akalla.bqmonitor.util.Utils;

/**
 * class holds the current ist of monitored queries it hadles new incomming
 * statisic data by tableau logs and provides query functions like aggregations
 * as well as saves to disk (json) and load from there
 *
 * main data provider for the GUI
 *
 * @author Andreas Kalla, Feb. 2020
 */
public class MonitorController {
    private static final Logger log = Logger.getLogger(MonitorController.class);

    private MonitorGui monitorGui;
    private JobController jobController;
    private LogAccessController logAccessController;
    private APIJobFinder apiJobFinder;

    public MonitorController(){
        gson = new GsonBuilder().create();
    }

    /**
     * starts all the needed classes in given order initiates showing the gui and
     * starting the log watcher
     * 
     * @param key
     * @param logDir
     */
    public void start(File key, File logDir) {
        try {
            monitorGui = new MonitorGui(this);
            apiJobFinder = new APIJobFinder();
            jobController = new JobController(key, monitorGui, apiJobFinder);
            apiJobFinder.init(jobController);
            logAccessController = new LogAccessController(logDir, jobController);

            log.info(" >>> START GUI <<<");
            monitorGui.showGui();

            log.info(" >>> START LOG SCANNER <<<");
            logAccessController.startScanning();

        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
            Utils.notifyUserAboutException(e);
        }
    }

    public void triggerLoadingInitialForList(int num) {
        jobController.loadLatestEntriesInverseOrderForList(num);
    }

    
    
    public void loadids(int num) {
        jobController.loadids(num);
    }
    
 
    
    public void testInsert() {
        JobEntity e = new JobEntity();
        e.setJobId("sdksjjhdui");
        e.setUserEmail("andreas@kalla.de");
        e.setStatStartTime(System.currentTimeMillis());
        e.setStatEndTime(System.currentTimeMillis() + 9901L);
        e.setStatement("select dnummy");
        e.setStatBytesBilled(2000000000L);
        e.setDone(true);
        e.setStatCacheHit(false);
        e.validate();
        monitorGui.insertToListAndKPis(e);

        e = new JobEntity();
        e.setJobId("ttr");
        e.setUserEmail("andreas@kalla.de");
        e.setStatStartTime(System.currentTimeMillis());
        e.setStatEndTime(System.currentTimeMillis() + 10010L);
        e.setStatement("fgfgfselect dnummy");
        e.setStatBytesBilled(9000000000L);
        e.setDone(true);
        e.setStatCacheHit(false);
        e.validate();
        monitorGui.insertToListAndKPis(e);

    }

    private Gson gson;

    public void jsonExport(DefaultListModel<JobEntity> model) {
        long start = System.currentTimeMillis();
        int size = model.size();
        File file = new File("bq-monitor.json");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int i=0; i<size;i++){
                String j = gson.toJson(model.get(i));
                writer.write(j);
                if (i+1!=size) {
                    writer.write("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("json export to bq-monitor.json took"+(System.currentTimeMillis()-start)+" ms");

    }
}
