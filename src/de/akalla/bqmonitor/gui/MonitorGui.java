package de.akalla.bqmonitor.gui;

import java.awt.*;
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Logger;

import de.akalla.bqmonitor.MonitorMain;
import de.akalla.bqmonitor.entities.JobEntity;
import de.akalla.bqmonitor.util.Utils;

/**
 * shows main window
 *
 * @author Andreas Kalla, Feb. 2020
 */
public class MonitorGui {
    private static final Logger log = Logger.getLogger(MonitorGui.class);
    private static final boolean INITIAL_ALWAYS_ON_TOP = true;
    private static final boolean INITIAL_SCROLL_TO_TOP = true;
    private static final int MAX_LIST_SIZE = 1000;
//
//    private File logDir;
//    private File keyfile;

    private JFrame window;

    private JLabel kpi1SinceStart;
    private JLabel kpi2LastHour;
    private JLabel kpi3Today;
    private JLabel kpi4SinceReset;
    private long lastRecentTimestamp = 0;
    private boolean scrollListToTop = true;

    private JList<JobEntity> queryList;
    private DefaultListModel<JobEntity> model;

    private JTextPane pane;
    private StyledDocument doc;
    private JSplitPane splitPane;
    private GridBagLayout layout;
    private JPanel controls;
    private JScrollPane paneScroller;
    private MonitorController monitorController;

    public MonitorGui(MonitorController monitorController) {
        this.monitorController = monitorController;
    }

    public void showGui() {
        SwingUtilities.invokeLater(() -> {
            try {
                initSwingWindow();
                initMenu();
                initTopBoxes();
                initSplitPane();
                initControlsAndShow();

            } catch (Exception e) {
                log.error(e);
                e.printStackTrace();
            }
        });
    }

    private void initSwingWindow() {
        window = new JFrame("BQ-Monitor");
        layout = new GridBagLayout();
        controls = new JPanel(layout);
    }

    /**
     * repaints the current status of the billed Enum to the GUI Label components
     */
    public void updateKPIs() {
        SwingUtilities.invokeLater(() -> {
            long currentTs = System.currentTimeMillis();
            long lastHourTs = currentTs - 60 * 60 * 1000;
            long last24hours = currentTs - 24 * 60 * 60 * 1000;
            kpi1SinceStart.setText(generateKpiHtml(getBilledBytesSince(0), "billed since start"));
            kpi2LastHour.setText(generateKpiHtml(getBilledBytesSince(lastHourTs), "billed last hour"));
            kpi3Today.setText(generateKpiHtml(getBilledBytesSince(last24hours), "billed last 24h"));
            kpi4SinceReset.setText(generateKpiHtml(getBilledBytesSince(lastRecentTimestamp), "since last reset"));
        });
    }


    /**
     * call with timestamp in past
     *
     * @param time
     */
    public long getBilledBytesSince(long time) {
        long res = 0;
        for (int i = 0; i < model.size(); i++) {
            JobEntity job = model.get(i);
            if (job.getStatBytesBilled() != null && job.getStatStartTime() >= time) {
                res += job.getStatBytesBilled();
            }
        }
        return res;
    }

    /**
     * Produces a HTML string with the current value of the KPI to show in the
     * application window. e.g. 17,4 GiB
     */
    public String generateKpiHtml(long bytesBilled, String subtext) {
        return "<html><font size=\"20\">" + Utils.humanReadableByteCountBin(bytesBilled) + "</font><br>" + subtext + "</html>";
    }


//    KPI1_BILLED_START(""),
//    KPI2_BILLED_HOUR("),
//    KPI3_BILLED_TODAY(),
//    KPI4_BILLED_RESET();

