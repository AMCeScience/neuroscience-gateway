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

/* Copyright 2007-2011 MTA SZTAKI LPDS, Budapest

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License. */
/*
 * Proxy Provider service
 */
package nl.amc.biolab.pgportal.services.credential;

import hu.sztaki.lpds.pgportal.services.asm.ASMCredentialProvider;
import java.net.Proxy;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;

/**
 * @author krisztian karoczkai
 */
@WebService(serviceName = "CredentialProvider")
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL)
public class CredentialProvider {

    /**
     * Web service operation. Getting proxy as a byte array
     *
     * @param pGroup user group
     * @param pUser user id
     * @param pMiddleware middleware name
     * @param pVo grid/vo/group name
     * @return user proxy
     * @throws Proxy does not exsists, file i/o error
     */
    @WebMethod(operationName = "get")
    public byte[] get(
            @WebParam(name = "pGroup") String pGroup,
            @WebParam(name = "pUser") String pUser,
            @WebParam(name = "pMiddleware") String pMiddleware,
            @WebParam(name = "pVo") String pVo)
            throws GetFault {

        ASMCredentialProvider asmcred = new ASMCredentialProvider();
        try {
            return asmcred.get(pUser, pVo);
        } catch (Exception ex) {
            String msg = "Could not get credentials for this User and VO";
            GetFaultBean faultBean = new GetFaultBean();
            faultBean.setMessage(msg);
            throw new GetFault("Error getting credentials for this User and VO",faultBean);
        }
    }
}
