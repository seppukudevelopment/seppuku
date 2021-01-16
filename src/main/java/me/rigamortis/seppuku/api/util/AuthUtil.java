package me.rigamortis.seppuku.api.util;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.net.Proxy;

/**
 * @author Seth
 */
public final class AuthUtil {

    public AuthUtil(final String name, final String password, boolean online) {
        if (online) {
            if (name != null && password != null) {
                (new Thread() {
                    public void run() {
                        AuthUtil.loginPassword(name, password);
                    }
                }).start();
            } else {
                System.out.println("Username and/or password is incorrect.");
            }
        } else {
            loginPasswordOffline(name);
        }
    }

    public static void loginPasswordOffline(String username) {
        Minecraft.getMinecraft().session = new Session(username, username, username, "MOJANG");
    }

    public static String loginPassword(String username, String password) {
        if (username == null || username.length() <= 0 || password == null || password.length() <= 0)
            return "Error";
        YggdrasilAuthenticationService a = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
        YggdrasilUserAuthentication b = (YggdrasilUserAuthentication)a.createUserAuthentication(Agent.MINECRAFT);
        b.setUsername(username);
        b.setPassword(password);
        try {
            b.logIn();
            Minecraft.getMinecraft()
                    .session = new Session(b.getSelectedProfile().getName(), b.getSelectedProfile().getId().toString(), b.getAuthenticatedToken(), "MOJANG");
        } catch (AuthenticationException e) {
            return e.getMessage();
        }
        return "Success";
    }
}

