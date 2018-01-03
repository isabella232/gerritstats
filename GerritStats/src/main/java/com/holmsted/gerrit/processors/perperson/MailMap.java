package com.holmsted.gerrit.processors.perperson;

import com.holmsted.gerrit.Commit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jhansche
 * @since 8/30/17
 */
public class MailMap {
    private static final Pattern MAIL_TO_MAIL_PATTERN = Pattern.compile("^<(\\S+)>\\s+<(\\S+)>$");

    private final Map<String, String> emails = new HashMap<>();
    private final Map<String, String> usernames = new HashMap<>();
    private final Map<String, String> names = new HashMap<>();

    private boolean empty = true;

    public void readMailMap(File mailMapFile) {
        if (mailMapFile != null && mailMapFile.exists()) {
            try {
                parseMailMap(new BufferedReader(new FileReader(mailMapFile)));
            } catch (FileNotFoundException e) {
                // Ignore
            }
        }
    }

    public void readMailMap(String mailMapContents) {
        parseMailMap(new BufferedReader(new StringReader(mailMapContents)));
    }

    private void parseMailMap(BufferedReader reader) {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                Matcher lineMatcher = MAIL_TO_MAIL_PATTERN.matcher(line);
                if (lineMatcher.matches()) {
                    emails.put(lineMatcher.group(2), lineMatcher.group(1));
                }
            }

            empty = emails.isEmpty() && usernames.isEmpty() && names.isEmpty();
        } catch (IOException e) {
            // ignore
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public Commit.Identity mapIdentity(Commit.Identity identity) {
        if (empty) return identity;

        String email = emails.get(identity.email);
        String name = names.get(identity.name);
        String username = usernames.get(identity.username);

        if (email != null || name != null || username != null) {
            if (name == null) name = identity.name;
            if (email == null) email = identity.email;
            if (username == null) username = identity.username;
            identity = new Commit.Identity(name, email, username);
        }

        return identity;
    }
}
