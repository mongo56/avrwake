package avrwake.gui;

import avrwake.comm.AVRControl;
import avrwake.sched.alarm.AlarmSched;
import avrwake.sched.alarm.AlarmTask;
import avrwake.sched.sleep.SleepSched;
import avrwake.sched.sleep.SleepTask;
import avrwake.sched.time.TimeSched;
import avrwake.sched.time.TimeTask;
import avrwake.sched.update.UpdateSched;
import avrwake.sched.update.UpdateTask;
import avrwake.shared.Common;
import avrwake.shared.ExtensionFileFilter;
import avrwake.structures.PreferencesStructure;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Blaz Majcen <bm@telaris.si>
 */
public class Main extends javax.swing.JFrame {
    // local variables
    private Preferences prefs;
    public PreferencesStructure prefsData;

    public AVRControl avrControl;
    
    private TimeSched timeSched;
    private AlarmTask task;
    private AlarmSched alarmSched, wAlarmSched;
    private SleepSched sleepSched;
    private UpdateSched updateSched;
    
    private boolean firstStart = true;
    
    private final Color SPINNER_FG = new Color(255, 200, 50);
    private final Color SPINNER_BG = new Color(60, 60, 60);
    
    private String sourceString = "SIBD";
    private String modeString = "MSMULTI CH IN";
    
    public Main() {
        initComponents();
        setMainIcon();
        setPreferences();
        startClock();
        startUpdateAVR();
    }
    
