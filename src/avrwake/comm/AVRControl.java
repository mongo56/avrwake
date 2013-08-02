package avrwake.comm;

import avrwake.shared.Common;
import avrwake.structures.PreferencesStructure;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.SocketException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.net.telnet.TelnetClient;

/**
 *
 * @author Blaz Majcen <bm@telaris.si>
 */
public class AVRControl {

    private final int AVR_TELNET_PORT = 23;
    private final char AVR_LAST_CHAR = 0x0d;
    private final int AVR_TIMEOUT = 1000;
    private final int AVR_COMMAND_PAUSE = 10;
    private PreferencesStructure prefs;
    private TelnetClient telnet;
    private boolean powered = false;

    public AVRControl(PreferencesStructure prefs) {
        this.prefs = prefs;
        telnet = new TelnetClient();
        telnet.setConnectTimeout(AVR_TIMEOUT);
        telnet.setReaderThread(true);
    }

    public void connect() {
        try {
            if (!telnet.isConnected()) {
                telnet.connect(prefs.getAvrHostname(), AVR_TELNET_PORT);
            }
        } catch (SocketException ex) {
            Logger.getLogger(AVRControl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AVRControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isConnected() {
        return telnet.isConnected();
    }

    public void disconnect() {
        try {
            if (telnet.isConnected()) {
                telnet.disconnect();
            }
        } catch (IOException ex) {
            Logger.getLogger(AVRControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String powerOn() {
        powered = true;
        return sendCommand("PWON");
    }

    public String standby() {
        powered = false;
        return sendCommand("PWSTANDBY");
    }

    public int getCurrentVolume() {
        String curVolume = sendCommand("MV?");
        if (curVolume != null) {
            Pattern p = Pattern.compile("\\d+");
            Matcher m = p.matcher(curVolume);
            while (m.find()) {
                String d = m.group();
                if (d.length() > 2) {
                    d = d.substring(0, 2);
                }
                return Integer.parseInt(d) - 80;
            }
        }
        return -1;
    }

    public String setVolume(int volume) {
        if (powered) {
            return sendCommand(String.format("MV%d", dbToDec(volume)));
        }
        return null;
    }

    public int dbToDec(int value) {
        int retValue = value + 80;
        if (retValue < 0) {
            retValue = 0;
        }
        if (retValue > 99) {
            retValue = 99;
        }
        return retValue;
    }

    public boolean isPowered() {
        String pwrStatus = sendCommand("PW?");
        if (pwrStatus != null) {
            if (pwrStatus.contains("ON")) {
                powered = true;
                return true;
            }
        }
        powered = false;
        return false;
    }

    public boolean isMuted() {
        String muStatus = sendCommand("MU?");
        if (muStatus != null) {
            if (muStatus.contains("ON")) {
                return true;
            }
        }
        return false;
    }

    public String setMute(boolean mute) {
        if (powered) {
            if (mute) {
                return sendCommand("MUON");
            } else {
                return sendCommand("MUOFF");
            }
        }
        return null;
    }

    public String getSource() {
        return sendCommand("SI?");
    }

    public void setSource(String source) {
        String curSource = getSource();
        if (curSource != null) {
            if (!curSource.matches(source)) {
                sendCommand(source);
            }
        }
    }

    public String getMode() {
        return sendCommand("MS?");
    }

    public void setMode(String mode) {
        String curMode = getMode();
        if (curMode != null) {
            if (!curMode.matches(mode)) {
                sendCommand(mode);
            }
        }
    }

    public String sendCommand(String value) {
        try {
            disconnect();
            connect();
            if (telnet.isConnected()) {
                telnet.setSoTimeout(100);
                telnet.setSoLinger(true, 0);
                InputStream in = telnet.getInputStream();
                PrintStream out = new PrintStream(telnet.getOutputStream());
                out.print(value + "\r\n");
                out.flush();
                System.out.println(Common.getSimpleTime(new Date()) + " - sent:" + value);
                Thread.sleep(AVR_COMMAND_PAUSE);
                String response = "";
                char result = (char) in.read();
                while (result != AVR_LAST_CHAR) {
                    response += result;
                    result = (char) in.read();

                }
                System.out.println(Common.getSimpleTime(new Date()) + " - received:" + response);
                in.close();
                out.close();
                disconnect();
                return response;
            }
            return null;
        } catch (InterruptedException ex) {
            Logger.getLogger(AVRControl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(AVRControl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
