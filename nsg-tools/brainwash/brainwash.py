# Copyright (C) 2014 Academic Medical Center of the University of Amsterdam
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.

#!/usr/bin/env python

import argparse
import os
import sys

import mysql.connector
from mysql.connector import errorcode

import lcg_util

# vim: tabstop=8 expandtab shiftwidth=4 softtabstop=4


def getFileList(tExpiration, dbuser, dbpasswd, dbhost, dbdatabase):
    fileList = list()
    try:
        cnx = mysql.connector.connect(user=dbuser, password=dbpasswd,
                                      host=dbhost, database=dbdatabase)
    except mysql.connector.Error as err:
        if err.errno == errorcode.ER_ACCESS_DENIED_ERROR:
            print("Something is wrong with your user name or password")
        elif err.errno == errorcode.ER_BAD_DB_ERROR:
            print("Database does not exists")
        else:
            print(err)
    else:
        cursor = cnx.cursor()
        # getting ReplicaURI older than 1 month
        query = ("SELECT dbId, ReplicaURI, Date "
                 "FROM `Replica` AS R "
                 "INNER JOIN `DataElement` AS D "
                 "ON R.`DataID` = D.`DataID` "
                 "WHERE D.`Date` < NOW() - INTERVAL (%s) DAY")

        cursor.execute(query, (tExpiration))
        fileList = cursor.fetchall()
        cursor.close()
        cnx.close()
        return fileList


def cleanGrid(fileList, delParams, dry=True):
    removalList = []
    if dry:
        for f in fileList:
            print(f)
            removalList.append(f[0])
    else:
        for f in fileList:
            aflag = 1
            se = ''
            vo = 'vlemed'
            config = ''
            insecure = 0
            verbose = 1
            output = lcg_util.lcg_del(f[1], aflag, se,
                                      vo, config, insecure,
                                      verbose)
            if output == 0:
                # removing from database
                removalList.append(f[0])
            else:
                # printing failed lfn
                print("[ERROR] %s could not be removed from the grid." % f[1])

    return removalList


def cleanDB(removalList, dbuser, dbpasswd, dbhost, dbdatabase, isDry=True):

    query = "DELETE FROM Replica WHERE dbId in (%s)" % ','.join(str(e) for e in removalList)
    if isDry:
        print(query)
    else:
            try:
                cnx = mysql.connector.connect(user=dbuser, password=dbpasswd,
                                              host=dbhost, database=dbdatabase)
            except mysql.connector.Error as err:
                if err.errno == errorcode.ER_ACCESS_DENIED_ERROR:
                    print("Something is wrong with your user name or password")
                elif err.errno == errorcode.ER_BAD_DB_ERROR:
                    print("Database does not exists")
                else:
                    print(err)
            else:
                cursor = cnx.cursor()
                cursor.execute("set autocommit = 1")
                # removing all the db rows for successfully removed replicas
                try:
                    cursor.execute(query)
                    cnx.commit()
                except mysql.connector.Error as err:
                    print(err)
                    # write fail report on disk
                    print("[ERROR] Could not remove the following Replica entries on NSG db")
                    print("[ERROR] %s" % removalList)
                else:
                    cursor.close()
                    cnx.close()


def main(argv):

    delParams = ['lcg-del', '-a', '']

    # parsing arguments
    parser = argparse.ArgumentParser(description='NeuroScience Gateway data grid cleaning')
    parser.add_argument('--host',
                        dest='host',
                        required=False,
                        default="127.0.0.1",
                        help='Database host')
    parser.add_argument('--db',
                        dest='db',
                        required=True,
                        help='NSG database')
    parser.add_argument('--user',
                        dest='user',
                        required=True,
                        help='NSG database user')
    parser.add_argument('--passwd',
                        dest='passwd',
                        required=True,
                        help='NSG database password')
    parser.add_argument('--expiration',
                        dest='tExpiration',
                        required=False,
                        default=30,
                        help='Remove replicas older than "expiration" time. Default value 30 days')
    parser.add_argument('-d',
                        dest='isDry',
                        action='store_true',
                        default=False,
                        help='Dry run, no changes on grid nor NSG data base')
    # parser.add_argument('-v',
    #                     dest='verbose',
    #                     action_store='False',
    #                     default=True,
    #                     help='Verbose mode')

    a = parser.parse_args()

    host = a.host
    db = a.db
    user = a.user
    passwd = a.passwd
    isDry = a.isDry
    tExpiration = a.tExpiration
    # verbose = a.verbose

    # getting list of lfn files
    l = getFileList(tExpiration, user, passwd, host, db)
    # cleaning the grid
    rl = cleanGrid(l, delParams, isDry)
    # removing references from DB
    cleanDB(rl, user, passwd, host, db, isDry)



if __name__ == "__main__":
    main(sys.argv[1:])
