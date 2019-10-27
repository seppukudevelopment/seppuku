package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * created by noil on 8/15/2019 at 4:41 PM
 */
public final class JoinDateCommand extends Command {

    public JoinDateCommand() {
        super("JoinDate", new String[]{"Jd"}, "Prints a given 9b9t player's join date (via 9b9t.com) in chat.", "JoinDate <Username>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final String givenUsername = split[1];

        if (givenUsername != null) {

            //fuckin newfags these days
            if(givenUsername.equalsIgnoreCase("Miniman392")) {
                final Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                final SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy");
                Seppuku.INSTANCE.logChat(givenUsername + " joined on " + formatter.format(calendar.getTime()));
                return;
            }

            if(givenUsername.equalsIgnoreCase("iSlime")) {
                Seppuku.INSTANCE.errorChat("Invalid username or the website is offline.");
                Seppuku.INSTANCE.errorChat("Who the fuck is iSlime?");
                return;
            }

            if(givenUsername.equalsIgnoreCase("FlashBak_")) {
                Seppuku.INSTANCE.logChat(givenUsername + " joined on 2011");
                return;
            }

            new Thread(() -> {
                final String date = getJoinDate(givenUsername);
                if (date != null) {
                    Seppuku.INSTANCE.logChat(givenUsername + " joined on " + date);
                } else {
                    Seppuku.INSTANCE.errorChat("Invalid username or the website is offline.");
                }
            }).start();
        } else {
            Seppuku.INSTANCE.errorChat("Error in username format " + "\247f\"" + split[1] + "\"");
            this.printUsage();
        }
    }

    private String getJoinDate(String username) {
        try {
            String data = this.getDataFromWebSite("http://9b9t.com/join-date/submit/" + username);
            if (data.contains("Join Date:")) {
                data = data.substring(data.indexOf("Join Date:")).substring(34);
                data = data.substring(0, 10);
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
                Date date = parser.parse(data);
                SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy");
                return formatter.format(date);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private String getDataFromWebSite(String site) throws Exception {
        URL url = new URL(site);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream(), Charset.forName("UTF-8")));
        StringBuilder stringBuilder = new StringBuilder();
        String inputLine;
        while ((inputLine = bufferedReader.readLine()) != null) {
            stringBuilder.append(inputLine);
        }
        bufferedReader.close();
        return stringBuilder.toString();
    }

}
