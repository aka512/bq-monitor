package de.akalla.bqmonitor.util;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.*;

import org.apache.log4j.Logger;

public class Utils {
    private static final Logger log = Logger.getLogger(Utils.class);
    static final DateFormat date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:SSS");
    static final DateFormat dateISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSSZ");

    // NOTE: Information on GridBagLayoutManager Layout Params
    // gridx - X coordinate of left top grid cell
    // gridy - Y coordinate of left top grid cell
    // gridwidth - Width of the component (like: colspan in HTML Tables)
    // gridheight - Height of the component
    // weightx - zero (stay at minimal width) or more
    // (e.g. 50: take up to 50 percent more of additional space)
    // weighty - zero (stay at minimal width) or more
    // (e.g. 50: take up to 50 percent more of additional space)
    // anchor - usually GridBagConstraints.CENTER:
    // Put the component in the center of its display area.
    // fill - usually GridBagConstraints.BOTH:
    // Resize the component both horizontally and vertically.
    // insets - Margin (like: css margin)
    // ipadx - extends minimum width by that value
    // ipady - extends minimum height by that value

    /**
     * turns around the anticlockwise params used by GridBaglayout clockwise is more
     * natural and known from css
     * 
     * @param top
     * @param right
     * @param bottom
     * @param left
     * @return Inset for GridBagLayout (like the css margin)
     */
    public static Insets insets(int top, int right, int bottom, int left) {
        return new Insets(top, left, bottom, right);
    }

    /**
     * convert byte size into human readable format
     * 
     * source https://stackoverflow.com/a/3758880
     * 
     * @param bytes
     * @return
     */
    public static String humanReadableByteCountBin(long bytes) {
        long b = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        return b < 1024L ? bytes + " B"
                : b <= 0xfffccccccccccccL >> 40 ? String.format("%.1f KiB", bytes / 0x1p10)
                        : b <= 0xfffccccccccccccL >> 30 ? String.format("%.1f MiB", bytes / 0x1p20)
                                : b <= 0xfffccccccccccccL >> 20 ? String.format("%.1f GiB", bytes / 0x1p30)
                                        : b <= 0xfffccccccccccccL >> 10 ? String.format("%.1f TiB", bytes / 0x1p40)
                                                : b <= 0xfffccccccccccccL
                                                        ? String.format("%.1f PiB", (bytes >> 10) / 0x1p40)
                                                        : String.format("%.1f EiB", (bytes >> 20) / 0x1p40);
    }

    /**
     * show a user friendly message with a stack trace, that the user is able to
     * report to the community
     * 
     * @param e
     */
    public static void notifyUserAboutException(Exception e) {
        JFrame ctx = new JFrame(); // empty frame to make the application terminate correctly.
        String sStackTrace = "";
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            sStackTrace = sw.toString(); // stack trace as a string
            System.out.println(sStackTrace);
        } catch (Exception ex) {
            log.error(ex);
        }
        JOptionPane.showMessageDialog(ctx,
                "An unexpected error occured. Please consider restarting the Application.\n\nMessage:\n" + sStackTrace);
        ctx.dispose();
    }

    public static void notifyUser(JFrame window, String string) {
        JOptionPane.showMessageDialog(window, string);
    }

    /**
     * expects a timestamp in unix epoch with millis and converts it to a local date
     * time string
     * 
     * @param ts in unixtime
     * @return dd MM yyyy HH:mm:ss:SSS
     */
    public static String formatOptionalTimestampToString(Long ts) {
        if (ts == null){
            return "?";
        }
        return date.format(new Date(ts));
    }

    /**
     * expects a timestamp in unix epoch with millis and converts it to a ISO date
     * time string
     *
     * @param ts in unixtime
     * @return yyyy-MM-ddTHH:mm:ss:SSSZ
     */
    public static String getTimestampToISOString(long ts) {
        return dateISO.format(new Date(ts));
    }

    /**
     * gets the GiB value in ##.# format (US)
     * 
     * @param bytes
     * @return
     */
    public static String getGB(Long bytes) {
        return String.format(Locale.US, "%.1f", bytes / 0x1p30);
    }
}