    private void initMenu() {
        final JMenuBar menuBar;
        final JMenu menueOne;
        final JMenu menueTwo;
        final JMenu menueThree;
        final JMenuItem info;
        final JMenuItem load;
        final JMenuItem reset;
        final JCheckBoxMenuItem alwaysOnTop;
        final JCheckBoxMenuItem scollListToTopCheckbox;

        menuBar = new JMenuBar();

        // Menu 1
        menueOne = new JMenu("Settings");
        menueOne.setMnemonic(KeyEvent.VK_S);

        // Menu 1 Option 1
        alwaysOnTop = new JCheckBoxMenuItem("Stay always on top", INITIAL_ALWAYS_ON_TOP);
        alwaysOnTop.setMnemonic(KeyEvent.VK_T);
        alwaysOnTop.addChangeListener(e -> {
            window.setAlwaysOnTop(alwaysOnTop.isSelected());
        });
        menueOne.add(alwaysOnTop);
        menuBar.add(menueOne);

        // Menu 1 Option 2
        scollListToTopCheckbox = new JCheckBoxMenuItem("Scroll List automatically to top", INITIAL_SCROLL_TO_TOP);
        scollListToTopCheckbox.setMnemonic(KeyEvent.VK_T);
        scollListToTopCheckbox.addChangeListener(e -> {
            scrollListToTop = scollListToTopCheckbox.isSelected();
        });
        menueOne.add(scollListToTopCheckbox);
        menuBar.add(menueOne);

        // Menu 2 Option 1

        menueTwo = new JMenu("Commands");
        menueTwo.setMnemonic(KeyEvent.VK_C);

        load = new JMenuItem("Load the 20 most recent logs", KeyEvent.VK_I);
        load.addActionListener(e -> {
            monitorController.triggerLoadingInitialForList(MonitorMain.LOAD_MAXJOBS_INITIAL);
        });
        menueTwo.add(load);
        // Menu 2 Option 1

        reset = new JMenuItem("Reset right KPI", KeyEvent.VK_I);
        reset.addActionListener(e -> {
            this.lastRecentTimestamp = System.currentTimeMillis();
            updateKPIs();
        });
        menueTwo.add(reset);
        menuBar.add(menueTwo);

        // Menu 3
        menueThree = new JMenu("Help");
        menueThree.setMnemonic(KeyEvent.VK_H);

        // Menu 3 Option 1
        info = new JMenuItem("Info", KeyEvent.VK_I);
        info.addActionListener(e -> {
            JOptionPane.showMessageDialog(window, "(c) Andreas Kalla, Feb. 2020, bq-monitor@ca.akalla.de");
        });
        menueThree.add(info);
        menuBar.add(menueThree);
        window.setJMenuBar(menuBar);
    }

    private void initTopBoxes() {
        kpi1SinceStart = new JLabel();
        layout.setConstraints(kpi1SinceStart, new GridBagConstraints(0, 0, 1, 1, 50, 0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, Utils.insets(5, 5, 5, 5), 0, 20));
        controls.add(kpi1SinceStart);

        kpi2LastHour = new JLabel();
        layout.setConstraints(kpi2LastHour, new GridBagConstraints(1, 0, 1, 1, 50, 0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, Utils.insets(5, 5, 5, 5), 0, 20));
        controls.add(kpi2LastHour);

        kpi3Today = new JLabel();
        layout.setConstraints(kpi3Today, new GridBagConstraints(2, 0, 1, 1, 50, 0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, Utils.insets(5, 5, 5, 5), 0, 20));
        controls.add(kpi3Today);

        kpi4SinceReset = new JLabel();
        layout.setConstraints(kpi4SinceReset, new GridBagConstraints(3, 0, 1, 1, 50, 0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, Utils.insets(5, 5, 5, 5), 0, 20));
        controls.add(kpi4SinceReset);

        updateKPIs();
    }

