"""
Copyright (C) 2013 Academic Medical Center of the University of Amsterdam
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or 
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
"""
 

#!/usr/bin/python
# -*- coding: utf-8 -*-

import MySQLdb as mdb
import sys

lifemail='j.l.font@amc.uva.nl'
xnatId='jlfonttest'
resourceId=1

try:
    conLR = mdb.connect('localhost', 'liferay', 'liferay', 'liferay')

    curLR = conLR.cursor()
    curLR.execute("SELECT UserId, UserId, FirstName, LastName, EmailAddress  FROM User_ WHERE emailAddress LIKE %s;", (lifemail) )
    row = curLR.fetchone()
    conLR.close()


    conGU = mdb.connect('localhost', 'root', 'key.nsg.mysql', 'neuroscience')
    curGU = conGU.cursor()
   
    # creates a new user in NSG using the info from LifeRay 
    # TODO: fix, 88888 has to be row[0]
    curGU.execute("INSERT INTO User(LiferayID, UserID, FirstName, LastName,UserEmail) VALUES (%s,%s,%s,%s,%s);",
                   ('88888',xnatId,row[2],row[3],row[4]))
    conGU.commit()
    
    # gets the NSG userKey for the new user
    # TODO: fix, 88888 should be variable row[0]
    curGU.execute("SELECT UserKey FROM User WHERE LiferayID = %s", ('88888'))
    nsgKey = curGU.fetchone()[0]
    
    # inserts a new relation between XNAT resource and new NSG user
    curGU.execute("INSERT INTO UserAuthentication(UserID,UserKey,ResourceID) VALUES (%s,%s,%s);",
                    (xnatId,nsgKey,resourceId))
    conGU.commit()

    curGU.execute("INSERT INTO UserApplication (ApplicationID, UserKey) \
                    (SELECT A.ApplicationId, U.userKey \
                     FROM Application A \
                     JOIN (SELECT User.userKey from User WHERE userKey=%s) U )",
                    (nsgKey))
    conGU.commit()
                    

    conGU.close()
    
    
except mdb.Error, e:
  
    print "Error %d: %s" % (e.args[0],e.args[1])
    sys.exit(1)
    
#finally:    
        
#    if con:    
#        con.close()