    private class OnExit extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            if (avrControl.isConnected()) {
                avrControl.disconnect();
            }
            prefsData.saveSettings(prefs);
            if (alarmSched != null) {
                alarmSched.shutdown();
            }
            if (wAlarmSched != null) {
                wAlarmSched.shutdown();
            }
            System.exit(0);
        }
    }
    private void setMainIcon() {
        this.setIconImage(new javax.swing.ImageIcon(getClass().getResource("/avrwake/gui/resources/main.png")).getImage());
        addWindowListener(new OnExit());
        setSpinnerLook();
        setHyperlinkText();
        firstStart = false;
    }
    
    private void setMainTitleText(String text) {
        this.setTitle("AVRControl + [" + text + "]");
    }
    
    private void setHyperlinkText() {
        try {
            mailLabel.setLabel("mongoz@gmail.com");
            mailLabel.setURL(new URL("mailto:mongoz@gmail.com"));
        } catch (MalformedURLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void setSpinnerLook() {
        setLook((JSpinner.DateEditor)alarmHourSpinner.getEditor(), SPINNER_BG, SPINNER_FG);
        setLook((JSpinner.DateEditor)alarmMinuteSpinner.getEditor(), SPINNER_BG, SPINNER_FG);
        setLook((JSpinner.DateEditor)wAlarmHourSpinner.getEditor(), SPINNER_BG, SPINNER_FG);
        setLook((JSpinner.DateEditor)wAlarmMinuteSpinner.getEditor(), SPINNER_BG, SPINNER_FG);
        setLook((JSpinner.DateEditor)sleepHourSpinner.getEditor(), SPINNER_BG, SPINNER_FG);
        setLook((JSpinner.DateEditor)sleepMinuteSpinner.getEditor(), SPINNER_BG, SPINNER_FG);
    }
    
    private void setLook(JSpinner.DateEditor editor, Color bg, Color fg) {
        editor.getTextField().setBackground(bg);
        editor.getTextField().setForeground(fg);
        editor.getTextField().setHorizontalAlignment(JTextField.CENTER);
    }
    
    private void setPreferences() {
        prefs = Preferences.userNodeForPackage(this.getClass());
        prefsData = new PreferencesStructure(prefs);
        prefsData.loadSettings();
        
        avrControl = new AVRControl(prefsData);
        avrControl.connect();
        
        presetValues();
    }
    
    private void presetValues() {
        avrHostnameField.setText(prefsData.getAvrHostname());
        
        Date curAlarmDate = getWeekdayAlarmDate();
        alarmEnable.setSelected(prefsData.isAlarmEnabled());
        checkAlarm();
        weekendAlarmEnable.setSelected(prefsData.iswAlarmEnabled());
        checkWeekendAlarm();
        alarmHourSpinner.getModel().setValue(curAlarmDate);
        alarmMinuteSpinner.getModel().setValue(curAlarmDate);
        musicPathField.setText(prefsData.getMusicName());
        
        curAlarmDate = getWeekendAlarmDate();
        wAlarmHourSpinner.getModel().setValue(curAlarmDate);
        wAlarmMinuteSpinner.getModel().setValue(curAlarmDate);
        wMusicPathField.setText(prefsData.getwMusicName());
        alarmVolumeSlider.setValue(prefsData.getAlarmVolume());
        
        sleepTimeEnable.setSelected(prefsData.isSleepEnabled());
        curAlarmDate = getSleepAlarmDate();
        sleepHourSpinner.getModel().setValue(curAlarmDate);
        sleepMinuteSpinner.getModel().setValue(curAlarmDate);
        checkSleep();
        loadAVRValues();
    }
    
    private Date getWeekdayAlarmDate() {
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, prefsData.getAlarmHour());
        calendar.set(Calendar.MINUTE, prefsData.getAlarmMinute());
        return calendar.getTime();
    }
    
    private Date getWeekendAlarmDate() {
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, prefsData.getwAlarmHour());
        calendar.set(Calendar.MINUTE, prefsData.getwAlarmMinute());
        return calendar.getTime();
    }
    
    private Date getSleepAlarmDate() {
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, prefsData.getSleepHour());
        calendar.set(Calendar.MINUTE, prefsData.getSleepMinute());
        return calendar.getTime();
    }
    
    public void loadAVRValues() {
        new Thread() {
            @Override
            public void run() {
                powerButton.setSelected(avrControl.isPowered());
                muteButton.setSelected(avrControl.isMuted());
                int curVolume = avrControl.getCurrentVolume();
                if (curVolume != -1) {
                    volumeSlider.setValue(curVolume);
                }
                sourceString = avrControl.getSource();
                modeString = avrControl.getMode();
                curSourceMode.setText(sourceString + " : " + modeString);
            }
        }.start();
    }

    private void saveSettings() {
        prefsData.setAvrHostname(avrHostnameField.getText());
    }
    
    private void startClock() {
        TimeTask timeTask = new TimeTask(prefsData, timeLabel, dateLabel, alarmTimeLeft, wAlarmTimeLeft);
        timeSched = new TimeSched(timeTask, TimeSched.DEFAULT_INTERVAL);
        timeSched.startTimeDateUpdate();
    }
    
    private void startUpdateAVR() {
        UpdateTask updateTask = new UpdateTask(this);
        updateSched = new UpdateSched(updateTask, UpdateSched.DEFAULT_INTERVAL);
        updateSched.startUpdatingAVRValues();
    }
    
    private void startAlarm() {
        task = new AlarmTask(this, prefsData.getMusicPath());
        alarmSched = new AlarmSched(task);
        alarmSched.startAlarm(getDateField((Date)alarmHourSpinner.getModel().getValue(), Calendar.HOUR_OF_DAY), 
                getDateField((Date)alarmMinuteSpinner.getModel().getValue(), Calendar.MINUTE), false);
    }
    
    private void stopAlarm() {
        if (alarmSched != null) {
            if (alarmSched.isRunning()) {
                alarmSched.stopAlarm(false);
            }
        }
    }
    
    private void startWeekendAlarm() {
        task = new AlarmTask(this, prefsData.getwMusicPath());
        wAlarmSched = new AlarmSched(task);
        wAlarmSched.startAlarm(getDateField((Date)wAlarmHourSpinner.getModel().getValue(), Calendar.HOUR_OF_DAY), 
                getDateField((Date)wAlarmMinuteSpinner.getModel().getValue(), Calendar.MINUTE), true);
    }
    
    private void stopWeekendAlarm() {
        if (wAlarmSched != null) {
            if (wAlarmSched.isRunning()) {
                wAlarmSched.stopAlarm(true);
            }
        }
    }
    
    private void startSleep() {
        SleepTask sleep = new SleepTask(this);
        sleepSched = new SleepSched(sleep);
        sleepSched.startAlarm(getDateField((Date)sleepHourSpinner.getModel().getValue(), Calendar.HOUR_OF_DAY), 
                getDateField((Date)sleepMinuteSpinner.getModel().getValue(), Calendar.MINUTE));
    }
    
    private void stopSleep() {
        if (sleepSched != null) {
            if (sleepSched.isRunning()) {
                sleepSched.stopAlarm();
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sourceGroup = new javax.swing.ButtonGroup();
        tabsPane = new javax.swing.JTabbedPane();
        avrControlTab = new javax.swing.JPanel();
        volumeSlider = new javax.swing.JSlider();
        volumeLabel = new javax.swing.JLabel();
        powerButton = new javax.swing.JToggleButton();
        refreshButton = new javax.swing.JButton();
        muteButton = new javax.swing.JToggleButton();
        controlStatusBar = new javax.swing.JPanel();
        controlStatusText = new javax.swing.JLabel();
        controlStatusIcon = new javax.swing.JLabel();
        masterVolumeLabel = new javax.swing.JLabel();
        sourceSelectLabel = new javax.swing.JLabel();
        sourceBD = new javax.swing.JButton();
        sourceTV = new javax.swing.JButton();
        surroundModeLabel = new javax.swing.JLabel();
        mchStereo = new javax.swing.JButton();
        mchIn = new javax.swing.JButton();
        curSourceMode = new javax.swing.JLabel();
        alarmTab = new javax.swing.JPanel();
        alarmStatusBar = new javax.swing.JPanel();
        alarmStatusText = new javax.swing.JLabel();
        alarmStatusIcon = new javax.swing.JLabel();
        timePanel = new javax.swing.JPanel();
        dateLabel = new javax.swing.JLabel();
        timeLabel = new javax.swing.JLabel();
        alarmHourSpinner = new javax.swing.JSpinner();
        alarmMinuteSpinner = new javax.swing.JSpinner();
        musicPathField = new javax.swing.JTextField();
        musicBrowseButton = new javax.swing.JButton();
        alarmEnable = new javax.swing.JCheckBox();
        weekendAlarmEnable = new javax.swing.JCheckBox();
        wAlarmHourSpinner = new javax.swing.JSpinner();
        wAlarmMinuteSpinner = new javax.swing.JSpinner();
        wMusicPathField = new javax.swing.JTextField();
        wMusicBrowseButton = new javax.swing.JButton();
        weekdaysLabel = new javax.swing.JLabel();
        weekendsLabel = new javax.swing.JLabel();
        alarmVolumeLabel = new javax.swing.JLabel();
        alarmVolumeSlider = new javax.swing.JSlider();
        stopAlarmButton = new javax.swing.JButton();
        sleepTimeEnable = new javax.swing.JCheckBox();
        sleepHourSpinner = new javax.swing.JSpinner();
        sleepMinuteSpinner = new javax.swing.JSpinner();
        sleepLabel = new javax.swing.JLabel();
        alarmTimeLeft = new javax.swing.JLabel();
        wAlarmTimeLeft = new javax.swing.JLabel();
        settingsTab = new javax.swing.JPanel();
        avrHostnameLabel = new javax.swing.JLabel();
        avrHostnameField = new javax.swing.JTextField();
        avrConnectionLabel = new javax.swing.JLabel();
        mailBar = new javax.swing.JPanel();
        mailLabel = new avrwake.gui.element.HyperlinkLabel();
        avrStringLabel = new javax.swing.JLabel();
        avrStringField = new javax.swing.JTextField();
        avrTestButton = new javax.swing.JButton();
        testingLabel = new javax.swing.JLabel();
        testScrollPane = new javax.swing.JScrollPane();
        testTextArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("avrwake/gui/Bundle"); // NOI18N
        setTitle(bundle.getString("Main.title")); // NOI18N
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        tabsPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabsPaneStateChanged(evt);
            }
        });

        avrControlTab.setBackground(new java.awt.Color(60, 60, 60));

        volumeSlider.setBackground(new java.awt.Color(50, 50, 50));
        volumeSlider.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        volumeSlider.setForeground(new java.awt.Color(255, 200, 50));
        volumeSlider.setMajorTickSpacing(5);
        volumeSlider.setMaximum(20);
        volumeSlider.setMinimum(-80);
        volumeSlider.setMinorTickSpacing(1);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintTrack(false);
        volumeSlider.setSnapToTicks(true);
        volumeSlider.setValue(-40);
        volumeSlider.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(200, 150, 30), 1, true));
        volumeSlider.setDoubleBuffered(true);
        volumeSlider.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                volumeSliderMouseWheelMoved(evt);
            }
        });
        volumeSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                volumeSliderMouseReleased(evt);
            }
        });
        volumeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                volumeSliderStateChanged(evt);
            }
        });

        volumeLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        volumeLabel.setForeground(new java.awt.Color(255, 200, 50));
        volumeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        volumeLabel.setText(bundle.getString("Main.volumeLabel.text")); // NOI18N

        powerButton.setBackground(new java.awt.Color(60, 60, 60));
        powerButton.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        powerButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/avrwake/gui/resources/power_off.png"))); // NOI18N
        powerButton.setText(bundle.getString("Main.powerButton.text")); // NOI18N
        powerButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/avrwake/gui/resources/power_on.png"))); // NOI18N
        powerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                powerButtonActionPerformed(evt);
            }
        });

        refreshButton.setBackground(new java.awt.Color(60, 60, 60));
        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/avrwake/gui/resources/refresh.png"))); // NOI18N
        refreshButton.setText(bundle.getString("Main.refreshButton.text")); // NOI18N
        refreshButton.setToolTipText(bundle.getString("Main.refreshButton.toolTipText")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        muteButton.setBackground(new java.awt.Color(60, 60, 60));
        muteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/avrwake/gui/resources/power_off.png"))); // NOI18N
        muteButton.setText(bundle.getString("Main.muteButton.text")); // NOI18N
        muteButton.setToolTipText(bundle.getString("Main.muteButton.toolTipText")); // NOI18N
        muteButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/avrwake/gui/resources/power_on.png"))); // NOI18N
        muteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                muteButtonActionPerformed(evt);
            }
        });

        controlStatusBar.setBackground(new java.awt.Color(40, 40, 40));

        controlStatusText.setBackground(new java.awt.Color(40, 40, 40));
        controlStatusText.setForeground(new java.awt.Color(255, 255, 255));
        controlStatusText.setText(bundle.getString("Main.controlStatusText.text")); // NOI18N

        controlStatusIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/avrwake/gui/resources/repeat.png"))); // NOI18N
        controlStatusIcon.setText(bundle.getString("Main.controlStatusIcon.text")); // NOI18N

        javax.swing.GroupLayout controlStatusBarLayout = new javax.swing.GroupLayout(controlStatusBar);
        controlStatusBar.setLayout(controlStatusBarLayout);
        controlStatusBarLayout.setHorizontalGroup(
            controlStatusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, controlStatusBarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(controlStatusIcon)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(controlStatusText, javax.swing.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE))
        );
        controlStatusBarLayout.setVerticalGroup(
            controlStatusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(controlStatusIcon, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
            .addComponent(controlStatusText, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        masterVolumeLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        masterVolumeLabel.setForeground(new java.awt.Color(255, 200, 50));
        masterVolumeLabel.setText(bundle.getString("Main.masterVolumeLabel.text")); // NOI18N

        sourceSelectLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        sourceSelectLabel.setForeground(new java.awt.Color(255, 200, 50));
        sourceSelectLabel.setText(bundle.getString("Main.sourceSelectLabel.text")); // NOI18N

        sourceBD.setBackground(new java.awt.Color(60, 60, 60));
        sourceBD.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        sourceBD.setText(bundle.getString("Main.sourceBD.text")); // NOI18N
        sourceBD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceBDActionPerformed(evt);
            }
        });

        sourceTV.setBackground(new java.awt.Color(60, 60, 60));
        sourceTV.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        sourceTV.setText(bundle.getString("Main.sourceTV.text")); // NOI18N
        sourceTV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceTVActionPerformed(evt);
            }
        });

        surroundModeLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        surroundModeLabel.setForeground(new java.awt.Color(255, 200, 50));
        surroundModeLabel.setText(bundle.getString("Main.surroundModeLabel.text")); // NOI18N

        mchStereo.setBackground(new java.awt.Color(60, 60, 60));
        mchStereo.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        mchStereo.setText(bundle.getString("Main.mchStereo.text")); // NOI18N
        mchStereo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mchStereoActionPerformed(evt);
            }
        });

        mchIn.setBackground(new java.awt.Color(60, 60, 60));
        mchIn.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        mchIn.setText(bundle.getString("Main.mchIn.text")); // NOI18N
        mchIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mchInActionPerformed(evt);
            }
        });

        curSourceMode.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        curSourceMode.setForeground(new java.awt.Color(153, 255, 153));
        curSourceMode.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        curSourceMode.setText(bundle.getString("Main.curSourceMode.text")); // NOI18N
        curSourceMode.setToolTipText(bundle.getString("Main.curSourceMode.toolTipText")); // NOI18N
        curSourceMode.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("Main.curSourceMode.border.title"), javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12), new java.awt.Color(255, 200, 50))); // NOI18N

        javax.swing.GroupLayout avrControlTabLayout = new javax.swing.GroupLayout(avrControlTab);
        avrControlTab.setLayout(avrControlTabLayout);
        avrControlTabLayout.setHorizontalGroup(
            avrControlTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(controlStatusBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(avrControlTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(avrControlTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(curSourceMode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(avrControlTabLayout.createSequentialGroup()
                        .addComponent(volumeSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(avrControlTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(muteButton)
                            .addComponent(refreshButton)))
                    .addComponent(volumeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(avrControlTabLayout.createSequentialGroup()
                        .addComponent(masterVolumeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(powerButton))
                    .addGroup(avrControlTabLayout.createSequentialGroup()
                        .addGroup(avrControlTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sourceSelectLabel)
                            .addGroup(avrControlTabLayout.createSequentialGroup()
                                .addComponent(sourceBD)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sourceTV))
                            .addComponent(surroundModeLabel)
                            .addGroup(avrControlTabLayout.createSequentialGroup()
                                .addComponent(mchStereo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mchIn)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        avrControlTabLayout.setVerticalGroup(
            avrControlTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(avrControlTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(avrControlTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(masterVolumeLabel)
                    .addComponent(powerButton))
                .addGap(18, 18, 18)
                .addComponent(volumeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(avrControlTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(avrControlTabLayout.createSequentialGroup()
                        .addComponent(muteButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(refreshButton))
                    .addComponent(volumeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39)
                .addComponent(sourceSelectLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(avrControlTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceBD)
                    .addComponent(sourceTV))
                .addGap(18, 18, 18)
                .addComponent(surroundModeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(avrControlTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mchStereo)
                    .addComponent(mchIn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 211, Short.MAX_VALUE)
                .addComponent(curSourceMode)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(controlStatusBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        refreshButton.getAccessibleContext().setAccessibleName(bundle.getString("Main.refreshButton.AccessibleContext.accessibleName")); // NOI18N
        muteButton.getAccessibleContext().setAccessibleName(bundle.getString("Main.muteButton.AccessibleContext.accessibleName")); // NOI18N

        tabsPane.addTab(bundle.getString("Main.avrControlTab.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/avrwake/gui/resources/avrcontrol.png")), avrControlTab); // NOI18N
        avrControlTab.getAccessibleContext().setAccessibleName(bundle.getString("Main.avrControlTab.AccessibleContext.accessibleName")); // NOI18N
        avrControlTab.getAccessibleContext().setAccessibleDescription(bundle.getString("Main.avrControlTab.AccessibleContext.accessibleDescription")); // NOI18N

        alarmTab.setBackground(new java.awt.Color(60, 60, 60));

        alarmStatusBar.setBackground(new java.awt.Color(40, 40, 40));

        alarmStatusText.setBackground(new java.awt.Color(40, 40, 40));
        alarmStatusText.setForeground(new java.awt.Color(255, 255, 255));
        alarmStatusText.setText(bundle.getString("Main.alarmStatusText.text")); // NOI18N

        alarmStatusIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/avrwake/gui/resources/repeat.png"))); // NOI18N
        alarmStatusIcon.setText(bundle.getString("Main.alarmStatusIcon.text")); // NOI18N

        javax.swing.GroupLayout alarmStatusBarLayout = new javax.swing.GroupLayout(alarmStatusBar);
        alarmStatusBar.setLayout(alarmStatusBarLayout);
        alarmStatusBarLayout.setHorizontalGroup(
            alarmStatusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, alarmStatusBarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(alarmStatusIcon)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(alarmStatusText, javax.swing.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE))
        );
        alarmStatusBarLayout.setVerticalGroup(
            alarmStatusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(alarmStatusIcon, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
            .addComponent(alarmStatusText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        timePanel.setBackground(new java.awt.Color(50, 50, 50));

        dateLabel.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        dateLabel.setForeground(new java.awt.Color(255, 204, 0));
        dateLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        dateLabel.setText(bundle.getString("Main.dateLabel.text")); // NOI18N
        dateLabel.setDoubleBuffered(true);

        timeLabel.setFont(new java.awt.Font("Tahoma", 0, 40)); // NOI18N
        timeLabel.setForeground(new java.awt.Color(255, 200, 50));
        timeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        timeLabel.setText(bundle.getString("Main.timeLabel.text")); // NOI18N
        timeLabel.setDoubleBuffered(true);

        javax.swing.GroupLayout timePanelLayout = new javax.swing.GroupLayout(timePanel);
        timePanel.setLayout(timePanelLayout);
        timePanelLayout.setHorizontalGroup(
            timePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(timeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(dateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        timePanelLayout.setVerticalGroup(
            timePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(timePanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(dateLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timeLabel))
        );

        alarmHourSpinner.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        alarmHourSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.MINUTE));
        alarmHourSpinner.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        alarmHourSpinner.setEditor(new javax.swing.JSpinner.DateEditor(alarmHourSpinner, "HH"));
        alarmHourSpinner.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                alarmHourSpinnerMouseWheelMoved(evt);
            }
        });

        alarmMinuteSpinner.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        alarmMinuteSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.MINUTE));
        alarmMinuteSpinner.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        alarmMinuteSpinner.setEditor(new javax.swing.JSpinner.DateEditor(alarmMinuteSpinner, "mm"));
        alarmMinuteSpinner.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                alarmMinuteSpinnerMouseWheelMoved(evt);
            }
        });

        musicPathField.setBackground(new java.awt.Color(70, 70, 70));
        musicPathField.setForeground(new java.awt.Color(255, 200, 50));
        musicPathField.setText(bundle.getString("Main.musicPathField.text")); // NOI18N
        musicPathField.setCaretColor(new java.awt.Color(255, 255, 255));

        musicBrowseButton.setBackground(new java.awt.Color(60, 60, 60));
        musicBrowseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/avrwake/gui/resources/music.png"))); // NOI18N
        musicBrowseButton.setText(bundle.getString("Main.musicBrowseButton.text")); // NOI18N
        musicBrowseButton.setToolTipText(bundle.getString("Main.musicBrowseButton.toolTipText")); // NOI18N
        musicBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                musicBrowseButtonActionPerformed(evt);
            }
        });

        alarmEnable.setBackground(new java.awt.Color(60, 60, 60));
        alarmEnable.setText(bundle.getString("Main.alarmEnable.text")); // NOI18N
        alarmEnable.setToolTipText(bundle.getString("Main.alarmEnable.toolTipText")); // NOI18N
        alarmEnable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alarmEnableActionPerformed(evt);
            }
        });

        weekendAlarmEnable.setBackground(new java.awt.Color(60, 60, 60));
        weekendAlarmEnable.setText(bundle.getString("Main.weekendAlarmEnable.text")); // NOI18N
        weekendAlarmEnable.setToolTipText(bundle.getString("Main.weekendAlarmEnable.toolTipText")); // NOI18N
        weekendAlarmEnable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                weekendAlarmEnableActionPerformed(evt);
            }
        });

        wAlarmHourSpinner.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        wAlarmHourSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.MINUTE));
        wAlarmHourSpinner.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        wAlarmHourSpinner.setEditor(new javax.swing.JSpinner.DateEditor(wAlarmHourSpinner, "HH"));
        wAlarmHourSpinner.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                wAlarmHourSpinnerMouseWheelMoved(evt);
            }
        });

        wAlarmMinuteSpinner.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        wAlarmMinuteSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.MINUTE));
        wAlarmMinuteSpinner.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        wAlarmMinuteSpinner.setEditor(new javax.swing.JSpinner.DateEditor(wAlarmMinuteSpinner, "mm"));
        wAlarmMinuteSpinner.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                wAlarmMinuteSpinnerMouseWheelMoved(evt);
            }
        });

        wMusicPathField.setBackground(new java.awt.Color(70, 70, 70));
        wMusicPathField.setForeground(new java.awt.Color(255, 200, 50));
        wMusicPathField.setText(bundle.getString("Main.wMusicPathField.text")); // NOI18N
        wMusicPathField.setCaretColor(new java.awt.Color(255, 255, 255));

        wMusicBrowseButton.setBackground(new java.awt.Color(60, 60, 60));
        wMusicBrowseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/avrwake/gui/resources/music.png"))); // NOI18N
        wMusicBrowseButton.setText(bundle.getString("Main.wMusicBrowseButton.text")); // NOI18N
        wMusicBrowseButton.setToolTipText(bundle.getString("Main.wMusicBrowseButton.toolTipText")); // NOI18N
        wMusicBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wMusicBrowseButtonActionPerformed(evt);
            }
        });

        weekdaysLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        weekdaysLabel.setForeground(new java.awt.Color(255, 255, 255));
        weekdaysLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        weekdaysLabel.setText(bundle.getString("Main.weekdaysLabel.text")); // NOI18N

        weekendsLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        weekendsLabel.setForeground(new java.awt.Color(255, 255, 255));
        weekendsLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        weekendsLabel.setText(bundle.getString("Main.weekendsLabel.text")); // NOI18N

        alarmVolumeLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        alarmVolumeLabel.setForeground(new java.awt.Color(255, 200, 50));
        alarmVolumeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        alarmVolumeLabel.setText(bundle.getString("Main.alarmVolumeLabel.text")); // NOI18N

        alarmVolumeSlider.setBackground(new java.awt.Color(50, 50, 50));
        alarmVolumeSlider.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        alarmVolumeSlider.setForeground(new java.awt.Color(255, 200, 50));
        alarmVolumeSlider.setMajorTickSpacing(5);
        alarmVolumeSlider.setMaximum(20);
        alarmVolumeSlider.setMinimum(-80);
        alarmVolumeSlider.setMinorTickSpacing(1);
        alarmVolumeSlider.setPaintLabels(true);
        alarmVolumeSlider.setPaintTicks(true);
        alarmVolumeSlider.setPaintTrack(false);
        alarmVolumeSlider.setSnapToTicks(true);
        alarmVolumeSlider.setValue(-40);
        alarmVolumeSlider.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(200, 150, 30), 1, true));
        alarmVolumeSlider.setDoubleBuffered(true);
        alarmVolumeSlider.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                alarmVolumeSliderMouseWheelMoved(evt);
            }
        });
        alarmVolumeSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                alarmVolumeSliderMouseReleased(evt);
            }
        });
        alarmVolumeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                alarmVolumeSliderStateChanged(evt);
            }
        });

        stopAlarmButton.setBackground(new java.awt.Color(60, 60, 60));
        stopAlarmButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/avrwake/gui/resources/stop.png"))); // NOI18N
        stopAlarmButton.setText(bundle.getString("Main.stopAlarmButton.text")); // NOI18N
        stopAlarmButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopAlarmButtonActionPerformed(evt);
            }
        });

        sleepTimeEnable.setBackground(new java.awt.Color(60, 60, 60));
        sleepTimeEnable.setText(bundle.getString("Main.sleepTimeEnable.text")); // NOI18N
        sleepTimeEnable.setToolTipText(bundle.getString("Main.sleepTimeEnable.toolTipText")); // NOI18N
        sleepTimeEnable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sleepTimeEnableActionPerformed(evt);
            }
        });

        sleepHourSpinner.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        sleepHourSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.MINUTE));
        sleepHourSpinner.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        sleepHourSpinner.setEditor(new javax.swing.JSpinner.DateEditor(sleepHourSpinner, "HH"));
        sleepHourSpinner.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                sleepHourSpinnerMouseWheelMoved(evt);
            }
        });

        sleepMinuteSpinner.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        sleepMinuteSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.MINUTE));
        sleepMinuteSpinner.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        sleepMinuteSpinner.setEditor(new javax.swing.JSpinner.DateEditor(sleepMinuteSpinner, "mm"));
        sleepMinuteSpinner.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                sleepMinuteSpinnerMouseWheelMoved(evt);
            }
        });

        sleepLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        sleepLabel.setForeground(new java.awt.Color(255, 200, 50));
        sleepLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/avrwake/gui/resources/battery.png"))); // NOI18N
        sleepLabel.setText(bundle.getString("Main.sleepLabel.text")); // NOI18N

        alarmTimeLeft.setBackground(new java.awt.Color(60, 60, 60));
        alarmTimeLeft.setForeground(new java.awt.Color(255, 255, 255));
        alarmTimeLeft.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        alarmTimeLeft.setText(bundle.getString("Main.alarmTimeLeft.text")); // NOI18N

        wAlarmTimeLeft.setBackground(new java.awt.Color(60, 60, 60));
        wAlarmTimeLeft.setForeground(new java.awt.Color(255, 255, 255));
        wAlarmTimeLeft.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        wAlarmTimeLeft.setText(bundle.getString("Main.wAlarmTimeLeft.text")); // NOI18N

        javax.swing.GroupLayout alarmTabLayout = new javax.swing.GroupLayout(alarmTab);
        alarmTab.setLayout(alarmTabLayout);
        alarmTabLayout.setHorizontalGroup(
            alarmTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(alarmStatusBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(timePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(alarmTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(alarmTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(alarmTimeLeft, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(stopAlarmButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(weekdaysLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(alarmTabLayout.createSequentialGroup()
                        .addComponent(alarmEnable)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(alarmHourSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(alarmMinuteSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(musicPathField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(musicBrowseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, alarmTabLayout.createSequentialGroup()
                        .addComponent(weekendAlarmEnable)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(wAlarmHourSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(wAlarmMinuteSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(wMusicPathField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(wMusicBrowseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(weekendsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                    .addComponent(alarmVolumeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(alarmVolumeSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(alarmTabLayout.createSequentialGroup()
                        .addComponent(sleepTimeEnable)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sleepHourSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sleepMinuteSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(sleepLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(wAlarmTimeLeft, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE))
                .addContainerGap())
        );
        alarmTabLayout.setVerticalGroup(
            alarmTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, alarmTabLayout.createSequentialGroup()
                .addComponent(timePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(weekdaysLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(alarmTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(alarmTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(musicPathField, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(musicBrowseButton))
                    .addGroup(alarmTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(alarmEnable, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(alarmHourSpinner, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(alarmMinuteSpinner, javax.swing.GroupLayout.Alignment.LEADING)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(alarmTimeLeft, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(weekendsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(alarmTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(alarmTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(wMusicPathField, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(wMusicBrowseButton))
                    .addGroup(alarmTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(weekendAlarmEnable, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(wAlarmHourSpinner, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(wAlarmMinuteSpinner, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(wAlarmTimeLeft, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addComponent(alarmVolumeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(alarmVolumeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stopAlarmButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 101, Short.MAX_VALUE)
                .addGroup(alarmTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(sleepTimeEnable, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sleepHourSpinner, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sleepMinuteSpinner)
                    .addComponent(sleepLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(alarmStatusBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        tabsPane.addTab(bundle.getString("Main.alarmTab.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/avrwake/gui/resources/alarm.png")), alarmTab); // NOI18N

        settingsTab.setBackground(new java.awt.Color(60, 60, 60));

        avrHostnameLabel.setForeground(new java.awt.Color(255, 255, 255));
        avrHostnameLabel.setText(bundle.getString("Main.avrHostnameLabel.text")); // NOI18N

        avrHostnameField.setBackground(new java.awt.Color(70, 70, 70));
        avrHostnameField.setForeground(new java.awt.Color(255, 200, 50));
        avrHostnameField.setText(bundle.getString("Main.avrHostnameField.text")); // NOI18N
        avrHostnameField.setCaretColor(new java.awt.Color(255, 255, 255));

        avrConnectionLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        avrConnectionLabel.setForeground(new java.awt.Color(255, 200, 50));
        avrConnectionLabel.setText(bundle.getString("Main.avrConnectionLabel.text")); // NOI18N

        mailBar.setBackground(new java.awt.Color(40, 40, 40));

        mailLabel.setForeground(new java.awt.Color(200, 200, 200));
        mailLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        mailLabel.setText(bundle.getString("Main.mailLabel.text")); // NOI18N

        javax.swing.GroupLayout mailBarLayout = new javax.swing.GroupLayout(mailBar);
        mailBar.setLayout(mailBarLayout);
        mailBarLayout.setHorizontalGroup(
            mailBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mailLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        mailBarLayout.setVerticalGroup(
            mailBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mailBarLayout.createSequentialGroup()
                .addComponent(mailLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        avrStringLabel.setForeground(new java.awt.Color(255, 255, 255));
        avrStringLabel.setText(bundle.getString("Main.avrStringLabel.text")); // NOI18N

        avrStringField.setBackground(new java.awt.Color(70, 70, 70));
        avrStringField.setFont(new java.awt.Font("Consolas", 0, 11)); // NOI18N
        avrStringField.setForeground(new java.awt.Color(255, 200, 50));
        avrStringField.setText(bundle.getString("Main.avrStringField.text")); // NOI18N
        avrStringField.setCaretColor(new java.awt.Color(255, 255, 255));

        avrTestButton.setBackground(new java.awt.Color(60, 60, 60));
        avrTestButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/avrwake/gui/resources/repeat.png"))); // NOI18N
        avrTestButton.setText(bundle.getString("Main.avrTestButton.text")); // NOI18N
        avrTestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                avrTestButtonActionPerformed(evt);
            }
        });

        testingLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        testingLabel.setForeground(new java.awt.Color(255, 200, 50));
        testingLabel.setText(bundle.getString("Main.testingLabel.text")); // NOI18N

        testScrollPane.setBackground(new java.awt.Color(60, 60, 60));

        testTextArea.setBackground(new java.awt.Color(40, 40, 40));
        testTextArea.setColumns(20);
        testTextArea.setFont(new java.awt.Font("Consolas", 0, 11)); // NOI18N
        testTextArea.setForeground(new java.awt.Color(255, 255, 102));
        testTextArea.setRows(5);
        testScrollPane.setViewportView(testTextArea);

        javax.swing.GroupLayout settingsTabLayout = new javax.swing.GroupLayout(settingsTab);
        settingsTab.setLayout(settingsTabLayout);
        settingsTabLayout.setHorizontalGroup(
            settingsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mailBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(settingsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(testScrollPane)
                    .addGroup(settingsTabLayout.createSequentialGroup()
                        .addGroup(settingsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(settingsTabLayout.createSequentialGroup()
                                .addComponent(avrStringLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(avrStringField, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(avrTestButton))
                            .addGroup(settingsTabLayout.createSequentialGroup()
                                .addComponent(avrHostnameLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(avrHostnameField, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(avrConnectionLabel)
                            .addComponent(testingLabel))
                        .addGap(0, 166, Short.MAX_VALUE)))
                .addContainerGap())
        );
        settingsTabLayout.setVerticalGroup(
            settingsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(avrConnectionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(avrHostnameLabel)
                    .addComponent(avrHostnameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(testingLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(avrStringLabel)
                    .addComponent(avrStringField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(avrTestButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(testScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(mailBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        tabsPane.addTab(bundle.getString("Main.settingsTab.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/avrwake/gui/resources/settings.png")), settingsTab); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabsPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabsPane)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void volumeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_volumeSliderStateChanged
        volumeLabel.setText(String.format("%ddB", volumeSlider.getValue()));
    }//GEN-LAST:event_volumeSliderStateChanged

    private void volumeSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_volumeSliderMouseReleased
        int volume = volumeSlider.getValue();
        avrControl.setVolume(volume);
        setControlStatusText("Volume set to " + volume + "dB");
    }//GEN-LAST:event_volumeSliderMouseReleased

    private void powerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_powerButtonActionPerformed
        boolean powered = false;
        if (powerButton.isSelected()) {
            if (!avrControl.isPowered()) {
                avrControl.powerOn();
                setControlStatusText("Powered on...");
                powered = true;
            }
        } else {
            if (avrControl.isPowered()) {
                avrControl.standby();
                setControlStatusText("On standby...");
            }
        }
        powerButton.setSelected(powered);
    }//GEN-LAST:event_powerButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        loadAVRValues();
        setControlStatusText("Updated...");
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void volumeSliderMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_volumeSliderMouseWheelMoved
        int volume = (evt.getWheelRotation() < 0) ? volumeSlider.getValue() + 2 : volumeSlider.getValue() - 2;
        volumeSlider.setValue(volume);
        avrControl.setVolume(volume);
        setControlStatusText("Volume set to " + volume + "dB");
    }//GEN-LAST:event_volumeSliderMouseWheelMoved

    private void avrTestButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_avrTestButtonActionPerformed
        String command = avrStringField.getText();
        String response = avrControl.sendCommand(command);
        if (response != null) {
            testTextArea.insert(String.format("%s : %s\n", Common.getSimpleTime(new Date()), response), 0);
        }
        setControlStatusText("Command '" + command + "' sent...");
    }//GEN-LAST:event_avrTestButtonActionPerformed

    private void muteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_muteButtonActionPerformed
        avrControl.setMute(muteButton.isSelected());
        if (muteButton.isSelected()) {
            setControlStatusText("Sound muted...");
        } else {
            setControlStatusText("Sound unmuted...");
        }
    }//GEN-LAST:event_muteButtonActionPerformed

    private void tabsPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabsPaneStateChanged
        if (!firstStart) {
            saveSettings();
        }
    }//GEN-LAST:event_tabsPaneStateChanged

    private void alarmHourSpinnerMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_alarmHourSpinnerMouseWheelMoved
        Date curHour = (Date) alarmHourSpinner.getModel().getValue();
        if (evt.getWheelRotation() < 0) {
            alarmHourSpinner.getModel().setValue(addSubDate(curHour, Calendar.HOUR_OF_DAY, 1));
        } else {
            alarmHourSpinner.getModel().setValue(addSubDate(curHour, Calendar.HOUR_OF_DAY, -1));
        }
        curHour = (Date) alarmHourSpinner.getModel().getValue();
        prefsData.setAlarmHour(getDateField(curHour, Calendar.HOUR_OF_DAY));
        alarmEnable.setSelected(false);
        stopAlarm();
    }//GEN-LAST:event_alarmHourSpinnerMouseWheelMoved

    private void alarmMinuteSpinnerMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_alarmMinuteSpinnerMouseWheelMoved
        Date curMinute = (Date) alarmMinuteSpinner.getModel().getValue();
        if (evt.getWheelRotation() < 0) {
            alarmMinuteSpinner.getModel().setValue(addSubDate(curMinute, Calendar.MINUTE,  1));
        } else {
            alarmMinuteSpinner.getModel().setValue(addSubDate(curMinute, Calendar.MINUTE, -1));
        }
        curMinute = (Date) alarmMinuteSpinner.getModel().getValue();
        prefsData.setAlarmMinute(getDateField(curMinute, Calendar.MINUTE));
        alarmEnable.setSelected(false);
        stopAlarm();
    }//GEN-LAST:event_alarmMinuteSpinnerMouseWheelMoved

    private void musicBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_musicBrowseButtonActionPerformed
        JFileChooser fc = new JFileChooser(prefsData.getMusicPath());
        FileFilter filter = new ExtensionFileFilter("MP3", new String[]{"MP3"});
        fc.setFileFilter(filter);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            String name = file.getName().substring(0, file.getName().length() - 4);
            musicPathField.setText(name);
            prefsData.setMusicName(name);
            prefsData.setMusicPath(file.getPath());
        }
        saveSettings();
    }//GEN-LAST:event_musicBrowseButtonActionPerformed

    private void alarmEnableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alarmEnableActionPerformed
        checkAlarm();
    }//GEN-LAST:event_alarmEnableActionPerformed

    private void checkAlarm() {
        prefsData.setAlarmEnabled(alarmEnable.isSelected());
        if (alarmEnable.isSelected()) {
            startAlarm();
        } else {
            stopAlarm();
        }
    }
    
    private void weekendAlarmEnableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_weekendAlarmEnableActionPerformed
        checkWeekendAlarm();
    }//GEN-LAST:event_weekendAlarmEnableActionPerformed

    private void checkWeekendAlarm() {
        prefsData.setwAlarmEnabled(weekendAlarmEnable.isSelected());
        if (weekendAlarmEnable.isSelected()) {
            startWeekendAlarm();
        } else {
            stopWeekendAlarm();
        }
    }
    
    private void wAlarmHourSpinnerMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_wAlarmHourSpinnerMouseWheelMoved
        Date curHour = (Date) wAlarmHourSpinner.getModel().getValue();
        if (evt.getWheelRotation() < 0) {
            wAlarmHourSpinner.getModel().setValue(addSubDate(curHour, Calendar.HOUR_OF_DAY, 1));
        } else {
            wAlarmHourSpinner.getModel().setValue(addSubDate(curHour, Calendar.HOUR_OF_DAY, -1));
        }
        curHour = (Date) wAlarmHourSpinner.getModel().getValue();
        prefsData.setwAlarmHour(getDateField(curHour, Calendar.HOUR_OF_DAY));
        weekendAlarmEnable.setSelected(false);
        stopAlarm();
    }//GEN-LAST:event_wAlarmHourSpinnerMouseWheelMoved

    private void wAlarmMinuteSpinnerMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_wAlarmMinuteSpinnerMouseWheelMoved
        Date curMinute = (Date) wAlarmMinuteSpinner.getModel().getValue();
        if (evt.getWheelRotation() < 0) {
            wAlarmMinuteSpinner.getModel().setValue(addSubDate(curMinute, Calendar.MINUTE,  1));
        } else {
            wAlarmMinuteSpinner.getModel().setValue(addSubDate(curMinute, Calendar.MINUTE, -1));
        }
        curMinute = (Date) wAlarmMinuteSpinner.getModel().getValue();
        prefsData.setwAlarmMinute(getDateField(curMinute, Calendar.MINUTE));
        weekendAlarmEnable.setSelected(false);
        stopAlarm();
    }//GEN-LAST:event_wAlarmMinuteSpinnerMouseWheelMoved

    private void wMusicBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wMusicBrowseButtonActionPerformed
        JFileChooser fc = new JFileChooser(prefsData.getwMusicPath());
        FileFilter filter = new ExtensionFileFilter("MP3", new String[]{"MP3"});
        fc.setFileFilter(filter);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            String name = file.getName().substring(0, file.getName().length() - 4);
            wMusicPathField.setText(name);
            prefsData.setwMusicName(name);
            prefsData.setwMusicPath(file.getPath());
        }
        saveSettings();
    }//GEN-LAST:event_wMusicBrowseButtonActionPerformed

    private void alarmVolumeSliderMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_alarmVolumeSliderMouseWheelMoved
        int volume = (evt.getWheelRotation() < 0) ? alarmVolumeSlider.getValue() + 2 : alarmVolumeSlider.getValue() - 2;
        alarmVolumeSlider.setValue(volume);
        setAlarmStatusText("Alarm volume set to " + volume + "dB");
        prefsData.setAlarmVolume(volume);
    }//GEN-LAST:event_alarmVolumeSliderMouseWheelMoved

    private void alarmVolumeSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_alarmVolumeSliderMouseReleased
        int volume = alarmVolumeSlider.getValue();
        setAlarmStatusText("Alarm volume set to " + volume + "dB");
        prefsData.setAlarmVolume(volume);
    }//GEN-LAST:event_alarmVolumeSliderMouseReleased

    private void alarmVolumeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_alarmVolumeSliderStateChanged
        alarmVolumeLabel.setText(String.format("Alarm volume: %ddB", alarmVolumeSlider.getValue()));
        prefsData.setAlarmVolume(alarmVolumeSlider.getValue());
    }//GEN-LAST:event_alarmVolumeSliderStateChanged

    private void stopAlarmButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopAlarmButtonActionPerformed
        if (task != null) {
            task.stopPlaying();
        }
    }//GEN-LAST:event_stopAlarmButtonActionPerformed

    private void sleepTimeEnableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sleepTimeEnableActionPerformed
        checkSleep();
    }//GEN-LAST:event_sleepTimeEnableActionPerformed

    private void checkSleep() {
        prefsData.setSleepEnabled(sleepTimeEnable.isSelected());
        if (sleepTimeEnable.isSelected()) {
            startSleep();
        } else {
            stopSleep();
        }
    }
    
    private void sleepHourSpinnerMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_sleepHourSpinnerMouseWheelMoved
        Date curHour = (Date) sleepHourSpinner.getModel().getValue();
        if (evt.getWheelRotation() < 0) {
            sleepHourSpinner.getModel().setValue(addSubDate(curHour, Calendar.HOUR_OF_DAY, 1));
        } else {
            sleepHourSpinner.getModel().setValue(addSubDate(curHour, Calendar.HOUR_OF_DAY, -1));
        }
        curHour = (Date) sleepHourSpinner.getModel().getValue();
        prefsData.setSleepHour(getDateField(curHour, Calendar.HOUR_OF_DAY));
        saveSettings();
    }//GEN-LAST:event_sleepHourSpinnerMouseWheelMoved

    private void sleepMinuteSpinnerMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_sleepMinuteSpinnerMouseWheelMoved
        Date curMinute = (Date) sleepMinuteSpinner.getModel().getValue();
        if (evt.getWheelRotation() < 0) {
            sleepMinuteSpinner.getModel().setValue(addSubDate(curMinute, Calendar.MINUTE, 1));
        } else {
            sleepMinuteSpinner.getModel().setValue(addSubDate(curMinute, Calendar.MINUTE, -1));
        }
        curMinute = (Date) sleepMinuteSpinner.getModel().getValue();
        prefsData.setSleepMinute(getDateField(curMinute, Calendar.MINUTE));
        saveSettings();
    }//GEN-LAST:event_sleepMinuteSpinnerMouseWheelMoved

    private void sourceBDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceBDActionPerformed
        avrControl.setSource("SIBD");
        sourceString = "SIBD";
        curSourceMode.setText(sourceString + " : " + modeString);
        setControlStatusText("Source set to BD...");
    }//GEN-LAST:event_sourceBDActionPerformed

    private void sourceTVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceTVActionPerformed
        avrControl.setSource("SITV");
        sourceString = "SITV";
        curSourceMode.setText(sourceString + " : " + modeString);
        setControlStatusText("Source set to TV...");
    }//GEN-LAST:event_sourceTVActionPerformed

    private void mchStereoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mchStereoActionPerformed
        avrControl.setMode("MSMCH STEREO");
        modeString = "MSMCH STEREO";
        curSourceMode.setText(sourceString + " : " + modeString);
        setControlStatusText("Source set to MCH Stereo...");
    }//GEN-LAST:event_mchStereoActionPerformed

    private void mchInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mchInActionPerformed
        avrControl.setMode("MSDOLBY DIGITAL");
        modeString = "MSMULTI CH IN";
        curSourceMode.setText(sourceString + " : " + modeString);
        setControlStatusText("Source set to Dolby Digital...");
    }//GEN-LAST:event_mchInActionPerformed
    
    public void setControlStatusText(String text) {
        controlStatusText.setText(String.format("%s : %s", Common.getSimpleTime(new Date()), text));
    }
    
    public void setAlarmStatusText(String text) {
        alarmStatusText.setText(String.format("%s : %s", Common.getSimpleTime(new Date()), text));
    }
    
    private Date addSubDate(Date date, int field, int incDec) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        calendar.set(field, calendar.get(field) + incDec);
        return calendar.getTime();
    }
    
    private int getDateField(Date date, int field) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        return calendar.get(field);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox alarmEnable;
    private javax.swing.JSpinner alarmHourSpinner;
    private javax.swing.JSpinner alarmMinuteSpinner;
    private javax.swing.JPanel alarmStatusBar;
    private javax.swing.JLabel alarmStatusIcon;
    private javax.swing.JLabel alarmStatusText;
    private javax.swing.JPanel alarmTab;
    private javax.swing.JLabel alarmTimeLeft;
    private javax.swing.JLabel alarmVolumeLabel;
    private javax.swing.JSlider alarmVolumeSlider;
    private javax.swing.JLabel avrConnectionLabel;
    private javax.swing.JPanel avrControlTab;
    private javax.swing.JTextField avrHostnameField;
    private javax.swing.JLabel avrHostnameLabel;
    private javax.swing.JTextField avrStringField;
    private javax.swing.JLabel avrStringLabel;
    private javax.swing.JButton avrTestButton;
    private javax.swing.JPanel controlStatusBar;
    private javax.swing.JLabel controlStatusIcon;
    private javax.swing.JLabel controlStatusText;
    private javax.swing.JLabel curSourceMode;
    private javax.swing.JLabel dateLabel;
    private javax.swing.JPanel mailBar;
    private avrwake.gui.element.HyperlinkLabel mailLabel;
    private javax.swing.JLabel masterVolumeLabel;
    private javax.swing.JButton mchIn;
    private javax.swing.JButton mchStereo;
    private javax.swing.JButton musicBrowseButton;
    private javax.swing.JTextField musicPathField;
    private javax.swing.JToggleButton muteButton;
    private javax.swing.JToggleButton powerButton;
    private javax.swing.JButton refreshButton;
    private javax.swing.JPanel settingsTab;
    private javax.swing.JSpinner sleepHourSpinner;
    private javax.swing.JLabel sleepLabel;
    private javax.swing.JSpinner sleepMinuteSpinner;
    private javax.swing.JCheckBox sleepTimeEnable;
    private javax.swing.JButton sourceBD;
    private javax.swing.ButtonGroup sourceGroup;
    private javax.swing.JLabel sourceSelectLabel;
    private javax.swing.JButton sourceTV;
    private javax.swing.JButton stopAlarmButton;
    private javax.swing.JLabel surroundModeLabel;
    private javax.swing.JTabbedPane tabsPane;
    private javax.swing.JScrollPane testScrollPane;
    private javax.swing.JTextArea testTextArea;
    private javax.swing.JLabel testingLabel;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JPanel timePanel;
    private javax.swing.JLabel volumeLabel;
    private javax.swing.JSlider volumeSlider;
    private javax.swing.JSpinner wAlarmHourSpinner;
    private javax.swing.JSpinner wAlarmMinuteSpinner;
    private javax.swing.JLabel wAlarmTimeLeft;
    private javax.swing.JButton wMusicBrowseButton;
    private javax.swing.JTextField wMusicPathField;
    private javax.swing.JLabel weekdaysLabel;
    private javax.swing.JCheckBox weekendAlarmEnable;
    private javax.swing.JLabel weekendsLabel;
    // End of variables declaration//GEN-END:variables
}