    private void initSplitPane() {
        // left side
        model = new DefaultListModel<>();
        queryList = new JList<JobEntity>(model);
        queryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        queryList.setCellRenderer(new ListCellRenderer<JobEntity>() {
            public Component getListCellRendererComponent(JList<? extends JobEntity> list, JobEntity value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel r = new JLabel("<html>" + value + "</html>");

                Border eBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
                Border lBorder = BorderFactory.createLineBorder(Color.GRAY, 1);
                r.setBorder(BorderFactory.createCompoundBorder(lBorder, eBorder));

                if (isSelected) {
                    r.setOpaque(true);
                    r.setBackground(Color.LIGHT_GRAY);

                }
                return r;
            }

        });

        queryList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent le) {
                int idx = queryList.getSelectedIndex();
                setPaneContent(idx);
            }
        });

        JScrollPane queryListScrollPane = new JScrollPane(queryList);
        // right side
        GridBagLayout detailsGridBagLayout = new GridBagLayout();
        JPanel detailsPanel = new JPanel(detailsGridBagLayout);

        pane = new JTextPane();
        pane.setBorder(BorderFactory.createLineBorder(Color.WHITE,3));
        doc = pane.getStyledDocument();
        Style style = pane.addStyle("Color Style", null);
        StyleConstants.setForeground(style, Color.DARK_GRAY);
        try {
            doc.insertString(doc.getLength(), "...", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        paneScroller = new JScrollPane(pane);
        detailsGridBagLayout.setConstraints(paneScroller, new GridBagConstraints(0, 0, 1, 1, 50, 50,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, Utils.insets(5, 5, 5, 5), 0, 0));
        detailsPanel.add(paneScroller);

        // Create a split pane with the two scroll panes in it.
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, queryListScrollPane, detailsPanel);
        layout.setConstraints(splitPane, new GridBagConstraints(0, 1, 4, 1, 50, 50, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, Utils.insets(5, 5, 5, 5), 0, 0));
        controls.add(splitPane);

    }

    private void setPaneContent(int idx) {
        if (idx != -1) {
            doc = pane.getStyledDocument();
            try {
                doc.remove(0, doc.getLength());
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
            Style style = pane.addStyle("Color Style", null);
            StyleConstants.setForeground(style, Color.DARK_GRAY);
            try {
                doc.insertString(0, queryList.getSelectedValue().toHumanString(), style);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            pane.setCaretPosition(0);
        } else {
            System.out.println("selection triggered, but nothing selected..");
        }
    }

    private void initControlsAndShow() {
        window.add(controls);
        window.setAlwaysOnTop(INITIAL_ALWAYS_ON_TOP);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setBounds(200, 200, 800, 500);
        window.setVisible(true);
        splitPane.setDividerLocation(0.4); // must be called after set visible
    }

    /**
     * gets the position in the model list for that job or -1 if not in the model
     *
     * @param job
     * @return index or -1
     */
    private int getIndexInModel(JobEntity job) {
        if (job == null || job.getJobId() == null) {
            System.out.println(job.toString());
            throw new RuntimeException("job or jobs fullJobId cannot be null");
        }
        String compareTo = job.getJobId();
        for (int i = 0; i < model.getSize(); i++) {
            JobEntity e = model.get(i);
            if (compareTo.equals(e.getJobId())) {
                return i;
            }
        }
        return -1;
    }


    /**
     * add the element at the top position, pushes others down select top element
     * automatically (if menu setting on and list valid only)
     * <p>
     * or updates the given element if in list already. (fullJobID is identifier)
     *
     * @param job
     */
    public void insertToListAndKPis(JobEntity job) {
        if (job == null) {
            throw new RuntimeException("job cannot be null");
        }
        SwingUtilities.invokeLater(() -> {
            int index = getIndexInModel(job);
            log.info("index=" + index);

            if (index > -1) {
                // needs update
                log.info("insertToList entry id=" + job.getJobId() + " already in list at index" + index);

                model.set(index, job); // updates the element at that position
                log.info(job.getStatStartTime());
                if (scrollListToTop) {
                    queryList.setSelectedIndex(0);
                }

            } else {
                // needs insert
                log.info("insertToList entry id=" + job.getJobId() + " new insert at top");

                model.add(0, job); // 0 means top of list
                index = 0;
                if (scrollListToTop) {
                    queryList.setSelectedIndex(0);
                }
                checkModelOverflow();
            }
            //update pane
            if (queryList.getSelectedIndex() == index) {

                log.debug("setPaneContent index=" + index);
                setPaneContent(index);
            }

            //KPI update
            if (job.isDone()) {
                updateKPIs();
            }
            monitorController.jsonExport(model);
        });
    }

    private void checkModelOverflow() {
        SwingUtilities.invokeLater(() -> {
            if (model.getSize() > MAX_LIST_SIZE) {
                model.removeRange(MAX_LIST_SIZE, model.size() - 1);

            }
        });
    }

    public JFrame getWindow() {
        return window;
    }
}
