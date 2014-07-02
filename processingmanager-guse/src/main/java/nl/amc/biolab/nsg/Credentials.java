/*
 * Copyright (C) 2013 Academic Medical Center of the University of Amsterdam
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.


 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.amc.biolab.nsg;

/**
 *
 * @author Mark Santcroos
 */
public class Credentials {

    private boolean init = false;
    
    private String userPass = null;
    private String userName = null;
    private String Vo = null;
    private byte[] Proxy = null;
    private String Token = null;
    private Boolean robot = false;
    
    public Credentials() {
        init = true;
    }
    
    /*
     * Username in combination with password
     */
    public void setUserPass(String username, String password) {
        userName = username;
        userPass = password;
    }
    
    public String getUsername() {
        return userName;
    }
    
    public String getPassword() {
        return userPass;
    }
    
    /*
     * gLite X509 Certificate in combination with VO
     */
    public void setX509Proxy(byte[] pr, String vo) {
        this.Proxy = pr;
        this.Vo = vo;
    }

    public byte[] getProxy() {
        return Proxy;
    }
                

    public String getVo() {
        return Vo;
    }

    public String getToken() {
        return Token;
    }
           
    /*
     * XNAT Security token
     */
    public void setToken(String token) {
        Token = token;
    }

    /*
     * Enable use of robot proxy
     */
    public void useRobot(boolean flag) {
        robot = flag;
    }
    
    public boolean canUseRobot() {
        return robot;
    }
}