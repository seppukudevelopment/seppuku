package me.rigamortis.seppuku.api.gui.menu;

/**
 * @author noil
 */
public class AltData {

    private final String email;
    private final String username;
    private final String password;
    private final boolean premium;

    public AltData(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.premium = true;
    }

    public AltData(String username, String password) {
        this.email = "";
        this.username = username;
        this.password = password;
        this.premium = true;
    }

    public AltData(String username) {
        this.email = "";
        this.username = username;
        this.password = "";
        this.premium = false;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean isPremium() {
        return this.premium;
    }
}

